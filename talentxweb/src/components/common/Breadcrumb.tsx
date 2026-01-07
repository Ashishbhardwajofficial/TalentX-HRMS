import React from 'react';
import { Link, useLocation } from 'react-router-dom';

interface BreadcrumbItem {
  label: string;
  path?: string;
}

interface BreadcrumbProps {
  items?: BreadcrumbItem[];
}

const Breadcrumb: React.FC<BreadcrumbProps> = ({ items }) => {
  const location = useLocation();

  // Generate breadcrumb items from current path if not provided
  const generateBreadcrumbs = (): BreadcrumbItem[] => {
    const pathSegments = location.pathname.split('/').filter(segment => segment);
    const breadcrumbs: BreadcrumbItem[] = [{ label: 'Dashboard', path: '/dashboard' }];

    let currentPath = '';
    pathSegments.forEach((segment, index) => {
      currentPath += `/${segment}`;

      // Convert segment to readable label
      const label = segment
        .split('-')
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join(' ');

      // Don't add path for the last item (current page)
      const isLast = index === pathSegments.length - 1;
      if (isLast) {
        breadcrumbs.push({ label });
      } else {
        breadcrumbs.push({ label, path: currentPath });
      }
    });

    return breadcrumbs;
  };

  const breadcrumbItems = items || generateBreadcrumbs();

  const breadcrumbStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center',
    padding: '10px 0',
    fontSize: '14px',
    color: '#666',
    marginBottom: '20px'
  };

  const itemStyle: React.CSSProperties = {
    display: 'flex',
    alignItems: 'center'
  };

  const linkStyle: React.CSSProperties = {
    color: '#007bff',
    textDecoration: 'none'
  };

  const separatorStyle: React.CSSProperties = {
    margin: '0 8px',
    color: '#ccc'
  };

  const currentStyle: React.CSSProperties = {
    color: '#333',
    fontWeight: '500'
  };

  return (
    <nav style={breadcrumbStyle}>
      {breadcrumbItems.map((item, index) => (
        <div key={index} style={itemStyle}>
          {index > 0 && <span style={separatorStyle}>›</span>}
          {item.path ? (
            <Link to={item.path} style={linkStyle}>
              {item.label}
            </Link>
          ) : (
            <span style={currentStyle}>{item.label}</span>
          )}
        </div>
      ))}
    </nav>
  );
};

export default Breadcrumb;