
***

# Store API Backend

A high-performance, scalable, and secure Spring Boot REST API for managing a comprehensive e-commerce platform. Built with a strong emphasis on advanced Java development practices, this application leverages the Spring Boot ecosystem, Spring Data JPA, and robust JUnit integration testing to deliver a reliable backend experience.

## Features
* **Role-Based Access Control (RBAC):** Secure JWT-based authentication distinguishing between `ADMIN` and `CUSTOMER` roles.
* **Complete E-Commerce Flow:** Manage everything from product catalogs and inventory thresholds to secure checkouts and order histories.
* **Automated Notifications:** Event-driven async notifications for low-stock alerts and checkout confirmations.
* **Resilient Inventory Management:** Atomic stock reductions with safeguards against overselling and price-spoofing.
* **Swagger UI Integration:** Auto-generated API documentation available at `/swagger-ui.html`.

## Tech Stack
* **Core:** Java, Spring Boot 3.x
* **Security:** Spring Security, JSON Web Tokens (JWT)
* **Data Persistence:** Spring Data JPA, Hibernate, MySQL
* **Utilities:** Lombok, Maven
* **Testing:** JUnit 5, MockMvc

---

## Getting Started

### Prerequisites
* Java 17 or higher
* MySQL Server (running on default port 3306)
* Maven

### Local Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Azaken1248/storeapp.git
   cd storeapp
   ```

2. **Configure the Database:**
   Ensure your local MySQL instance matches the credentials in `src/main/resources/application.properties` or update them accordingly:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/productapi_db?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=YourPasswordHere
   ```

3. **Build and Run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   The server will start on `http://localhost:8686`. 

*(Note: The database is pre-seeded with an admin account `superadmin@store.com` / `dummy_password!` upon initialization).*

---

## Frontend Integration Guide

This API is designed to be easily consumable by any frontend framework (React, Vue, Angular). 

### 1. CORS Configuration
The backend is globally configured to allow cross-origin requests. To ensure session and token integrity, your frontend HTTP client (e.g., Axios or Fetch) **must** be configured to send credentials. 

**Axios Example:**
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8686/api',
  withCredentials: true // Crucial for CORS
});
```

### 2. Authentication Flow (JWT)
Upon successful login, the API returns a JWT. You must attach this token to the `Authorization` header of all subsequent protected requests.

**Login Request:**
```javascript
const login = async (email, password) => {
  const response = await api.post('/auth/login', { email, password });
  const { token, id, role } = response.data;
  
  // Store token securely (e.g., localStorage or in-memory)
  localStorage.setItem('jwt_token', token);
  return response.data;
};
```

**Authenticated Request Interceptor:**
```javascript
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### 3. Error Handling
The API utilizes a global exception handler. All errors follow a predictable JSON structure, making frontend error-mapping straightforward:
```json
{
  "timestamp": "2026-04-19T21:03:00.000",
  "status": 409,
  "error": "Conflict",
  "message": "Insufficient stock. Only 3 left for Product ID: 5"
}
```

---

## API Endpoints Reference

### Authentication (`/api/auth`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/login` | Authenticate user and receive JWT | Public |

### Users (`/api/users`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/` | Register a new user | Public |
| `GET` | `/` | Retrieve all users | `ADMIN` |
| `GET` | `/{id}` | Get specific user details | `ADMIN` or Self |
| `PUT` | `/{id}` | Update user details | `ADMIN` or Self |
| `DELETE` | `/{id}` | Delete a user | `ADMIN` |

### Products (`/api/products`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/` | Create a new product | `ADMIN` |
| `GET` | `/` | Get all products (query `?includeInactive=true` for admins) | Public |
| `GET` | `/{id}` | Get product by ID | Public |
| `PUT` | `/{id}` | Update product details | `ADMIN` |
| `DELETE` | `/{id}` | Delete a product | `ADMIN` |

### Checkout & Orders (`/api/checkout` & `/api/orders`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/checkout` | Process checkout and create order | `CUSTOMER` |
| `GET` | `/api/orders` | Get all platform orders | `ADMIN` |
| `GET` | `/api/orders/{id}` | Get order details | `ADMIN` or Self |
| `GET` | `/api/orders/user/{userId}`| Get order history for a user | `ADMIN` or Self |
| `GET` | `/api/orders/{orderId}/items`| Get line items for a specific order | Public |

### Inventory (`/api/inventory`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/` | Initialize inventory for a product | `ADMIN` |
| `GET` | `/` | Get all inventory records | `ADMIN` |
| `GET` | `/{productId}` | Get inventory count for specific product | `ADMIN` |
| `PUT` | `/{productId}` | Update stock quantity | `ADMIN` |
| `GET` | `/low-stock` | Get items below their threshold | `ADMIN` |

### Notifications (`/api/notifications`)
| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/` | Manually dispatch a notification | `ADMIN` |
| `GET` | `/` | Get all global notifications | `ADMIN` |
| `GET` | `/user/{userId}` | Get notifications for a specific user | `ADMIN` or Self |
| `PUT` | `/{id}/read` | Mark a notification as read | `ADMIN` or Self |

---

## Credits & Contributors

This backend ecosystem was architected and developed by:

* **Rohit Sinha** — Product Module, Authentication, System Integration, Integration Testing, & Deployment
* **Pramith L** — User Module
* **Srujan BJ** — Notification Module
* **Pranav Yugan** — Orders & Order Items Module
* **Sreeram Venugopal** — Inventory Module
