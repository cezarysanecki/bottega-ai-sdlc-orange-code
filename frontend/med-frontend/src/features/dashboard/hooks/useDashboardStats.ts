import { useQuery } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const DASHBOARD_STATS_QUERY = `
  query DashboardStats {
    dashboardStats {
      totalPatients
      activeProviders
      openInvoices
      upcomingAppointments
    }
  }
`;

interface DashboardStatsData {
  dashboardStats: {
    totalPatients: number;
    activeProviders: number;
    openInvoices: number;
    upcomingAppointments: number;
  };
}

export const useDashboardStats = () => {
  return useQuery({
    queryKey: queryKeys.dashboard.stats(),
    queryFn: async () => {
      const data = await graphqlClient.request<DashboardStatsData>(DASHBOARD_STATS_QUERY);
      return data.dashboardStats;
    },
  });
};
