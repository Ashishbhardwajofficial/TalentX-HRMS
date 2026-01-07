// frontend/src/api/leaveApi.ts
import apiClient from "./axiosClient";
import {
  LeaveRequest,
  LeaveType,
  LeaveBalance,
  LeaveStatus,
  PaginationParams,
  PaginatedResponse
} from "../types";
import { mockLeaveRequests, mockLeaveBalances, mockLeaveTypes, createPaginatedResponse, simulateDelay } from "./mockData";

// Check if mock mode is enabled
const USE_MOCK = process.env.REACT_APP_USE_MOCK === 'true';

// Leave API request/response types
export interface LeaveRequestCreateDTO {
  employeeId: number;
  leaveTypeId: number;
  startDate: string; // ISO date
  endDate: string; // ISO date
  reason: string;
  attachmentUrl?: string;
  isHalfDay?: boolean;
  halfDayPeriod?: string;
  isEmergency?: boolean;
  emergencyContact?: string;
  contactDetails?: string;
}

export interface LeaveRequestUpdateDTO {
  id: number;
  startDate?: string;
  endDate?: string;
  reason?: string;
  attachmentUrl?: string;
}

export interface LeaveRequestResponseDTO {
  id: number;
  employeeId: number;
  leaveTypeId: number;
  startDate: string;
  endDate: string;
  totalDays: number;
  reason?: string;
  status: LeaveStatus;
  reviewedBy?: number;
  reviewedAt?: string;
  reviewComments?: string;
  createdAt: string;
  updatedAt: string;

  // New fields for half-day leave
  isHalfDay?: boolean;
  halfDayPeriod?: string;

  // New fields for emergency leave
  isEmergency?: boolean;
  emergencyContact?: string;
  contactDetails?: string;

  // Attachment field
  attachmentPath?: string;

  // Audit fields
  active?: boolean;
  createdBy?: string;
  updatedBy?: string;
  version?: number;

  // Populated fields from relationships
  employee?: {
    id: number;
    employeeNumber: string;
    firstName: string;
    lastName: string;
    fullName: string;
    departmentName?: string;
  };
  leaveType?: {
    id: number;
    organizationId: number;
    name: string;
    code: string;
    description?: string;
    isPaid: boolean;
    maxDaysPerYear?: number;
    accrualRate?: number;
    requiresApproval: boolean;
    allowNegativeBalance: boolean;
    isActive: boolean;
  };
  reviewer?: {
    id: number;
    firstName: string;
    lastName: string;
    fullName: string;
  };
}

export interface LeaveApprovalDTO {
  id: number;
  status: LeaveStatus.APPROVED | LeaveStatus.REJECTED;
  reviewComments?: string;
}

export interface LeaveBalanceDTO {
  id: number;
  employeeId: number;
  leaveTypeId: number;
  year: number;
  allocatedDays: number;
  usedDays: number;
  pendingDays: number;
  availableDays: number; // Computed field
  carriedForwardDays: number;
  createdAt: string;
  updatedAt: string;

  // Populated fields from relationships
  employee?: {
    id: number;
    employeeNumber: string;
    fullName: string;
  };
  leaveType?: {
    id: number;
    name: string;
    maxDaysPerYear?: number;
  };
}

export interface LeaveSearchParams extends PaginationParams {
  employeeId?: number;
  leaveTypeId?: number;
  status?: LeaveStatus;
  startDateFrom?: string;
  startDateTo?: string;
  endDateFrom?: string;
  endDateTo?: string;
  departmentId?: number;
  managerId?: number;
  search?: string;
}

export interface LeaveCalendarEvent {
  id: number;
  title: string;
  start: string;
  end: string;
  employee: {
    id: number;
    fullName: string;
  };
  leaveType: {
    id: number;
    name: string;
  };
  status: LeaveStatus;
}

// Leave API client interface
export interface LeaveApiClient {
  getLeaveRequests(params: LeaveSearchParams): Promise<PaginatedResponse<LeaveRequestResponseDTO>>;
  getLeaveRequest(id: number): Promise<LeaveRequestResponseDTO>;
  createLeaveRequest(data: LeaveRequestCreateDTO): Promise<LeaveRequestResponseDTO>;
  updateLeaveRequest(id: number, data: LeaveRequestUpdateDTO): Promise<LeaveRequestResponseDTO>;
  deleteLeaveRequest(id: number): Promise<void>;
  approveLeaveRequest(data: LeaveApprovalDTO): Promise<LeaveRequestResponseDTO>;
  getLeaveBalance(employeeId: number, year?: number): Promise<LeaveBalanceDTO[]>;
  getLeaveTypes(): Promise<LeaveType[]>;
  getLeaveCalendar(startDate: string, endDate: string, departmentId?: number): Promise<LeaveCalendarEvent[]>;
  getEmployeeLeaveHistory(employeeId: number, params?: PaginationParams): Promise<PaginatedResponse<LeaveRequestResponseDTO>>;
}

