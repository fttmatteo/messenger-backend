import React, { useState } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import './Dashboard.css';

function Dashboard() {
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  const closeSidebar = () => {
    setSidebarOpen(false);
  };

  return (
    <div className="dashboard">
      {/* Hamburger button - visible on mobile */}
      <button className="hamburger-btn" onClick={toggleSidebar}>
        <span className="hamburger-icon"></span>
        <span className="hamburger-icon"></span>
        <span className="hamburger-icon"></span>
      </button>

      {/* Overlay - visible when sidebar is open on mobile */}
      {sidebarOpen && <div className="sidebar-overlay" onClick={closeSidebar}></div>}

      <nav className={`sidebar ${sidebarOpen ? 'sidebar-open' : ''}`}>
        <div className="sidebar-header">
          <h2>Messenger System</h2>
        </div>
        <ul className="nav-menu">
          <li>
            <Link to="/dashboard/services" onClick={closeSidebar}>Services</Link>
          </li>
          <li>
            <Link to="/dashboard/services/create" onClick={closeSidebar}>Create Service</Link>
          </li>
          {localStorage.getItem('role') === 'ADMIN' && (
            <>
              <li>
                <Link to="/dashboard/employees" onClick={closeSidebar}>Employees</Link>
              </li>
              <li>
                <Link to="/dashboard/dealerships" onClick={closeSidebar}>Dealerships</Link>
              </li>
            </>
          )}
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
