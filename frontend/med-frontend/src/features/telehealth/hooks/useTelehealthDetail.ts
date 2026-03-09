import { useQuery } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const TELEHEALTH_SESSION_QUERY = `
  query TelehealthSession($vendorSessionId: String!) {
    telehealthSession(vendorSessionId: $vendorSessionId) {
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

interface TelehealthSessionData {
  telehealthSession: TelehealthSession;
}

export const useTelehealthDetail = (vendorSessionId: string) => {
  return useQuery({
    queryKey: queryKeys.telehealth.session(vendorSessionId),
    queryFn: async () => {
      const data = await graphqlClient.request<TelehealthSessionData>(
        TELEHEALTH_SESSION_QUERY,
        { vendorSessionId }
      );
      return data.telehealthSession;
    },
    enabled: !!vendorSessionId,
  });
};
