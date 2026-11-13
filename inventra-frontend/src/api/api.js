import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

/**
 * Parses and returns a user-readable error message from Spring Boot error responses.
 */
export const getErrorMessage = (error) => {
  if (error.response?.data?.message) {
    return error.response.data.message;
  }
  if (error.response?.data?.fieldErrors) {
    return Object.entries(error.response.data.fieldErrors)
      .map(([field, msg]) => `${field}: ${msg}`)
      .join(' | ');
  }
  if (error.response?.data?.error) {
    return `${error.response.data.error} (Status ${error.response.status})`;
  }
  return error.message || 'An unexpected error occurred. Please verify backend is running.';
};

export default api;
