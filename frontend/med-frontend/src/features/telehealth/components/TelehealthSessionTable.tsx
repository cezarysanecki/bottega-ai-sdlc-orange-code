import { Table, Alert } from 'antd';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import { TelehealthStatusBadge } from './TelehealthStatusBadge';

interface TelehealthSession {
  vendorSessionId: string;
  status: string;
  startedAt?: string;
  endedAt?: string;
  lastSyncedAt?: string;
  failureReason?: string;
}

interface TelehealthSessionTableProps {
  sessions: TelehealthSession[];
  loading: boolean;
  error?: string;
}

export const TelehealthSessionTable = ({ sessions, loading, error }: TelehealthSessionTableProps) => {
  const navigate = useNavigate();

  const columns: ColumnsType<TelehealthSession> = [
    {
      title: 'Session ID',
      dataIndex: 'vendorSessionId',
      key: 'vendorSessionId',
      width: 200,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => <TelehealthStatusBadge status={status} />,
    },
    {
      title: 'Started At',
      dataIndex: 'startedAt',
      key: 'startedAt',
      render: (value) => (value ? new Date(value).toLocaleString() : '-'),
    },
    {
      title: 'Ended At',
      dataIndex: 'endedAt',
      key: 'endedAt',
      render: (value) => (value ? new Date(value).toLocaleString() : '-'),
    },
    {
      title: 'Last Synced',
      dataIndex: 'lastSyncedAt',
      key: 'lastSyncedAt',
      render: (value) => (value ? new Date(value).toLocaleString() : 'Never'),
    },
    {
      title: 'Failure Reason',
      dataIndex: 'failureReason',
      key: 'failureReason',
      render: (value) => value || '-',
    },
  ];

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon />;
  }

  return (
    <Table
      columns={columns}
      dataSource={sessions}
      rowKey="vendorSessionId"
      loading={loading}
      onRow={(record) => ({
        onClick: () => navigate(`/telehealth/${record.vendorSessionId}`),
        style: { cursor: 'pointer' },
      })}
      pagination={{
        pageSize: 10,
        showSizeChanger: true,
        showTotal: (total) => `Total ${total} sessions`,
      }}
    />
  );
};
