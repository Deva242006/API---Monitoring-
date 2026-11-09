import React from 'react';
import { ApiStatus, IncidentSeverity, IncidentStatus } from '../types';

interface StatusBadgeProps {
  status?: ApiStatus | IncidentStatus | IncidentSeverity | string;
  type?: 'api' | 'incident-status' | 'severity';
  size?: 'sm' | 'md';
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, type = 'api', size = 'sm' }) => {
  if (!status) return null;

  const sizeClasses = size === 'sm' ? 'px-2.5 py-0.5 text-xs font-medium' : 'px-3 py-1 text-sm font-medium';

  let badgeColor = 'bg-slate-800 text-slate-300 border-slate-700';
  let dotColor = 'bg-slate-400';

  if (type === 'api') {
    switch (status) {
      case 'HEALTHY':
        badgeColor = 'bg-emerald-950/60 text-emerald-400 border-emerald-800/60';
        dotColor = 'bg-emerald-400';
        break;
      case 'DEGRADED':
        badgeColor = 'bg-amber-950/60 text-amber-400 border-amber-800/60';
        dotColor = 'bg-amber-400';
        break;
      case 'DOWN':
        badgeColor = 'bg-rose-950/60 text-rose-400 border-rose-800/60';
        dotColor = 'bg-rose-400 animate-pulse';
        break;
      default:
        badgeColor = 'bg-slate-800/80 text-slate-400 border-slate-700';
        dotColor = 'bg-slate-400';
    }
  } else if (type === 'severity') {
    switch (status) {
      case 'CRITICAL':
        badgeColor = 'bg-red-950 text-red-400 border-red-800 font-bold';
        dotColor = 'bg-red-500';
        break;
      case 'HIGH':
        badgeColor = 'bg-rose-950/60 text-rose-400 border-rose-800';
        dotColor = 'bg-rose-400';
        break;
      case 'MEDIUM':
        badgeColor = 'bg-amber-950/60 text-amber-400 border-amber-800';
        dotColor = 'bg-amber-400';
        break;
      case 'LOW':
        badgeColor = 'bg-blue-950/60 text-blue-400 border-blue-800';
        dotColor = 'bg-blue-400';
        break;
    }
  } else if (type === 'incident-status') {
    switch (status) {
      case 'OPEN':
        badgeColor = 'bg-red-950/60 text-red-400 border-red-800';
        dotColor = 'bg-red-400';
        break;
      case 'ACKNOWLEDGED':
        badgeColor = 'bg-amber-950/60 text-amber-400 border-amber-800';
        dotColor = 'bg-amber-400';
        break;
      case 'IN_PROGRESS':
        badgeColor = 'bg-blue-950/60 text-blue-400 border-blue-800';
        dotColor = 'bg-blue-400';
        break;
      case 'RESOLVED':
        badgeColor = 'bg-emerald-950/60 text-emerald-400 border-emerald-800';
        dotColor = 'bg-emerald-400';
        break;
    }
  }

  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border ${sizeClasses} ${badgeColor}`}>
      <span className={`h-1.5 w-1.5 rounded-full ${dotColor}`} />
      <span>{status}</span>
    </span>
  );
};

