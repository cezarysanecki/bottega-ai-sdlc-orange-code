import { Table, Tag, Alert, Button, Modal, Input, Space, message } from 'antd';
import { CheckCircleOutlined, CloseCircleOutlined, CheckOutlined, CloseOutlined } from '@ant-design/icons';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { Appointment } from '../hooks/useAppointments';
import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { appointmentService } from '../../../shared/api/appointmentService';
import { CompleteAppointmentModal } from './CompleteAppointmentModal';

interface AppointmentTableProps {
  appointments: Appointment[];
  loading: boolean;
  error?: string;
  pagination?: TablePaginationConfig;
}

export const AppointmentTable = ({ appointments, loading, error, pagination }: AppointmentTableProps) => {
  const [cancelModalOpen, setCancelModalOpen] = useState(false);
  const [completeModalOpen, setCompleteModalOpen] = useState(false);
  const [selectedAppointment, setSelectedAppointment] = useState<Appointment | null>(null);
  const [cancelReason, setCancelReason] = useState('');
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const cancelMutation = useMutation({
    mutationFn: (params: { id: number; reason: string }) =>
      appointmentService.cancelAppointment(params.id, params.reason),
    onSuccess: () => {
      message.success('Appointment cancelled successfully');
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
      setCancelModalOpen(false);
      setCancelReason('');
      setSelectedAppointment(null);
    },
    onError: () => {
      message.error('Failed to cancel appointment');
    }
  });

  const confirmMutation = useMutation({
    mutationFn: (id: number) =>
      appointmentService.updateStatus(id, 'CONFIRMED'),
    onSuccess: () => {
      message.success('Appointment confirmed');
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
    onError: () => {
      message.error('Failed to confirm appointment');
    }
  });

  const noShowMutation = useMutation({
    mutationFn: (id: number) =>
      appointmentService.updateStatus(id, 'NO_SHOW'),
    onSuccess: () => {
      message.success('Appointment marked as no-show');
      queryClient.invalidateQueries({ queryKey: ['appointments'] });
    },
    onError: () => {
      message.error('Failed to update appointment');
    }
  });

  const columns: ColumnsType<Appointment> = [
    {
      title: 'Appointment Code',
      dataIndex: 'appointmentCode',
      key: 'appointmentCode',
      width: 150,
      render: (code: string) => (
        <Button type="link" onClick={() => navigate(`/appointments/${code}`)} style={{ padding: 0 }}>{code}</Button>
      ),
    },
    {
      title: 'Patient',
      key: 'patient',
      render: (_, record) =>
        record.patient ? `${record.patient.firstName} ${record.patient.lastName}` : '-',
    },
    {
      title: 'Provider',
      dataIndex: ['provider', 'fullName'],
      key: 'provider',
      render: (value) => value || '-',
    },
    {
      title: 'Start Time',
      dataIndex: 'startTime',
      key: 'startTime',
      render: (value) => (value ? new Date(value).toLocaleString() : '-'),
    },
    {
      title: 'End Time',
      dataIndex: 'endTime',
      key: 'endTime',
      render: (value) => (value ? new Date(value).toLocaleString() : '-'),
    },
    {
      title: 'Room',
      dataIndex: 'roomCode',
      key: 'roomCode',
      width: 100,
      render: (value) => value || '-',
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => {
        const color = status === 'CONFIRMED' ? 'green' : status === 'CANCELLED' ? 'red' : 'blue';
        return <Tag color={color}>{status}</Tag>;
      },
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 220,
      render: (_: any, record: Appointment) => (
        <Space direction="vertical" size="small" style={{ width: '100%' }}>
          {record.status === 'SCHEDULED' && (
            <Button
              size="small"
              type="default"
              icon={<CheckOutlined />}
              onClick={() => confirmMutation.mutate(record.id)}
              loading={confirmMutation.isPending}
              block
            >
              Confirm
            </Button>
          )}
          {(record.status === 'SCHEDULED' || record.status === 'CONFIRMED') && (
            <Button
              size="small"
              type="primary"
              icon={<CheckCircleOutlined />}
              onClick={() => {
                setSelectedAppointment(record);
                setCompleteModalOpen(true);
              }}
              block
            >
              Complete & Document
            </Button>
          )}
          {(record.status === 'SCHEDULED' || record.status === 'CONFIRMED') && (
            <Button
              size="small"
              icon={<CloseCircleOutlined />}
              onClick={() => noShowMutation.mutate(record.id)}
              loading={noShowMutation.isPending}
              block
            >
              Mark No Show
            </Button>
          )}
          {(record.status === 'SCHEDULED' || record.status === 'CONFIRMED') && (
            <Button
              size="small"
              danger
              icon={<CloseOutlined />}
              onClick={() => {
                setSelectedAppointment(record);
                setCancelModalOpen(true);
              }}
              block
            >
              Cancel
            </Button>
          )}
        </Space>
      ),
    },
  ];

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon />;
  }

  return (
    <>
      <Table
        columns={columns}
        dataSource={appointments}
        rowKey="appointmentCode"
        loading={loading}
        pagination={pagination ? {
          ...pagination,
          showSizeChanger: true,
          showTotal: (total) => `Total ${total} appointments`,
          pageSizeOptions: ['5', '10', '20', '50'],
        } : false}
      />

      <Modal
        title="Cancel Appointment"
        open={cancelModalOpen}
        onOk={() => selectedAppointment && cancelMutation.mutate({ id: selectedAppointment.id, reason: cancelReason })}
        onCancel={() => {
          setCancelModalOpen(false);
          setCancelReason('');
          setSelectedAppointment(null);
        }}
        confirmLoading={cancelMutation.isPending}
      >
        <Input.TextArea
          placeholder="Reason for cancellation (optional)"
          value={cancelReason}
          onChange={(e) => setCancelReason(e.target.value)}
          rows={4}
        />
      </Modal>

      <CompleteAppointmentModal
        open={completeModalOpen}
        appointment={selectedAppointment}
        onClose={() => {
          setCompleteModalOpen(false);
          setSelectedAppointment(null);
        }}
        onSuccess={() => {
          queryClient.invalidateQueries({ queryKey: ['appointments'] });
        }}
      />
    </>
  );
};
