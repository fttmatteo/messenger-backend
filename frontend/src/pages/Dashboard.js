import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import './Dashboard.css';

function Dashboard() {
  const navigate = useNavigate();

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <div className="dashboard">
      <nav className="sidebar">
        <div className="sidebar-header">
          <h2>Messenger System</h2>
        </div>
        <ul className="nav-menu">
          <li>
            <Link to="/dashboard/services">Services</Link>
          </li>
          <li>
            <Link to="/dashboard/services/create">Create Service</Link>
          </li>
          <li>
            <Link to="/dashboard/employees">Employees</Link>
          </li>
          <li>
            <Link to="/dashboard/dealerships">Dealerships</Link>
          </li>
        </ul>
        <div className="sidebar-footer">
          <button onClick={handleLogout} className="logout-btn">
            Logout
          </button>
        </div>
      </nav>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}

export default Dashboard;
