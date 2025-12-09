# Messenger System - Frontend

This is the frontend application for the Messenger System, built with React.

## Features

- **Authentication**: Login system with JWT token-based authentication
- **Service Deliveries Management**: Create, view, and update service deliveries with plate image recognition
- **Employee Management**: View and create employees (messengers and admins)
- **Dealership Management**: View and create dealerships
- **Status Tracking**: Track service delivery status with history

## Prerequisites

- Node.js (v14 or higher)
- npm or yarn
- Backend API running (default: http://localhost:8080)

## Installation

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure the API URL (optional):
   - Copy `.env` file and adjust `REACT_APP_API_URL` if needed
   - Default is `http://localhost:8080`

## Running the Application

Start the development server:

```bash
npm start
```

The application will open at [http://localhost:3000](http://localhost:3000)

## Building for Production

Build the app for production:

```bash
npm run build
```

This creates an optimized production build in the `build` folder.

## Project Structure

```
frontend/
├── public/              # Static files
├── src/
│   ├── components/      # Reusable components
│   ├── pages/          # Page components
│   ├── services/       # API service layer
│   ├── App.js          # Main app component with routing
│   └── index.js        # Entry point
└── package.json
```

## Available Pages

- `/login` - Login page
- `/dashboard/services` - List all service deliveries
- `/dashboard/services/create` - Create new service delivery
- `/dashboard/services/:id` - View and update service details
- `/dashboard/employees` - Employee management
- `/dashboard/dealerships` - Dealership management

## Technologies Used

- React 18
- React Router v6
- Axios for HTTP requests
- CSS for styling

## Default Login Credentials

Use the credentials configured in your backend seed data.

