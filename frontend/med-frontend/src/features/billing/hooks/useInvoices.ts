import { useQuery } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const INVOICES_QUERY = `
  query Invoices($status: String) {
    invoices(status: $status) {
      invoiceNumber
      status
      issuedAt
      dueDate
      totalAmount
      balanceCents
      notes
    }
  }
`;

interface Invoice {
  invoiceNumber: string;
  status: string;
  issuedAt?: string;
  dueDate?: string;
  totalAmount: number;
  balanceCents: number;
  notes?: string;
}

interface InvoicesData {
  invoices: Invoice[];
}

export const useInvoices = (status?: string) => {
  return useQuery({
    queryKey: queryKeys.invoices.list(status),
    queryFn: async () => {
      const data = await graphqlClient.request<InvoicesData>(INVOICES_QUERY, { status });
      return data.invoices;
    },
  });
};
