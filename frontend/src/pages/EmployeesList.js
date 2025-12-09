import React, { useState, useEffect } from 'react';
import { employeeService } from '../services/employeeService';
import './EmployeesList.css';

function EmployeesList() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [editingId, setEditingId] = useState(null);

  const [newEmployee, setNewEmployee] = useState({
    fullName: '',
    document: '',
    phone: '',
    userName: '',
    password: '',
    role: 'MESSENGER',
  });

  useEffect(() => {
    loadEmployees();
  }, []);

  const loadEmployees = async () => {
    try {
      setLoading(true);
      const data = await employeeService.getAll();
      setEmployees(data);
      setError('');
    } catch (err) {
      setError('Failed to load employees');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setNewEmployee({
      ...newEmployee,
      [e.target.name]: e.target.value,
    });
  };

  const handleEdit = (employee) => {
    setNewEmployee({
      fullName: employee.fullName,
      document: employee.document,
      phone: employee.phone || '',
      userName: employee.userName || '',
      password: '', // Password usually blank on edit unless changing
      role: employee.role,
    });
    setEditingId(employee.idEmployee);
    setShowCreateForm(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this employee?')) {
      try {
        await employeeService.delete(id);
        alert('Employee deleted successfully');
        loadEmployees();
      } catch (err) {
        alert(err.response?.data || 'Failed to delete employee');
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await employeeService.update(editingId, newEmployee);
        alert('Employee updated successfully!');
      } else {
        await employeeService.create(newEmployee);
        alert('Employee created successfully!');
      }
      setShowCreateForm(false);
      setEditingId(null);
      setNewEmployee({ fullName: '', document: '', phone: '', userName: '', password: '', role: 'MESSENGER' });
      loadEmployees();
    } catch (err) {
      alert(err.response?.data || 'Failed to save employee');
    }
  };

  const filteredEmployees = employees.filter((employee) => {
    const term = searchTerm.toLowerCase();
    return (
      employee.fullName?.toLowerCase().includes(term) ||
      employee.document?.toString().includes(term) ||
      employee.userName?.toLowerCase().includes(term)
    );
  });

  return (
    <div className="employees-list">
      <div className="page-header">
        <h1>Employees</h1>
        <div className="header-actions">
          <input
            type="text"
            placeholder="Search employees..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          <button
            onClick={() => {
              setShowCreateForm(!showCreateForm);
              setEditingId(null);
              setNewEmployee({ fullName: '', document: '', phone: '', userName: '', password: '', role: 'MESSENGER' });
            }}
            className="btn-primary"
          >
            {showCreateForm ? 'Cancel' : 'Create New Employee'}
          </button>
        </div>
      </div>

      {showCreateForm && (
        <div className="create-form">
          <h2>{editingId ? 'Edit Employee' : 'Create New Employee'}</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Full Name *</label>
              <input
                type="text"
                name="fullName"
                value={newEmployee.fullName}
                onChange={handleChange}
                required
                placeholder="Enter full name"
              />
            </div>

            <div className="form-group">
              <label>Document *</label>
              <input
                type="text"
                name="document"
                value={newEmployee.document}
                onChange={handleChange}
                required
                placeholder="Enter document number"
              />
            </div>

            <div className="form-group">
              <label>User Name *</label>
              <input
                type="text"
                name="userName"
                value={newEmployee.userName}
                onChange={handleChange}
                required
                placeholder="Enter username for login"
              />
            </div>

            <div className="form-group">
              <label>Phone</label>
              <input
                type="text"
                name="phone"
                value={newEmployee.phone}
                onChange={handleChange}
                placeholder="Enter phone number"
              />
            </div>

            <div className="form-group">
              <label>Password {editingId && '(Leave blank to keep current)'}</label>
              <input
                type="password"
                name="password"
                value={newEmployee.password}
                onChange={handleChange}
                required={!editingId}
                placeholder="Enter password"
              />
            </div>

            <div className="form-group">
              <label>Role *</label>
              <select name="role" value={newEmployee.role} onChange={handleChange} required>
                <option value="MESSENGER">Messenger</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>

            <button type="submit" className="btn-primary">
              {editingId ? 'Update Employee' : 'Create Employee'}
            </button>
          </form>
        </div>
      )}

      {loading && <div className="loading">Loading employees...</div>}
      {error && <div className="error">{error}</div>}

      {!loading && !error && (
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Document</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredEmployees.length === 0 ? (
                <tr>
                  <td colSpan="5" className="no-data">
                    No employees found
                  </td>
                </tr>
              ) : (
                filteredEmployees.map((employee) => (
                  <tr key={employee.idEmployee}>
                    <td>{employee.idEmployee}</td>
                    <td>{employee.fullName}</td>
                    <td>{employee.document}</td>
                    <td>
                      <span className={`role-badge ${employee.role.toLowerCase()}`}>
                        {employee.role}
                      </span>
                    </td>
                    <td>
                      <button onClick={() => handleEdit(employee)} className="btn-small edit-btn">Edit</button>
                      <button onClick={() => handleDelete(employee.idEmployee)} className="btn-small delete-btn">Delete</button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export default EmployeesList;
