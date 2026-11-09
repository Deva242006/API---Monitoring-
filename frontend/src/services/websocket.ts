import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

type HealthUpdateCallback = (data: {
  apiId: number;
  apiName: string;
  status: string;
  responseTime: number;
  checkedAt: string;
}) => void;

type IncidentUpdateCallback = (data: {
  type: string;
  incidentId?: number;
  apiName?: string;
  title?: string;
  severity?: string;
  assignedTo?: string;
}) => void;

type NotificationCallback = (data: {
  id: number;
  title: string;
  message: string;
  type: string;
  createdAt: string;
}) => void;

class WebSocketService {
  private client: Client | null = null;
  private isConnected: boolean = false;
  private healthSubscribers: Set<HealthUpdateCallback> = new Set();
  private incidentSubscribers: Set<IncidentUpdateCallback> = new Set();
  private notificationSubscribers: Set<NotificationCallback> = new Set();

  connect() {
    if (this.client && this.client.active) return;

    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.isConnected = true;
        console.log('[WebSocket] Connected to Sentinel STOMP broker');

        // Subscribe to live health updates
        this.client?.subscribe('/topic/health-updates', (message) => {
          try {
            const data = JSON.parse(message.body);
            this.healthSubscribers.forEach((cb) => cb(data));
          } catch (e) {
            console.error('Error parsing health update', e);
          }
        });

        // Subscribe to incidents
        this.client?.subscribe('/topic/incidents', (message) => {
          try {
            const data = JSON.parse(message.body);
            this.incidentSubscribers.forEach((cb) => cb(data));
          } catch (e) {
            console.error('Error parsing incident update', e);
          }
        });

        // Subscribe to personal notifications
        this.client?.subscribe('/user/queue/notifications', (message) => {
          try {
            const data = JSON.parse(message.body);
            this.notificationSubscribers.forEach((cb) => cb(data));
          } catch (e) {
            console.error('Error parsing notification', e);
          }
        });
      },
      onDisconnect: () => {
        this.isConnected = false;
        console.log('[WebSocket] Disconnected from STOMP broker');
      },
      onStompError: (frame) => {
        console.error('[WebSocket] Broker error: ' + frame.headers['message']);
      },
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.isConnected = false;
    }
  }

  onHealthUpdate(cb: HealthUpdateCallback) {
    this.healthSubscribers.add(cb);
    return () => this.healthSubscribers.delete(cb);
  }

  onIncidentUpdate(cb: IncidentUpdateCallback) {
    this.incidentSubscribers.add(cb);
    return () => this.incidentSubscribers.delete(cb);
  }

  onNotification(cb: NotificationCallback) {
    this.notificationSubscribers.add(cb);
    return () => this.notificationSubscribers.delete(cb);
  }

  get connected() {
    return this.isConnected;
  }
}

export const wsService = new WebSocketService();
