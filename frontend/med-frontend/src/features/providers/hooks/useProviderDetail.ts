import { useQuery } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const PROVIDERS_QUERY = `
  query Providers {
    providers {
      providerCode
      fullName
      specialty
      officeRoom
      preferredContact
    }
  }
`;

interface Provider {
  providerCode: string;
  fullName: string;
  specialty: string;
  officeRoom?: string;
  preferredContact?: string;
}

interface ProvidersData {
  providers: Provider[];
}

export const useProviderDetail = (code: string) => {
  return useQuery({
    queryKey: queryKeys.providers.detail(code),
    queryFn: async () => {
      // Fetch all providers and filter by code
      // This is not optimal but works with current backend query structure
      const data = await graphqlClient.request<ProvidersData>(PROVIDERS_QUERY);
      const provider = data.providers.find(p => p.providerCode === code);
      if (!provider) {
        throw new Error('Provider not found');
      }
      return provider;
    },
    enabled: !!code,
  });
};
