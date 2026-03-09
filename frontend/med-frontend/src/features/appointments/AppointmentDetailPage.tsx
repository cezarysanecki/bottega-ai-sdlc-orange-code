import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Button, Space, Spin, Alert, Card, Descriptions, Tag } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useAppointmentDetail } from './hooks/useAppointmentDetail';

const { Title } = Typography;

export const AppointmentDetailPage = () => {
  const { code } = useParams<{ code: string }>();
  const navigate = useNavigate();
  const { data: appointment, isLoading, error } = useAppointmentDetail(code || '');

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error || !appointment) {
    return (
      <Alert
        message="Error loading appointment"
        description={error instanceof Error ? error.message : 'Appointment not found'}
        type="error"
        showIcon
      />
    );
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CONFIRMED':
        return 'green';
      case 'CANCELLED':
        return 'red';
      case 'COMPLETED':
        return 'blue';
      case 'NO_SHOW':
        return 'orange';
      default:
        return 'default';
    }
  };

  return (
    <div>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div>
          <Button
            type="link"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/appointments')}
            style={{ paddingLeft: 0 }}
          >
            Back to Appointments
          </Button>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Title level={2}>Appointment {appointment.appointmentCode}</Title>
            <Tag color={getStatusColor(appointment.status)}>{appointment.status}</Tag>
          </div>
        </div>

        <Card title="Appointment Details">
          <Descriptions column={2} bordered>
            <Descriptions.Item label="Appointment Code">
              {appointment.appointmentCode}
            </Descriptions.Item>
            <Descriptions.Item label="Status">
              <Tag color={getStatusColor(appointment.status)}>{appointment.status}</Tag>
            </Descriptions.Item>
            <Descriptions.Item label="Start Time">
              {appointment.startTime ? new Date(appointment.startTime).toLocaleString() : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="End Time">
              {appointment.endTime ? new Date(appointment.endTime).toLocaleString() : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Room">
              {appointment.roomCode || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Appointment Type">
              {appointment.appointmentType || '-'}
            </Descriptions.Item>
          </Descriptions>
        </Card>

        <Card title="Patient Information">
          <Descriptions column={2} bordered>
            <Descriptions.Item label="Name">
              {appointment.patient ? `${appointment.patient.firstName} ${appointment.patient.lastName}` : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Patient Code">
              {appointment.patient?.patientCode || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Date of Birth">
              {appointment.patient?.dateOfBirth ? new Date(appointment.patient.dateOfBirth).toLocaleDateString() : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Insurance Provider">
              {appointment.patient?.insuranceProvider || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Insurance Plan">
              {appointment.patient?.insurancePlanCode || '-'}
            </Descriptions.Item>
          </Descriptions>
        </Card>

        <Card title="Provider Information">
          <Descriptions column={2} bordered>
            <Descriptions.Item label="Name">
              {appointment.provider?.fullName || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Provider Code">
              {appointment.provider?.providerCode || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Specialty">
              {appointment.provider?.specialty || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Office Room">
              {appointment.provider?.officeRoom || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Preferred Contact">
              {appointment.provider?.preferredContact || '-'}
            </Descriptions.Item>
          </Descriptions>
        </Card>

        {appointment.legacyNotes && (
          <Card title="Notes">
            <p>{appointment.legacyNotes}</p>
          </Card>
        )}
      </Space>
    </div>
  );
};
