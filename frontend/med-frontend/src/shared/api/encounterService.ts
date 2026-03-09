import { axiosClient } from './axiosClient';

export interface EncounterProcedure {
  procedureCode: string;
  description: string;
  quantity: number;
}

export interface PatientSummary {
  patientCode: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  insurancePlanCode?: string;
  insuranceProvider?: string;
}

export interface ProviderSummary {
  providerCode: string;
  fullName: string;
  specialty: string;
  officeRoom?: string;
  preferredContact?: string;
}

export interface Encounter {
  id: number;
  encounterCode: string;
  appointmentId: number;
  appointmentCode: string;
  encounterTime: string;
  reason: string;
  summary?: string;
  patient: PatientSummary;
  provider: ProviderSummary;
  procedures: EncounterProcedure[];
}

export interface CreateEncounterRequest {
  appointmentId: number;
  reason: string;
  procedureCodes: string[];
  summary?: string;
}

export interface PagedEncounters {
  content: Encounter[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const encounterService = {
  createEncounter: async (data: CreateEncounterRequest): Promise<Encounter> => {
    const response = await axiosClient.post<Encounter>('/encounters', data);
    return response.data;
  },

  getEncounter: async (encounterCode: string): Promise<Encounter> => {
    const response = await axiosClient.get<Encounter>(`/encounters/${encounterCode}`);
    return response.data;
  },

  getEncounters: async (page = 0, size = 10): Promise<PagedEncounters> => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString()
    });
    const response = await axiosClient.get<PagedEncounters>(`/encounters?${params}`);
    return response.data;
  },

  checkExistingEncounter: async (appointmentId: number): Promise<boolean> => {
    try {
      const response = await axiosClient.get<{ exists: boolean }>(
        `/encounters/check/${appointmentId}`
      );
      return response.data.exists;
    } catch (error) {
      // If endpoint doesn't exist yet, assume no encounter exists
      return false;
    }
  },

  updateSummary: async (encounterCode: string, summary: string): Promise<Encounter> => {
    const response = await axiosClient.patch<Encounter>(
      `/encounters/${encounterCode}/summary`,
      { summary }
    );
    return response.data;
  }
};
