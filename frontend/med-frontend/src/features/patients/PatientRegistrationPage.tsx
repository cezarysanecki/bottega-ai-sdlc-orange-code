import { Card, Typography, Form, Input, DatePicker, Select, Button, message } from 'antd';
import { useNavigate } from 'react-router-dom';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { patientService } from '../../shared/api/patientService';

const { Title } = Typography;

export const PatientRegistrationPage = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: patientService.createPatient,
    onSuccess: () => {
      message.success('Patient registered successfully');
      queryClient.invalidateQueries({ queryKey: ['patients'] });
      form.resetFields();
      navigate('/patients');
    },
    onError: (error: any) => {
      message.error(error.message || 'Failed to register patient');
    }
  });

  const onFinish = (values: any) => {
    const data = {
      ...values,
      dateOfBirth: values.dateOfBirth?.format('YYYY-MM-DD')
    };
    createMutation.mutate(data);
  };

  return (
    <div style={{ padding: '24px', maxWidth: '800px', margin: '0 auto' }}>
      <Title level={2}>Register New Patient</Title>
      <Card>
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
        >
          <Form.Item
            name="patientCode"
            label="Patient Code"
            rules={[{ required: true, message: 'Patient code is required' }]}
          >
            <Input placeholder="PAT-0001" />
          </Form.Item>

          <Form.Item
            name="firstName"
            label="First Name"
            rules={[{ required: true }]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            name="lastName"
            label="Last Name"
            rules={[{ required: true }]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            name="dateOfBirth"
            label="Date of Birth"
            rules={[{ required: true }]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="gender" label="Gender">
            <Select
              placeholder="Select gender"
              options={[
                { value: 'MALE', label: 'Male' },
                { value: 'FEMALE', label: 'Female' },
                { value: 'OTHER', label: 'Other' }
              ]}
            />
          </Form.Item>

          <Form.Item
            name="email"
            label="Email"
            rules={[{ type: 'email', message: 'Invalid email' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item name="primaryPhone" label="Phone">
            <Input />
          </Form.Item>

          <Form.Item name="insurancePlanCode" label="Insurance Plan">
            <Select
              placeholder="Select insurance plan"
              allowClear
              options={[
                { value: 'BCBS-001', label: 'Blue Cross Blue Shield' },
                { value: 'AETNA-001', label: 'Aetna' },
                { value: 'UNITED-001', label: 'United Healthcare' }
              ]}
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={createMutation.isPending}
            >
              Register Patient
            </Button>
            <Button
              style={{ marginLeft: '8px' }}
              onClick={() => navigate('/patients')}
            >
              Cancel
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};
