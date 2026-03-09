import { Modal, Form, InputNumber, Select, Input, Alert } from 'antd';
import { useRecordPayment } from './hooks/useRecordPayment';
import { useNotification } from '../../app/contexts/NotificationContext';
import { useState } from 'react';

interface PaymentModalProps {
  visible: boolean;
  onClose: () => void;
  invoiceNumber: string;
  balanceCents: number;
}

export const PaymentModal = ({ visible, onClose, invoiceNumber, balanceCents }: PaymentModalProps) => {
  const [form] = Form.useForm();
  const { showNotification } = useNotification();
  const { mutateAsync: recordPayment, isPending } = useRecordPayment();
  const [validationError, setValidationError] = useState<string | null>(null);

  const handleSubmit = async (values: any) => {
    const amountCents = Math.round(values.amountDollars * 100);

    // Validation: Cannot exceed invoice balance
    if (amountCents > balanceCents) {
      setValidationError(`Payment amount cannot exceed invoice balance of $${(balanceCents / 100).toFixed(2)}`);
      return;
    }

    try {
      await recordPayment({
        invoiceNumber,
        amountCents,
        method: values.method,
        paymentReference: values.paymentReference,
        postedBy: values.postedBy || 'System',
      });

      showNotification('success', 'Payment recorded successfully');
      form.resetFields();
      setValidationError(null);
      onClose();
    } catch (error) {
      showNotification('error', error instanceof Error ? error.message : 'Failed to record payment');
    }
  };

  const handleCancel = () => {
    form.resetFields();
    setValidationError(null);
    onClose();
  };

  return (
    <Modal
      title="Record Payment"
      open={visible}
      onOk={form.submit}
      onCancel={handleCancel}
      confirmLoading={isPending}
      okText="Record Payment"
      width={600}
    >
      {validationError && (
        <Alert message={validationError} type="error" showIcon style={{ marginBottom: 16 }} />
      )}

      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        initialValues={{
          method: 'CARD',
        }}
      >
        <Form.Item
          label="Amount (USD)"
          name="amountDollars"
          rules={[
            { required: true, message: 'Please enter payment amount' },
            { type: 'number', min: 0.01, message: 'Amount must be greater than 0' },
          ]}
        >
          <InputNumber
            style={{ width: '100%' }}
            precision={2}
            placeholder="0.00"
            prefix="$"
            max={balanceCents / 100}
          />
        </Form.Item>

        <Form.Item
          label="Payment Method"
          name="method"
          rules={[{ required: true, message: 'Please select payment method' }]}
        >
          <Select>
            <Select.Option value="CASH">Cash</Select.Option>
            <Select.Option value="CHECK">Check</Select.Option>
            <Select.Option value="CARD">Credit/Debit Card</Select.Option>
            <Select.Option value="WIRE">Wire Transfer</Select.Option>
            <Select.Option value="OTHER">Other</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item
          label="Payment Reference"
          name="paymentReference"
          tooltip="Transaction ID, check number, or other reference"
        >
          <Input placeholder="e.g., Check #1234, Transaction ABC123" />
        </Form.Item>

        <Form.Item
          label="Posted By"
          name="postedBy"
          rules={[{ required: true, message: 'Please enter who is posting this payment' }]}
        >
          <Input placeholder="Staff name or user ID" />
        </Form.Item>
      </Form>
    </Modal>
  );
};
