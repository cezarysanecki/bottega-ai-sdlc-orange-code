import { useState } from 'react';
import { Modal, Form, Input, Select, Steps, message, Space, Typography } from 'antd';
import type { Appointment } from '../hooks/useAppointments';
import { useProcedures } from '../../encounters/hooks/useProcedures';
import { useCreateEncounter } from '../../encounters/hooks/useCreateEncounter';
import { appointmentService } from '../../../shared/api/appointmentService';
import { encounterService } from '../../../shared/api/encounterService';

const { TextArea } = Input;
const { Text } = Typography;

interface CompleteAppointmentModalProps {
  open: boolean;
  appointment: Appointment | null;
  onClose: () => void;
  onSuccess: () => void;
}

interface FormValues {
  reason: string;
  procedureCodes: string[];
  summary?: string;
}

export const CompleteAppointmentModal = ({
  open,
  appointment,
  onClose,
  onSuccess
}: CompleteAppointmentModalProps) => {
  const [form] = Form.useForm<FormValues>();
  const [currentStep, setCurrentStep] = useState(0);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [encounterExists, setEncounterExists] = useState(false);

  const { data: procedures, isLoading: proceduresLoading } = useProcedures();
  const createEncounterMutation = useCreateEncounter();

  // Check if encounter already exists when modal opens
  const checkExistingEncounter = async () => {
    if (appointment) {
      const exists = await encounterService.checkExistingEncounter(appointment.id);
      setEncounterExists(exists);
    }
  };

  const handleModalOpen = () => {
    if (open && appointment) {
      checkExistingEncounter();
      form.resetFields();
      setCurrentStep(0);
    }
  };

  // Call when modal opens
  useState(() => {
    handleModalOpen();
  });

  const validateStep1 = async () => {
    try {
      await form.validateFields(['reason']);
      return true;
    } catch {
      return false;
    }
  };

  const handleNext = async () => {
    const isValid = await validateStep1();
    if (isValid) {
      setCurrentStep(1);
    }
  };

  const handleBack = () => {
    setCurrentStep(0);
  };

  const handleCancel = () => {
    form.resetFields();
    setCurrentStep(0);
    setEncounterExists(false);
    onClose();
  };

  const handleSubmit = async () => {
    if (!appointment) return;

    try {
      // Validate step 2 fields
      await form.validateFields(['procedureCodes', 'summary']);

      const values = form.getFieldsValue();

      // Validate appointment status
      if (appointment.status !== 'CONFIRMED' && appointment.status !== 'SCHEDULED') {
        message.error('Can only complete appointments that are CONFIRMED or SCHEDULED');
        return;
      }

      // Check for existing encounter
      if (encounterExists) {
        message.error('An encounter already exists for this appointment');
        return;
      }

      // Validate summary length (max 5000 chars)
      if (values.summary && values.summary.length > 5000) {
        message.error('Summary must be 5000 characters or less');
        return;
      }

      setIsSubmitting(true);

      // Step 1: Update appointment status to COMPLETED
      await appointmentService.updateStatus(appointment.id, 'COMPLETED');

      // Step 2: Create encounter with summary
      await createEncounterMutation.mutateAsync({
        appointmentId: appointment.id,
        reason: values.reason,
        procedureCodes: values.procedureCodes,
        summary: values.summary
      });

      message.success('Appointment completed and encounter created successfully!');
      handleCancel();
      onSuccess();
    } catch (error: any) {
      console.error('Error completing appointment:', error);
      message.error(error.response?.data?.message || 'Failed to complete appointment and create encounter');
    } finally {
      setIsSubmitting(false);
    }
  };

  const procedureOptions = procedures?.map(p => ({
    label: `${p.procedureCode} - ${p.description} ($${p.defaultCost})`,
    value: p.procedureCode
  })) || [];

  const steps = [
    {
      title: 'Confirm Completion',
      content: (
        <Form.Item
          name="reason"
          label="Reason for Visit"
          rules={[
            { required: true, message: 'Please enter the reason for visit' },
            { max: 500, message: 'Reason must be 500 characters or less' }
          ]}
        >
          <TextArea
            rows={4}
            placeholder="Enter the reason for this visit (e.g., Annual checkup, Follow-up, etc.)"
            showCount
            maxLength={500}
          />
        </Form.Item>
      )
    },
    {
      title: 'Document Visit',
      content: (
        <>
          <Form.Item
            name="procedureCodes"
            label="Procedures Performed"
            rules={[
              { required: true, message: 'Please select at least one procedure' },
              {
                validator: (_, value) => {
                  if (!value || value.length === 0) {
                    return Promise.reject('At least one procedure is required');
                  }
                  return Promise.resolve();
                }
              }
            ]}
          >
            <Select
              mode="multiple"
              placeholder="Select procedures performed during the visit"
              options={procedureOptions}
              loading={proceduresLoading}
              showSearch
              filterOption={(input, option) =>
                (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
            />
          </Form.Item>
          <Form.Item
            name="summary"
            label="Clinical Summary (Optional)"
            rules={[
              { max: 5000, message: 'Summary must be 5000 characters or less' }
            ]}
          >
            <TextArea
              rows={6}
              placeholder="Enter clinical notes, observations, and treatment plan (optional)"
              showCount
              maxLength={5000}
            />
          </Form.Item>
        </>
      )
    }
  ];

  return (
    <Modal
      title="Complete Appointment & Create Encounter"
      open={open}
      onOk={currentStep === 0 ? handleNext : handleSubmit}
      okText={currentStep === 0 ? 'Next' : 'Complete & Create Encounter'}
      cancelText={currentStep === 0 ? 'Cancel' : 'Back'}
      onCancel={currentStep === 0 ? handleCancel : handleBack}
      confirmLoading={isSubmitting}
      width={700}
      destroyOnHidden
    >
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {appointment && (
          <div style={{ padding: '12px', background: '#f5f5f5', borderRadius: '4px' }}>
            <Text strong>Appointment: </Text>
            <Text>{appointment.appointmentCode}</Text>
            <br />
            <Text strong>Patient: </Text>
            <Text>{appointment.patient.firstName} {appointment.patient.lastName}</Text>
            <br />
            <Text strong>Provider: </Text>
            <Text>{appointment.provider.fullName}</Text>
          </div>
        )}

        <Steps current={currentStep} items={steps.map(s => ({ title: s.title }))} />

        <Form
          form={form}
          layout="vertical"
          preserve={false}
        >
          {steps[currentStep].content}
        </Form>
      </Space>
    </Modal>
  );
};
