import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Button, Space, Spin, Alert, Tabs } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { usePatientDetail } from './hooks/usePatientDetail';
import { PatientInfoCard } from './components/PatientInfoCard';
import { InsuranceCard } from './components/InsuranceCard';

const { Title } = Typography;

export const PatientDetailPage = () => {
  const { code } = useParams<{ code: string }>();
  const navigate = useNavigate();
  const { data: patient, isLoading, error } = usePatientDetail(code || '');

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error || !patient) {
    return (
      <Alert
        message="Error loading patient"
        description={error instanceof Error ? error.message : 'Patient not found'}
        type="error"
        showIcon
      />
    );
  }

  const items = [
    {
      key: 'demographics',
      label: 'Demographics',
      children: <PatientInfoCard patient={patient} />,
    },
    {
      key: 'insurance',
      label: 'Insurance',
      children: <InsuranceCard patientCode={patient.patientCode} />,
    },
  ];

  return (
    <div>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div>
          <Button
            type="link"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/patients')}
            style={{ paddingLeft: 0 }}
          >
            Back to Patients
          </Button>
          <Title level={2}>
            {patient.firstName} {patient.lastName}
          </Title>
        </div>
        <Tabs items={items} />
      </Space>
    </div>
  );
};
