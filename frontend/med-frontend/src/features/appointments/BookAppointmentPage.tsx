import { Typography, Button, Card, Form, Select, DatePicker, Space, Spin } from 'antd';
import { useNavigate } from 'react-router-dom';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { usePatients } from '../patients/hooks/usePatients';
import { useProviders } from '../providers/hooks/useProviders';
import { useBookAppointment } from './hooks/useBookAppointment';
import { useNotification } from '../../app/contexts/NotificationContext';

const { Title } = Typography;
const { RangePicker } = DatePicker;

export const BookAppointmentPage = () => {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const { showNotification } = useNotification();

  const { data: patients, isLoading: loadingPatients } = usePatients();
  const { data: providers, isLoading: loadingProviders } = useProviders();
  const { mutateAsync: bookAppointment, isPending } = useBookAppointment();

  const handleSubmit = async (values: any) => {
    try {
      const [startTime, endTime] = values.dateRange;


      await bookAppointment({
        patientCode: values.patientCode,
        providerCode: values.providerCode,
        appointmentType: values.appointmentType || 'CHECKUP',
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString(),
        roomCode: values.roomCode,
      });

      showNotification('success', 'Appointment booked successfully');
      navigate('/appointments');
    } catch (error) {
      showNotification('error', error instanceof Error ? error.message : 'Failed to book appointment');
    }
  };

  if (loadingPatients || loadingProviders) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <Space direction="vertical" size="large" style={{ width: '100%', maxWidth: 800 }}>
        <div>
          <Button
            type="link"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/appointments')}
            style={{ paddingLeft: 0 }}
          >
            Back to Appointments
          </Button>
          <Title level={2}>Book New Appointment</Title>
        </div>

        <Card>
          <Form
            form={form}
            layout="vertical"
            onFinish={handleSubmit}
          >
            <Form.Item
              label="Patient"
              name="patientCode"
              rules={[{ required: true, message: 'Please select a patient' }]}
            >
              <Select
                showSearch
                placeholder="Select patient"
                optionFilterProp="children"
                filterOption={(input, option) =>
                  (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
                options={patients?.map(p => ({
                  value: p.patientCode,
                  label: `${p.firstName} ${p.lastName} (${p.patientCode})`,
                }))}
              />
            </Form.Item>

            <Form.Item
              label="Provider"
              name="providerCode"
              rules={[{ required: true, message: 'Please select a provider' }]}
            >
              <Select
                showSearch
                placeholder="Select provider"
                optionFilterProp="children"
                filterOption={(input, option) =>
                  (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
                }
                options={providers?.map(p => ({
                  value: p.providerCode,
                  label: `${p.fullName} - ${p.specialty}`,
                }))}
              />
            </Form.Item>

            <Form.Item
              label="Appointment Type"
              name="appointmentType"
              initialValue="CHECKUP"
            >
              <Select>
                <Select.Option value="CHECKUP">Checkup</Select.Option>
                <Select.Option value="FOLLOW_UP">Follow-up</Select.Option>
                <Select.Option value="CONSULTATION">Consultation</Select.Option>
                <Select.Option value="TELEHEALTH">Telehealth</Select.Option>
              </Select>
            </Form.Item>

            <Form.Item
              label="Date & Time"
              name="dateRange"
              rules={[{ required: true, message: 'Please select start and end time' }]}
            >
              <RangePicker
                showTime={{ format: 'HH:mm' }}
                format="YYYY-MM-DD HH:mm"
                style={{ width: '100%' }}
              />
            </Form.Item>

            <Form.Item
              label="Room (Optional)"
              name="roomCode"
            >
              <Select placeholder="Select room" allowClear>
                <Select.Option value="ROOM-101">Room 101</Select.Option>
                <Select.Option value="ROOM-102">Room 102</Select.Option>
                <Select.Option value="ROOM-103">Room 103</Select.Option>
                <Select.Option value="ROOM-104">Room 104</Select.Option>
              </Select>
            </Form.Item>

            <Form.Item>
              <Space>
                <Button type="primary" htmlType="submit" loading={isPending} size="large">
                  Book Appointment
                </Button>
                <Button onClick={() => navigate('/appointments')} size="large">
                  Cancel
                </Button>
              </Space>
            </Form.Item>
          </Form>
        </Card>
      </Space>
    </div>
  );
};
