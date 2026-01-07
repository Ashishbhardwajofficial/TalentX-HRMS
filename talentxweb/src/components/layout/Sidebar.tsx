import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';

import {
  LayoutDashboard,
  Users,
  Calendar,
  Palmtree,
  CircleDollarSign,
  Target,
  BarChart3,
  ChevronLeft,
  UserPlus,
  ClipboardList,
  FileText,
  History,
  Settings,
  Building2,
  MapPin,
  Search,
  GraduationCap,
  Award,
  Clock,
  Laptop,
  CreditCard,
  HeartPulse,
  ShieldCheck,
  Lock,
  Monitor
} from 'lucide-react';

interface SidebarProps {
  collapsed: boolean;
  onToggle: () => void;
}

interface MenuItem {
  id: string;
  label: string;
  icon: string;
  path: string;
  children?: MenuItem[];
}

const Sidebar: React.FC<SidebarProps> = ({ collapsed, onToggle }) => {
  const location = useLocation();
  const [expandedMenus, setExpandedMenus] = useState<string[]>([]);

  const menuItems: MenuItem[] = [
    {
      id: 'dashboard',
      label: 'Dashboard',
      icon: 'LayoutDashboard',
      path: '/dashboard',
    },
    {
      id: 'core',
      label: 'Core Management',
      icon: 'Users',
      path: '/employees',
      children: [
        { id: 'employees-list', label: 'All Employees', icon: 'Users', path: '/employees' },
        { id: 'departments', label: 'Departments', icon: 'Building2', path: '/departments' },
        { id: 'locations', label: 'Locations', icon: 'MapPin', path: '/locations' },
      ],
    },
    {
      id: 'talent',
      label: 'Talent & Growth',
      icon: 'Target',
      path: '/performance',
      children: [
        { id: 'job-postings', label: 'Job Postings', icon: 'Search', path: '/recruitment' },
        { id: 'interviews', label: 'Interview Schedule', icon: 'Calendar', path: '/recruitment/interviews' },
        { id: 'candidates', label: 'Candidate Evaluation', icon: 'ClipboardList', path: '/recruitment/candidates' },
        { id: 'performance', label: 'Performance', icon: 'Target', path: '/performance' },
        { id: 'training', label: 'Training', icon: 'GraduationCap', path: '/training' },
        { id: 'skills', label: 'Skills & Matrix', icon: 'Award', path: '/skills' },
      ],
    },
    {
      id: 'operations',
      label: 'Operations',
      icon: 'Calendar',
      path: '/attendance',
      children: [
        { id: 'attendance', label: 'Attendance', icon: 'Clock', path: '/attendance' },
        { id: 'leave', label: 'Leave & Holidays', icon: 'Palmtree', path: '/leave' },
        { id: 'assets', label: 'Inventory Control', icon: 'Laptop', path: '/assets' },
        { id: 'asset-assignments', label: 'Asset Assignments', icon: 'Monitor', path: '/assets/assignments' },
        { id: 'expenses', label: 'Expenses', icon: 'CreditCard', path: '/expenses' },
      ],
    },
    {
      id: 'finance',
      label: 'Finance & Payroll',
      icon: 'CircleDollarSign',
      path: '/payroll',
      children: [
        { id: 'payroll-runs', label: 'Payroll Runs', icon: 'History', path: '/payroll/runs' },
        { id: 'tax-declaration', label: 'Tax Declaration', icon: 'FileText', path: '/payroll/tax-declaration' },
        { id: 'benefits', label: 'Benefits', icon: 'HeartPulse', path: '/benefits' },
      ],
    },
    {
      id: 'governance',
      label: 'Governance',
      icon: 'ShieldCheck',
      path: '/compliance',
      children: [
        { id: 'compliance', label: 'Compliance', icon: 'ShieldCheck', path: '/compliance' },
        { id: 'documents', label: 'Documents', icon: 'FileText', path: '/documents' },
        { id: 'audit', label: 'Audit Logs', icon: 'History', path: '/audit' },
      ],
    },
    {
      id: 'system',
      label: 'System Settings',
      icon: 'Settings',
      path: '/users',
      children: [
        { id: 'user-management', label: 'User Management', icon: 'UserPlus', path: '/users' },
        { id: 'role-permissions', label: 'Roles & Permissions', icon: 'Lock', path: '/roles' },
        { id: 'system-settings', label: 'General Settings', icon: 'Settings', path: '/settings' },
      ],
    },
    {
      id: 'reports',
      label: 'Reports',
      icon: 'BarChart3',
      path: '/reports',
    },
  ];

  const getIcon = (iconName: string, active: boolean) => {
    const props = { className: `w-5 h-5 transition-colors ${active ? 'text-white' : 'text-secondary-500 group-hover:text-primary-500'}` };
    switch (iconName) {
      case 'LayoutDashboard': return <LayoutDashboard {...props} />;
      case 'Users': return <Users {...props} />;
      case 'Calendar': return <Calendar {...props} />;
      case 'Palmtree': return <Palmtree {...props} />;
      case 'CircleDollarSign': return <CircleDollarSign {...props} />;
      case 'Target': return <Target {...props} />;
      case 'BarChart3': return <BarChart3 {...props} />;
      case 'UserPlus': return <UserPlus {...props} />;
      case 'ClipboardList': return <ClipboardList {...props} />;
      case 'FileText': return <FileText {...props} />;
      case 'History': return <History {...props} />;
      case 'Settings': return <Settings {...props} />;
      case 'Building2': return <Building2 {...props} />;
      case 'MapPin': return <MapPin {...props} />;
      case 'Search': return <Search {...props} />;
      case 'GraduationCap': return <GraduationCap {...props} />;
      case 'Award': return <Award {...props} />;
      case 'Clock': return <Clock {...props} />;
      case 'Laptop': return <Laptop {...props} />;
      case 'CreditCard': return <CreditCard {...props} />;
      case 'HeartPulse': return <HeartPulse {...props} />;
      case 'ShieldCheck': return <ShieldCheck {...props} />;
      case 'Lock': return <Lock {...props} />;
      default: return <BarChart3 {...props} />;
    }
  };

  const toggleSubmenu = (menuId: string) => {
    setExpandedMenus(prev =>
      prev.includes(menuId)
        ? prev.filter(id => id !== menuId)
        : [...prev, menuId]
    );
  };

  const isActive = (path: string) => location.pathname === path || (path !== '/dashboard' && location.pathname.startsWith(path));
  const isExpanded = (menuId: string) => expandedMenus.includes(menuId);

  return (
    <aside
      className={`fixed top-16 left-0 h-[calc(100vh-4rem)] bg-[var(--sidebar-bg)] border-r border-[var(--border-color)] transition-all duration-300 z-40 ${collapsed ? 'w-20' : 'w-64'
        }`}
    >
      <div className="flex flex-col h-full py-4 px-3 overflow-y-auto no-scrollbar">
        <ul className="space-y-1.5">
          {menuItems.map((item) => {
            const active = isActive(item.path);
            const expanded = isExpanded(item.id);

            return (
              <li key={item.id} className="group">
                {item.children ? (
                  <>
                    <button
                      onClick={() => toggleSubmenu(item.id)}
                      className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 ${active
                        ? 'bg-gradient-primary text-white shadow-premium'
                        : 'text-secondary-600 dark:text-secondary-400 hover:bg-secondary-100 dark:hover:bg-secondary-800'
                        }`}
                    >
                      {getIcon(item.icon, active)}
                      {!collapsed && (
                        <>
                          <span className="flex-1 text-left text-sm font-bold tracking-tight">
                            {item.label}
                          </span>
                          <svg
                            className={`w-3.5 h-3.5 transition-transform duration-300 ${expanded ? 'rotate-180' : ''}`}
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M19 9l-7 7-7-7" />
                          </svg>
                        </>
                      )}
                    </button>
                    {!collapsed && expanded && (
                      <ul className="mt-1 space-y-1 ml-4 border-l border-secondary-100 dark:border-secondary-800 animate-slide-up">
                        {item.children.map((child) => {
                          const childActive = location.pathname === child.path;
                          return (
                            <li key={child.id}>
                              <Link
                                to={child.path}
                                className={`flex items-center gap-3 px-6 py-2 rounded-r-xl transition-all duration-200 text-sm font-medium ${childActive
                                  ? 'text-primary-600 dark:text-primary-400 bg-primary-50 dark:bg-primary-900/10 border-l-2 border-primary-600 -ml-px'
                                  : 'text-secondary-500 hover:text-secondary-900 dark:hover:text-white hover:bg-secondary-50 dark:hover:bg-secondary-800/50'
                                  }`}
                              >
                                {getIcon(child.icon, false)}
                                <span>{child.label}</span>
                              </Link>
                            </li>
                          );
                        })}
                      </ul>
                    )}
                  </>
                ) : (
                  <Link
                    to={item.path}
                    className={`flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 ${active
                      ? 'bg-gradient-primary text-white shadow-premium'
                      : 'text-secondary-600 dark:text-secondary-400 hover:bg-secondary-100 dark:hover:bg-secondary-800'
                      }`}
                  >
                    {getIcon(item.icon, active)}
                    {!collapsed && (
                      <span className="text-sm font-bold tracking-tight">{item.label}</span>
                    )}
                  </Link>
                )}
              </li>
            );
          })}
        </ul>

        {/* Action Toggle (Footer) */}
        <div className="mt-auto pt-4 border-t border-secondary-100 dark:border-secondary-800">
          <button
            onClick={onToggle}
            className="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-secondary-500 hover:bg-secondary-100 dark:hover:bg-secondary-800 transition-all group"
          >
            <div className={`p-1.5 rounded-lg bg-secondary-100 dark:bg-secondary-800 transition-transform duration-300 ${collapsed ? 'rotate-180' : ''}`}>
              <ChevronLeft className="w-4 h-4" />
            </div>
            {!collapsed && <span className="text-sm font-bold tracking-tight">Collapse View</span>}
          </button>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
