import { Tag } from 'antd';

interface InvoiceStatusBadgeProps {
  status: string;
}

export const InvoiceStatusBadge = ({ status }: InvoiceStatusBadgeProps) => {
  const getColor = () => {
    switch (status.toUpperCase()) {
      case 'PAID':
        return 'green';
      case 'OPEN':
        return 'blue';
      case 'OVERDUE':
        return 'red';
      case 'CANCELLED':
        return 'default';
      default:
        return 'default';
    }
  };

  return <Tag color={getColor()}>{status}</Tag>;
};
