import { useQuery } from '@tanstack/react-query';
import { fetchJson } from '../../../shared/api/fetchClient';
import { queryKeys } from '../../../shared/api/queryKeys';

interface PatientSummary {
  patientCode: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  insurancePlanCode?: string;
  insuranceProvider?: string;
}

interface ProviderSummary {
  providerCode: string;
  fullName: string;
  specialty: string;
  officeRoom?: string;
  preferredContact?: string;
}

export interface Appointment {
  id: number;
  appointmentUid: string;
  appointmentCode: string;
  status: string;
  startTime: string;
  endTime: string;
  patient: PatientSummary;
  provider: ProviderSummary;
  roomCode?: string;
  legacyNotes?: string;
}

export interface PagedAppointments {
  content: Appointment[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

interface UseAppointmentsOptions {
  page?: number;
  size?: number;
  status?: string;
}

export const useAppointments = (options: UseAppointmentsOptions = {}) => {
  const { page = 0, size = 10, status } = options;

  return useQuery({
    queryKey: queryKeys.appointments.list(page, size, status),
    queryFn: async () => {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
        ...(status && { status }),
      });
      const data = await fetchJson<PagedAppointments>(`/api/appointments?${params}`);
      return data;
    },
  });
};
