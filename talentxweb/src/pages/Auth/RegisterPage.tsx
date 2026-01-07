import React from 'react';
import { Link } from 'react-router-dom';
import './AuthPages.css';

const RegisterPage: React.FC = () => {
  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1 className="auth-title">Create Account</h1>
          <p className="auth-subtitle">Join TalentX HRMS</p>
        </div>

        <div className="auth-form">
          <p>Registration is currently disabled. Please contact your administrator.</p>
        </div>

        <div className="auth-footer">
          <p>
            Already have an account?{' '}
            <Link to="/login" className="auth-link">
              Sign in here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;