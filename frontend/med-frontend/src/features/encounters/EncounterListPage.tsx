import { useState } from 'react';
import { Typography, Table, Tag, Space, Alert, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import { useEncounters } from './hooks/useEncounters';
import type { Encounter } from '../../shared/api/encounterService';

const { Title } = Typography;

export const EncounterListPage = () => {
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const navigate = useNavigate();

  const { data, isLoading, error } = useEncounters(page, pageSize);

  const columns: ColumnsType<Encounter> = [
    {
      title: 'Encounter Code',
      dataIndex: 'encounterCode',
      key: 'encounterCode',
      width: 140,
      render: (code: string) => (
        <Button type="link" onClick={() => navigate(`/encounters/${code}`)} style={{ padding: 0 }}>{code}</Button>
      ),
    },
    {
      title: 'Patient',
      key: 'patient',
      width: 180,
      render: (_, record) =>
        record.patient ? `${record.patient.firstName} ${record.patient.lastName}` : '-',
    },
    {
      title: 'Provider',
      dataIndex: ['provider', 'fullName'],
      key: 'provider',
      width: 160,
      render: (value) => value || '-',
    },
    {
      title: 'Date & Time',
      dataIndex: 'encounterTime',
      key: 'encounterTime',
      width: 180,
      render: (value: string) => value ? new Date(value).toLocaleString() : '-',
    },
    {
      title: 'Reason',
      dataIndex: 'reason',
      key: 'reason',
      ellipsis: true,
    },
    {
      title: 'Procedures',
      dataIndex: 'procedures',
      key: 'procedures',
      width: 100,
      render: (procedures: any[]) => (
        <Tag color="blue">{procedures?.length || 0}</Tag>
      ),
    },
    {
      title: 'Summary',
      dataIndex: 'summary',
      key: 'summary',
      width: 120,
      render: (summary?: string) => (
        <Tag color={summary ? 'green' : 'default'}>
          {summary ? 'Complete' : 'Pending'}
        </Tag>
      ),
    },
  ];

  if (error) {
    return (
      <div>
        <Title level={2}>Encounters</Title>
        <Alert
          message="Error Loading Encounters"
          description={error instanceof Error ? error.message : 'Failed to load encounters'}
          type="error"
          showIcon
        />
      </div>
    );
  }

  return (
    <div>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <Title level={2}>Encounters</Title>

        <Table
          columns={columns}
          dataSource={data?.content || []}
          rowKey="encounterCode"
          loading={isLoading}
          pagination={{
            current: page + 1,
            pageSize: pageSize,
            total: data?.totalElements || 0,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} encounters`,
            pageSizeOptions: ['5', '10', '20', '50'],
            onChange: (newPage, newPageSize) => {
              setPage(newPage - 1);
              setPageSize(newPageSize);
            },
          }}
          onRow={(record) => ({
            style: { cursor: 'pointer' },
            onClick: () => navigate(`/encounters/${record.encounterCode}`),
          })}
        />
      </Space>
    </div>
  );
};
