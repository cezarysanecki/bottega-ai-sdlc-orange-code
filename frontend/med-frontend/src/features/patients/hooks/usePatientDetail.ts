import { useQuery } from '@tanstack/react-query';
import { graphqlClient } from '../../../shared/api/graphql/client';
import { queryKeys } from '../../../shared/api/queryKeys';

const PATIENT_DETAIL_QUERY = `
  query PatientByCode($code: String!) {
    patientByCode(code: $code) {
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

interface PatientDetailData {
  patientByCode: Patient;
}

export const usePatientDetail = (code: string) => {
  return useQuery({
    queryKey: queryKeys.patients.detail(code),
    queryFn: async () => {
      const data = await graphqlClient.request<PatientDetailData>(PATIENT_DETAIL_QUERY, { code });
      return data.patientByCode;
    },
    enabled: !!code,
  });
};
