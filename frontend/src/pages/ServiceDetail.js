import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { serviceDeliveryService } from '../services/serviceDeliveryService';
import './ServiceDetail.css';

function ServiceDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [service, setService] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadService = async () => {
    try {
      setLoading(true);
      const data = await serviceDeliveryService.getById(id);
      setService(data);
      setError('');
    } catch (err) {
      setError('Failed to load service details');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadService();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);


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

  if (loading) {
    return <div className="loading">Loading service details...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  if (!service) {
    return <div className="error">Service not found</div>;
  }

  return (
    <div className="service-detail">
      <div className="page-header">
        <h1 className="plate-header">
          {service.plate?.plateNumber ? service.plate.plateNumber.replace(/^(.{3})(.*)$/, '$1 $2') : 'Unknown Plate'}
        </h1>
        <button onClick={() => navigate('/dashboard/services')} className="btn-secondary">
          Back to List
        </button>
      </div>

      <div className="detail-container">
        <div className="detail-section">
          <h2>Service Information</h2>
          <div className="detail-row">
            <span className="label">Status:</span>
            <span
              className="status-badge"
              style={{ backgroundColor: getStatusColor(service.currentStatus) }}
            >
              {service.currentStatus}
            </span>
          </div>

          <div className="detail-row">
            <span className="label">Plate Type:</span>
            <span>{service.plate?.plateType || 'N/A'}</span>
          </div>
          <div className="detail-row">
            <span className="label">Messenger:</span>
            <span>{service.messenger?.fullName || 'N/A'}</span>
          </div>
          <div className="detail-row">
            <span className="label">Dealership:</span>
            <span>{service.dealership?.name || 'N/A'}</span>
          </div>
          <div className="detail-row">
            <span className="label">Zone:</span>
            <span>{service.dealership?.zone || 'N/A'}</span>
          </div>
          {service.photos && service.photos.length > 0 && (
            <div className="detail-row service-photos">
              <span className="label">Service Photos:</span>
              <div className="photos-grid">
                {service.photos.map(photo => (
                  <a key={photo.idPhoto} href={`http://${window.location.hostname}:8080/api/files/${photo.photoPath.split('/').pop()}`} target="_blank" rel="noopener noreferrer">
                    <img
                      src={`http://${window.location.hostname}:8080/api/files/${photo.photoPath.split('/').pop()}`}
                      alt="Service photo"
                      className="history-photo-thumbnail"
                    />
                  </a>
                ))}
              </div>
            </div>
          )}
        </div>

        {service.history && service.history.length > 0 && (
          <div className="detail-section">
            <h2>Status History</h2>
            <div className="history-list">
              {service.history.map((history, index) => (
                <div key={history.idStatusHistory || index} className="history-item">
                  <div className="history-status" style={{ backgroundColor: getStatusColor(history.newStatus) }}>
                    {history.previousStatus} → {history.newStatus}
                  </div>
                  <div className="history-details">
                    <p><strong>Date:</strong> {new Date(history.changeDate).toLocaleString()}</p>
                    <p><strong>Changed By:</strong> {history.changedBy?.fullName || 'N/A'}</p>
                    {history.photos && history.photos.length > 0 && (
                      <div className="history-photos">
                        <p><strong>Evidence Photos:</strong></p>
                        <div className="photos-grid">
                          {history.photos.map(photo => (
                            <a key={photo.idPhoto} href={`http://${window.location.hostname}:8080/api/files/${photo.photoPath.split('/').pop()}`} target="_blank" rel="noopener noreferrer">
                              <img
                                src={`http://${window.location.hostname}:8080/api/files/${photo.photoPath.split('/').pop()}`}
                                alt="Status evidence"
                                className="history-photo-thumbnail"
                              />
                            </a>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}


      </div>
    </div>
  );
}

export default ServiceDetail;
