import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { serviceDeliveryService } from '../services/serviceDeliveryService';
import './ServicesList.css';

function ServicesList() {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('');

  const loadServices = async () => {
    try {
      setLoading(true);
      let data;
      if (filterStatus) {
        data = await serviceDeliveryService.getByStatus(filterStatus);
      } else {
        data = await serviceDeliveryService.getAll();
      }
      setServices(data);
      setError('');
    } catch (err) {
      setError('Failed to load services');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadServices();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterStatus]);

  const getStatusColor = (status) => {
    switch (status) {
      case 'PENDING':
        return '#ffc107';
      case 'IN_TRANSIT':
        return '#2196f3';
      case 'DELIVERED':
        return '#4caf50';
      case 'CANCELLED':
        return '#f44336';
      default:
        return '#9e9e9e';
    }
  };

  return (
    <div className="services-list">
      <div className="page-header">
        <h1>Service Deliveries</h1>
        <Link to="/dashboard/services/create" className="btn-primary">
          Create New Service
        </Link>
      </div>

      <div className="filter-section">
        <label>Filter by Status:</label>
        <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
          <option value="">All</option>
          <option value="PENDING">Pending</option>
          <option value="IN_TRANSIT">In Transit</option>
          <option value="DELIVERED">Delivered</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </div>

      {loading && <div className="loading">Loading services...</div>}
      {error && <div className="error">{error}</div>}

      {!loading && !error && (
        <div className="services-grid">
          {services.length === 0 ? (
            <div className="no-data">No services found</div>
          ) : (
            services.map((service) => (
              <div key={service.id} className="service-card">
                <div className="service-header">
                  <h3>Service #{service.id}</h3>
                  <span
                    className="status-badge"
                    style={{ backgroundColor: getStatusColor(service.status) }}
                  >
                    {service.status}
                  </span>
                </div>
                <div className="service-details">
                  <p>
                    <strong>Plate:</strong> {service.plateNumber || 'N/A'}
                  </p>
                  <p>
                    <strong>Messenger:</strong> {service.messengerName || 'N/A'}
                  </p>
                  <p>
                    <strong>Dealership:</strong> {service.dealershipName || 'N/A'}
                  </p>
                  <p>
                    <strong>Created:</strong>{' '}
                    {service.createdAt
                      ? new Date(service.createdAt).toLocaleDateString()
                      : 'N/A'}
                  </p>
                </div>
                <Link to={`/dashboard/services/${service.id}`} className="view-btn">
                  View Details
                </Link>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default ServicesList;
