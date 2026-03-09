import { useQuery } from '@tanstack/react-query';
import { procedureService, type Procedure } from '../../../shared/api/procedureService';
import { queryKeys } from '../../../shared/api/queryKeys';

export const useProcedures = () => {
  return useQuery<Procedure[], Error>({
    queryKey: queryKeys.procedures.list(),
    queryFn: () => procedureService.getProcedures(),
    staleTime: 5 * 60 * 1000, // 5 minutes - procedures don't change often
  });
};
