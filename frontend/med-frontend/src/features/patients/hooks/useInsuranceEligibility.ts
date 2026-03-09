import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const CHECK_ELIGIBILITY_MUTATION = `
  mutation CheckInsuranceEligibility($patientCode: String!) {
    checkInsuranceEligibility(patientCode: $patientCode) {
      patientCode
      planCode
      status
      copay
      deductibleRemaining
      coverageNotes
      fetchedAt
      fromCache
    }
  }
`;

const INVALIDATE_ELIGIBILITY_MUTATION = `
  mutation InvalidateInsuranceEligibility($patientCode: String!) {
    invalidateInsuranceEligibility(patientCode: $patientCode)
  }
`;

interface InsuranceEligibility {
  patientCode: string;
  planCode?: string;
  status?: string;
  copay?: number;
  deductibleRemaining?: number;
  coverageNotes?: string;
  fetchedAt?: string;
  fromCache: boolean;
}

interface CheckEligibilityData {
  checkInsuranceEligibility: InsuranceEligibility;
}

interface InvalidateEligibilityData {
  invalidateInsuranceEligibility: boolean;
}

export const useInsuranceEligibility = (patientCode: string) => {
  const queryClient = useQueryClient();

  const query = useQuery({
    queryKey: queryKeys.insurance.eligibility(patientCode),
    queryFn: async () => {
      // Don't auto-fetch on mount; return null
      return null as InsuranceEligibility | null;
    },
    enabled: false,
  });

  const checkMutation = useMutation({
    mutationFn: async () => {
      const data = await graphqlClient.request<CheckEligibilityData>(
        CHECK_ELIGIBILITY_MUTATION,
        { patientCode }
      );
      return data.checkInsuranceEligibility;
    },
    onSuccess: (data) => {
      queryClient.setQueryData(queryKeys.insurance.eligibility(patientCode), data);
    },
  });

  const invalidateMutation = useMutation({
    mutationFn: async () => {
      const data = await graphqlClient.request<InvalidateEligibilityData>(
        INVALIDATE_ELIGIBILITY_MUTATION,
        { patientCode }
      );
      return data.invalidateInsuranceEligibility;
    },
    onSuccess: () => {
      queryClient.setQueryData(queryKeys.insurance.eligibility(patientCode), null);
    },
  });

  return {
    data: query.data,
    isLoading: query.isLoading,
    error: query.error,
    checkEligibility: checkMutation.mutateAsync,
    invalidateCache: invalidateMutation.mutateAsync,
    isChecking: checkMutation.isPending,
    isInvalidating: invalidateMutation.isPending,
  };
};
