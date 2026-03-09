import { Card, Button, Descriptions, Space, Spin, Alert, Tag } from 'antd';
import { SyncOutlined, DeleteOutlined } from '@ant-design/icons';
import { useInsuranceEligibility } from '../hooks/useInsuranceEligibility';
import { useNotification } from '../../../app/contexts/NotificationContext';

interface InsuranceCardProps {
  patientCode: string;
}

export const InsuranceCard = ({ patientCode }: InsuranceCardProps) => {
  const { showNotification } = useNotification();
  const {
    data: eligibility,
    isLoading,
    error,
    checkEligibility,
    invalidateCache,
    isChecking,
    isInvalidating,
  } = useInsuranceEligibility(patientCode);

  const handleCheck = async () => {
    try {
      await checkEligibility();
      showNotification('success', 'Insurance eligibility checked successfully');
    } catch (err) {
      showNotification('error', 'Failed to check insurance eligibility');
    }
  };

  const handleInvalidate = async () => {
    try {
      await invalidateCache();
      showNotification('success', 'Insurance cache invalidated');
    } catch (err) {
      showNotification('error', 'Failed to invalidate cache');
    }
  };

  if (isLoading) {
    return (
      <Card title="Insurance Information">
        <div style={{ textAlign: 'center', padding: '20px' }}>
          <Spin />
        </div>
      </Card>
    );
  }

  return (
    <Card
      title="Insurance Information"
      extra={
        <Space>
          <Button
            icon={<SyncOutlined />}
            onClick={handleCheck}
            loading={isChecking}
            type="primary"
          >
            Check Eligibility
          </Button>
          {eligibility && (
            <Button
              icon={<DeleteOutlined />}
              onClick={handleInvalidate}
              loading={isInvalidating}
              danger
            >
              Clear Cache
            </Button>
          )}
        </Space>
      }
    >
      {error && (
        <Alert
          message="Error"
          description={error instanceof Error ? error.message : 'Unknown error'}
          type="error"
          showIcon
          style={{ marginBottom: 16 }}
        />
      )}

      {eligibility ? (
        <Descriptions column={1} bordered>
          <Descriptions.Item label="Plan Code">{eligibility.planCode || 'N/A'}</Descriptions.Item>
          <Descriptions.Item label="Status">
            <Tag color={eligibility.status === 'ACTIVE' ? 'green' : 'red'}>
              {eligibility.status}
            </Tag>
          </Descriptions.Item>
          <Descriptions.Item label="Copay">
            ${((eligibility.copay || 0) / 100).toFixed(2)}
          </Descriptions.Item>
          <Descriptions.Item label="Deductible Remaining">
            ${((eligibility.deductibleRemaining || 0) / 100).toFixed(2)}
          </Descriptions.Item>
          <Descriptions.Item label="Coverage Notes">
            {eligibility.coverageNotes || 'None'}
          </Descriptions.Item>
          <Descriptions.Item label="Fetched At">{eligibility.fetchedAt}</Descriptions.Item>
          <Descriptions.Item label="From Cache">
            {eligibility.fromCache ? 'Yes' : 'No'}
          </Descriptions.Item>
        </Descriptions>
      ) : (
        <Alert
          message="No eligibility data"
          description="Click 'Check Eligibility' to fetch insurance information"
          type="info"
          showIcon
        />
      )}
    </Card>
  );
};
