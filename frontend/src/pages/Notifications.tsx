import React, { useState, useEffect } from 'react';
import api from '../services/api';
import { NotificationItem } from '../types';
import { Bell, CheckCheck, Clock, CheckCircle } from 'lucide-react';
import { useToast } from '../components/Toast';

export const Notifications: React.FC = () => {
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const { addToast } = useToast();

  const fetchNotifications = async () => {
    try {
      const res = await api.get('/notifications?page=0&size=50');
      setNotifications(res.data.data.content);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const handleMarkAsRead = async (id: number) => {
    try {
      await api.patch(`/notifications/${id}/read`);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await api.patch('/notifications/read-all');
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      addToast({ type: 'success', title: 'All notifications marked as read' });
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="space-y-6 max-w-4xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold tracking-tight text-white flex items-center gap-2">
            Notification Center
          </h2>
          <p className="text-xs text-slate-400 mt-1">
            Real-time incident alerts, assignment updates, and API recovery dispatches
          </p>
        </div>
        <button
          onClick={handleMarkAllRead}
          className="px-3 py-1.5 rounded-lg bg-slate-900 border border-slate-800 text-xs text-slate-300 hover:text-white hover:bg-slate-800 transition-colors flex items-center gap-1.5 self-start"
        >
          <CheckCheck className="w-3.5 h-3.5 text-emerald-400" />
          <span>Mark all as read</span>
        </button>
      </div>

      <div className="rounded-xl bg-slate-900 border border-slate-800 divide-y divide-slate-800/80 overflow-hidden">
        {loading ? (
          <div className="py-12 text-center text-xs text-slate-500">Loading notifications...</div>
        ) : notifications.length === 0 ? (
          <div className="p-12 text-center flex flex-col items-center gap-2">
            <Bell className="w-8 h-8 text-slate-600" />
            <h3 className="text-sm font-semibold text-slate-300">No notifications</h3>
            <p className="text-xs text-slate-500">You're caught up with all alerts and activities.</p>
          </div>
        ) : (
          notifications.map((n) => (
            <div
              key={n.id}
              onClick={() => !n.isRead && handleMarkAsRead(n.id)}
              className={`p-4 flex items-start justify-between gap-4 transition-colors cursor-pointer ${
                !n.isRead ? 'bg-slate-800/25 hover:bg-slate-800/40' : 'hover:bg-slate-800/20'
              }`}
            >
              <div className="flex items-start gap-3">
                <div
                  className={`h-8 w-8 rounded-lg flex items-center justify-center shrink-0 mt-0.5 ${
                    n.type.includes('DOWN') || n.type.includes('INCIDENT_CREATED')
                      ? 'bg-rose-950/60 text-rose-400 border border-rose-800/60'
                      : n.type.includes('RECOVERED') || n.type.includes('RESOLVED')
                      ? 'bg-emerald-950/60 text-emerald-400 border border-emerald-800/60'
                      : 'bg-slate-800 text-slate-400'
                  }`}
                >
                  <Bell className="w-4 h-4" />
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <h4 className="text-xs font-semibold text-slate-200">{n.title}</h4>
                    {!n.isRead && (
                      <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
                    )}
                  </div>
                  <p className="text-xs text-slate-400 mt-1 whitespace-pre-wrap">{n.message}</p>
                  <span className="text-[10px] text-slate-500 font-mono mt-1.5 inline-block">
                    {n.createdAt?.replace('T', ' ').split('.')[0]}
                  </span>
                </div>
              </div>

              {!n.isRead && (
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleMarkAsRead(n.id);
                  }}
                  className="p-1 text-slate-500 hover:text-emerald-400 transition-colors"
                  title="Mark as read"
                >
                  <CheckCircle className="w-4 h-4" />
                </button>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
};

