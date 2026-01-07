// frontend/src/api/authApi.ts
import apiClient from "./axiosClient";
import { LoginRequest, JwtResponse, User, RoleInfo } from "../types";

// Authentication API client interface
export interface AuthApiClient {
  login(credentials: LoginRequest): Promise<JwtResponse>;
  logout(): Promise<void>;
  getCurrentUser(): Promise<User>;
  refreshToken(): Promise<JwtResponse>;
  validateToken(): Promise<boolean>;
}

// Authentication request/response types
export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface LogoutRequest {
  token: string;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: Array<{
    id: number;
    name: string;
    description?: string;
    permissions: Array<{
      id: number;
      name: string;
      resource: string;
      action: string;
    }>;
  }>;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}

// Implementation of authentication API client
class AuthApiClientImpl implements AuthApiClient {
  private readonly AUTH_ENDPOINTS = {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    ME: '/auth/me',
    REFRESH: '/auth/refresh',
    VALIDATE: '/auth/validate'
  } as const;

  /**
   * Authenticate user with username and password
   */
  async login(credentials: LoginRequest): Promise<JwtResponse> {
    try {
      const response = await apiClient.post<JwtResponse>(
        this.AUTH_ENDPOINTS.LOGIN,
        credentials
      );

      // Store tokens in localStorage
      if (response.token) {
        localStorage.setItem('token', response.token);
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
        // Store user info
        localStorage.setItem('user', JSON.stringify(response.user));
      }

      return response;
    } catch (error) {
      // Clear any existing tokens on login failure
      this.clearTokens();
      throw error;
    }
  }

  /**
   * Logout current user and invalidate tokens
   */
  async logout(): Promise<void> {
    try {
      const token = localStorage.getItem('token');
      if (token) {
        await apiClient.post<void>(this.AUTH_ENDPOINTS.LOGOUT, { token });
      }
    } catch (error) {
      // Log error but don't throw - we still want to clear local tokens
      console.warn('Logout request failed:', error);
    } finally {
      // Always clear local tokens
      this.clearTokens();
    }
  }

  /**
   * Get current authenticated user information
   */
  async getCurrentUser(): Promise<User> {
    try {
      const response = await apiClient.get<UserResponse>(this.AUTH_ENDPOINTS.ME);

      // Convert UserResponse to User type
      const roles: RoleInfo[] = response.roles.map(role => {
        const mappedRole: RoleInfo = {
          id: role.id,
          name: role.name,
          isActive: true, // Default value - assuming active roles
          assignedAt: new Date().toISOString(), // Default value - current time
          ...(role.description && { description: role.description })
        };

        return mappedRole;
      });

      const user: User = {
        id: response.id,
        organizationId: 1, // TODO: Get from context/auth
        email: response.email,
        isActive: true, // Default value
        isVerified: true, // Default value
        twoFactorEnabled: false, // Default value
        createdAt: response.createdAt,
        updatedAt: response.updatedAt,
        roles,
        ...(response.username && { username: response.username }),
        ...(response.lastLoginAt && { lastLoginAt: response.lastLoginAt })
      };

      // Update stored user info
      localStorage.setItem('user', JSON.stringify(user));

      return user;
    } catch (error) {
      // If getting current user fails, clear tokens
      this.clearTokens();
      throw error;
    }
  }

  /**
   * Refresh authentication token using refresh token
   */
  async refreshToken(): Promise<JwtResponse> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    try {
      const response = await apiClient.post<JwtResponse>(
        this.AUTH_ENDPOINTS.REFRESH,
        { refreshToken }
      );

      // Update stored tokens
      if (response.token) {
        localStorage.setItem('token', response.token);
        if (response.refreshToken) {
          localStorage.setItem('refreshToken', response.refreshToken);
        }
        localStorage.setItem('user', JSON.stringify(response.user));
      }

      return response;
    } catch (error) {
      // Clear tokens if refresh fails
      this.clearTokens();
      throw error;
    }
  }

  /**
   * Validate current token
   */
  async validateToken(): Promise<boolean> {
    const token = localStorage.getItem('token');
    if (!token) {
      return false;
    }

    try {
      await apiClient.get<{ valid: boolean }>(this.AUTH_ENDPOINTS.VALIDATE);
      return true;
    } catch (error) {
      // Token is invalid, clear it
      this.clearTokens();
      return false;
    }
  }

  /**
   * Clear all authentication tokens and user data from localStorage
   */
  private clearTokens(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  }

  /**
   * Get stored user from localStorage
   */
  getStoredUser(): User | null {
    try {
      const userStr = localStorage.getItem('user');
      return userStr ? JSON.parse(userStr) : null;
    } catch (error) {
      console.warn('Failed to parse stored user:', error);
      return null;
    }
  }

  /**
   * Check if user is authenticated (has valid token)
   */
  isAuthenticated(): boolean {
    return !!localStorage.getItem('token');
  }

  /**
   * Get stored token
   */
  getToken(): string | null {
    return localStorage.getItem('token');
  }

  /**
   * Get stored refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem('refreshToken');
  }
}

// Create and export singleton instance
const authApi = new AuthApiClientImpl();

export default authApi;

// Export the class for testing purposes
export { AuthApiClientImpl };