# High-Level API Design for Brokage Firm

## 1. Core Functionality & User Stories
**Create Order:** As a brokage firm employee (admin) or a customer, I want to create a new stock order.

**List Orders:** As an admin, I want to list orders for any customer, filtered by date, status, or asset. As a customer, I want to list only my own orders, with the same filters.

**Cancel Order:** As an admin or a customer, I want to cancel a *pending* order.

**List Assets:** As an admin, I want to list all assets for any customer. As a customer, I want to list my own assets.

**Match Order:** As an admin, I want to match a *pending* order.


## 2. Scope & Roles

### Actors
**Admin (Employees):** can create/list/cancel/match orders for any customer; manage customers & assets.

**Customer:** can only access/manipulate their own data after login.

**Order lifecycle:** PENDING → MATCHED | CANCELED
- Creation always starts as PENDING.
- “Delete” means cancel (soft-delete) and keep full audit.

## 3. Database Schema 

#### Customer Entity:
Table: customers

Entity: Customer
- id: (VARCHAR, unique UUID)
- firstName: (VARCHAR)
- lastName: (VARCHAR)
- fullName (VARCHAR)
- username (VARCHAR, Unique, Indexed) 
- password (VARCHAR, Hashed - e.g., BCrypt)
- role (VARCHAR) - Values: ROLE_CUSTOMER, ROLE_ADMIN 
- createDate (TIMESTAMP)

#### Asset Entity: (per-customer holding)
Table: assets

Entity: Asset
- id: (BIGINT, Primary Key, Auto-Generated)
- customerId: (Foreign Key to customers.id)
- assetName: (VARCHAR) - e.g., "AAPL", "GOOGL"
- size: (DECIMAL) – Total number of shares owned.
- usableSize: (DECIMAL) – available shares not reserved by open SELL orders (Shares not tied up in pending SELL orders (size - size_in_pending_sell_orders).)

- version (optimistic lock)
- Constraint: Unique constraint on (customerId, assetName)
- Check: size >= 0, usableSize >= 0, usableSize <= size

#### Order Entity:
Table: orders

Entity: Order
- id: (BIGINT, Primary Key, Auto-Generated)
- customerId: (UUID, Foreign Key to Customer.id)
- assetName: (VARCHAR)
- orderSide: (ENUM) - Values: BUY, SELL
- size: (BIGINT > 0) - Number of shares in the order.
- price: (DECIMAL(18,6) > 0) - Price per share for the order.
- status: (ENUM) - Values: PENDING, MATCHED, CANCELED
- createDate: (TIMESTAMP)
- updateDate: (TIMESTAMP, nullable)

```
// Future Considerations:
- idempotencyKey (nullable, unique per customer) – to avoid dup creates
- metadata (JSONB) – optional notes, external broker refs
- Indexes: (customerId, createDate desc), (customerId, status), (assetName, status)
```

## 4. RESTful API Endpoints
All endpoints will be versioned under /api/v1/.

### a. Authentication Endpoints

#### Login
Basic authentication with username and password.

#### //TODO: Future Implementation:
Endpoint: POST /api/v1/auth/login

Description: Authenticates a user (customer or admin) and returns a JWT.

Request Body: LoginRequestDTO { username, password }

Response Body: LoginResponseDTO { jwtToken, username, role }

Authorization: Public.

### b. Customer-Facing Endpoints
These endpoints are for logged-in customers (ROLE_CUSTOMER). The customerId is always inferred from the JWT token, not passed as a parameter.

#### Order Creation

Endpoint: POST /api/v1/orders

Description: Creates a new stock order with PENDING status for the authenticated customer.

``` DELETE
**Authorization:**

- Admins: Must have ROLE_ADMIN. Can create an order for any customerId in the request body.

- Customers: Must have ROLE_CUSTOMER. The customerId in the request body must match the customerId associated with their username in the *Customer* table.
```

Request Body: application/json

- customerId: (string, required) The unique ID of the customer.
- assetName: (string, required) The name of the stock.
- orderSide: (string, required, enum: "BUY", "SELL") The side of the order.
- size: (integer, required, > 0) The number of shares.
- price: (number, required, > 0) The price per share.

Response: 201 Created with the new order ID.

**Error Responses:**
- 400 Bad Request: Invalid input (e.g., missing fields, negative size/price).
- 401 Unauthorized.
- 403 unauthorized (wrong customer).
- 404 Not Found: Customer not found (pre-check required).
- 500 Internal Server Error: Server-side issue.

#### List Orders

Endpoint: GET /api/v1/orders

Description: Lists orders based on query parameters.

**Query Parameters:**

- customerId: (string, optional) Filters orders by customer ID.
- dateRange: (timestamp) Filters orders by dateRange.
- status: (string, optional, enum: "PENDING", "MATCHED", "CANCELED") Filters orders by status.

**Response: 200 OK**

- list: (array of objects) A list of order objects.
    - id: (string) Order ID.
    - customerId: (string) Customer ID.
    - assetName: (string) Asset name.
    - orderSide: (string) "BUY" or "SELL".
    - size: (integer) Number of shares.
    - price: (number) Price per share.
    - status: (string) "PENDING", "MATCHED", or "CANCELED".
    - createDate: (string) Timestamp of creation.


#### Delete Order

### c. Admin-Facing Endpoints (for Employees)

## 5. Authentication & Authorization with JWTs


> Note: Security: JWT will be implemented. UserCredentials must be seperated from Customer table.