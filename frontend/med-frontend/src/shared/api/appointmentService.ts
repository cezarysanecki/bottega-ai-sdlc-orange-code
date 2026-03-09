import { axiosClient } from './axiosClient';

export const appointmentService = {
  getAppointments: async (page = 0, size = 10, status?: string) => {
    const params = new URLSearchParams({ page: page.toString(), size: size.toString() });
    if (status) params.append('status', status);
    const response = await axiosClient.get(`/appointments?${params}`);
    return response.data;
  },

  createAppointment: async (data: any) => {
    const response = await axiosClient.post('/appointments', data);
    return response.data;
  },

  checkAvailability: async (providerCode: string, startTime: string, endTime: string) => {
    const params = new URLSearchParams({ providerCode, startTime, endTime });
    const response = await axiosClient.get(`/appointments/check-availability?${params}`);
    return response.data;
  },

  cancelAppointment: async (appointmentId: number, reason?: string) => {
    const response = await axiosClient.patch(
      `/appointments/${appointmentId}/cancel`,
      { reason }
    );
    return response.data;
  },

  updateStatus: async (appointmentId: number, status: string, notes?: string) => {
    const response = await axiosClient.patch(
      `/appointments/${appointmentId}/status`,
      { status, notes }
    );
    return response.data;
  },

  rescheduleAppointment: async (
    appointmentId: number,
    startTime: string,
    endTime: string,
    roomCode?: string
  ) => {
    const response = await axiosClient.put(
      `/appointments/${appointmentId}/reschedule`,
      { startTime, endTime, roomCode }
    );
    return response.data;
  }
};
