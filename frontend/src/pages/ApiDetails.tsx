import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import api from '../services/api';
import { MonitoredApi, HealthCheck } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { useToast } from '../components/Toast';
import {
  ArrowLeft,
  Play,
  Activity,
  Clock,
  CheckCircle,
  XCircle,
  ExternalLink,
  Shield,
  Layers,
  Power,
} from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';

export const ApiDetails: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const [apiData, setApiData] = useState<MonitoredApi | null>(null);
  const [history, setHistory] = useState<HealthCheck[]>([]);
  const [metrics, setMetrics] = useState<{ uptimePercentage?: number; avgResponseTime?: number } | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [probing, setProbing] = useState<boolean>(false);

  const { addToast } = useToast();

  const fetchDetails = async () => {
    try {
      const [apiRes, healthRes, metricsRes] = await Promise.all([
        api.get(`/apis/${id}`),
        api.get(`/apis/${id}/health`),
        api.get(`/apis/${id}/metrics`),
      ]);
      setApiData(apiRes.data.data);
      setHistory(healthRes.data.data);
      setMetrics(metricsRes.data.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetails();
  }, [id]);

  const handleManualCheck = async () => {
    setProbing(true);
    try {
      await api.post(`/apis/${id}/check`);
      addToast({ type: 'info', title: 'Health check dispatched', message: 'Probe running...' });
      setTimeout(() => {
        fetchDetails();
        setProbing(false);
      }, 1500);
    } catch (err: any) {
      addToast({ type: 'error', title: 'Probe failed', message: err.response?.data?.message });
      setProbing(false);
    }
  };

  const handleToggle = async () => {
    try {
      await api.patch(`/apis/${id}/toggle`);
      addToast({ type: 'success', title: 'Monitoring status toggled' });
      fetchDetails();
    } catch (err: any) {
      addToast({ type: 'error', title: 'Action failed', message: err.response?.data?.message });
    }
  };

  if (loading || !apiData) {
    return <div className="py-12 text-center text-xs text-slate-500">Loading endpoint metrics...</div>;
  }

  // Chart data from recent checks (reversed for chronological order)
  const chartData = [...history].reverse().map((h) => ({
    time: h.checkedAt ? h.checkedAt.split('T')[1]?.substring(0, 5) : '',
    latency: h.responseTimeMs || 0,
    status: h.status,
  }));

  return (
    <div className="space-y-6">
      {/* Back button & Header */}
      <div>
        <Link
          to="/apis"
          className="inline-flex items-center gap-1.5 text-xs text-slate-400 hover:text-slate-200 mb-3"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to APIs</span>
        </Link>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span
              className={`text-xs font-mono px-2 py-1 rounded font-bold ${
                apiData.httpMethod === 'GET'
                  ? 'bg-blue-950/80 text-blue-400 border border-blue-800/60'
                  : apiData.httpMethod === 'POST'
                  ? 'bg-emerald-950/80 text-emerald-400 border border-emerald-800/60'
                  : 'bg-amber-950/80 text-amber-400 border border-amber-800/60'
              }`}
            >
              {apiData.httpMethod}
            </span>
            <div>
              <div className="flex items-center gap-2.5">
                <h2 className="text-xl font-bold tracking-tight text-white">{apiData.name}</h2>
                <StatusBadge status={apiData.currentStatus} type="api" />
              </div>
              <p className="text-xs text-slate-400 font-mono mt-0.5">{apiData.endpointUrl}</p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleToggle}
              className={`px-3 py-1.5 rounded-lg border text-xs font-medium transition-colors flex items-center gap-1.5 ${
                apiData.active
                  ? 'bg-slate-900 border-slate-800 text-slate-300 hover:bg-slate-800'
                  : 'bg-emerald-950/50 border-emerald-800 text-emerald-400 hover:bg-emerald-950'
              }`}
            >
              <Power className="w-3.5 h-3.5" />
              <span>{apiData.active ? 'Pause' : 'Resume'}</span>
            </button>
            <button
              onClick={handleManualCheck}
              disabled={probing}
              className="px-3.5 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-semibold text-xs transition-colors flex items-center gap-1.5 disabled:opacity-50"
            >
              <Play className="w-3.5 h-3.5" />
              <span>{probing ? 'Probing...' : 'Check Now'}</span>
            </button>
          </div>
        </div>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <span className="text-xs font-medium text-slate-400">7-Day Uptime</span>
          <p className="text-2xl font-bold text-white mt-1.5">
            {metrics?.uptimePercentage !== undefined ? `${metrics.uptimePercentage}%` : '100%'}
          </p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">SLA Target: 99.9%</span>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <span className="text-xs font-medium text-slate-400">Avg Latency</span>
          <p className="text-2xl font-bold text-white mt-1.5">
            {metrics?.avgResponseTime ? `${metrics.avgResponseTime}ms` : '—'}
          </p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">
            Last: {apiData.lastResponseTimeMs ? `${apiData.lastResponseTimeMs}ms` : '—'}
          </span>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <span className="text-xs font-medium text-slate-400">Check Interval</span>
          <p className="text-2xl font-bold text-white mt-1.5">{apiData.monitoringIntervalSeconds}s</p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">
            Timeout: {apiData.timeoutMs}ms
          </span>
        </div>

        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <span className="text-xs font-medium text-slate-400">Consecutive Failures</span>
          <p
            className={`text-2xl font-bold mt-1.5 ${
              apiData.consecutiveFailures > 0 ? 'text-rose-400' : 'text-slate-200'
            }`}
          >
            {apiData.consecutiveFailures}
          </p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">Incident trigger at: 3</span>
        </div>
      </div>

      {/* Latency History Chart */}
      <div className="p-5 rounded-xl bg-slate-900 border border-slate-800">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-sm font-semibold text-white">Recent Latency Trend</h3>
            <p className="text-xs text-slate-400">Response time duration over latest probes</p>
          </div>
        </div>
        <div className="h-56">
          {chartData.length === 0 ? (
            <div className="flex h-full items-center justify-center text-xs text-slate-500">
              No historical data available. Run a manual probe to record telemetry.
            </div>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
                <XAxis dataKey="time" stroke="#64748b" fontSize={11} tickLine={false} />
                <YAxis stroke="#64748b" fontSize={11} tickLine={false} unit="ms" />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '8px',
                    fontSize: '12px',
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="latency"
                  stroke="#10b981"
                  strokeWidth={2}
                  dot={{ r: 3, fill: '#10b981' }}
                  name="Response (ms)"
                />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      {/* Recent Probes Log */}
      <div className="p-5 rounded-xl bg-slate-900 border border-slate-800">
        <h3 className="text-sm font-semibold text-white mb-1">Latest Health Probes</h3>
        <p className="text-xs text-slate-400 mb-4">Most recent probe outputs and HTTP status codes</p>

        <div className="divide-y divide-slate-800/60">
          {history.length === 0 ? (
            <div className="py-6 text-center text-xs text-slate-500">No probes recorded yet.</div>
          ) : (
            history.map((check) => (
              <div key={check.id} className="py-3 flex items-center justify-between gap-4 text-xs">
                <div className="flex items-center gap-3">
                  {check.status === 'HEALTHY' ? (
                    <CheckCircle className="w-4 h-4 text-emerald-400 shrink-0" />
                  ) : (
                    <XCircle className="w-4 h-4 text-rose-400 shrink-0" />
                  )}
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-slate-200">
                        {check.statusCode ? `HTTP ${check.statusCode}` : 'Error'}
                      </span>
                      <StatusBadge status={check.status} type="api" />
                    </div>
                    {check.errorMessage && (
                      <p className="text-[11px] text-rose-400 mt-0.5 font-mono">{check.errorMessage}</p>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-4 text-right">
                  <span className="font-mono text-slate-300">
                    {check.responseTimeMs ? `${check.responseTimeMs}ms` : '—'}
                  </span>
                  <span className="text-slate-500 text-[11px]">
                    {check.checkedAt?.replace('T', ' ').split('.')[0]}
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

