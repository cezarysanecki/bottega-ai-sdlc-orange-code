import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Button, Space, Spin, Alert } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useEncounterDetail } from './hooks/useEncounterDetail';
import { EncounterInfoCard } from './components/EncounterInfoCard';
import { EncounterProceduresTable } from './components/EncounterProceduresTable';
import { EncounterSummaryEditor } from './components/EncounterSummaryEditor';

const { Title } = Typography;

export const EncounterDetailPage = () => {
  const { encounterCode } = useParams<{ encounterCode: string }>();
  const navigate = useNavigate();
  const { data: encounter, isLoading, error } = useEncounterDetail(encounterCode || '');

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error || !encounter) {
    return (
      <Alert
        message="Error loading encounter"
        description={error instanceof Error ? error.message : 'Encounter not found'}
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
            onClick={() => navigate('/encounters')}
            style={{ paddingLeft: 0 }}
          >
            Back to Encounters
          </Button>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Title level={2}>Encounter {encounter.encounterCode}</Title>
          </div>
        </div>

        <EncounterInfoCard encounter={encounter} />

        <EncounterProceduresTable procedures={encounter.procedures || []} />

        <EncounterSummaryEditor encounter={encounter} />
      </Space>
    </div>
  );
};
