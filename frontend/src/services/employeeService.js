import api from './api';

export const employeeService = {
  create: async (employee) => {
    const response = await api.post('/employees', employee);
    return response.data;
  },

  getAll: async () => {
    const response = await api.get('/employees');
    return response.data;
  },

  getById: async (id) => {
    const response = await api.get(`/employees/${id}`);
    return response.data;
  },
};
