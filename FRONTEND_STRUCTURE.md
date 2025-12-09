# Frontend Application Structure

## Directory Layout

```
frontend/
├── public/
│   ├── index.html          # Main HTML file
│   ├── favicon.ico         # App icon
│   └── manifest.json       # PWA manifest
│
├── src/
│   ├── components/         # Reusable components
│   │   └── PrivateRoute.js # Authentication guard for protected routes
│   │
│   ├── pages/              # Page components
│   │   ├── Login.js        # Login page
│   │   ├── Login.css
│   │   ├── Dashboard.js    # Main dashboard layout
│   │   ├── Dashboard.css
│   │   ├── ServicesList.js # List all services
│   │   ├── ServicesList.css
│   │   ├── CreateService.js # Create new service
│   │   ├── CreateService.css
│   │   ├── ServiceDetail.js # View/update service
│   │   ├── ServiceDetail.css
│   │   ├── EmployeesList.js # Employee management
│   │   ├── EmployeesList.css
│   │   ├── DealershipsList.js # Dealership management
│   │   └── DealershipsList.css
│   │
│   ├── services/           # API services
│   │   ├── api.js          # Base Axios configuration
│   │   ├── authService.js  # Authentication API
│   │   ├── serviceDeliveryService.js # Service delivery API
│   │   ├── employeeService.js # Employee API
│   │   └── dealershipService.js # Dealership API
│   │
│   ├── App.js              # Main app with routing
│   ├── App.css             # Global app styles
│   ├── index.js            # Entry point
│   └── index.css           # Global styles
│
├── .env                    # Environment variables
├── package.json            # Dependencies
└── README.md              # Documentation
```

## Component Hierarchy

```
App
├── Router
    ├── /login
    │   └── Login
    │
    └── /dashboard (PrivateRoute)
        └── Dashboard
            ├── /dashboard/services
            │   └── ServicesList
            │
            ├── /dashboard/services/create
            │   └── CreateService
            │
            ├── /dashboard/services/:id
            │   └── ServiceDetail
            │
            ├── /dashboard/employees
            │   └── EmployeesList
            │
            └── /dashboard/dealerships
                └── DealershipsList
```

## Routes

| Path | Component | Description | Protected |
|------|-----------|-------------|-----------|
| `/` | Redirect to `/login` | Root path | No |
| `/login` | Login | User authentication | No |
| `/dashboard` | Dashboard | Main layout | Yes |
| `/dashboard/services` | ServicesList | List all services | Yes |
| `/dashboard/services/create` | CreateService | Create new service | Yes |
| `/dashboard/services/:id` | ServiceDetail | View/update service | Yes |
| `/dashboard/employees` | EmployeesList | Manage employees | Yes |
| `/dashboard/dealerships` | DealershipsList | Manage dealerships | Yes |

## Services Layer

### api.js
- Base Axios instance configuration
- Request interceptor (adds JWT token)
- Response interceptor (handles 401 errors)

### authService.js
- `login(credentials)` - Authenticate user
- `logout()` - Clear authentication
- `isAuthenticated()` - Check auth status

### serviceDeliveryService.js
- `createService(formData)` - Create service with image
- `updateStatus(id, formData)` - Update service status
- `getById(id)` - Get service details
- `getAll()` - Get all services
- `getByMessenger(messengerId)` - Filter by messenger
- `getByDealership(dealershipId)` - Filter by dealership
- `getByStatus(status)` - Filter by status

### employeeService.js
- `create(employee)` - Create new employee
- `getAll()` - Get all employees
- `getById(id)` - Get employee details

### dealershipService.js
- `create(dealership)` - Create new dealership
- `getAll()` - Get all dealerships
- `getById(id)` - Get dealership details

## State Management

The application uses React's built-in state management:
- `useState` for component-level state
- `useEffect` for side effects and data fetching
- Local Storage for JWT token persistence

## Styling

- CSS modules per component
- Consistent color scheme (purple gradient)
- Responsive design
- Modern card-based layouts

### Color Palette
- Primary: `#667eea` to `#764ba2` (gradient)
- Background: `#f5f6fa`
- Success: `#4caf50`
- Warning: `#ffc107`
- Error: `#f44336`
- Info: `#2196f3`

## Features by Page

### Login
- Document number input
- Password input
- Error handling
- Auto-redirect on success

### Dashboard
- Sidebar navigation
- Logout functionality
- Nested routing outlet

### Services List
- Grid view of services
- Status filtering
- Color-coded status badges
- Quick navigation to details

### Create Service
- Dealership selection
- Messenger selection
- Image upload with preview
- Manual plate number fallback
- Form validation

### Service Detail
- Full service information
- Status history timeline
- Update status form
- File uploads (signature, photos)

### Employees List
- Table view
- Create employee form
- Role badges
- Inline creation

### Dealerships List
- Card grid view
- Create dealership form
- Inline creation
- Contact information display

## Authentication Flow

1. User enters credentials on `/login`
2. Frontend calls `POST /auth/login`
3. Backend returns JWT token
4. Token stored in localStorage
5. Token added to all subsequent requests via interceptor
6. On 401 response, user redirected to `/login`

## Data Flow

```
Component → Service → API (Axios) → Backend
                ↓
            localStorage (token)
                ↓
        Axios Interceptor (adds token to headers)
```

## Error Handling

- Network errors caught in service layer
- User-friendly error messages
- Automatic token expiry handling
- Form validation on submit
