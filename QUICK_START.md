# Quick Start Guide

This guide will help you get the Messenger System up and running with both backend and frontend.

## Prerequisites

- Java 21
- Maven
- Node.js (v14 or higher)
- MySQL (optional, uses H2 by default)

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/fttmatteo/messenger-backend.git
cd messenger-backend
```

### 2. Start the Backend

```bash
cd messenger
./mvnw spring-boot:run
```

The backend API will be available at: `http://localhost:8080`

### 3. Start the Frontend (in a new terminal)

```bash
cd frontend
npm install
npm start
```

The frontend will be available at: `http://localhost:3000`

## Default Configuration

### Backend
- **Port:** 8080
- **Database:** H2 (in-memory) or MySQL
- **Seed Data:** Available in `seed-data.sql`

### Frontend
- **Port:** 3000
- **API URL:** http://localhost:8080 (configurable in `.env`)

## Features Overview

### Authentication
- Login with document number and password
- JWT token-based authentication
- Automatic token refresh

### Service Deliveries
- Create new service with plate image upload
- Automatic plate recognition using Google Cloud Vision
- Manual plate number fallback
- Track service status (Pending, In Transit, Delivered, Cancelled)
- Update status with signature and photos
- View complete status history

### Employee Management
- Create employees (Messengers and Admins)
- View all employees
- Assign employees to services

### Dealership Management
- Create dealerships
- View all dealerships
- Assign dealerships to services

## API Documentation

Import the Postman collection `Messenger_API.postman_collection.json` for complete API documentation and testing.

## Troubleshooting

### Backend Issues

**Port already in use:**
```bash
# Change the port in messenger/src/main/resources/application.properties
server.port=8081
```

**Database connection issues:**
- The application uses H2 by default (no setup required)
- For MySQL, update `application.properties` with your credentials

### Frontend Issues

**Port 3000 already in use:**
```bash
# The app will prompt to use another port (3001)
# Or set PORT environment variable
PORT=3001 npm start
```

**API connection issues:**
- Verify backend is running on port 8080
- Check `.env` file in frontend directory
- Update `REACT_APP_API_URL` if needed

**CORS errors:**
- Backend has CORS configured for development
- If issues persist, check SecurityConfig in the backend

## Production Deployment

### Backend
```bash
cd messenger
./mvnw clean package
java -jar target/messenger-0.0.1-SNAPSHOT.jar
```

### Frontend
```bash
cd frontend
npm run build
# Deploy the 'build' folder to your hosting service
```

## Technology Stack

### Backend
- Spring Boot 4.0.0
- Java 21
- Spring Data JPA
- Spring Security
- JWT Authentication
- Google Cloud Vision API
- H2/MySQL Database

### Frontend
- React 18
- React Router v6
- Axios
- Modern CSS

## Support

For issues or questions, please open an issue on GitHub.
