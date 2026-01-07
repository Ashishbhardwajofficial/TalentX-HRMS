import { LoginRequest, JwtResponse, User } from '../types';
import { StorageService } from './storage';

// Base API URL - this should be configured based on environment
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

// Auth service class
export class AuthService {
  private static readonly AUTH_ENDPOINTS = {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
    ME: '/auth/me',
  };

  // Login method
  static async login(credentials: LoginRequest): Promise<JwtResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}${this.AUTH_ENDPOINTS.LOGIN}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Login failed');
      }

      const data: JwtResponse = await response.json();
      return data;
    } catch (error) {
      if (error instanceof Error) {
        throw error;
      }
      throw new Error('Network error during login');
    }
  }

  // Logout method
  static async logout(): Promise<void> {
    try {
      const token = StorageService.getToken();
      if (!token) return;

      await fetch(`${API_BASE_URL}${this.AUTH_ENDPOINTS.LOGOUT}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });
    } catch (error) {
      console.error('Logout API call failed:', error);
      // Don't throw error for logout - we'll clear local storage anyway
    }
  }

  // Refresh token method
  static async refreshToken(refreshToken: string): Promise<JwtResponse> {
    try {
      const response = await fetch(`${API_BASE_URL}${this.AUTH_ENDPOINTS.REFRESH}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ refreshToken }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Token refresh failed');
      }

      const data: JwtResponse = await response.json();
      return data;
    } catch (error) {
      if (error instanceof Error) {
        throw error;
      }
      throw new Error('Network error during token refresh');
    }
  }

  // Get current user method
  static async getCurrentUser(): Promise<User> {
    try {
      const token = StorageService.getToken();
      if (!token) {
        throw new Error('No authentication token available');
      }

      const response = await fetch(`${API_BASE_URL}${this.AUTH_ENDPOINTS.ME}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to get user information');
      }

      const data: User = await response.json();
      return data;
    } catch (error) {
      if (error instanceof Error) {
        throw error;
      }
      throw new Error('Network error while fetching user information');
    }
  }

  // Check if token is expired (basic check)
  static isTokenExpired(token: string): boolean {
    try {
      const parts = token.split('.');
      if (parts.length !== 3 || !parts[1]) return true;

      const payload = JSON.parse(atob(parts[1]));
      const currentTime = Date.now() / 1000;
      return payload.exp < currentTime;
    } catch (error) {
      return true; // If we can't parse the token, consider it expired
    }
  }

  // Get token expiration time
  static getTokenExpiration(token: string): Date | null {
    try {
      const parts = token.split('.');
      if (parts.length !== 3 || !parts[1]) return null;

      const payload = JSON.parse(atob(parts[1]));
      return new Date(payload.exp * 1000);
    } catch (error) {
      return null;
    }
  }
}