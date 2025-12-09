import api from './api';

export const serviceDeliveryService = {
  createService: async (formData) => {
    const response = await api.post('/services/create', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  updateStatus: async (id, formData) => {
    const response = await api.put(`/services/${id}/status`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/services/${id}`);
    return response.data;
  },

  getAll: async () => {
    const response = await api.get('/services');
    return response.data;
  },

  getByMessenger: async (messengerId) => {
    const response = await api.get(`/services/messenger/${messengerId}`);
    return response.data;
  },

  getByDealership: async (dealershipId) => {
    const response = await api.get(`/services/dealership/${dealershipId}`);
    return response.data;
  },

  getByStatus: async (status) => {
    const response = await api.get(`/services/status/${status}`);
    return response.data;
  },
};
