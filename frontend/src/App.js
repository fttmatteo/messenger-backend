import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import ServicesList from './pages/ServicesList';
import CreateService from './pages/CreateService';
import ServiceDetail from './pages/ServiceDetail';
import EmployeesList from './pages/EmployeesList';
import DealershipsList from './pages/DealershipsList';
import PrivateRoute from './components/PrivateRoute';
import './App.css';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/dashboard"
          element={
            <PrivateRoute>
              <Dashboard />
            </PrivateRoute>
          }
        >
          <Route index element={<Navigate to="/dashboard/services" />} />
          <Route path="services" element={<ServicesList />} />
          <Route path="services/create" element={<CreateService />} />
          <Route path="services/:id" element={<ServiceDetail />} />
          <Route path="employees" element={<EmployeesList />} />
          <Route path="dealerships" element={<DealershipsList />} />
        </Route>
        <Route path="/" element={<Navigate to="/login" />} />
      </Routes>
    </Router>
  );
}

export default App;
