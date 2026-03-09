import { Typography, Select, Space } from 'antd';
import { useState } from 'react';
import { InvoiceTable } from './components/InvoiceTable';
import { useInvoices } from './hooks/useInvoices';

const { Title } = Typography;

export const InvoiceListPage = () => {
  const [statusFilter, setStatusFilter] = useState<string | undefined>(undefined);
  const { data: invoices, isLoading, error } = useInvoices(statusFilter);

  return (
    <div>
      <Title level={2}>Invoices</Title>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div>
          <span style={{ marginRight: 8 }}>Filter by status:</span>
          <Select
            style={{ width: 200 }}
            placeholder="All statuses"
            allowClear
            value={statusFilter}
            onChange={setStatusFilter}
          >
            <Select.Option value="OPEN">Open</Select.Option>
            <Select.Option value="PAID">Paid</Select.Option>
            <Select.Option value="OVERDUE">Overdue</Select.Option>
            <Select.Option value="CANCELLED">Cancelled</Select.Option>
          </Select>
        </div>
        <InvoiceTable
          invoices={invoices || []}
          loading={isLoading}
          error={error instanceof Error ? error.message : undefined}
        />
      </Space>
    </div>
  );
};
