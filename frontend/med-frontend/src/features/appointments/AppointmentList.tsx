import { Card, List, Spin, Typography } from 'antd';
import { useQuery } from '@tanstack/react-query';
import { fetchJson } from '../../shared/api/fetchClient';
import { useEffect } from 'react';

const APPOINTMENT_QUERY_KEY = ['appointments', 'preview'];

export const AppointmentList = () => {
  const { data, isLoading, refetch } = useQuery<string[]>({
    queryKey: APPOINTMENT_QUERY_KEY,
    queryFn: () => fetchJson<string[]>('/api/appointments/preview'),
    staleTime: 10_000
  });

  useEffect(() => {
    const timer = setInterval(() => refetch(), 30_000);
    return () => clearInterval(timer);
  }, [refetch]);

  if (isLoading) {
    return <Spin tip="Loading schedule..." />;
  }

  return (
    <Card title="Upcoming Appointments">
      <List
        dataSource={data ?? []}
        locale={{ emptyText: 'No appointments scheduled' }}
        renderItem={(item) => <List.Item>{item}</List.Item>}
      />
      {data && data.length > 2 && (
        <Typography.Text type="secondary">Fetched {data.length} items from legacy API.</Typography.Text>
      )}
    </Card>
  );
};
