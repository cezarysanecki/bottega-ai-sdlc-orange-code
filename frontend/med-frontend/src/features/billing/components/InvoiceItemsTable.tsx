import { Table } from 'antd';
import type { ColumnsType } from 'antd/es/table';

interface InvoiceItem {
  lineItemCode: string;
  description?: string;
  quantity: number;
  unitAmount: number;
  legacyTaxCents?: number;
}

interface InvoiceItemsTableProps {
  items: InvoiceItem[];
}

export const InvoiceItemsTable = ({ items }: InvoiceItemsTableProps) => {
  const columns: ColumnsType<InvoiceItem> = [
    {
      title: 'Item Code',
      dataIndex: 'lineItemCode',
      key: 'lineItemCode',
      width: 150,
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      render: (value) => value || '-',
    },
    {
      title: 'Quantity',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 100,
    },
    {
      title: 'Unit Amount',
      dataIndex: 'unitAmount',
      key: 'unitAmount',
      width: 130,
      render: (value) => `$${(value || 0).toFixed(2)}`,
    },
    {
      title: 'Total',
      key: 'total',
      width: 130,
      render: (_, record) => `$${((record.quantity || 0) * (record.unitAmount || 0)).toFixed(2)}`,
    },
  ];

  return (
    <Table
      columns={columns}
      dataSource={items}
      rowKey="lineItemCode"
      pagination={false}
    />
  );
};
