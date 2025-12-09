import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { serviceDeliveryService } from '../services/serviceDeliveryService';
import { dealershipService } from '../services/dealershipService';
import { employeeService } from '../services/employeeService';
import './CreateService.css';

function CreateService() {
  const [formData, setFormData] = useState({
    dealershipId: '',
    messengerDocument: '',
    manualPlateNumber: '',
    image: null,
  });
  const [dealerships, setDealerships] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [imagePreview, setImagePreview] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [dealershipsData, employeesData] = await Promise.all([
        dealershipService.getAll(),
        employeeService.getAll(),
      ]);
      setDealerships(dealershipsData);
      setEmployees(employeesData);
    } catch (err) {
      setError('Failed to load data');
      console.error(err);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setFormData({
        ...formData,
        image: file,
      });
      
      // Create preview
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      const data = new FormData();
      data.append('image', formData.image);
      data.append('dealershipId', formData.dealershipId);
      data.append('messengerDocument', formData.messengerDocument);
      if (formData.manualPlateNumber) {
        data.append('manualPlateNumber', formData.manualPlateNumber);
      }

      await serviceDeliveryService.createService(data);
      setSuccess('Service created successfully!');
      setTimeout(() => {
        navigate('/dashboard/services');
      }, 2000);
    } catch (err) {
      setError(err.response?.data || 'Failed to create service');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="create-service">
      <div className="page-header">
        <h1>Create New Service</h1>
      </div>

      <div className="form-container">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label htmlFor="dealershipId">Dealership *</label>
            <select
              id="dealershipId"
              name="dealershipId"
              value={formData.dealershipId}
              onChange={handleChange}
              required
            >
              <option value="">Select a dealership</option>
              {dealerships.map((dealership) => (
                <option key={dealership.id} value={dealership.id}>
                  {dealership.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="messengerDocument">Messenger *</label>
            <select
              id="messengerDocument"
              name="messengerDocument"
              value={formData.messengerDocument}
              onChange={handleChange}
              required
            >
              <option value="">Select a messenger</option>
              {employees.map((employee) => (
                <option key={employee.id} value={employee.document}>
                  {employee.name} - {employee.document}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="image">Plate Image *</label>
            <input
              type="file"
              id="image"
              name="image"
              onChange={handleImageChange}
              accept="image/*"
              required
            />
            {imagePreview && (
              <div className="image-preview">
                <img src={imagePreview} alt="Plate preview" />
              </div>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="manualPlateNumber">Manual Plate Number (Optional)</label>
            <input
              type="text"
              id="manualPlateNumber"
              name="manualPlateNumber"
              value={formData.manualPlateNumber}
              onChange={handleChange}
              placeholder="Enter plate number manually if needed"
            />
            <small>Only fill this if automatic recognition fails</small>
          </div>

          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <div className="form-actions">
            <button type="button" onClick={() => navigate('/dashboard/services')} className="btn-secondary">
              Cancel
            </button>
            <button type="submit" disabled={loading} className="btn-primary">
              {loading ? 'Creating...' : 'Create Service'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreateService;
