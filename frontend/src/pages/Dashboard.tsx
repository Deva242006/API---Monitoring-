import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import { wsService } from '../services/websocket';
import { DashboardSummary, Incident, MonitoredApi } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  Clock,
  Radio,
  Server,
  TrendingUp,
  XCircle,
  ArrowUpRight,
} from 'lucide-react';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';

export const Dashboard: React.FC = () => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [recentIncidents, setRecentIncidents] = useState<Incident[]>([]);
  const [topApis, setTopApis] = useState<MonitoredApi[]>([]);
  const [liveEvents, setLiveEvents] = useState<
    Array<{ id: string; apiName: string; status: string; responseTime: number; time: string }>
  >([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Mock trend data for response times
  const responseTimeTrend = [
    { time: '00:00', avg: 142, p95: 220 },
    { time: '04:00', avg: 120, p95: 190 },
    { time: '08:00', avg: 185, p95: 310 },
    { time: '12:00', avg: 210, p95: 380 },
    { time: '16:00', avg: 165, p95: 260 },
    { time: '20:00', avg: 135, p95: 215 },
    { time: 'Now', avg: summary?.avgResponseTime || 140, p95: (summary?.avgResponseTime || 140) * 1.5 },
  ];

  const fetchDashboardData = async () => {
    try {
      const [sumRes, incRes, apiRes] = await Promise.all([
        api.get('/dashboard/summary'),
        api.get('/incidents?page=0&size=5'),
        api.get('/apis?page=0&size=6'),
      ]);
      setSummary(sumRes.data.data);
      setRecentIncidents(incRes.data.data.content);
      setTopApis(apiRes.data.data.content);
    } catch (err) {
      console.error('Failed to load dashboard data', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();

    // Subscribe to live health check events
    const unsubHealth = wsService.onHealthUpdate((event) => {
      setLiveEvents((prev) => [
        {
          id: Math.random().toString(),
          apiName: event.apiName,
          status: event.status,
          responseTime: event.responseTime,
          time: new Date().toLocaleTimeString(),
        },
        ...prev.slice(0, 14),
      ]);

      // Refetch summary periodically on events
      setSummary((prev) => {
        if (!prev) return null;
        return {
          ...prev,
          avgResponseTime: Math.round(((prev.avgResponseTime + event.responseTime) / 2) * 10) / 10,
        };
      });
    });

    return () => {
      unsubHealth();
    };
  }, []);

  const healthData = [
    { name: 'Healthy', value: summary?.healthyApis || 0, color: '#10b981' },
    { name: 'Degraded', value: summary?.degradedApis || 0, color: '#f59e0b' },
    { name: 'Down', value: summary?.downApis || 0, color: '#f43f5e' },
  ];

  if (loading) {
    return (
      <div className="flex h-96 items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <Activity className="w-8 h-8 text-emerald-400 animate-spin" />
          <p className="text-xs text-slate-400 font-mono">Loading telemetry & monitoring state...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Page Title & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            System Observability Overview
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Real-time status, synthetic health-checks and incident resolution metrics
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={fetchDashboardData}
            className="px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800 text-xs text-slate-300 hover:text-white hover:bg-slate-800 transition-colors flex items-center gap-1.5"
          >
            <Clock className="w-3.5 h-3.5 text-slate-400" />
            <span>Refresh</span>
          </button>
          <Link
            to="/apis"
            className="px-3.5 py-1.5 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-semibold text-xs transition-colors flex items-center gap-1.5 shadow-sm"
          >
            <span>+ Register API</span>
          </Link>
        </div>
      </div>

      {/* KPI Cards Grid */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
        {/* Total APIs */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Total APIs</span>
            <Server className="w-4 h-4 text-slate-500" />
          </div>
          <p className="text-2xl font-bold text-white mt-2">{summary?.totalApis || 0}</p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">Registered endpoints</span>
        </div>

        {/* Healthy APIs */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-emerald-400">Healthy</span>
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          </div>
          <p className="text-2xl font-bold text-emerald-400 mt-2">{summary?.healthyApis || 0}</p>
          <span className="text-[11px] text-emerald-500/80 mt-1 inline-block">Operational</span>
        </div>

        {/* Down APIs */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-rose-400">Down / Outage</span>
            <XCircle className="w-4 h-4 text-rose-400" />
          </div>
          <p className="text-2xl font-bold text-rose-400 mt-2">{summary?.downApis || 0}</p>
          <span className="text-[11px] text-rose-500/80 mt-1 inline-block">Action required</span>
        </div>

        {/* Overall Uptime */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">24h Uptime</span>
            <TrendingUp className="w-4 h-4 text-emerald-400" />
          </div>
          <p className="text-2xl font-bold text-white mt-2">{summary?.uptimePercentage || 100}%</p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">
            {summary?.failedChecks || 0} failed checks
          </span>
        </div>

        {/* Avg Response Time */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-400">Avg Latency</span>
            <Activity className="w-4 h-4 text-sky-400" />
          </div>
          <p className="text-2xl font-bold text-white mt-2">{summary?.avgResponseTime || 0}ms</p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">Global probe mean</span>
        </div>

        {/* Active Incidents */}
        <div className="p-4 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-amber-400">Active Incidents</span>
            <AlertTriangle className="w-4 h-4 text-amber-400" />
          </div>
          <p className="text-2xl font-bold text-amber-400 mt-2">{summary?.activeIncidents || 0}</p>
          <span className="text-[11px] text-slate-500 mt-1 inline-block">
            {summary?.resolvedIncidents || 0} resolved
          </span>
        </div>
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Response Time Line Chart */}
        <div className="lg:col-span-2 p-5 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-semibold text-white">Latency Performance (ms)</h3>
              <p className="text-xs text-slate-400">Average vs P95 synthetic request duration</p>
            </div>
            <div className="flex items-center gap-4 text-xs">
              <span className="flex items-center gap-1.5 text-emerald-400">
                <span className="w-2.5 h-0.5 bg-emerald-400 rounded-full" /> Avg Response
              </span>
              <span className="flex items-center gap-1.5 text-sky-400">
                <span className="w-2.5 h-0.5 bg-sky-400 rounded-full" /> P95 Response
              </span>
            </div>
          </div>
          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={responseTimeTrend}>
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
                  dataKey="avg"
                  stroke="#10b981"
                  strokeWidth={2}
                  dot={{ r: 3, fill: '#10b981' }}
                  name="Avg Latency"
                />
                <Line
                  type="monotone"
                  dataKey="p95"
                  stroke="#38bdf8"
                  strokeWidth={2}
                  strokeDasharray="4 4"
                  dot={false}
                  name="P95 Latency"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Health Distribution Donut */}
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex flex-col">
          <h3 className="text-sm font-semibold text-white mb-1">Health Distribution</h3>
          <p className="text-xs text-slate-400 mb-4">Real-time status across all active endpoints</p>
          <div className="h-44 relative flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={healthData.filter((d) => d.value > 0).length ? healthData : [{ name: 'None', value: 1, color: '#334155' }]}
                  innerRadius={50}
                  outerRadius={70}
                  paddingAngle={4}
                  dataKey="value"
                >
                  {healthData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#0f172a',
                    borderColor: '#334155',
                    borderRadius: '8px',
                    fontSize: '12px',
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
            <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
              <span className="text-2xl font-bold text-white">{summary?.totalApis || 0}</span>
              <span className="text-[10px] text-slate-500 uppercase tracking-wider">APIs</span>
            </div>
          </div>
          <div className="mt-auto pt-4 border-t border-slate-800 grid grid-cols-3 gap-2 text-center text-xs">
            <div>
              <div className="flex items-center justify-center gap-1 text-emerald-400">
                <span className="w-2 h-2 rounded-full bg-emerald-400" />
                <span>Healthy</span>
              </div>
              <p className="font-semibold text-slate-200 mt-1">{summary?.healthyApis || 0}</p>
            </div>
            <div>
              <div className="flex items-center justify-center gap-1 text-amber-400">
                <span className="w-2 h-2 rounded-full bg-amber-400" />
                <span>Degraded</span>
              </div>
              <p className="font-semibold text-slate-200 mt-1">{summary?.degradedApis || 0}</p>
            </div>
            <div>
              <div className="flex items-center justify-center gap-1 text-rose-400">
                <span className="w-2 h-2 rounded-full bg-rose-400" />
                <span>Down</span>
              </div>
              <p className="font-semibold text-slate-200 mt-1">{summary?.downApis || 0}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Two Column Grid: Monitored APIs & Recent Incidents / Live Feed */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Top Monitored APIs */}
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-semibold text-white">Monitored Endpoints</h3>
              <p className="text-xs text-slate-400">Live operational status of key microservices</p>
            </div>
            <Link
              to="/apis"
              className="text-xs text-emerald-400 hover:text-emerald-300 flex items-center gap-1 font-medium"
            >
              View all <ArrowUpRight className="w-3.5 h-3.5" />
            </Link>
          </div>
          <div className="divide-y divide-slate-800/60">
            {topApis.length === 0 ? (
              <div className="py-8 text-center text-xs text-slate-500">
                No APIs registered yet. Click "+ Register API" to begin monitoring.
              </div>
            ) : (
              topApis.map((api) => (
                <div key={api.id} className="py-3 flex items-center justify-between gap-4">
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-800 text-slate-300 font-semibold">
                        {api.httpMethod}
                      </span>
                      <Link
                        to={`/apis/${api.id}`}
                        className="text-xs font-semibold text-slate-200 hover:text-emerald-400 truncate"
                      >
                        {api.name}
                      </Link>
                    </div>
                    <p className="text-[11px] text-slate-500 font-mono truncate mt-0.5">{api.endpointUrl}</p>
                  </div>
                  <div className="flex items-center gap-3 shrink-0">
                    <span className="text-xs text-slate-400 font-mono">
                      {api.lastResponseTimeMs ? `${api.lastResponseTimeMs}ms` : '—'}
                    </span>
                    <StatusBadge status={api.currentStatus} type="api" />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Recent Incidents & WebSocket Live Stream */}
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-semibold text-white flex items-center gap-2">
                Recent Incidents & Outages
              </h3>
              <p className="text-xs text-slate-400">Active investigations and automatic tickets</p>
            </div>
            <Link
              to="/incidents"
              className="text-xs text-emerald-400 hover:text-emerald-300 flex items-center gap-1 font-medium"
            >
              All incidents <ArrowUpRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          <div className="space-y-3 flex-1">
            {recentIncidents.length === 0 ? (
              <div className="py-8 text-center text-xs text-emerald-400/80 flex flex-col items-center gap-2">
                <CheckCircle2 className="w-6 h-6 text-emerald-400" />
                <span>All monitored services operating normally. No active incidents.</span>
              </div>
            ) : (
              recentIncidents.map((inc) => (
                <div
                  key={inc.id}
                  className="p-3 rounded-lg bg-slate-950/60 border border-slate-800/80 hover:border-slate-700 transition-colors flex items-center justify-between gap-3"
                >
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <StatusBadge status={inc.severity} type="severity" />
                      <Link
                        to={`/incidents/${inc.id}`}
                        className="text-xs font-semibold text-slate-200 hover:text-emerald-400 truncate"
                      >
                        {inc.title}
                      </Link>
                    </div>
                    <p className="text-[11px] text-slate-500 mt-1">
                      API: <span className="text-slate-400">{inc.apiName}</span> · Assigned:{' '}
                      <span className="text-slate-400">{inc.assignedToName || 'Unassigned'}</span>
                    </p>
                  </div>
                  <StatusBadge status={inc.status} type="incident-status" />
                </div>
              ))
            )}
          </div>

          {/* Real-time Ticker */}
          {liveEvents.length > 0 && (
            <div className="mt-4 pt-3 border-t border-slate-800">
              <div className="flex items-center gap-2 text-[11px] font-semibold text-slate-400 mb-2">
                <Radio className="w-3.5 h-3.5 text-emerald-400 animate-pulse" />
                <span>Live Probe Ticker</span>
              </div>
              <div className="space-y-1 max-h-24 overflow-y-auto">
                {liveEvents.slice(0, 3).map((ev) => (
                  <div
                    key={ev.id}
                    className="text-[11px] font-mono text-slate-400 flex items-center justify-between px-2 py-1 rounded bg-slate-950/40"
                  >
                    <span className="truncate">{ev.apiName}</span>
                    <span className="flex items-center gap-2">
                      <span className={ev.status === 'HEALTHY' ? 'text-emerald-400' : 'text-rose-400 font-bold'}>
                        {ev.status}
                      </span>
                      <span className="text-slate-500">{ev.responseTime}ms</span>
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

