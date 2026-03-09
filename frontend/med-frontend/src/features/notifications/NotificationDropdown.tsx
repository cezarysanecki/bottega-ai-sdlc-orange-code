import { List, Typography, Empty } from 'antd';
import { NotificationItem } from './components/NotificationItem';

const { Text } = Typography;

interface Notification {
  id: string;
  message: string;
  timestamp: string;
  read: boolean;
}

interface NotificationDropdownProps {
  notifications: Notification[];
}

export const NotificationDropdown = ({ notifications }: NotificationDropdownProps) => {
  if (notifications.length === 0) {
    return (
      <div style={{ width: 400, padding: 24, backgroundColor: 'white', borderRadius: 8 }}>
        <Empty description="No notifications" />
      </div>
    );
  }

  return (
    <div style={{ width: 400, backgroundColor: 'white', borderRadius: 8 }}>
      <div style={{ padding: '12px 16px', borderBottom: '1px solid #f0f0f0' }}>
        <Text strong>Notifications</Text>
      </div>
      <List
        dataSource={notifications}
        renderItem={(notification) => (
          <NotificationItem notification={notification} />
        )}
        style={{ maxHeight: 400, overflowY: 'auto' }}
      />
    </div>
  );
};
