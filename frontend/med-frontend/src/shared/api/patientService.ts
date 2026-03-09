import { axiosClient } from './axiosClient';

export const patientService = {
  getPatients: async () => {
    const response = await axiosClient.get('/patients');
    return response.data;
  },

  getPatientDetail: async (patientCode: string) => {
    const response = await axiosClient.get(`/patients/${patientCode}`);
    return response.data;
  },

  createPatient: async (data: any) => {
    const response = await axiosClient.post('/patients', data);
    return response.data;
  },

  updatePatient: async (patientCode: string, data: any) => {
    const response = await axiosClient.put(`/patients/${patientCode}`, data);
    return response.data;
  },

  updateAllergies: async (patientCode: string, allergies: string) => {
    const response = await axiosClient.patch(
      `/patients/${patientCode}/allergies`,
      { allergies }
    );
    return response.data;
  },

  checkEligibility: async (patientId: string) => {
    const response = await axiosClient.post(`/patients/${patientId}/eligibility/check`, {});
    return response.data;
  }
};
