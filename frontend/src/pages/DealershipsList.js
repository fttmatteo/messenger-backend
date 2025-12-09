import React, { useState, useEffect } from 'react';
import { dealershipService } from '../services/dealershipService';
import './DealershipsList.css';

function DealershipsList() {
  const [dealerships, setDealerships] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newDealership, setNewDealership] = useState({
    name: '',
    address: '',
    phone: '',
  });

  useEffect(() => {
    loadDealerships();
  }, []);

  const loadDealerships = async () => {
    try {
      setLoading(true);
      const data = await dealershipService.getAll();
      setDealerships(data);
      setError('');
    } catch (err) {
      setError('Failed to load dealerships');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setNewDealership({
      ...newDealership,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await dealershipService.create(newDealership);
      setShowCreateForm(false);
      setNewDealership({ name: '', address: '', phone: '' });
      loadDealerships();
      alert('Dealership created successfully!');
    } catch (err) {
      alert(err.response?.data || 'Failed to create dealership');
    }
  };

  return (
    <div className="dealerships-list">
      <div className="page-header">
        <h1>Dealerships</h1>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="btn-primary"
        >
          {showCreateForm ? 'Cancel' : 'Create New Dealership'}
        </button>
      </div>

      {showCreateForm && (
        <div className="create-form">
          <h2>Create New Dealership</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Name *</label>
              <input
                type="text"
                name="name"
                value={newDealership.name}
                onChange={handleChange}
                required
                placeholder="Enter dealership name"
              />
            </div>

            <div className="form-group">
              <label>Address *</label>
              <input
                type="text"
                name="address"
                value={newDealership.address}
                onChange={handleChange}
                required
                placeholder="Enter address"
              />
            </div>

            <div className="form-group">
              <label>Phone *</label>
              <input
                type="tel"
                name="phone"
                value={newDealership.phone}
                onChange={handleChange}
                required
                placeholder="Enter phone number"
              />
            </div>

            <button type="submit" className="btn-primary">
              Create Dealership
            </button>
          </form>
        </div>
      )}

      {loading && <div className="loading">Loading dealerships...</div>}
      {error && <div className="error">{error}</div>}

      {!loading && !error && (
        <div className="dealerships-grid">
          {dealerships.length === 0 ? (
            <div className="no-data">No dealerships found</div>
          ) : (
            dealerships.map((dealership) => (
              <div key={dealership.id} className="dealership-card">
                <h3>{dealership.name}</h3>
                <div className="dealership-details">
                  <p>
                    <strong>Address:</strong> {dealership.address}
                  </p>
                  <p>
                    <strong>Phone:</strong> {dealership.phone}
                  </p>
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
}

export default DealershipsList;
