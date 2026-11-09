import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { HealthCheck } from '../types';
import { StatusBadge } from '../components/StatusBadge';
import { History, Search, RefreshCw, ChevronLeft, ChevronRight } from 'lucide-react';

export const HealthHistory: React.FC = () => {
  const [checks, setChecks] = useState<HealthCheck[]>([]);
  const [page, setPage] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(1);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [search, setSearch] = useState<string>('');

  const fetchHistory = async (pageNumber: number = 0) => {
    setLoading(true);
    try {
      const res = await api.get(`/health-checks?page=${pageNumber}&size=25`);
      setChecks(res.data.data.content);
      setPage(res.data.data.page);
      setTotalPages(res.data.data.totalPages);
      setTotalElements(res.data.data.totalElements);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHistory(0);
  }, []);

  const filtered = checks.filter(
    (c) =>
      c.apiName.toLowerCase().includes(search.toLowerCase()) ||
      c.status.toLowerCase().includes(search.toLowerCase()) ||
      (c.errorMessage && c.errorMessage.toLowerCase().includes(search.toLowerCase()))
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            Health Check Probe History
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Audit log of all synthetic probes executed by the Sentinel monitoring engine ({totalElements} total)
          </p>
        </div>
        <button
          onClick={() => fetchHistory(page)}
          className="px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800 text-xs text-slate-300 hover:text-white hover:bg-slate-800 transition-colors flex items-center gap-1.5 self-start"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
          <span>Refresh</span>
        </button>
      </div>

      {/* Search Input */}
      <div className="relative max-w-md">
        <Search className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
        <input
          type="text"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Filter by API name, status, or error..."
          className="w-full bg-slate-900 border border-slate-800 rounded-lg pl-10 pr-4 py-2 text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-emerald-500/60"
        />
      </div>

      {/* Table */}
      <div className="rounded-xl bg-slate-900 border border-slate-800 overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-950/60 text-slate-400 border-b border-slate-800 font-medium">
              <tr>
                <th className="py-3 px-4">Probe Time</th>
                <th className="py-3 px-4">Target API</th>
                <th className="py-3 px-4">Status</th>
                <th className="py-3 px-4">HTTP Code</th>
                <th className="py-3 px-4">Latency</th>
                <th className="py-3 px-4">Error / Response Message</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {loading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-500">
                    Loading probe audit trail...
                  </td>
                </tr>
              ) : filtered.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-slate-500">
                    No probe records found.
                  </td>
                </tr>
              ) : (
                filtered.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-800/40 transition-colors font-mono">
                    <td className="py-3 px-4 text-slate-400 text-[11px] whitespace-nowrap">
                      {item.checkedAt?.replace('T', ' ').split('.')[0]}
                    </td>
                    <td className="py-3 px-4 font-sans font-semibold text-slate-200">
                      {item.apiName}
                    </td>
                    <td className="py-3 px-4 font-sans">
                      <StatusBadge status={item.status} type="api" />
                    </td>
                    <td className="py-3 px-4 text-slate-300">
                      {item.statusCode ? (
                        <span className={item.statusCode < 400 ? 'text-emerald-400' : 'text-rose-400 font-bold'}>
                          {item.statusCode}
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>
                    <td className="py-3 px-4 text-slate-300">
                      {item.responseTimeMs ? `${item.responseTimeMs}ms` : '—'}
                    </td>
                    <td className="py-3 px-4 text-slate-400 text-[11px] max-w-sm truncate font-sans">
                      {item.errorMessage || <span className="text-slate-600">None</span>}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination footer */}
        <div className="p-3 border-t border-slate-800 flex items-center justify-between text-xs text-slate-400">
          <span>
            Page {page + 1} of {Math.max(1, totalPages)}
          </span>
          <div className="flex items-center gap-1">
            <button
              onClick={() => fetchHistory(page - 1)}
              disabled={page <= 0}
              className="p-1 rounded bg-slate-800 disabled:opacity-40 hover:bg-slate-700 transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={() => fetchHistory(page + 1)}
              disabled={page >= totalPages - 1}
              className="p-1 rounded bg-slate-800 disabled:opacity-40 hover:bg-slate-700 transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

