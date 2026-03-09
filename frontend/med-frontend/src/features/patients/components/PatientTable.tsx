import { Table, Alert } from 'antd';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';

interface Patient {
  patientCode: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  insurancePlanCode?: string;
  insuranceProvider?: string;
}

interface PatientTableProps {
  patients: Patient[];
  loading: boolean;
  error?: string;
}

export const PatientTable = ({ patients, loading, error }: PatientTableProps) => {
  const navigate = useNavigate();

  const columns: ColumnsType<Patient> = [
    {
      title: 'Patient Code',
      dataIndex: 'patientCode',
      key: 'patientCode',
      width: 150,
    },
    {
      title: 'First Name',
      dataIndex: 'firstName',
      key: 'firstName',
    },
    {
      title: 'Last Name',
      dataIndex: 'lastName',
      key: 'lastName',
    },
    {
      title: 'Date of Birth',
      dataIndex: 'dateOfBirth',
      key: 'dateOfBirth',
      width: 150,
    },
    {
      title: 'Insurance Provider',
      dataIndex: 'insuranceProvider',
      key: 'insuranceProvider',
      render: (value) => value || '-',
    },
    {
      title: 'Plan Code',
      dataIndex: 'insurancePlanCode',
      key: 'insurancePlanCode',
      width: 120,
      render: (value) => value || '-',
    },
  ];

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon />;
  }

  return (
    <Table
      columns={columns}
      dataSource={patients}
      rowKey="patientCode"
      loading={loading}
      onRow={(record) => ({
        onClick: () => navigate(`/patients/${record.patientCode}`),
        style: { cursor: 'pointer' },
      })}
      pagination={{
        pageSize: 10,
        showSizeChanger: true,
        showTotal: (total) => `Total ${total} patients`,
      }}
    />
  );
};
