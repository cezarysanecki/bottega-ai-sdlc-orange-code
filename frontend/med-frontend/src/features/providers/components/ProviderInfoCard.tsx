import { Card, Descriptions, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import { CalendarOutlined } from '@ant-design/icons';

interface Provider {
  providerCode: string;
  fullName: string;
  specialty: string;
  officeRoom?: string;
  preferredContact?: string;
}

interface ProviderInfoCardProps {
  provider: Provider;
}

export const ProviderInfoCard = ({ provider }: ProviderInfoCardProps) => {
  const navigate = useNavigate();

  return (
    <Card
      title="Provider Information"
      extra={
        <Button
          type="primary"
          icon={<CalendarOutlined />}
          onClick={() => navigate(`/appointments/new?provider=${provider.providerCode}`)}
        >
          Book Appointment
        </Button>
      }
    >
      <Descriptions column={1} bordered>
        <Descriptions.Item label="Provider Code">{provider.providerCode}</Descriptions.Item>
        <Descriptions.Item label="Full Name">{provider.fullName}</Descriptions.Item>
        <Descriptions.Item label="Specialty">{provider.specialty}</Descriptions.Item>
        <Descriptions.Item label="Office Room">
          {provider.officeRoom || 'Not specified'}
        </Descriptions.Item>
        <Descriptions.Item label="Preferred Contact">
          {provider.preferredContact || 'Not specified'}
        </Descriptions.Item>
      </Descriptions>
    </Card>
  );
};
