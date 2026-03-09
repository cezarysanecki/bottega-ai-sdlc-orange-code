import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Button, Space, Spin, Alert } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useTelehealthDetail } from './hooks/useTelehealthDetail';
import { SessionInfoCard } from './components/SessionInfoCard';

const { Title } = Typography;

export const TelehealthDetailPage = () => {
  const { vendorSessionId } = useParams<{ vendorSessionId: string }>();
  const navigate = useNavigate();
  const { data: session, isLoading, error } = useTelehealthDetail(vendorSessionId || '');

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error || !session) {
    return (
      <Alert
        message="Error loading telehealth session"
        description={error instanceof Error ? error.message : 'Session not found'}
        type="error"
        showIcon
      />
    );
  }

  return (
    <div>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div>
          <Button
            type="link"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/telehealth')}
            style={{ paddingLeft: 0 }}
          >
            Back to Telehealth Sessions
          </Button>
          <Title level={2}>Session {session.vendorSessionId}</Title>
        </div>
        <SessionInfoCard session={session} />
      </Space>
    </div>
  );
};
