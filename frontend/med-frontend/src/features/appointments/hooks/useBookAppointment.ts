import { useMutation, useQueryClient } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const CREATE_APPOINTMENT_MUTATION = `
  mutation CreateAppointment($request: AppointmentInput!) {
    createAppointment(request: $request) {
      id
      appointmentUid
      status
      startTime
      endTime
      roomCode
    }
  }
`;

interface AppointmentInput {
  patientCode: string;
  providerCode: string;
  appointmentType?: string;
  startTime: string;
  endTime: string;
  roomCode?: string;
}

interface CreateAppointmentData {
  createAppointment: {
    id: string;
    appointmentUid: string;
    status: string;
    startTime?: string;
    endTime?: string;
    roomCode?: string;
  };
}

export const useBookAppointment = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (input: AppointmentInput) => {
      const data = await graphqlClient.request<CreateAppointmentData>(
        CREATE_APPOINTMENT_MUTATION,
        { request: input }
      );
      return data.createAppointment;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.appointments.all });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard.stats() });
    },
  });
};
