import { useState, useEffect } from 'react';
import { fetchJson } from '../../../shared/api/fetchClient';

interface Notification {
  id: string;
  message: string;
  timestamp: string;
  read: boolean;
}

export const useNotificationPolling = () => {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    const pollNotifications = async () => {
      try {
        const response = await fetchJson<string[]>('/api/notifications/poll');

        // Transform backend response to notification format
        // Backend returns array of strings, we convert to notification objects
        const messages: string[] = Array.isArray(response) ? response : [];
        const newNotifications: Notification[] = messages.map((msg, index) => ({
          id: `notif-${Date.now()}-${index}`,
          message: msg,
          timestamp: new Date().toISOString(),
          read: false,
        }));

        // Only add truly new notifications (not duplicates)
        setNotifications((prev) => {
          const existingMessages = new Set(prev.map(n => n.message));
          const filtered = newNotifications.filter(n => !existingMessages.has(n.message));
          return [...filtered, ...prev].slice(0, 20); // Keep last 20
        });

        setUnreadCount((prev) => prev + newNotifications.filter(n => {
          const existingMessages = new Set(notifications.map(x => x.message));
          return !existingMessages.has(n.message);
        }).length);
      } catch (error) {
        console.error('Failed to poll notifications:', error);
      }
    };

    // Poll immediately on mount
    pollNotifications();

    // Then poll every 30 seconds
    const interval = setInterval(pollNotifications, 30000);

    return () => clearInterval(interval);
  }, []);

  return { notifications, unreadCount };
};
