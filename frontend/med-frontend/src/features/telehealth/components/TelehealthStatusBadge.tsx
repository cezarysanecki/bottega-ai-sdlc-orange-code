import { Tag } from 'antd';

interface TelehealthStatusBadgeProps {
  status: string;
}

export const TelehealthStatusBadge = ({ status }: TelehealthStatusBadgeProps) => {
  const getColor = () => {
    switch (status.toUpperCase()) {
      case 'ACTIVE':
        return 'green';
      case 'COMPLETED':
        return 'blue';
      case 'FAILED':
        return 'red';
      case 'PENDING':
        return 'orange';
      default:
        return 'default';
    }
  };

  return <Tag color={getColor()}>{status}</Tag>;
};
