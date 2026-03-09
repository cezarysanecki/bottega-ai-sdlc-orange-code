import axios from 'axios';

export const axiosClient = axios.create({
  baseURL: '/api',
  timeout: 15000
});

// Request interceptor to add JWT token
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // eslint-disable-next-line no-console
    console.warn('API error', error);
    return Promise.reject(error);
  }
);
