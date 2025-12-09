import React, { useState, useEffect, useRef } from 'react';
import SignatureCanvas from 'react-signature-canvas';
import { Link } from 'react-router-dom';
import { serviceDeliveryService } from '../services/serviceDeliveryService';
import './ServicesList.css';

function ServicesList() {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [updatingServiceId, setUpdatingServiceId] = useState(null);
  const [updateData, setUpdateData] = useState({
    status: '',
    observation: '',
    status: '',
    observation: '',
    photos: [],
  });
  const sigCanvas = useRef({});

  const clearSignature = () => {
    sigCanvas.current.clear();
  };

  const handleUpdateClick = (service) => {
    setUpdatingServiceId(service.idServiceDelivery);
    setUpdateData({
      status: service.currentStatus || '',
      observation: '',
      signature: null,
      photos: [],
    });
  };

  const handleUpdateCancel = () => {
    setUpdatingServiceId(null);
    setUpdateData({
      status: '',
      observation: '',
      signature: null,
      photos: [],
    });
  };

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
      if (updateData.status === 'DELIVERED') {
        if (!window.confirm('Warning: Setting the status to DELIVERED is a final action. You will not be able to change the status afterwards. Do you want to proceed?')) {
          return;
        }
      }

      const formData = new FormData();
      formData.append('status', updateData.status);

      if (updateData.observation) {
        formData.append('observation', updateData.observation);
      }

      if (updateData.status === 'DELIVERED') {
        if (sigCanvas.current.isEmpty()) {
          alert('Signature is required for DELIVERED status.');
          return;
        }
        const signatureBlob = await new Promise(resolve => sigCanvas.current.getCanvas().toBlob(resolve, 'image/png'));
        formData.append('signature', signatureBlob, 'signature.png');
      } else if (!['CANCELED', 'CANCELLED', 'OBSERVED', 'ASSIGNED'].includes(updateData.status)) {
        // for other statuses where signature is required
        if (sigCanvas.current.isEmpty()) {
          alert('Signature is required.');
          return;
        }
        const signatureBlob = await new Promise(resolve => sigCanvas.current.getCanvas().toBlob(resolve, 'image/png'));
        formData.append('signature', signatureBlob, 'signature.png');
      }

      updateData.photos.forEach((photo) => {
        formData.append('photos', photo);
      });

      // Validation already handled above with canvas check
      if (!['CANCELED', 'CANCELLED', 'OBSERVED', 'ASSIGNED', 'DELIVERED'].includes(updateData.status)) {
        if (updateData.photos.length === 0) {
          alert('At least one photo is required.');
          return;
        }
        if (!updateData.observation || !updateData.observation.trim()) {
          alert('Observation is required.');
          return;
        }
      }

      await serviceDeliveryService.updateStatus(updatingServiceId, formData);
      setUpdatingServiceId(null);
      loadServices();
      alert('Status updated successfully!');
    } catch (err) {
      alert(err.response?.data || 'Failed to update status');
    }
  };

  const isSignatureRequired = () => {
    return !['CANCELED', 'CANCELLED', 'OBSERVED', 'ASSIGNED'].includes(updateData.status);
  };

  const isPhotoRequired = () => {
    return !['DELIVERED', 'CANCELED', 'CANCELLED', 'OBSERVED', 'ASSIGNED'].includes(updateData.status);
  };

  const isObservationRequired = () => {
    return !['DELIVERED', 'CANCELED', 'CANCELLED', 'OBSERVED', 'ASSIGNED'].includes(updateData.status);
  };

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
      case 'ASSIGNED':
        return '#9c27b0';
      case 'PENDING':
        return '#ffc107';
      case 'IN_TRANSIT':
        return '#2196f3';
      case 'DELIVERED':
        return '#4caf50';
      case 'FAILED':
        return '#e91e63';
      case 'RETURNED':
        return '#ff9800';
      case 'CANCELLED':
      case 'CANCELED':
        return '#f44336';
      case 'OBSERVED':
        return '#795548';
      case 'RESOLVED':
        return '#009688';
      default:
        return '#9e9e9e';
    }
  };

  // Filter services by search term
  const filteredServices = services.filter((service) => {
    const term = searchTerm.toLowerCase();
    return (
      service.plate?.plateNumber?.toLowerCase().includes(term) ||
      service.messenger?.fullName?.toLowerCase().includes(term) ||
      service.dealership?.name?.toLowerCase().includes(term) ||
      service.currentStatus?.toLowerCase().includes(term)
    );
  });

  return (
    <div className="services-list">
      <div className="page-header">
        <h1>Service Deliveries</h1>
        <Link to="/dashboard/services/create" className="btn-primary">
          Create New Service
        </Link>
      </div>

      <div className="filter-section">
        <div className="filter-row">
          <input
            type="text"
            placeholder="Search by plate, messenger, dealership..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          <select value={filterStatus} onChange={(e) => setFilterStatus(e.target.value)}>
            <option value="">All Statuses</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="PENDING">Pending</option>
            <option value="DELIVERED">Delivered</option>
            <option value="FAILED">Failed</option>
            <option value="RETURNED">Returned</option>
            <option value="CANCELED">Canceled</option>
            <option value="OBSERVED">Observed</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </div>
      </div>

      {loading && <div className="loading">Loading services...</div>}
      {error && <div className="error">{error}</div>}

      {!loading && !error && (
        <div className="services-grid">
          {filteredServices.length === 0 ? (
            <div className="no-data">No services found</div>
          ) : (
            filteredServices.map((service) => (
              <div key={service.idServiceDelivery} className="service-card">
                <div className="service-header">
                  <h3 className="plate-header-small">
                    {service.plate?.plateNumber ? service.plate.plateNumber.replace(/^(.{3})(.*)$/, '$1 $2') : 'Unknown Plate'}
                  </h3>
                  <span
                    className="status-badge"
                    style={{ backgroundColor: getStatusColor(service.currentStatus) }}
                  >
                    {service.currentStatus}
                  </span>
                </div>
                <div className="service-details">

                  <p>
                    <strong>Messenger:</strong> {service.messenger?.fullName || 'N/A'}
                  </p>
                  <p>
                    <strong>Dealership:</strong> {service.dealership?.name || 'N/A'}
                  </p>
                  <p>
                    <strong>Status:</strong> {service.currentStatus || 'N/A'}
                  </p>
                </div>
                <div className="card-actions">
                  <Link to={`/dashboard/services/${service.idServiceDelivery}`} className="view-btn">
                    View Details
                  </Link>
                  {service.currentStatus !== 'DELIVERED' && (
                    <button
                      className="action-btn"
                      onClick={() => updatingServiceId === service.idServiceDelivery ? handleUpdateCancel() : handleUpdateClick(service)}
                    >
                      {updatingServiceId === service.idServiceDelivery ? 'Cancel' : 'Change Status'}
                    </button>
                  )}
                </div>

                {updatingServiceId === service.idServiceDelivery && (
                  <div className="inline-update-form">
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
                          <option value="ASSIGNED">Assigned</option>
                          <option value="PENDING">Pending</option>
                          <option value="DELIVERED">Delivered</option>
                          <option value="FAILED">Failed</option>
                          <option value="RETURNED">Returned</option>
                          <option value="CANCELED">Canceled</option>
                          <option value="OBSERVED">Observed</option>
                          <option value="RESOLVED">Resolved</option>
                        </select>
                      </div>

                      <div className="form-group">
                        <label>Observation {isObservationRequired() && '*'}</label>
                        <textarea
                          name="observation"
                          value={updateData.observation}
                          onChange={handleUpdateChange}
                          rows="2"
                          placeholder="Observation..."
                        />
                      </div>

                      {isSignatureRequired() && (
                        <div className="form-group">
                          <label>Signature *</label>
                          <div style={{ border: '1px solid #ddd', borderRadius: '4px', background: 'white' }}>
                            <SignatureCanvas
                              ref={sigCanvas}
                              penColor='black'
                              canvasProps={{ width: 300, height: 150, className: 'sigCanvas' }}
                            />
                          </div>
                          <button
                            type="button"
                            onClick={clearSignature}
                            style={{
                              marginTop: '5px',
                              padding: '5px 10px',
                              fontSize: '12px',
                              background: '#f44336',
                              color: 'white',
                              border: 'none',
                              borderRadius: '3px',
                              cursor: 'pointer'
                            }}
                          >
                            Clear Signature
                          </button>
                        </div>
                      )}

                      {isPhotoRequired() && (
                        <div className="form-group">
                          <label>Photos *</label>
                          <input
                            type="file"
                            name="photos"
                            onChange={handleFileChange}
                            accept="image/*"
                            multiple
                          />
                        </div>
                      )}

                      <button type="submit" className="btn-primary full-width">
                        Submit
                      </button>
                    </form>
                  </div>
                )}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default ServicesList;
