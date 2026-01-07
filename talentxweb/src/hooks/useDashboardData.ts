import { useState, useEffect } from 'react';
import dashboardApi, { DashboardStatistics } from '../api/dashboardApi';

export interface DashboardDataState {
  data: DashboardStatistics | null;
  loading: boolean;
  error: string | null;
  refetch: () => Promise<void>;
}

export const useDashboardData = (): DashboardDataState => {
  const [data, setData] = useState<DashboardStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setLoading(true);
      setError(null);
      const statistics = await dashboardApi.getDashboardStatistics();
      setData(statistics);
    } catch (err: any) {
      console.error('Error loading dashboard data:', err);
      setError(err.message || 'Failed to load dashboard data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  return {
    data,
    loading,
    error,
    refetch: fetchData,
  };
};

// Helper function to calculate trend from current and previous values
export const calculateTrend = (current: number, previous: number): { direction: 'up' | 'down' | 'neutral'; value: number } => {
  if (previous === 0) {
    return { direction: 'neutral', value: 0 };
  }

  const percentChange = ((current - previous) / previous) * 100;

  if (Math.abs(percentChange) < 0.1) {
    return { direction: 'neutral', value: 0 };
  }

  return {
    direction: percentChange > 0 ? 'up' : 'down',
    value: Math.abs(Math.round(percentChange * 10) / 10),
  };
};

// Helper function to determine status based on value and thresholds
export const determineStatus = (
  value: number,
  thresholds: { critical?: number; warning?: number; success?: number }
): 'critical' | 'danger' | 'warning' | 'success' | 'info' | 'neutral' => {
  if (thresholds.critical !== undefined && value >= thresholds.critical) {
    return 'critical';
  }
  if (thresholds.warning !== undefined && value >= thresholds.warning) {
    return 'warning';
  }
  if (thresholds.success !== undefined && value >= thresholds.success) {
    return 'success';
  }
  return 'info';
};
