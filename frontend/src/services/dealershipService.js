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

  update: async (id, dealership) => {
    const response = await api.put(`/dealerships/${id}`, dealership);
    return response.data;
  },

  delete: async (id) => {
    const response = await api.delete(`/dealerships/${id}`);
    return response.data;
  },
};
