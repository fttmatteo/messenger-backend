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
  const [showUpdateForm, setShowUpdateForm] = useState(false);
  const [updateData, setUpdateData] = useState({
    status: '',
    observation: '',
    signature: null,
    photos: [],
    userDocument: '',
  });

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

  const handleUpdateChange = (e) => {
    setUpdateData({
      ...updateData,
      [e.target.name]: e.target.value,
    });
  };

  const handleFileChange = (e) => {
    const { name, files } = e.target;
    if (name === 'signature') {
      setUpdateData({ ...updateData, signature: files[0] });
    } else if (name === 'photos') {
      setUpdateData({ ...updateData, photos: Array.from(files) });
    }
  };

  const handleUpdateSubmit = async (e) => {
    e.preventDefault();
    try {
      const formData = new FormData();
      formData.append('status', updateData.status);
      formData.append('userDocument', updateData.userDocument);

      if (updateData.observation) {
        formData.append('observation', updateData.observation);
      }

      if (updateData.signature) {
        formData.append('signature', updateData.signature);
      }

      updateData.photos.forEach((photo) => {
        formData.append('photos', photo);
      });

      await serviceDeliveryService.updateStatus(id, formData);
      setShowUpdateForm(false);
      loadService();
      alert('Status updated successfully!');
    } catch (err) {
      alert(err.response?.data || 'Failed to update status');
    }
  };

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
        <h1>Service Details #{service.idServiceDelivery}</h1>
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
            <span className="label">Plate Number:</span>
            <span>{service.plate?.plateNumber || 'N/A'}</span>
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
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="actions-section">
          <button
            onClick={() => setShowUpdateForm(!showUpdateForm)}
            className="btn-primary"
          >
            {showUpdateForm ? 'Cancel Update' : 'Update Status'}
          </button>
        </div>

        {showUpdateForm && (
          <div className="detail-section update-form">
            <h2>Update Service Status</h2>
            <form onSubmit={handleUpdateSubmit}>
              <div className="form-group">
                <label>Status *</label>
                <select
                  name="status"
                  value={updateData.status}
                  onChange={handleUpdateChange}
                  required
                >
                  <option value="">Select status</option>
                  <option value="PENDING">Pending</option>
                  <option value="IN_TRANSIT">In Transit</option>
                  <option value="DELIVERED">Delivered</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>

              <div className="form-group">
                <label>User Document *</label>
                <input
                  type="text"
                  name="userDocument"
                  value={updateData.userDocument}
                  onChange={handleUpdateChange}
                  required
                  placeholder="Enter your document number"
                />
              </div>

              <div className="form-group">
                <label>Observation</label>
                <textarea
                  name="observation"
                  value={updateData.observation}
                  onChange={handleUpdateChange}
                  rows="3"
                  placeholder="Add any observations..."
                />
              </div>

              <div className="form-group">
                <label>Signature</label>
                <input
                  type="file"
                  name="signature"
                  onChange={handleFileChange}
                  accept="image/*"
                />
              </div>

              <div className="form-group">
                <label>Photos</label>
                <input
                  type="file"
                  name="photos"
                  onChange={handleFileChange}
                  accept="image/*"
                  multiple
                />
              </div>

              <button type="submit" className="btn-primary">
                Submit Update
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}

export default ServiceDetail;
