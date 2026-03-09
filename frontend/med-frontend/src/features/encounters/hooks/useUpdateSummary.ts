import { useMutation, useQueryClient } from '@tanstack/react-query';
import { encounterService, type Encounter } from '../../../shared/api/encounterService';
import { queryKeys } from '../../../shared/api/queryKeys';

interface UpdateSummaryParams {
  encounterCode: string;
  summary: string;
}

export const useUpdateSummary = () => {
  const queryClient = useQueryClient();

  return useMutation<Encounter, Error, UpdateSummaryParams>({
    mutationFn: ({ encounterCode, summary }: UpdateSummaryParams) =>
      encounterService.updateSummary(encounterCode, summary),
    onSuccess: (data) => {
      // Invalidate and refetch the specific encounter detail
      queryClient.invalidateQueries({ queryKey: queryKeys.encounters.detail(data.encounterCode) });
      // Invalidate the encounters list
      queryClient.invalidateQueries({ queryKey: queryKeys.encounters.all });
    },
  });
};
