import React, { useState, useEffect } from 'react';
import { dealershipService } from '../services/dealershipService';
import './DealershipsList.css';

function DealershipsList() {
  const [dealerships, setDealerships] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [editingId, setEditingId] = useState(null);

  const [newDealership, setNewDealership] = useState({
    name: '',
    address: '',
    phone: '',
    zone: '',
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

  const handleEdit = (dealership) => {
    setNewDealership({
      name: dealership.name,
      address: dealership.address,
      phone: dealership.phone,
      zone: dealership.zone || '',
    });
    setEditingId(dealership.idDealership);
    setShowCreateForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this dealership?')) {
      try {
        await dealershipService.delete(id);
        alert('Dealership deleted successfully');
        loadDealerships();
      } catch (err) {
        alert(err.response?.data || 'Failed to delete dealership');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await dealershipService.update(editingId, newDealership);
        alert('Dealership updated successfully!');
      } else {
        await dealershipService.create(newDealership);
        alert('Dealership created successfully!');
      }
      setShowCreateForm(false);
      setEditingId(null);
      setNewDealership({ name: '', address: '', phone: '', zone: '' });
      loadDealerships();
    } catch (err) {
      alert(err.response?.data || 'Failed to save dealership');
    }
  };

  const filteredDealerships = dealerships.filter(d =>
    d.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    d.address.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="dealerships-list">
      <div className="page-header">
        <h1>Dealerships</h1>
        <div className="header-actions">
          <input
            type="text"
            placeholder="Search dealerships..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          <button
            onClick={() => {
              setShowCreateForm(!showCreateForm);
              setEditingId(null);
              setNewDealership({ name: '', address: '', phone: '', zone: '' });
            }}
            className="btn-primary"
          >
            {showCreateForm ? 'Cancel' : 'Create New Dealership'}
          </button>
        </div>
      </div>

      {showCreateForm && (
        <div className="create-form">
          <h2>{editingId ? 'Edit Dealership' : 'Create New Dealership'}</h2>
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

            <div className="form-group">
              <label>Zone </label>
              <input
                type="text"
                name="zone"
                value={newDealership.zone}
                onChange={handleChange}
                placeholder="Enter zone"
              />
            </div>

            <button type="submit" className="btn-primary">
              {editingId ? 'Update Dealership' : 'Create Dealership'}
            </button>
          </form>
        </div>
      )}

      {loading && <div className="loading">Loading dealerships...</div>}
      {error && <div className="error">{error}</div>}

      {!loading && !error && (
        <div className="dealerships-grid">
          {filteredDealerships.length === 0 ? (
            <div className="no-data">No dealerships found</div>
          ) : (
            filteredDealerships.map((dealership) => (
              <div key={dealership.idDealership} className="dealership-card">
                <h3>{dealership.name}</h3>
                <div className="dealership-details">
                  <p>
                    <strong>Address:</strong> {dealership.address}
                  </p>
                  <p>
                    <strong>Phone:</strong> {dealership.phone}
                  </p>
                  <p>
                    <strong>Zone:</strong> {dealership.zone || 'N/A'}
                  </p>
                </div>
                <div className="card-actions">
                  <button onClick={() => handleEdit(dealership)} className="btn-small edit-btn">Edit</button>
                  <button onClick={() => handleDelete(dealership.idDealership)} className="btn-small delete-btn">Delete</button>
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
