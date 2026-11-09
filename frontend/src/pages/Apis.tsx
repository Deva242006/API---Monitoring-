import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { MonitoredApi, Project } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';
import {
  Activity,
  Plus,
  Search,
  Play,
  Trash2,
  Power,
  ExternalLink,
  Clock,
  Filter,
} from 'lucide-react';

export const Apis: React.FC = () => {
  const [apis, setApis] = useState<MonitoredApi[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [search, setSearch] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Modal State
  const [showModal, setShowModal] = useState<boolean>(false);
  const [name, setName] = useState<string>('');
  const [endpointUrl, setEndpointUrl] = useState<string>('');
  const [httpMethod, setHttpMethod] = useState<'GET' | 'POST' | 'PUT' | 'DELETE'>('GET');
  const [expectedStatusCode, setExpectedStatusCode] = useState<number>(200);
  const [timeoutMs, setTimeoutMs] = useState<number>(5000);
  const [monitoringIntervalSeconds, setMonitoringIntervalSeconds] = useState<number>(60);
  const [projectId, setProjectId] = useState<number | ''>('');
  const [description, setDescription] = useState<string>('');

  const { user } = useAuth();
  const { addToast } = useToast();
  const canManage = user?.role === 'ADMIN' || user?.role === 'DEVELOPER';

  const fetchApis = async () => {
    try {
      const res = await api.get('/apis?page=0&size=100');
      setApis(res.data.data.content);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const fetchProjects = async () => {
    try {
      const res = await api.get('/projects?page=0&size=100');
      setProjects(res.data.data.content);
      if (res.data.data.content.length > 0) {
        setProjectId(res.data.data.content[0].id);
      }
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    fetchApis();
    fetchProjects();
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!projectId) {
      addToast({ type: 'warning', title: 'Please select a project' });
      return;
    }

    try {
      await api.post('/apis', {
        name,
        endpointUrl,
        httpMethod,
        expectedStatusCode: Number(expectedStatusCode),
        timeoutMs: Number(timeoutMs),
        monitoringIntervalSeconds: Number(monitoringIntervalSeconds),
        projectId: Number(projectId),
        description,
      });

      addToast({ type: 'success', title: 'API registered successfully' });
      setShowModal(false);
      setName('');
      setEndpointUrl('');
      setDescription('');
      fetchApis();
    } catch (err: any) {
      addToast({
        type: 'error',
        title: 'Registration failed',
        message: err.response?.data?.message || 'Check URL or permissions (SSRF validation active)',
      });
    }
  };

  const handleManualCheck = async (id: number, e: React.MouseEvent) => {
    e.preventDefault();
    try {
      await api.post(`/apis/${id}/check`);
      addToast({ type: 'info', title: 'Health check dispatched', message: 'Probe running in background...' });
      setTimeout(fetchApis, 1500);
    } catch (err: any) {
      addToast({ type: 'error', title: 'Check trigger failed', message: err.response?.data?.message });
    }
  };

  const handleToggle = async (id: number, e: React.MouseEvent) => {
    e.preventDefault();
    try {
      await api.patch(`/apis/${id}/toggle`);
      addToast({ type: 'success', title: 'API status toggled' });
      fetchApis();
    } catch (err: any) {
      addToast({ type: 'error', title: 'Action failed', message: err.response?.data?.message });
    }
  };

  const handleDelete = async (id: number, e: React.MouseEvent) => {
    e.preventDefault();
    if (!window.confirm('Delete this monitored API and its health history?')) return;
    try {
      await api.delete(`/apis/${id}`);
      addToast({ type: 'success', title: 'API deleted' });
      fetchApis();
    } catch (err: any) {
      addToast({ type: 'error', title: 'Delete failed', message: err.response?.data?.message });
    }
  };

  const filteredApis = apis.filter((a) => {
    const matchesSearch =
      a.name.toLowerCase().includes(search.toLowerCase()) ||
      a.endpointUrl.toLowerCase().includes(search.toLowerCase()) ||
      a.projectName?.toLowerCase().includes(search.toLowerCase());
    const matchesStatus = statusFilter === 'ALL' || a.currentStatus === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            Monitored API Endpoints
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Continuous synthetic health-checks, status probing, latency tracking & SSRF-safe checks
          </p>
        </div>
        {canManage && (
          <button
            onClick={() => setShowModal(true)}
            className="px-3.5 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-semibold text-xs transition-colors flex items-center gap-1.5 self-start shadow-sm"
          >
            <Plus className="w-4 h-4" />
            <span>Register API</span>
          </button>
        )}
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div className="relative max-w-md flex-1">
          <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by API name, URL, or project..."
            className="w-full bg-slate-900 border border-slate-800 rounded-lg pl-10 pr-4 py-2 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-emerald-500/60"
          />
        </div>

        <div className="flex items-center gap-2">
          <Filter className="w-3.5 h-3.5 text-slate-500" />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-900 border border-slate-800 text-xs text-slate-300 rounded-lg px-3 py-2 focus:outline-none focus:border-emerald-500/60"
          >
            <option value="ALL">All Statuses</option>
            <option value="HEALTHY">Healthy</option>
            <option value="DEGRADED">Degraded</option>
            <option value="DOWN">Down</option>
            <option value="UNKNOWN">Unknown</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="rounded-xl bg-slate-900 border border-slate-800 overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 text-slate-400 border-b border-slate-800 font-medium">
              <tr>
                <th className="py-3 px-4">Method & Name</th>
                <th className="py-3 px-4">Endpoint URL</th>
                <th className="py-3 px-4">Project</th>
                <th className="py-3 px-4">Current Health</th>
                <th className="py-3 px-4">Latency</th>
                <th className="py-3 px-4">Interval</th>
                <th className="py-3 px-4">Last Probe</th>
                <th className="py-3 px-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    Loading monitored APIs...
                  </td>
                </tr>
              ) : filteredApis.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    No APIs match your filter criteria.
                  </td>
                </tr>
              ) : (
                filteredApis.map((apiItem) => (
                  <tr
                    key={apiItem.id}
                    className="hover:bg-slate-800/40 transition-colors group"
                  >
                    <td className="py-3.5 px-4">
                      <div className="flex items-center gap-2">
                        <span
                          className={`text-[10px] font-mono px-1.5 py-0.5 rounded font-bold ${
                            apiItem.httpMethod === 'GET'
                              ? 'bg-blue-950/80 text-blue-400 border border-blue-800/60'
                              : apiItem.httpMethod === 'POST'
                              ? 'bg-emerald-950/80 text-emerald-400 border border-emerald-800/60'
                              : apiItem.httpMethod === 'PUT'
                              ? 'bg-amber-950/80 text-amber-400 border border-amber-800/60'
                              : 'bg-rose-950/80 text-rose-400 border border-rose-800/60'
                          }`}
                        >
                          {apiItem.httpMethod}
                        </span>
                        <Link
                          to={`/apis/${apiItem.id}`}
                          className="font-semibold text-slate-200 hover:text-emerald-400 transition-colors"
                        >
                          {apiItem.name}
                        </Link>
                      </div>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-400 max-w-xs truncate">
                      {apiItem.endpointUrl}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">{apiItem.projectName}</td>
                    <td className="py-3.5 px-4">
                      <StatusBadge status={apiItem.currentStatus} type="api" />
                    </td>
                    <td className="py-3.5 px-4 font-mono text-slate-300">
                      {apiItem.lastResponseTimeMs ? `${apiItem.lastResponseTimeMs}ms` : '—'}
                    </td>
                    <td className="py-3.5 px-4 text-slate-400">{apiItem.monitoringIntervalSeconds}s</td>
                    <td className="py-3.5 px-4 text-slate-400 text-[11px]">
                      {apiItem.lastCheckedAt ? apiItem.lastCheckedAt.replace('T', ' ').split('.')[0] : 'Never'}
                    </td>
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1">
                        {canManage && (
                          <>
                            <button
                              onClick={(e) => handleManualCheck(apiItem.id, e)}
                              className="p-1.5 rounded-lg text-slate-400 hover:text-emerald-400 hover:bg-emerald-950/20 transition-colors"
                              title="Run manual health probe now"
                            >
                              <Play className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={(e) => handleToggle(apiItem.id, e)}
                              className={`p-1.5 rounded-lg transition-colors ${
                                apiItem.active
                                  ? 'text-emerald-400 hover:text-amber-400 hover:bg-amber-950/20'
                                  : 'text-slate-500 hover:text-emerald-400 hover:bg-emerald-950/20'
                              }`}
                              title={apiItem.active ? 'Pause monitoring' : 'Resume monitoring'}
                            >
                              <Power className="w-3.5 h-3.5" />
                            </button>
                            <button
                              onClick={(e) => handleDelete(apiItem.id, e)}
                              className="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-950/20 transition-colors"
                              title="Delete API"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </>
                        )}
                        <Link
                          to={`/apis/${apiItem.id}`}
                          className="p-1.5 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors"
                          title="View metrics & history"
                        >
                          <ExternalLink className="w-3.5 h-3.5" />
                        </Link>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Register API Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/80 backdrop-blur-sm p-4 overflow-y-auto">
          <div className="w-full max-w-lg rounded-2xl bg-slate-900 border border-slate-800 p-6 shadow-2xl my-8">
            <h3 className="text-base font-bold text-white mb-1">Register Monitored API</h3>
            <p className="text-xs text-slate-400 mb-5">
              Enter endpoint details. SSRF validation will automatically block internal, loopback, or cloud-metadata URLs.
            </p>

            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">API Name *</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Authentication Microservice"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 placeholder:text-slate-600 focus:outline-none focus:border-emerald-500/60"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Project *</label>
                <select
                  required
                  value={projectId}
                  onChange={(e) => setProjectId(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60"
                >
                  {projects.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div className="col-span-1">
                  <label className="block text-xs font-medium text-slate-300 mb-1.5">HTTP Method</label>
                  <select
                    value={httpMethod}
                    onChange={(e: any) => setHttpMethod(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60"
                  >
                    <option value="GET">GET</option>
                    <option value="POST">POST</option>
                    <option value="PUT">PUT</option>
                    <option value="DELETE">DELETE</option>
                  </select>
                </div>
                <div className="col-span-2">
                  <label className="block text-xs font-medium text-slate-300 mb-1.5">Expected HTTP Status</label>
                  <input
                    type="number"
                    required
                    value={expectedStatusCode}
                    onChange={(e) => setExpectedStatusCode(Number(e.target.value))}
                    placeholder="200"
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60 font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Endpoint URL *</label>
                <input
                  type="url"
                  required
                  value={endpointUrl}
                  onChange={(e) => setEndpointUrl(e.target.value)}
                  placeholder="https://api.github.com/status"
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 placeholder:text-slate-600 focus:outline-none focus:border-emerald-500/60 font-mono"
                />
                <p className="text-[10px] text-slate-500 mt-1">Must be an accessible HTTP/HTTPS URL.</p>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1.5">Timeout (ms)</label>
                  <input
                    type="number"
                    min="1000"
                    max="60000"
                    value={timeoutMs}
                    onChange={(e) => setTimeoutMs(Number(e.target.value))}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60 font-mono"
                  />
                </div>
                <div>
                  <label className="block text-xs font-medium text-slate-300 mb-1.5">Interval (seconds)</label>
                  <input
                    type="number"
                    min="10"
                    max="86400"
                    value={monitoringIntervalSeconds}
                    onChange={(e) => setMonitoringIntervalSeconds(Number(e.target.value))}
                    className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60 font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-300 mb-1.5">Description (optional)</label>
                <textarea
                  rows={2}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Notes about the service SLA..."
                  className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 placeholder:text-slate-600 focus:outline-none focus:border-emerald-500/60 resize-none"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-3 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-3.5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-medium text-slate-300 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-semibold text-xs transition-colors"
                >
                  Save & Start Monitoring
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

