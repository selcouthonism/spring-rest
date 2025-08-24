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

**Customer:** can only access/manipulate their own data.

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

Request Body: CreateOrderDTO

- customerId: (string, required) The unique ID of the customer.
- assetName: (string, required) The name of the stock.
- orderSide: (string, required, enum: "BUY", "SELL") The side of the order.
- size: (integer, required, > 0) The number of shares.
- price: (number, required, > 0) The price per share.

Response Body: OrderDTO

status Codes: 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden.

#### List Orders

Endpoint: GET /api/v1/orders

Description: Retrieves a list of all orders for the authenticated customer.

Query Params: ?customerId=1&from=2025-01-01T00:00:44Z&to=2025-08-30T13:51:51Z&orderStatus=CANCELED (optional filtering).

Response Body: List<OrderDTO>

Status Codes: 200 OK, 401 Unauthorized.

#### Delete Order
Endpoint: DELETE /api/v1/orders/{orderId}

Description: Cancels a PENDING order belonging to the authenticated customer.

Response Body: 204 No Content or OrderDTO of the canceled order.

Status Codes: 204 No Content, 401 Unauthorized, 403 Forbidden (if order doesn't belong to user), 404 Not Found, 409 Conflict (if order is not in PENDING state).

#### Get My Asset

Endpoint: GET /api/v1/assets

Description: Retrieves the asset for the authenticated customer.

Response Body: List<AssetDTO>

Status Codes: 200 OK, 401 Unauthorized.

### c. Admin-Facing Endpoints (for Employees)
These endpoints require ROLE_ADMIN and allow employees to manage any customer's data.

#### Create Order for a Customer
Endpoint: POST /api/v1/admin/orders

Description: Creates a new stock order for a specific customer.

Request Body: CreateOrderDTO { customerId, assetName, orderSide, size, price }

Response Body: OrderDTO

Status Codes: 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden.

#### List Orders for a Specific Customer

Endpoint: GET /api/v1/admin/orders

Description: Retrieves all orders.

Query Params: ?customerId=1&from=2025-01-01T00:00:44Z&to=2025-08-30T13:51:51Z&orderStatus=CANCELED (optional filtering).

Response Body: List<OrderDTO>

Status Codes: 200 OK, 401 Unauthorized.

#### Cancel Any Order

Endpoint: DELETE /api/v1/admin/orders/{orderId}

Description: Cancels any PENDING order in the system.

Response Body: 204 No Content

Status Codes: 204 No Content, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict.

#### Match Any Order

Endpoint: GET /api/v1/admin/orders/{orderId}/match

Description: Matches any PENDING order in the system.

Response Body: OrderDTO

Status Codes: Status Codes: 200 OK, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict.

## 5. Authentication & Authorization with JWTs
> Note:
- Security: JWT will be implemented. 
- UserCredentials must be seperated from Customer table.