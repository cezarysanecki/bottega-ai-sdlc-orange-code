import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Button, Space, Spin, Alert } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useProviderDetail } from './hooks/useProviderDetail';
import { ProviderInfoCard } from './components/ProviderInfoCard';

const { Title } = Typography;

export const ProviderDetailPage = () => {
  const { code } = useParams<{ code: string }>();
  const navigate = useNavigate();
  const { data: provider, isLoading, error } = useProviderDetail(code || '');

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error || !provider) {
    return (
      <Alert
        message="Error loading provider"
        description={error instanceof Error ? error.message : 'Provider not found'}
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
            onClick={() => navigate('/providers')}
            style={{ paddingLeft: 0 }}
          >
            Back to Providers
          </Button>
          <Title level={2}>{provider.fullName}</Title>
        </div>
        <ProviderInfoCard provider={provider} />
      </Space>
    </div>
  );
};
