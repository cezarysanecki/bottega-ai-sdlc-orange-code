import { Badge, Dropdown, Button } from 'antd';
import { BellOutlined } from '@ant-design/icons';
import { NotificationDropdown } from './NotificationDropdown';
import { useNotificationPolling } from './hooks/useNotificationPolling';

export const NotificationBell = () => {
  const { notifications, unreadCount } = useNotificationPolling();

  return (
    <Dropdown
      menu={{ items: [] }}
      popupRender={() => <NotificationDropdown notifications={notifications} />}
      trigger={['click']}
      placement="bottomRight"
    >
      <Badge count={unreadCount} offset={[-5, 5]}>
        <Button
          type="text"
          icon={<BellOutlined style={{ fontSize: 20, color: 'white' }} />}
          style={{ border: 'none' }}
        />
      </Badge>
    </Dropdown>
  );
};
