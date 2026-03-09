import { Card, Descriptions, Button, Alert } from 'antd';
import { SyncOutlined } from '@ant-design/icons';
import { TelehealthStatusBadge } from './TelehealthStatusBadge';
import { useSyncSession } from '../hooks/useSyncSession';
import { useNotification } from '../../../app/contexts/NotificationContext';

interface TelehealthSession {
  vendorSessionId: string;
  status: string;
  startedAt?: string;
  endedAt?: string;
  lastSyncedAt?: string;
  failureReason?: string;
}

interface SessionInfoCardProps {
  session: TelehealthSession;
}

export const SessionInfoCard = ({ session }: SessionInfoCardProps) => {
  const { showNotification } = useNotification();
  const { mutateAsync: syncSession, isPending } = useSyncSession();

  const handleSync = async () => {
    try {
      await syncSession(session.vendorSessionId);
      showNotification('success', 'Telehealth session synced successfully');
    } catch (error) {
      showNotification('error', 'Failed to sync telehealth session');
    }
  };

  return (
    <Card
      title="Session Information"
      extra={
        <Button
          icon={<SyncOutlined />}
          onClick={handleSync}
          loading={isPending}
          type="primary"
        >
          Sync with Vendor
        </Button>
      }
    >
      {session.failureReason && (
        <Alert
          message="Session Failed"
          description={session.failureReason}
          type="error"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      <Descriptions column={1} bordered>
        <Descriptions.Item label="Vendor Session ID">
          {session.vendorSessionId}
        </Descriptions.Item>
        <Descriptions.Item label="Status">
          <TelehealthStatusBadge status={session.status} />
        </Descriptions.Item>
        <Descriptions.Item label="Started At">
          {session.startedAt ? new Date(session.startedAt).toLocaleString() : 'Not started'}
        </Descriptions.Item>
        <Descriptions.Item label="Ended At">
          {session.endedAt ? new Date(session.endedAt).toLocaleString() : 'Not ended'}
        </Descriptions.Item>
        <Descriptions.Item label="Last Synced At">
          {session.lastSyncedAt ? new Date(session.lastSyncedAt).toLocaleString() : 'Never synced'}
        </Descriptions.Item>
      </Descriptions>
    </Card>
  );
};
