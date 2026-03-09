import { useMutation, useQueryClient } from '@tanstack/react-query';
import { encounterService, type CreateEncounterRequest, type Encounter } from '../../../shared/api/encounterService';
import { queryKeys } from '../../../shared/api/queryKeys';

export const useCreateEncounter = () => {
  const queryClient = useQueryClient();

  return useMutation<Encounter, Error, CreateEncounterRequest>({
    mutationFn: (data: CreateEncounterRequest) => encounterService.createEncounter(data),
    onSuccess: () => {
      // Invalidate encounters list to refetch
      queryClient.invalidateQueries({ queryKey: queryKeys.encounters.all });
      // Invalidate appointments list as well since appointment status changed
      queryClient.invalidateQueries({ queryKey: queryKeys.appointments.all });
    },
  });
};
