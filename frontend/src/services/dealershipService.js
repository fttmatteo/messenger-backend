import api from './api';

export const dealershipService = {
  create: async (dealership) => {
    const response = await api.post('/dealerships', dealership);
    return response.data;
  },

  getAll: async () => {
    const response = await api.get('/dealerships');
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/dealerships/${id}`);
    return response.data;
  },
};
