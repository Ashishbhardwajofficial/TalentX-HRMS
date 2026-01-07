import React, { createContext, useContext, useReducer, useEffect, ReactNode } from 'react';
import { User, LoginRequest, JwtResponse } from '../types';
import { StorageService } from '../services/storage';
import { AuthService } from '../services/auth';

// Auth state interface
interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  error: string | null;
}

// Auth actions
type AuthAction =
  | { type: 'AUTH_START' }
  | { type: 'AUTH_SUCCESS'; payload: { user: User; token: string } }
  | { type: 'AUTH_FAILURE'; payload: string }
  | { type: 'LOGOUT' }
  | { type: 'CLEAR_ERROR' }
  | { type: 'SET_LOADING'; payload: boolean };

// Auth context interface
interface AuthContextType extends AuthState {
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
  refreshToken: () => Promise<void>;
}

// Initial state
const initialState: AuthState = {
  user: null,
  token: null,
  isAuthenticated: false,
  loading: true,
  error: null,
};

// Auth reducer
const authReducer = (state: AuthState, action: AuthAction): AuthState => {
  switch (action.type) {
    case 'AUTH_START':
      return {
        ...state,
        loading: true,
        error: null,
      };
    case 'AUTH_SUCCESS':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.token,
        isAuthenticated: true,
        loading: false,
        error: null,
      };
    case 'AUTH_FAILURE':
      return {
        ...state,
        user: null,
        token: null,
        isAuthenticated: false,
        loading: false,
        error: action.payload,
      };
    case 'LOGOUT':
      return {
        ...state,
        user: null,
        token: null,
        isAuthenticated: false,
        loading: false,
        error: null,
      };
    case 'CLEAR_ERROR':
      return {
        ...state,
        error: null,
      };
    case 'SET_LOADING':
      return {
        ...state,
        loading: action.payload,
      };
    default:
      return state;
  }
};

// Create context
const AuthContext = createContext<AuthContextType | undefined>(undefined);

// Auth provider props
interface AuthProviderProps {
  children: ReactNode;
}

// Auth provider component
export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [state, dispatch] = useReducer(authReducer, initialState);

  // Initialize auth state from storage
  useEffect(() => {
    const initializeAuth = (): void => {
      try {
        const token = StorageService.getToken();
        const user = StorageService.getUser();

        if (token && user) {
          // Validate token expiration here if needed
          dispatch({
            type: 'AUTH_SUCCESS',
            payload: { user, token },
          });
        } else {
          dispatch({ type: 'SET_LOADING', payload: false });
        }
      } catch (error) {
        console.error('Error initializing auth:', error);
        StorageService.clearAll();
        dispatch({ type: 'SET_LOADING', payload: false });
      }
    };

    initializeAuth();
  }, []);

  // Login function
  const login = async (credentials: LoginRequest): Promise<void> => {
    dispatch({ type: 'AUTH_START' });

    try {
      // Use real AuthService or fallback to mock for development
      const response = await (process.env.NODE_ENV === 'development'
        ? mockLogin(credentials)
        : AuthService.login(credentials));

      // Store tokens and user data
      StorageService.setToken(response.token);
      if (response.refreshToken) {
        StorageService.setRefreshToken(response.refreshToken);
      }
      StorageService.setUser(response.user);

      dispatch({
        type: 'AUTH_SUCCESS',
        payload: {
          user: response.user,
          token: response.token,
        },
      });
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Login failed';
      dispatch({
        type: 'AUTH_FAILURE',
        payload: errorMessage,
      });
      throw error;
    }
  };

  // Logout function
  const logout = async (): Promise<void> => {
    try {
      // Call logout API endpoint
      await AuthService.logout();

      // Clear storage
      StorageService.clearAll();

      dispatch({ type: 'LOGOUT' });
    } catch (error) {
      console.error('Logout error:', error);
      // Clear storage even if API call fails
      StorageService.clearAll();
      dispatch({ type: 'LOGOUT' });
    }
  };

  // Clear error function
  const clearError = (): void => {
    dispatch({ type: 'CLEAR_ERROR' });
  };

  // Refresh token function
  const refreshToken = async (): Promise<void> => {
    try {
      const refreshToken = StorageService.getRefreshToken();
      if (!refreshToken) {
        throw new Error('No refresh token available');
      }

      // Use real AuthService for token refresh
      const response = await AuthService.refreshToken(refreshToken);

      // Update stored tokens and user data
      StorageService.setToken(response.token);
      if (response.refreshToken) {
        StorageService.setRefreshToken(response.refreshToken);
      }
      StorageService.setUser(response.user);

      dispatch({
        type: 'AUTH_SUCCESS',
        payload: {
          user: response.user,
          token: response.token,
        },
      });
    } catch (error) {
      console.error('Token refresh failed:', error);
      await logout();
      throw error;
    }
  };

  // Mock login function (to be replaced with real API call)
  const mockLogin = async (credentials: LoginRequest): Promise<JwtResponse> => {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 1000));

    // Mock validation
    if (credentials.username === 'admin' && credentials.password === 'password') {
      return {
        token: 'mock-jwt-token',
        type: 'Bearer',
        expiresIn: 3600,
        user: {
          id: 1,
          organizationId: 1,
          email: 'admin@talentx.com',
          username: 'admin',
          isActive: true,
          isVerified: true,
          lastLoginAt: new Date().toISOString(),
          twoFactorEnabled: false,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString(),
          roles: [
            {
              id: 1,
              name: 'ADMIN',
              description: 'System Administrator',
              isActive: true,
              assignedAt: new Date().toISOString(),
            },
          ],
        },
      };
    } else {
      throw new Error('Invalid credentials');
    }
  };

  const contextValue: AuthContextType = {
    ...state,
    login,
    logout,
    clearError,
    refreshToken,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook to use auth context
export const useAuthContext = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuthContext must be used within an AuthProvider');
  }
  return context;
};