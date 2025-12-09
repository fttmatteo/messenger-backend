import React, { useState, useEffect } from 'react';
import { employeeService } from '../services/employeeService';
import './EmployeesList.css';

function EmployeesList() {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreateForm, setShowCreateForm] = useState(false);
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await employeeService.create(newEmployee);
      setShowCreateForm(false);
      setNewEmployee({ fullName: '', document: '', phone: '', userName: '', password: '', role: 'MESSENGER' });
      loadEmployees();
      alert('Employee created successfully!');
    } catch (err) {
      alert(err.response?.data || 'Failed to create employee');
    }
  };

  return (
    <div className="employees-list">
      <div className="page-header">
        <h1>Employees</h1>
        <button
          onClick={() => setShowCreateForm(!showCreateForm)}
          className="btn-primary"
        >
          {showCreateForm ? 'Cancel' : 'Create New Employee'}
        </button>
      </div>

      {showCreateForm && (
        <div className="create-form">
          <h2>Create New Employee</h2>
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
              <label>Password *</label>
              <input
                type="password"
                name="password"
                value={newEmployee.password}
                onChange={handleChange}
                required
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
              Create Employee
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
              </tr>
            </thead>
            <tbody>
              {employees.length === 0 ? (
                <tr>
                  <td colSpan="4" className="no-data">
                    No employees found
                  </td>
                </tr>
              ) : (
                employees.map((employee) => (
                  <tr key={employee.idEmployee}>
                    <td>{employee.idEmployee}</td>
                    <td>{employee.fullName}</td>
                    <td>{employee.document}</td>
                    <td>
                      <span className={`role-badge ${employee.role.toLowerCase()}`}>
                        {employee.role}
                      </span>
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
