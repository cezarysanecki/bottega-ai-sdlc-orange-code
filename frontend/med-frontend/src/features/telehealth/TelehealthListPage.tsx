import { Typography, Space } from 'antd';
import { TelehealthSessionTable } from './components/TelehealthSessionTable';
import { useTelehealthSessions } from './hooks/useTelehealthSessions';

const { Title } = Typography;

export const TelehealthListPage = () => {
  const { data: sessions, isLoading, error } = useTelehealthSessions();

  return (
    <div>
      <Title level={2}>Telehealth Sessions</Title>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <TelehealthSessionTable
          sessions={sessions || []}
          loading={isLoading}
          error={error instanceof Error ? error.message : undefined}
        />
      </Space>
    </div>
  );
};
