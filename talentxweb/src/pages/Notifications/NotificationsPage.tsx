import React, { useState, useEffect, useCallback } from 'react';
import notificationApi, { SystemNotificationDTO, NotificationSearchParams } from '../../api/notificationApi';
import DataTable, { ColumnDefinition } from '../../components/common/DataTable';
import Button from '../../components/common/Button';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import Modal from '../../components/common/Modal';
import { exportToCSV } from '../../utils/exportUtils';
import { NotificationType } from '../../types';
import { PaginatedResponse } from '../../types';

interface NotificationFilters {
  notificationType?: NotificationType | undefined;
  isRead?: boolean | undefined;
  dateRange?: {
    start: string;
    end: string;
  } | undefined;
}

const NotificationsPage: React.FC = () => {
  const [notifications, setNotifications] = useState<SystemNotificationDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    total: 0
  });
  const [filters, setFilters] = useState<NotificationFilters>({});
  const [selectedNotification, setSelectedNotification] = useState<SystemNotificationDTO | null>(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [markingAsRead, setMarkingAsRead] = useState<number | null>(null);

  const loadNotifications = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const params: NotificationSearchParams = {
        page: pagination.page,
        size: pagination.size,
        sort: 'createdAt',
        direction: 'desc',
        ...filters,
        createdAfter: filters.dateRange?.start,
        createdBefore: filters.dateRange?.end
      };

      const response: PaginatedResponse<SystemNotificationDTO> = await notificationApi.getNotifications(params);

      setNotifications(response.content);
      setPagination(prev => ({
        ...prev,
        total: response.totalElements
      }));
    } catch (err: any) {
      console.error('Error loading notifications:', err);
      setError(err.message || 'Failed to load notifications');
    } finally {
      setLoading(false);
    }
  }, [pagination.page, pagination.size, filters]);

  useEffect(() => {
    loadNotifications();
  }, [loadNotifications]);

  const handleMarkAsRead = async (notificationId: number) => {
    try {
      setMarkingAsRead(notificationId);
      await notificationApi.markAsRead(notificationId);

      // Update the notification in the list
      setNotifications(prev =>
        prev.map(notification =>
          notification.id === notificationId
            ? { ...notification, isRead: true, readAt: new Date().toISOString() }
            : notification
        )
      );
    } catch (err: any) {
      console.error('Error marking notification as read:', err);
      setError(err.message || 'Failed to mark notification as read');
    } finally {
      setMarkingAsRead(null);
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      setLoading(true);
      await notificationApi.markAllAsRead();
      await loadNotifications(); // Reload to get updated data
    } catch (err: any) {
      console.error('Error marking all notifications as read:', err);
      setError(err.message || 'Failed to mark all notifications as read');
    } finally {
      setLoading(false);
    }
  };

  const handleDeleteNotification = async (notificationId: number) => {
    if (!window.confirm('Are you sure you want to delete this notification?')) {
      return;
    }

    try {
      await notificationApi.deleteNotification(notificationId);
      setNotifications(prev => prev.filter(n => n.id !== notificationId));
      setPagination(prev => ({ ...prev, total: prev.total - 1 }));
    } catch (err: any) {
      console.error('Error deleting notification:', err);
      setError(err.message || 'Failed to delete notification');
    }
  };

  const handleViewDetails = (notification: SystemNotificationDTO) => {
    setSelectedNotification(notification);
    setShowDetailModal(true);

    // Mark as read if not already read
    if (!notification.isRead) {
      handleMarkAsRead(notification.id);
    }
  };

  const handleActionClick = (notification: SystemNotificationDTO) => {
    if (notification.actionUrl) {
      window.open(notification.actionUrl, '_blank');
    }

    // Mark as read if not already read
    if (!notification.isRead) {
      handleMarkAsRead(notification.id);
    }
  };

  const handleExport = () => {
    exportToCSV(notifications, 'notifications', [
      { key: 'notificationType', header: 'Type' },
      { key: 'title', header: 'Title' },
      { key: 'message', header: 'Message' },
      { key: 'isRead', header: 'Status' },
      { key: 'createdAt', header: 'Created' }
    ]);
  };

  const getNotificationTypeColor = (type: NotificationType): string => {
    switch (type) {
      case NotificationType.SUCCESS:
        return '#10b981';
      case NotificationType.WARNING:
        return '#f59e0b';
      case NotificationType.ERROR:
        return '#ef4444';
      case NotificationType.COMPLIANCE_ALERT:
        return '#dc2626';
      case NotificationType.APPROVAL_REQUEST:
        return '#8b5cf6';
      default:
        return '#6b7280';
    }
  };

  const getNotificationTypeIcon = (type: NotificationType): string => {
    switch (type) {
      case NotificationType.SUCCESS:
        return '✅';
      case NotificationType.WARNING:
        return '⚠️';
      case NotificationType.ERROR:
        return '❌';
      case NotificationType.COMPLIANCE_ALERT:
        return '🚨';
      case NotificationType.APPROVAL_REQUEST:
        return '📋';
      default:
        return 'ℹ️';
    }
  };

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleString();
  };

  const columns: ColumnDefinition<SystemNotificationDTO>[] = [
    {
      key: 'notificationType',
      header: 'Type',
      render: (value: NotificationType) => (
        <span
          style={{
            color: getNotificationTypeColor(value),
            fontWeight: 'bold'
          }}
        >
          {getNotificationTypeIcon(value)} {value.replace('_', ' ')}
        </span>
      )
    },
    {
      key: 'title',
      header: 'Title',
      render: (value: string, notification: SystemNotificationDTO) => (
        <div>
          <strong style={{ color: notification.isRead ? '#6b7280' : '#111827' }}>
            {value}
          </strong>
          {!notification.isRead && (
            <span style={{
              marginLeft: '8px',
              backgroundColor: '#3b82f6',
              color: 'white',
              padding: '2px 6px',
              borderRadius: '10px',
              fontSize: '10px'
            }}>
              NEW
            </span>
          )}
        </div>
      )
    },
    {
      key: 'message',
      header: 'Message',
      render: (value: string) => (
        <div style={{
          maxWidth: '300px',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap'
        }}>
          {value}
        </div>
      )
    },
    {
      key: 'createdAt',
      header: 'Created',
      render: (value: string) => formatDate(value)
    },
    {
      key: 'isRead',
      header: 'Status',
      render: (value: boolean, notification: SystemNotificationDTO) => (
        <span style={{
          color: value ? '#10b981' : '#f59e0b',
          fontWeight: 'bold'
        }}>
          {value ? '✓ Read' : '● Unread'}
          {notification.readAt && (
            <div style={{ fontSize: '12px', color: '#6b7280' }}>
              {formatDate(notification.readAt)}
            </div>
          )}
        </span>
      )
    },
    {
      key: 'id',
      header: 'Actions',
      render: (value: number, notification: SystemNotificationDTO) => (
        <div style={{ display: 'flex', gap: '8px' }}>
          <Button
            size="sm"
            variant="primary"
            onClick={() => handleViewDetails(notification)}
          >
            View
          </Button>
          {notification.actionUrl && (
            <Button
              size="sm"
              variant="secondary"
              onClick={() => handleActionClick(notification)}
            >
              Action
            </Button>
          )}
          {!notification.isRead && (
            <Button
              size="sm"
              variant="success"
              onClick={() => handleMarkAsRead(notification.id)}
              isLoading={markingAsRead === notification.id}
              disabled={markingAsRead === notification.id}
            >
              Mark Read
            </Button>
          )}
          <Button
            size="sm"
            variant="danger"
            onClick={() => handleDeleteNotification(notification.id)}
          >
            Delete
          </Button>
        </div>
      )
    }
  ];

  if (loading && notifications.length === 0) {
    return <LoadingSpinner message="Loading notifications..." overlay />;
  }

  return (
    <div style={{ padding: '20px' }}>
      <div style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        marginBottom: '20px'
      }}>
        <h1>Notifications</h1>
        <div style={{ display: 'flex', gap: '10px' }}>
          <Button
            variant="secondary"
            onClick={loadNotifications}
            isLoading={loading}
          >
            Refresh
          </Button>
          <Button
            variant="primary"
            onClick={handleMarkAllAsRead}
          >
            Mark All Read
          </Button>
        </div>
      </div>

      {/* Filters */}
      <div style={{
        marginBottom: '20px',
        padding: '15px',
        backgroundColor: '#f9fafb',
        borderRadius: '8px',
        display: 'flex',
        gap: '15px',
        alignItems: 'center'
      }}>
        <div>
          <label htmlFor="type-filter" style={{ marginRight: '5px' }}>Type:</label>
          <select
            id="type-filter"
            value={filters.notificationType || ''}
            onChange={(e) => setFilters(prev => ({
              ...prev,
              notificationType: e.target.value as NotificationType || undefined
            }))}
          >
            <option value="">All Types</option>
            {Object.values(NotificationType).map(type => (
              <option key={type} value={type}>
                {getNotificationTypeIcon(type)} {type.replace('_', ' ')}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label htmlFor="status-filter" style={{ marginRight: '5px' }}>Status:</label>
          <select
            id="status-filter"
            value={filters.isRead === undefined ? '' : filters.isRead.toString()}
            onChange={(e) => setFilters(prev => ({
              ...prev,
              isRead: e.target.value === '' ? undefined : e.target.value === 'true'
            }))}
          >
            <option value="">All</option>
            <option value="false">Unread</option>
            <option value="true">Read</option>
          </select>
        </div>

        <Button
          variant="secondary"
          size="sm"
          onClick={() => setFilters({})}
        >
          Clear Filters
        </Button>
      </div>

      {error && (
        <div style={{
          marginBottom: '20px',
          padding: '10px',
          backgroundColor: '#fef2f2',
          color: '#dc2626',
          borderRadius: '4px'
        }}>
          {error}
        </div>
      )}

      <DataTable
        data={notifications}
        columns={columns}
        loading={loading}
        pagination={{
          page: pagination.page + 1, // DataTable expects 1-based page numbers
          size: pagination.size,
          total: pagination.total
        }}
        onPageChange={(page) => setPagination(prev => ({ ...prev, page: page - 1 }))}
        onPageSizeChange={(size) => setPagination(prev => ({ ...prev, size, page: 0 }))}
        onExport={handleExport}
      />

      {/* Notification Detail Modal */}
      <Modal
        isOpen={showDetailModal}
        onClose={() => setShowDetailModal(false)}
        title="Notification Details"
        size="md"
      >
        {selectedNotification && (
          <div>
            <div style={{ marginBottom: '15px' }}>
              <strong>Type:</strong>
              <span style={{
                marginLeft: '10px',
                color: getNotificationTypeColor(selectedNotification.notificationType)
              }}>
                {getNotificationTypeIcon(selectedNotification.notificationType)} {selectedNotification.notificationType.replace('_', ' ')}
              </span>
            </div>

            <div style={{ marginBottom: '15px' }}>
              <strong>Title:</strong>
              <div style={{ marginTop: '5px' }}>{selectedNotification.title}</div>
            </div>

            <div style={{ marginBottom: '15px' }}>
              <strong>Message:</strong>
              <div style={{ marginTop: '5px', whiteSpace: 'pre-wrap' }}>
                {selectedNotification.message}
              </div>
            </div>

            <div style={{ marginBottom: '15px' }}>
              <strong>Created:</strong>
              <div style={{ marginTop: '5px' }}>{formatDate(selectedNotification.createdAt)}</div>
            </div>

            {selectedNotification.readAt && (
              <div style={{ marginBottom: '15px' }}>
                <strong>Read At:</strong>
                <div style={{ marginTop: '5px' }}>{formatDate(selectedNotification.readAt)}</div>
              </div>
            )}

            {selectedNotification.expiresAt && (
              <div style={{ marginBottom: '15px' }}>
                <strong>Expires:</strong>
                <div style={{ marginTop: '5px' }}>{formatDate(selectedNotification.expiresAt)}</div>
              </div>
            )}

            {selectedNotification.actionUrl && (
              <div style={{ marginTop: '20px' }}>
                <Button
                  variant="primary"
                  onClick={() => {
                    window.open(selectedNotification.actionUrl, '_blank');
                    setShowDetailModal(false);
                  }}
                >
                  Go to Action
                </Button>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
};

export default NotificationsPage;