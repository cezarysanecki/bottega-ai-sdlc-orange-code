import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Button, Space, Spin, Alert, Card, Descriptions } from 'antd';
import { ArrowLeftOutlined, FilePdfOutlined, DollarOutlined } from '@ant-design/icons';
import { useState } from 'react';
import { useInvoiceDetail } from './hooks/useInvoiceDetail';
import { InvoiceItemsTable } from './components/InvoiceItemsTable';
import { InvoiceStatusBadge } from './components/InvoiceStatusBadge';
import { PaymentModal } from './PaymentModal';

const { Title } = Typography;

export const InvoiceDetailPage = () => {
  const { invoiceNumber } = useParams<{ invoiceNumber: string }>();
  const navigate = useNavigate();
  const [paymentModalVisible, setPaymentModalVisible] = useState(false);
  const { data: invoice, isLoading, error } = useInvoiceDetail(invoiceNumber || '');

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: '50px' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (error || !invoice) {
    return (
      <Alert
        message="Error loading invoice"
        description={error instanceof Error ? error.message : 'Invoice not found'}
        type="error"
        showIcon
      />
    );
  }

  const handleExportPDF = () => {
    window.open(`/api/invoices/export?invoiceNumber=${invoice.invoiceNumber}`, '_blank');
  };

  return (
    <div>
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        <div>
          <Button
            type="link"
            icon={<ArrowLeftOutlined />}
            onClick={() => navigate('/billing/invoices')}
            style={{ paddingLeft: 0 }}
          >
            Back to Invoices
          </Button>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Title level={2}>Invoice {invoice.invoiceNumber}</Title>
            <Space>
              <Button icon={<FilePdfOutlined />} onClick={handleExportPDF}>
                Export PDF
              </Button>
              <Button
                type="primary"
                icon={<DollarOutlined />}
                onClick={() => setPaymentModalVisible(true)}
                disabled={invoice.balanceCents <= 0}
              >
                Record Payment
              </Button>
            </Space>
          </div>
        </div>

        <Card title="Invoice Details">
          <Descriptions column={2} bordered>
            <Descriptions.Item label="Invoice Number">{invoice.invoiceNumber}</Descriptions.Item>
            <Descriptions.Item label="Status">
              <InvoiceStatusBadge status={invoice.status} />
            </Descriptions.Item>
            <Descriptions.Item label="Issued At">
              {invoice.issuedAt ? new Date(invoice.issuedAt).toLocaleDateString() : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Due Date">
              {invoice.dueDate ? new Date(invoice.dueDate).toLocaleDateString() : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="Total Amount">
              ${(invoice.totalAmount || 0).toFixed(2)}
            </Descriptions.Item>
            <Descriptions.Item label="Balance">
              ${((invoice.balanceCents || 0) / 100).toFixed(2)}
            </Descriptions.Item>
            {invoice.notes && (
              <Descriptions.Item label="Notes" span={2}>
                {invoice.notes}
              </Descriptions.Item>
            )}
          </Descriptions>
        </Card>

        <Card title="Line Items">
          <InvoiceItemsTable items={invoice.items || []} />
        </Card>

        <PaymentModal
          visible={paymentModalVisible}
          onClose={() => setPaymentModalVisible(false)}
          invoiceNumber={invoice.invoiceNumber}
          balanceCents={invoice.balanceCents}
        />
      </Space>
    </div>
  );
};
