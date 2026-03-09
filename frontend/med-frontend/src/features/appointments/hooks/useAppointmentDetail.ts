import { useQuery } from '@tanstack/react-query';
import { fetchJson } from '../../../shared/api/fetchClient';
import { queryKeys } from '../../../shared/api/queryKeys';
import type { Appointment } from './useAppointments';

export const useAppointmentDetail = (appointmentCode: string) => {
  return useQuery({
    queryKey: queryKeys.appointments.detail(appointmentCode),
    queryFn: async () => {
      const data = await fetchJson<Appointment>(`/api/appointments/${appointmentCode}`);
      return data;
    },
    enabled: !!appointmentCode,
  });
};
