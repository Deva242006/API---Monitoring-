import React, { useState } from 'react';
import { Settings as SettingsIcon, Bell, Server, Mail, ShieldCheck, Check } from 'lucide-react';
import { useToast } from '../components/Toast';

export const Settings: React.FC = () => {
  const { addToast } = useToast();
  const [smtpHost, setSmtpHost] = useState('smtp.gmail.com');
  const [smtpPort, setSmtpPort] = useState('587');
  const [failureThreshold, setFailureThreshold] = useState('3');
  const [defaultInterval, setDefaultInterval] = useState('60');
  const [saved, setSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSaved(true);
    addToast({ type: 'success', title: 'System settings saved' });
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
          System & Engine Configuration
        </h2>
        <p className="text-xs text-slate-400 mt-1">
          Tune the scheduler probe concurrency, outage trigger thresholds, and email dispatch preferences
        </p>
      </div>

      <form onSubmit={handleSave} className="space-y-6">
        {/* Monitoring Engine Settings */}
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 space-y-4">
          <div className="flex items-center gap-2.5 pb-3 border-b border-slate-800">
            <Server className="w-4 h-4 text-emerald-400" />
            <h3 className="text-sm font-semibold text-white">Monitoring Engine Defaults</h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">
                Default Monitoring Interval (seconds)
              </label>
              <input
                type="number"
                value={defaultInterval}
                onChange={(e) => setDefaultInterval(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60 font-mono"
              />
              <p className="text-[10px] text-slate-500 mt-1">Global interval applied to newly registered APIs.</p>
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">
                Consecutive Failure Threshold
              </label>
              <input
                type="number"
                value={failureThreshold}
                onChange={(e) => setFailureThreshold(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60 font-mono"
              />
              <p className="text-[10px] text-slate-500 mt-1">
                Number of sequential failed probes before automatically declaring an incident.
              </p>
            </div>
          </div>
        </div>

        {/* Email & SMTP Configuration */}
        <div className="p-5 rounded-xl bg-slate-900 border border-slate-800 space-y-4">
          <div className="flex items-center gap-2.5 pb-3 border-b border-slate-800">
            <Mail className="w-4 h-4 text-sky-400" />
            <h3 className="text-sm font-semibold text-white">Alert Dispatch & SMTP Settings</h3>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">SMTP Server Host</label>
              <input
                type="text"
                value={smtpHost}
                onChange={(e) => setSmtpHost(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60 font-mono"
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-slate-300 mb-1.5">SMTP Port</label>
              <input
                type="text"
                value={smtpPort}
                onChange={(e) => setSmtpPort(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60 font-mono"
              />
            </div>
          </div>
        </div>

        {/* Save button */}
        <div className="flex justify-end">
          <button
            type="submit"
            className="px-4 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-semibold text-xs transition-colors flex items-center gap-1.5 shadow-sm"
          >
            {saved ? <Check className="w-4 h-4" /> : null}
            <span>{saved ? 'Configuration Saved' : 'Save Configurations'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
