import { StorageService } from './storage';
import { AuthService } from './auth';

// Base API URL
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// API Error class
export class ApiError extends Error {
  constructor(
    message: string,
    public status?: number,
    public fieldErrors?: Array<{ field: string; message: string }>
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// API Client class
export class ApiClient {
  private baseURL: string;

  constructor(baseURL: string = API_BASE_URL) {
    this.baseURL = baseURL;
  }

  // Generic request method
  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${this.baseURL}${endpoint}`;
    const token = StorageService.getToken();

    // Default headers
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };

    // Add authorization header if token exists
    if (token) {
      // Check if token is expired
      if (AuthService.isTokenExpired(token)) {
        try {
          // Try to refresh token
          const refreshToken = StorageService.getRefreshToken();
          if (refreshToken) {
            const response = await AuthService.refreshToken(refreshToken);
            StorageService.setToken(response.token);
            StorageService.setUser(response.user);
            headers.Authorization = `Bearer ${response.token}`;
          } else {
            // No refresh token, clear storage and redirect to login
            StorageService.clearAll();
            window.location.href = '/login';
            throw new ApiError('Authentication required', 401);
          }
        } catch (error) {
          // Refresh failed, clear storage and redirect to login
          StorageService.clearAll();
          window.location.href = '/login';
          throw new ApiError('Authentication required', 401);
        }
      } else {
        headers.Authorization = `Bearer ${token}`;
      }
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      // Handle different response types
      if (!response.ok) {
        const contentType = response.headers.get('content-type');
        let errorData: any = {};

        if (contentType && contentType.includes('application/json')) {
          try {
            errorData = await response.json();
          } catch (e) {
            // If JSON parsing fails, use default error
          }
        }

        const message = errorData.message || `HTTP ${response.status}: ${response.statusText}`;
        const fieldErrors = errorData.fieldErrors || errorData.errors;

        throw new ApiError(message, response.status, fieldErrors);
      }

      // Handle empty responses
      const contentType = response.headers.get('content-type');
      if (contentType && contentType.includes('application/json')) {
        return await response.json();
      } else {
        // For non-JSON responses, return the response object itself
        return response as unknown as T;
      }
    } catch (error) {
      if (error instanceof ApiError) {
        throw error;
      }

      // Network or other errors
      throw new ApiError(
        error instanceof Error ? error.message : 'Network error occurred'
      );
    }
  }

  // HTTP methods
  async get<T>(endpoint: string, params?: Record<string, any>): Promise<T> {
    let url = endpoint;
    if (params) {
      const searchParams = new URLSearchParams();
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          searchParams.append(key, String(value));
        }
      });
      const queryString = searchParams.toString();
      if (queryString) {
        url += `?${queryString}`;
      }
    }

    return this.request<T>(url, {
      method: 'GET',
    });
  }

  async post<T>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : null,
    });
  }

  async put<T>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : null,
    });
  }

  async patch<T>(endpoint: string, data?: any): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'PATCH',
      body: data ? JSON.stringify(data) : null,
    });
  }

  async delete<T>(endpoint: string): Promise<T> {
    return this.request<T>(endpoint, {
      method: 'DELETE',
    });
  }

  // File upload method
  async uploadFile<T>(endpoint: string, file: File, additionalData?: Record<string, any>): Promise<T> {
    const formData = new FormData();
    formData.append('file', file);

    if (additionalData) {
      Object.entries(additionalData).forEach(([key, value]) => {
        formData.append(key, String(value));
      });
    }

    const token = StorageService.getToken();
    const headers: Record<string, string> = {};

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    return this.request<T>(endpoint, {
      method: 'POST',
      headers,
      body: formData,
    });
  }
}

// Default API client instance
export const apiClient = new ApiClient();