import { axiosClient } from './axiosClient';

export interface Procedure {
  procedureCode: string;
  description: string;
  defaultCost: number;
}

export const procedureService = {
  getProcedures: async (): Promise<Procedure[]> => {
    const response = await axiosClient.get<Procedure[]>('/procedures');
    return response.data;
  }
};
