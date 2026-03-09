import { Typography, Row, Col, Spin, Alert } from 'antd';
import { useDashboardStats } from './hooks/useDashboardStats';
import { StatCard } from './components/StatCard';
import {
  UserOutlined,
  MedicineBoxOutlined,
  FileTextOutlined,
  CalendarOutlined,
} from '@ant-design/icons';

const { Title } = Typography;

export const DashboardPage = () => {
  const { data: stats, isLoading, error } = useDashboardStats();

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error) {
    return (
      <Alert
        message="Error loading dashboard"
        description={error instanceof Error ? error.message : 'Unknown error'}
        type="error"
        showIcon
      />
    );
  }

  return (
    <div>
      <Title level={2}>Dashboard</Title>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="Total Patients"
            value={stats?.totalPatients || 0}
            icon={<UserOutlined style={{ fontSize: 24 }} />}
            color="#1890ff"
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="Active Providers"
            value={stats?.activeProviders || 0}
            icon={<MedicineBoxOutlined style={{ fontSize: 24 }} />}
            color="#52c41a"
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="Open Invoices"
            value={stats?.openInvoices || 0}
            icon={<FileTextOutlined style={{ fontSize: 24 }} />}
            color="#fa8c16"
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <StatCard
            title="Upcoming Appointments"
            value={stats?.upcomingAppointments || 0}
            icon={<CalendarOutlined style={{ fontSize: 24 }} />}
            color="#722ed1"
          />
        </Col>
      </Row>
    </div>
  );
};
