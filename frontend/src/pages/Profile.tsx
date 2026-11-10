import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { User, Mail, Shield, KeyRound, Phone, Check } from 'lucide-react';
import { useToast } from '../components/Toast';

export const Profile: React.FC = () => {
  const { user } = useAuth();
  const { addToast } = useToast();
  const [phone, setPhone] = useState(user?.phone || '');
  const [saved, setSaved] = useState(false);

  const handleUpdate = (e: React.FormEvent) => {
    e.preventDefault();
    setSaved(true);
    addToast({ type: 'success', title: 'Profile updated' });
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
          User Profile & Security
        </h2>
        <p className="text-xs text-slate-400 mt-1">Manage your identity and authentication credentials</p>
      </div>

      <div className="p-6 rounded-2xl bg-slate-900 border border-slate-800 space-y-6">
        {/* User Identity Banner */}
        <div className="flex items-center gap-4 pb-6 border-b border-slate-800">
          <div className="h-16 w-16 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-xl font-bold text-emerald-400 uppercase">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div>
            <h3 className="text-base font-bold text-white">{user?.fullName}</h3>
            <p className="text-xs text-slate-400 font-mono mt-0.5">{user?.email}</p>
            <span className="inline-block mt-2 text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-emerald-400 font-semibold border border-slate-700">
              Role: {user?.role}
            </span>
          </div>
        </div>

        {/* Profile Details Form */}
        <form onSubmit={handleUpdate} className="space-y-4">
          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">Full Name</label>
            <input
              type="text"
              disabled
              value={user?.fullName || ''}
              className="w-full bg-slate-950/60 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-400 cursor-not-allowed"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">Email Address</label>
            <input
              type="email"
              disabled
              value={user?.email || ''}
              className="w-full bg-slate-950/60 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-400 cursor-not-allowed font-mono"
            />
          </div>

          <div>
            <label className="block text-xs font-medium text-slate-300 mb-1.5">Phone (for SMS alerts)</label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="+1 (555) 000-0000"
              className="w-full bg-slate-950 border border-slate-800 rounded-lg px-3.5 py-2 text-xs text-slate-200 focus:outline-none focus:border-emerald-500/60 font-mono"
            />
          </div>

          <div className="flex justify-end pt-3">
            <button
              type="submit"
              className="px-4 py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-slate-950 font-semibold text-xs transition-colors flex items-center gap-1.5"
            >
              {saved && <Check className="w-4 h-4" />}
              <span>{saved ? 'Saved' : 'Update Profile'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

