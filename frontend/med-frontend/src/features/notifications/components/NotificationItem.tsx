import { List, Typography, Badge } from 'antd';

const { Text } = Typography;

interface Notification {
  id: string;
  message: string;
  timestamp: string;
  read: boolean;
}

interface NotificationItemProps {
  notification: Notification;
}

export const NotificationItem = ({ notification }: NotificationItemProps) => {
  const timeAgo = (timestamp: string) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diff = Math.floor((now.getTime() - date.getTime()) / 1000 / 60); // minutes

    if (diff < 1) return 'Just now';
    if (diff < 60) return `${diff}m ago`;
    if (diff < 1440) return `${Math.floor(diff / 60)}h ago`;
    return `${Math.floor(diff / 1440)}d ago`;
  };

  return (
    <List.Item
      style={{
        backgroundColor: notification.read ? 'white' : '#f0f5ff',
        padding: '12px 16px',
        cursor: 'pointer',
        borderBottom: '1px solid #f0f0f0',
      }}
    >
      <div style={{ width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
          <Text style={{ flex: 1 }}>{notification.message}</Text>
          {!notification.read && (
            <Badge status="processing" style={{ marginLeft: 8 }} />
          )}
        </div>
        <Text type="secondary" style={{ fontSize: 12 }}>
          {timeAgo(notification.timestamp)}
        </Text>
      </div>
    </List.Item>
  );
};
