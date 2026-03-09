import { useState } from 'react';
import { Typography, Button, Space } from 'antd';
import { useNavigate } from 'react-router-dom';
import { PlusOutlined } from '@ant-design/icons';
import { AppointmentTable } from './components/AppointmentTable';
import { useAppointments } from './hooks/useAppointments';

const { Title } = Typography;

export const AppointmentListPage = () => {
  const navigate = useNavigate();
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);

  const { data, isLoading, error } = useAppointments({ page, size: pageSize });

  const handlePageChange = (newPage: number, newPageSize: number) => {
    setPage(newPage - 1); // Ant Design uses 1-based pagination, backend uses 0-based
    setPageSize(newPageSize);
  };

  return (
    <div>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={2}>Appointments</Title>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            size="large"
            onClick={() => navigate('/appointments/new')}
          >
            Book Appointment
          </Button>
        </div>
        <AppointmentTable
          appointments={data?.content || []}
          loading={isLoading}
          error={error instanceof Error ? error.message : undefined}
          pagination={{
            current: page + 1, // Convert back to 1-based for Ant Design
            pageSize,
            total: data?.totalElements || 0,
            onChange: handlePageChange,
          }}
        />
      </Space>
    </div>
  );
};
