import React from 'react';
import { Navigate } from 'react-router-dom';
import { authService } from '../services/authService';

function PrivateRoute({ children }) {
  return authService.isAuthenticated() ? children : <Navigate to="/login" />;
}

export default PrivateRoute;
