import { useQuery } from '@tanstack/react-query';
import { encounterService, type PagedEncounters } from '../../../shared/api/encounterService';
import { queryKeys } from '../../../shared/api/queryKeys';

export const useEncounters = (page = 0, size = 10) => {
  return useQuery<PagedEncounters, Error>({
    queryKey: queryKeys.encounters.list({ page, size }),
    queryFn: () => encounterService.getEncounters(page, size),
    staleTime: 30000, // 30 seconds
  });
};