// Implementation of leave API client
class LeaveApiClientImpl implements LeaveApiClient {
  private readonly LEAVE_ENDPOINTS = {
    BASE: '/leaves',
    BY_ID: (id: number) => `/leaves/${id}`,
    APPROVE: (id: number) => `/leaves/${id}/approve`,
    BALANCE: '/leaves/balance',
    EMPLOYEE_BALANCE: (employeeId: number) => `/leaves/balance/employee/${employeeId}`,
    TYPES: '/leaves/types',
    CALENDAR: '/leaves/calendar',
    EMPLOYEE_HISTORY: (employeeId: number) => `/leaves/employee/${employeeId}/history`
  } as const;

  async getLeaveRequests(params: LeaveSearchParams): Promise<PaginatedResponse<LeaveRequestResponseDTO>> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();
      let filtered = [...mockLeaveRequests];

      // Apply filters
      if (params.employeeId) {
        filtered = filtered.filter(lr => lr.employeeId === params.employeeId);
      }
      if (params.leaveTypeId) {
        filtered = filtered.filter(lr => lr.leaveTypeId === params.leaveTypeId);
      }
      if (params.status) {
        filtered = filtered.filter(lr => lr.status === params.status);
      }
      if (params.startDateFrom) {
        filtered = filtered.filter(lr => new Date(lr.startDate) >= new Date(params.startDateFrom!));
      }
      if (params.startDateTo) {
        filtered = filtered.filter(lr => new Date(lr.startDate) <= new Date(params.startDateTo!));
      }
      if (params.search) {
        const search = params.search.toLowerCase();
        filtered = filtered.filter(lr =>
          lr.employee?.fullName.toLowerCase().includes(search) ||
          lr.reason?.toLowerCase().includes(search)
        );
      }

