import { useMutation, useQueryClient } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const SYNC_TELEHEALTH_SESSION_MUTATION = `
  mutation SyncTelehealthSession($vendorSessionId: String!) {
    syncTelehealthSession(vendorSessionId: $vendorSessionId) {
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

interface SyncTelehealthSessionData {
  syncTelehealthSession: TelehealthSession;
}

export const useSyncSession = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (vendorSessionId: string) => {
      const data = await graphqlClient.request<SyncTelehealthSessionData>(
        SYNC_TELEHEALTH_SESSION_MUTATION,
        { vendorSessionId }
      );
      return data.syncTelehealthSession;
    },
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: queryKeys.telehealth.all });
      queryClient.setQueryData(
        queryKeys.telehealth.session(data.vendorSessionId),
        data
      );
    },
  });
};
