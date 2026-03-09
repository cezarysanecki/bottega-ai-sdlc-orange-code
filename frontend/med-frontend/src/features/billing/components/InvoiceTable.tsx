import { Table, Alert } from 'antd';
import { useNavigate } from 'react-router-dom';
import type { ColumnsType } from 'antd/es/table';
import { InvoiceStatusBadge } from './InvoiceStatusBadge';

interface Invoice {
  invoiceNumber: string;
  status: string;
  issuedAt?: string;
  dueDate?: string;
  totalAmount: number;
  balanceCents: number;
}

interface InvoiceTableProps {
  invoices: Invoice[];
  loading: boolean;
  error?: string;
}

export const InvoiceTable = ({ invoices, loading, error }: InvoiceTableProps) => {
  const navigate = useNavigate();

  const columns: ColumnsType<Invoice> = [
    {
      title: 'Invoice #',
      dataIndex: 'invoiceNumber',
      key: 'invoiceNumber',
      width: 150,
    },
    {
      title: 'Status',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status) => <InvoiceStatusBadge status={status} />,
    },
    {
      title: 'Issued',
      dataIndex: 'issuedAt',
      key: 'issuedAt',
      width: 120,
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    {
      title: 'Due Date',
      dataIndex: 'dueDate',
      key: 'dueDate',
      width: 120,
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    {
      title: 'Total Amount',
      dataIndex: 'totalAmount',
      key: 'totalAmount',
      width: 130,
      render: (value) => `$${(value || 0).toFixed(2)}`,
    },
    {
      title: 'Balance',
      dataIndex: 'balanceCents',
      key: 'balanceCents',
      width: 130,
      render: (value) => `$${((value || 0) / 100).toFixed(2)}`,
    },
  ];

  if (error) {
    return <Alert message="Error" description={error} type="error" showIcon />;
  }

  return (
    <Table
      columns={columns}
      dataSource={invoices}
      rowKey="invoiceNumber"
      loading={loading}
      onRow={(record) => ({
        onClick: () => navigate(`/billing/invoices/${record.invoiceNumber}`),
        style: { cursor: 'pointer' },
      })}
      pagination={{
        pageSize: 10,
        showSizeChanger: true,
        showTotal: (total) => `Total ${total} invoices`,
      }}
    />
  );
};
