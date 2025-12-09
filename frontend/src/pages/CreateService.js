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
  const [captureMode, setCaptureMode] = useState('camera'); // 'file' or 'camera'
  const [userRole, setUserRole] = useState('');
  const [stream, setStream] = useState(null);
  const navigate = useNavigate();
  const videoRef = React.useRef(null);
  const canvasRef = React.useRef(null);

  useEffect(() => {
    setUserRole(localStorage.getItem('role'));
  }, []);

  useEffect(() => {
    loadData();
    startCamera(); // Start camera on mount since default mode is 'camera'
    // eslint-disable-next-line react-hooks/exhaustive-deps
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

  const startCamera = async () => {
    try {
      const mediaStream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment' }, // Use back camera on mobile
        audio: false,
      });
      setStream(mediaStream);
      if (videoRef.current) {
        videoRef.current.srcObject = mediaStream;
      }
    } catch (err) {
      setError('Failed to access camera. Please check permissions.');
      console.error(err);
    }
  };

  const stopCamera = () => {
    if (stream) {
      stream.getTracks().forEach((track) => track.stop());
      setStream(null);
    }
  };

  const capturePhoto = () => {
    if (videoRef.current && canvasRef.current) {
      const video = videoRef.current;
      const canvas = canvasRef.current;
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const ctx = canvas.getContext('2d');
      ctx.drawImage(video, 0, 0);

      canvas.toBlob((blob) => {
        const file = new File([blob], 'camera-capture.jpg', { type: 'image/jpeg' });
        setFormData({
          ...formData,
          image: file,
        });
        setImagePreview(canvas.toDataURL('image/jpeg'));
        stopCamera();
      }, 'image/jpeg');
    }
  };

  const toggleCaptureMode = (mode) => {
    setCaptureMode(mode);
    if (mode === 'camera') {
      startCamera();
    } else {
      stopCamera();
    }
    // Clear previous image
    setFormData({ ...formData, image: null });
    setImagePreview(null);
  };

  // Cleanup camera on unmount
  useEffect(() => {
    return () => {
      stopCamera();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

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
                <option key={dealership.idDealership} value={dealership.idDealership}>
                  {dealership.name}
                </option>
              ))}
            </select>
          </div>

          {userRole === 'ADMIN' && (
            <div className="form-group">
              <label htmlFor="messengerDocument">Messenger *</label>
              <select
                id="messengerDocument"
                name="messengerDocument"
                value={formData.messengerDocument}
                onChange={handleChange}
                required={userRole === 'ADMIN'}
              >
                <option value="">Select a messenger</option>
                {employees
                  .filter((employee) => employee.role === 'MESSENGER')
                  .map((employee) => (
                    <option key={employee.idEmployee} value={employee.document}>
                      {employee.fullName}
                    </option>
                  ))}
              </select>
            </div>
          )}

          <div className="form-group">
            <label>Plate Image *</label>

            <div className="capture-mode-toggle">
              <button
                type="button"
                className={`toggle-btn ${captureMode === 'file' ? 'active' : ''}`}
                onClick={() => toggleCaptureMode('file')}
              >
                📁 Upload File
              </button>
              <button
                type="button"
                className={`toggle-btn ${captureMode === 'camera' ? 'active' : ''}`}
                onClick={() => toggleCaptureMode('camera')}
              >
                📷 Take Photo
              </button>
            </div>

            {captureMode === 'file' ? (
              <input
                type="file"
                id="image"
                name="image"
                onChange={handleImageChange}
                accept="image/*"
                required={!formData.image}
              />
            ) : (
              <div className="camera-container">
                {stream && (
                  <>
                    <video
                      ref={videoRef}
                      autoPlay
                      playsInline
                      className="camera-preview"
                    />
                    <button
                      type="button"
                      onClick={capturePhoto}
                      className="capture-btn"
                    >
                      📸 Capture Photo
                    </button>
                  </>
                )}
                <canvas ref={canvasRef} style={{ display: 'none' }} />
              </div>
            )}

            {imagePreview && (
              <div className="image-preview">
                <img src={imagePreview} alt="Plate preview" />
                <button
                  type="button"
                  onClick={() => {
                    setImagePreview(null);
                    setFormData({ ...formData, image: null });
                    if (captureMode === 'camera') {
                      startCamera();
                    }
                  }}
                  className="retake-btn"
                >
                  🔄 Retake
                </button>
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
