# Messenger System

A complete messenger/delivery service management system with backend API and frontend interface.

## Project Structure

- `/messenger` - Spring Boot backend API (Java 21)
- `/frontend` - React frontend application

## Backend (Spring Boot)

The backend is a REST API built with Spring Boot that provides:
- Authentication with JWT
- Service delivery management with plate recognition (Google Cloud Vision)
- Employee management
- Dealership management
- Image upload and processing

### Running the Backend

```bash
cd messenger
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

See `Messenger_API.postman_collection.json` for API documentation.

## Frontend (React)

The frontend is a React single-page application that provides a modern UI for managing the messenger system.

### Running the Frontend

```bash
cd frontend
npm install
npm start
```

The application will be available at `http://localhost:3000`

See `/frontend/README.md` for more details.

## Features

- 🔐 JWT-based authentication
- 📦 Service delivery tracking with status history
- 🚗 Automatic plate recognition from images
- 👥 Employee and dealership management
- 📱 Modern, responsive UI
- 🔄 Real-time status updates

## Database

The application uses H2 database for development and MySQL for production.
Seed data is provided in `seed-data.sql`.

## License

See LICENSE file for details.
