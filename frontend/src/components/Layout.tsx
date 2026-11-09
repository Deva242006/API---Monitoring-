import React, { useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Navbar } from './Navbar';
import { wsService } from '../services/websocket';
import { useToast } from './Toast';

export const Layout: React.FC = () => {
  const { addToast } = useToast();

  useEffect(() => {
    // Connect WebSocket
    wsService.connect();

    // Health update listener (toast if API goes DOWN)
    const unsubHealth = wsService.onHealthUpdate((data) => {
      if (data.status === 'DOWN') {
        addToast({
          type: 'error',
          title: `API Outage: ${data.apiName}`,
          message: `Health check returned DOWN status (${data.responseTime}ms)`,
        });
      }
    });

    // Incident updates listener
    const unsubIncident = wsService.onIncidentUpdate((data) => {
      if (data.type === 'INCIDENT_CREATED') {
        addToast({
          type: 'warning',
          title: 'New Incident Detected',
          message: data.title || 'An API outage has generated an incident.',
        });
      } else if (data.type === 'API_RECOVERED') {
        addToast({
          type: 'success',
          title: 'API Recovered',
          message: `${data.apiName} is now healthy again.`,
        });
      }
    });

    return () => {
      unsubHealth();
      unsubIncident();
      wsService.disconnect();
    };
  }, [addToast]);

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden font-sans">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <Navbar />
        <main className="flex-1 overflow-y-auto p-6 bg-slate-950/60">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

