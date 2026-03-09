import { Typography, Input, Space, Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { PatientTable } from './components/PatientTable';
import { usePatients } from './hooks/usePatients';

const { Title } = Typography;
const { Search } = Input;

export const PatientListPage = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();
  const { data: patients, isLoading, error } = usePatients();

  const filteredPatients = patients?.filter((patient) => {
    const fullName = `${patient.firstName} ${patient.lastName}`.toLowerCase();
    const search = searchTerm.toLowerCase();
    return (
      fullName.includes(search) ||
      patient.patientCode.toLowerCase().includes(search)
    );
  });

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <Title level={2} style={{ margin: 0 }}>Patients</Title>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          size="large"
          onClick={() => navigate('/patients/register')}
        >
          Register Patient
        </Button>
      </div>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Search
          placeholder="Search by name or patient code"
          allowClear
          size="large"
          onSearch={setSearchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ maxWidth: 400 }}
        />
        <PatientTable
          patients={filteredPatients || []}
          loading={isLoading}
          error={error instanceof Error ? error.message : undefined}
        />
      </Space>
    </div>
  );
};
