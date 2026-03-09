import { useQuery } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const PATIENTS_QUERY = `
  query Patients {
    patients {
      patientCode
      firstName
      lastName
      dateOfBirth
      insurancePlanCode
      insuranceProvider
    }
  }
`;

interface Patient {
  patientCode: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  insurancePlanCode?: string;
  insuranceProvider?: string;
}

interface PatientsData {
  patients: Patient[];
}

export const usePatients = () => {
  return useQuery({
    queryKey: queryKeys.patients.list(),
    queryFn: async () => {
      const data = await graphqlClient.request<PatientsData>(PATIENTS_QUERY);
      return data.patients;
    },
  });
};
