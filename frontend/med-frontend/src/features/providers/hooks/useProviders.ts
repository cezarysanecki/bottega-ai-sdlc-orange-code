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

export const useProviders = () => {
  return useQuery({
    queryKey: queryKeys.providers.list(),
    queryFn: async () => {
      const data = await graphqlClient.request<ProvidersData>(PROVIDERS_QUERY);
      return data.providers;
    },
  });
};
