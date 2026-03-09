import { useMutation, useQueryClient } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const RECORD_PAYMENT_MUTATION = `
  mutation PostPayment($input: PaymentInput!) {
    postPayment(input: $input) {
      invoiceNumber
      status
      balanceCents
      totalAmount
    }
  }
`;

interface PaymentInput {
  invoiceNumber: string;
  amountCents: number;
  method?: string;
  paymentReference?: string;
  postedBy?: string;
}

interface PostPaymentData {
  postPayment: {
    invoiceNumber: string;
    status: string;
    balanceCents: number;
    totalAmount: number;
  };
}

export const useRecordPayment = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: PaymentInput) => {
      const data = await graphqlClient.request<PostPaymentData>(
        RECORD_PAYMENT_MUTATION,
        { input }
      );
      return data.postPayment;
    },
    onSuccess: (data) => {
      // queryClient.invalidateQueries({ queryKey: queryKeys.invoices.all });

      queryClient.invalidateQueries({ queryKey: queryKeys.invoices.detail(data.invoiceNumber) });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard.stats() });
    },
  });
};
