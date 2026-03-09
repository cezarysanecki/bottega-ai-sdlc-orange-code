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
      items {
        lineItemCode
        description
        quantity
        unitAmount
        legacyTaxCents
      }
    }
  }
`;

interface InvoiceItem {
  lineItemCode: string;
  description?: string;
  quantity: number;
  unitAmount: number;
  legacyTaxCents?: number;
}

interface Invoice {
  invoiceNumber: string;
  status: string;
  issuedAt?: string;
  dueDate?: string;
  totalAmount: number;
  balanceCents: number;
  notes?: string;
  items: InvoiceItem[];
}

interface InvoicesData {
  invoices: Invoice[];
}

export const useInvoiceDetail = (invoiceNumber: string) => {
  return useQuery({
    queryKey: queryKeys.invoices.detail(invoiceNumber),
    queryFn: async () => {
      // Fetch all invoices and filter by invoice number
      // This is not optimal but works with the current backend query structure
      const data = await graphqlClient.request<InvoicesData>(INVOICES_QUERY, { status: undefined });
      const invoice = data.invoices.find(inv => inv.invoiceNumber === invoiceNumber);
      if (!invoice) {
        throw new Error('Invoice not found');
      }
      return invoice;
    },
    enabled: !!invoiceNumber,
  });
};
