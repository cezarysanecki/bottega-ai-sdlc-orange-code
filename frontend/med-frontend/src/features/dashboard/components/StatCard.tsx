import { Card, Statistic } from 'antd';
import { ReactNode } from 'react';

interface StatCardProps {
  title: string;
  value: number;
  icon: ReactNode;
  color: string;
}

export const StatCard = ({ title, value, icon, color }: StatCardProps) => {
  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Statistic title={title} value={value} />
        <div style={{ color, fontSize: 32 }}>{icon}</div>
      </div>
    </Card>
  );
};
