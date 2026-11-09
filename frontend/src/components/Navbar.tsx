import React, { useState, useEffect } from 'react';
import { Bell, Sun, Moon, LogOut, Radio } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';
import { wsService } from '../services/websocket';
import api from '../services/api';
import { NotificationItem } from '../types';
import { useNavigate } from 'react-router-dom';

export const Navbar: React.FC = () => {
  const { user, logout } = useAuth();
  const { theme, toggleTheme } = useTheme();
  const navigate = useNavigate();

  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [showNotifications, setShowNotifications] = useState<boolean>(false);
  const [wsConnected, setWsConnected] = useState<boolean>(false);

  useEffect(() => {
    // Fetch unread count & initial notifications
    const fetchNotifications = async () => {
      try {
        const [countRes, listRes] = await Promise.all([
          api.get('/notifications/unread-count'),
          api.get('/notifications?page=0&size=5'),
        ]);
        setUnreadCount(countRes.data.data);
        setNotifications(listRes.data.data.content);
      } catch (err) {
        console.error('Failed to load notifications', err);
      }
    };

    fetchNotifications();

    // Check WS status every 3 seconds
    const interval = setInterval(() => {
      setWsConnected(wsService.connected);
    }, 3000);

    // Listen to real-time notifications via WS
    const unsubscribe = wsService.onNotification((newNotif) => {
      setUnreadCount((prev) => prev + 1);
      setNotifications((prev) => [
        {
          id: newNotif.id,
          title: newNotif.title,
          message: newNotif.message,
          type: newNotif.type,
          isRead: false,
          createdAt: newNotif.createdAt,
        },
        ...prev.slice(0, 4),
      ]);
    });

    return () => {
      clearInterval(interval);
      unsubscribe();
    };
  }, []);

  const markAllAsRead = async () => {
    try {
      await api.patch('/notifications/read-all');
      setUnreadCount(0);
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <header className="h-16 bg-slate-900 border-b border-slate-800 flex items-center justify-between px-6 z-20">
      {/* Left Status pill */}
      <div className="flex items-center gap-3">
        <div className="flex items-center gap-2 px-3 py-1 rounded-full bg-slate-800/80 border border-slate-700/60 text-xs">
          <Radio className={`w-3.5 h-3.5 ${wsConnected ? 'text-emerald-400 animate-pulse' : 'text-amber-400'}`} />
          <span className="text-slate-300 font-medium">
            {wsConnected ? 'Live Real-Time Engine' : 'Syncing Engine...'}
          </span>
        </div>
      </div>

      {/* Right Action Icons */}
      <div className="flex items-center gap-4">
        {/* Theme Toggle */}
        <button
          onClick={toggleTheme}
          className="p-2 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors"
          title="Toggle theme"
        >
          {theme === 'dark' ? <Sun className="w-4 h-4" /> : <Moon className="w-4 h-4" />}
        </button>

        {/* Notifications Dropdown */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications((prev) => !prev)}
            className="relative p-2 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-colors"
            title="Notifications"
          >
            <Bell className="w-4 h-4" />
            {unreadCount > 0 && (
              <span className="absolute top-1.5 right-1.5 h-4 min-w-[16px] px-1 rounded-full bg-rose-500 text-[10px] font-bold text-white flex items-center justify-center animate-pulse">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>

          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 bg-slate-900 border border-slate-800 rounded-xl shadow-2xl overflow-hidden z-50">
              <div className="p-3 border-b border-slate-800 flex items-center justify-between">
                <span className="text-xs font-semibold text-slate-200">Notifications</span>
                {unreadCount > 0 && (
                  <button
                    onClick={markAllAsRead}
                    className="text-[11px] text-emerald-400 hover:text-emerald-300 transition-colors"
                  >
                    Mark all read
                  </button>
                )}
              </div>
              <div className="max-h-72 overflow-y-auto divide-y divide-slate-800/60">
                {notifications.length === 0 ? (
                  <div className="p-6 text-center text-xs text-slate-400">No notifications</div>
                ) : (
                  notifications.map((n) => (
                    <div
                      key={n.id}
                      className={`p-3 text-xs hover:bg-slate-800/40 cursor-pointer transition-colors ${
                        !n.isRead ? 'bg-slate-800/20' : ''
                      }`}
                      onClick={() => {
                        setShowNotifications(false);
                        navigate('/notifications');
                      }}
                    >
                      <p className="font-medium text-slate-200 truncate">{n.title}</p>
                      <p className="text-slate-400 text-[11px] line-clamp-2 mt-0.5">{n.message}</p>
                    </div>
                  ))
                )}
              </div>
              <div className="p-2 border-t border-slate-800 text-center">
                <button
                  onClick={() => {
                    setShowNotifications(false);
                    navigate('/notifications');
                  }}
                  className="text-xs text-slate-400 hover:text-slate-200"
                >
                  View all notifications →
                </button>
              </div>
            </div>
          )}
        </div>

        {/* User profile & Logout */}
        <div className="flex items-center gap-3 pl-2 border-l border-slate-800">
          <div className="text-right">
            <p className="text-xs font-semibold text-slate-200">{user?.fullName}</p>
            <p className="text-[10px] text-slate-400">{user?.email}</p>
          </div>
          <button
            onClick={logout}
            className="p-2 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-950/20 transition-colors"
            title="Log out"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
};

