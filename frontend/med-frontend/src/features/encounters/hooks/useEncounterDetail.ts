import { useQuery } from '@tanstack/react-query';
import { encounterService, type Encounter } from '../../../shared/api/encounterService';
import { queryKeys } from '../../../shared/api/queryKeys';

export const useEncounterDetail = (encounterCode: string) => {
  return useQuery<Encounter, Error>({
    queryKey: queryKeys.encounters.detail(encounterCode),
    queryFn: () => encounterService.getEncounter(encounterCode),
    enabled: !!encounterCode,
    staleTime: 30000, // 30 seconds
  });
};
