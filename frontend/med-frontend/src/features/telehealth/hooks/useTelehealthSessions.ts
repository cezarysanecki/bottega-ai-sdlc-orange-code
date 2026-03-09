import { useQuery } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const TELEHEALTH_SESSIONS_QUERY = `
  query TelehealthSessions {
    telehealthSessions {
      vendorSessionId
      status
      failureReason
      startedAt
      endedAt
      lastSyncedAt
    }
  }
`;

interface TelehealthSession {
  vendorSessionId: string;
  status: string;
  failureReason?: string;
  startedAt?: string;
  endedAt?: string;
  lastSyncedAt?: string;
}

interface TelehealthSessionsData {
  telehealthSessions: TelehealthSession[];
}

export const useTelehealthSessions = () => {
  return useQuery({
    queryKey: queryKeys.telehealth.sessions(),
    queryFn: async () => {
      const data = await graphqlClient.request<TelehealthSessionsData>(TELEHEALTH_SESSIONS_QUERY);
      return data.telehealthSessions;
    },
  });
};
