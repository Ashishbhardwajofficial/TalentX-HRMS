import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Navbar from './Navbar';
import Sidebar from './Sidebar';
import Breadcrumb from '../common/Breadcrumb';
import ErrorBoundary from '../common/ErrorBoundary';

export interface MainLayoutProps {
  children?: React.ReactNode;
}

const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  React.useEffect(() => {
    const handleResize = () => {
      const mobile = window.innerWidth < 768;
      setIsMobile(mobile);
      if (mobile) {
        setSidebarOpen(false);
      }
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  return (
    <div className="min-h-screen bg-secondary-50">
      <Navbar
        onMenuClick={toggleSidebar}
        sidebarOpen={sidebarOpen}
      />

      <Sidebar
        collapsed={!sidebarOpen}
        onToggle={toggleSidebar}
      />

      <main
        className={`pt-16 min-h-screen transition-all duration-300 ${sidebarOpen && !isMobile ? 'ml-64' : 'ml-20'
          }`}
      >
        <div className="p-6">
          <ErrorBoundary>
            <Breadcrumb />
            <div className="mt-4">
              {children || <Outlet />}
            </div>
          </ErrorBoundary>
        </div>
      </main>

      {/* Mobile overlay */}
      {isMobile && sidebarOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-30 animate-fade-in"
          onClick={() => setSidebarOpen(false)}
        />
      )}
    </div>
  );
};

export default MainLayout;