      return createPaginatedResponse(filtered, params.page || 0, params.size || 10);
    }

    // Real API call
    const queryParams = this.buildQueryParams(params);
    return apiClient.get<PaginatedResponse<LeaveRequestResponseDTO>>(
      `${this.LEAVE_ENDPOINTS.BASE}?${queryParams}`
    );
  }

  async getLeaveRequest(id: number): Promise<LeaveRequestResponseDTO> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();
      const leaveRequest = mockLeaveRequests.find(lr => lr.id === id);
      if (!leaveRequest) {
        throw new Error(`Leave request with ID ${id} not found`);
      }
      return leaveRequest;
    }

    // Real API call
    return apiClient.get<LeaveRequestResponseDTO>(this.LEAVE_ENDPOINTS.BY_ID(id));
  }

  async createLeaveRequest(data: LeaveRequestCreateDTO): Promise<LeaveRequestResponseDTO> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();

      // Calculate total days
      const start = new Date(data.startDate);
      const end = new Date(data.endDate);
      const totalDays = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1;

      const newLeaveRequest: LeaveRequestResponseDTO = {
        id: mockLeaveRequests.length + 1,
        employeeId: data.employeeId,
        leaveTypeId: data.leaveTypeId,
        startDate: data.startDate,
        endDate: data.endDate,
        totalDays,
        reason: data.reason,
        status: "PENDING" as LeaveStatus,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      mockLeaveRequests.push(newLeaveRequest);
      return newLeaveRequest;
    }

    // Real API call
    return apiClient.post<LeaveRequestResponseDTO>(this.LEAVE_ENDPOINTS.BASE, data);
  }

  async updateLeaveRequest(id: number, data: LeaveRequestUpdateDTO): Promise<LeaveRequestResponseDTO> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();
      const index = mockLeaveRequests.findIndex(lr => lr.id === id);
      if (index === -1) {
        throw new Error(`Leave request with ID ${id} not found`);
      }

      const existing = mockLeaveRequests[index];
      if (!existing) {
        throw new Error(`Leave request with ID ${id} not found`);
      }

      const updated: LeaveRequestResponseDTO = {
        ...existing,
        updatedAt: new Date().toISOString()
      };

      if (data.startDate) updated.startDate = data.startDate;
      if (data.endDate) updated.endDate = data.endDate;
      if (data.reason) updated.reason = data.reason;

      // Recalculate total days if dates changed
      if (data.startDate || data.endDate) {
        const start = new Date(updated.startDate);
        const end = new Date(updated.endDate);
        updated.totalDays = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1;
      }

      mockLeaveRequests[index] = updated;
      return updated;
    }

    // Real API call
    return apiClient.put<LeaveRequestResponseDTO>(this.LEAVE_ENDPOINTS.BY_ID(id), data);
  }

  async deleteLeaveRequest(id: number): Promise<void> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();
      const index = mockLeaveRequests.findIndex(lr => lr.id === id);
      if (index === -1) {
        throw new Error(`Leave request with ID ${id} not found`);
      }
      mockLeaveRequests.splice(index, 1);
      return;
    }

    // Real API call
    return apiClient.delete<void>(this.LEAVE_ENDPOINTS.BY_ID(id));
  }

  async approveLeaveRequest(data: LeaveApprovalDTO): Promise<LeaveRequestResponseDTO> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();
      const index = mockLeaveRequests.findIndex(lr => lr.id === data.id);
      if (index === -1) {
        throw new Error(`Leave request with ID ${data.id} not found`);
      }

      const existing = mockLeaveRequests[index];
      if (!existing) {
        throw new Error(`Leave request with ID ${data.id} not found`);
      }

      const updated: LeaveRequestResponseDTO = {
        ...existing,
        status: data.status,
        reviewedAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };

      if (data.reviewComments) {
        updated.reviewComments = data.reviewComments;
      }

      mockLeaveRequests[index] = updated;
      return updated;
    }

    // Real API call
    return apiClient.put<LeaveRequestResponseDTO>(this.LEAVE_ENDPOINTS.APPROVE(data.id), data);
  }

  async getLeaveBalance(employeeId: number, year?: number): Promise<LeaveBalanceDTO[]> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();
      let filtered = mockLeaveBalances.filter(lb => lb.employeeId === employeeId);

      if (year) {
        filtered = filtered.filter(lb => lb.year === year);
      }

      return filtered;
    }

    // Real API call
    const params = year ? `?year=${year}` : '';
    return apiClient.get<LeaveBalanceDTO[]>(`${this.LEAVE_ENDPOINTS.EMPLOYEE_BALANCE(employeeId)}${params}`);
  }

  async getLeaveTypes(): Promise<LeaveType[]> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();
      return mockLeaveTypes;
    }

    // Real API call
    return apiClient.get<LeaveType[]>(this.LEAVE_ENDPOINTS.TYPES);
  }

  async getLeaveCalendar(startDate: string, endDate: string, departmentId?: number): Promise<LeaveCalendarEvent[]> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();
      const filtered = mockLeaveRequests.filter(lr => {
        const leaveStart = new Date(lr.startDate);
        const leaveEnd = new Date(lr.endDate);
        const rangeStart = new Date(startDate);
        const rangeEnd = new Date(endDate);

        return leaveStart <= rangeEnd && leaveEnd >= rangeStart;
      });

      return filtered.map(lr => ({
        id: lr.id,
        title: `${lr.employee?.fullName || 'Employee'} - ${lr.leaveType?.name || 'Leave'}`,
        start: lr.startDate,
        end: lr.endDate,
        employee: {
          id: lr.employeeId,
          fullName: lr.employee?.fullName || 'Employee'
        },
        leaveType: {
          id: lr.leaveTypeId,
          name: lr.leaveType?.name || 'Leave'
        },
        status: lr.status
      }));
    }

    // Real API call
    const params = new URLSearchParams({ startDate, endDate });
    if (departmentId) {
      params.append('departmentId', departmentId.toString());
    }
    return apiClient.get<LeaveCalendarEvent[]>(`${this.LEAVE_ENDPOINTS.CALENDAR}?${params}`);
  }

  async getEmployeeLeaveHistory(employeeId: number, params: PaginationParams = { page: 0, size: 10 }): Promise<PaginatedResponse<LeaveRequestResponseDTO>> {
    // Mock mode
    if (USE_MOCK) {
      await simulateDelay();
      const filtered = mockLeaveRequests.filter(lr => lr.employeeId === employeeId);
      return createPaginatedResponse(filtered, params.page || 0, params.size || 10);
    }

    // Real API call
    const queryParams = this.buildQueryParams(params);
    return apiClient.get<PaginatedResponse<LeaveRequestResponseDTO>>(
      `${this.LEAVE_ENDPOINTS.EMPLOYEE_HISTORY(employeeId)}?${queryParams}`
    );
  }

  private buildQueryParams(params: Record<string, any>): string {
    const searchParams = new URLSearchParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        searchParams.append(key, value.toString());
      }
    });

    return searchParams.toString();
  }
}

// Create and export singleton instance
const leaveApi = new LeaveApiClientImpl();

export default leaveApi;

// Export the class for testing purposes
export { LeaveApiClientImpl };