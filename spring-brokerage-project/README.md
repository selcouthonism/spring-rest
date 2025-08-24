# Project Overview
Project Name: Stock Orders Management System

## Purpose:
This project implements a backend system for a brokerage platform, allowing customers to manage stock orders. It provides endpoints for creating, listing, retrieving, and canceling orders while ensuring proper security, business validation, and concurrency control.

### Project Structure
src/main/java <br>
 ├─ assembler       : Converts between domain models and DTOs for cleaner API design. <br>
 ├─ controller      : REST controllers (OrderController, etc.) <br>
 ├─ dto             : Data transfer objects (OrderDTO, CreateOrderDTO, AssetDTO) <br>
 ├─ exceptions      : Implements centralized exception management with custom error responses. <br>
 ├─ mapper          : MapStruct mapper for entity<->DTO conversion <br>
 ├─ model           : JPA entities (Order, Customer, Asset) <br>
 ├─ repository      : Spring Data JPA repositories <br>
 ├─ service         : Service layer (OrderService & OrderServiceImpl) <br>
 └─ security        : Security classes (CustomUserDetails) <br>
 └─ utility         : EnumValidator


### Test Structure:
src/test/java <br>
 ├─ controller      : Integration tests for controllers (OrderControllerIT) <br>
 ├─ service         : Unit tests for service layer (OrderServiceImplTest) <br>
 └─ repository      : Repository tests using @DataJpaTest

## Build and Run

Clean and compile: 
```
mvn clean install
```

If you want to skip tests temporarily: 
```
mvn clean install -DskipTests
```

Run the Spring Boot application with Maven:
```
mvn spring-boot:run
```

## Test Endpoints using curl:
The following customer, asset and order are created on initialization. (LoadDatabase.class)

**Customer:**
- username: customer1 - pass: password1 - Role: CUSTOMER
- username: customer2 - pass: password2 - Role: CUSTOMER
- username: customer3 - pass: password3 - Role: CUSTOMER
- username: admin1 - pass: admin_password1 - Role: CUSTOMER

**Asset:**
- customer: customer1 - assetName: TRY - size: 10000 - usableSize: 10000
- customer: customer2 - assetName: TRY - size: 10000 - usableSize: 10000
- customer: customer3 - assetName: TRY - size: 10000 - usableSize: 10000

**Order:**
- customer: customer1 - assetName: TRY - orderSide: SELL - size: 100 - price: 0.1 - orderStatus: CANCELED
- customer: customer2 - assetName: TRY - orderSide: SELL - size: 100 - price: 0.1 - orderStatus: CANCELED

### Assets:
#### Get Asset (Customer)
Endpoint: GET /api/v1/assets/{assetId}

Customer gets its own asset.
```
curl -u customer1:password1 http://localhost:8080/api/v1/assets/1 | json_pp
```

#### Get Asset (Customer)
Endpoint: GET /api/v1/assets/{assetId}

If a customer attempts to access another customer's asset, the API will respond with HTTP 403 Forbidden.
```
curl -u customer1:password1 http://localhost:8080/api/v1/assets/2 | json_pp
```

#### Get Asset (Admin)
Endpoint: GET /api/v1/assets/{assetId}

```
curl -u admin1:admin_password1 http://localhost:8080/api/v1/assets/2 | json_pp
```

#### List Assets (Customer)
Endpoint: GET /api/v1/assets

```
curl -u customer1:password1 http://localhost:8080/api/v1/assets | json_pp
```

With query parameters (Optional: customerId, assetName):
```
curl -u customer1:password1 "http://localhost:8080/api/v1/assets?customerId=1&assetName=TRY" | json_pp
```

#### List Assets (Admin)
Endpoint: GET /api/v1/assets

```
curl -u admin1:admin_password1 localhost:8080/api/v1/assets | json_pp
```

With query parameters (Optional: customerId, assetName):
```
curl -u admin1:admin_password1 "http://localhost:8080/api/v1/assets?customerId=1&assetName=TRY" | json_pp
```

### Orders:

#### Get Order (Customer)
Endpoint: GET /api/v1/orders/{orderId}

Customer gets its own order.
```
curl -u customer1:password1 http://localhost:8080/api/v1/orders/1 | json_pp
```

If a customer attempts to access another customer's order, the API will respond with HTTP 403 Forbidden.
```
curl -u customer2:password2 http://localhost:8080/api/v1/orders/1 | json_pp
```

#### Get Order (Admin)
Endpoint: GET /api/v1/admin/orders/{orderId}
```
curl -u admin1:admin_password1 http://localhost:8080/api/v1/admin/orders/2 | json_pp
```

### List Orders (Customer)
Endpoint: GET /api/v1/orders
```
curl -u customer1:password1 http://localhost:8080/api/v1/orders | json_pp
```

With query parameters (Optional: customerId, from, to, orderStatus):
```
curl -u customer1:password1 "http://localhost:8080/api/v1/orders?customerId=1&from=2025-08-01T13:46:44Z&to=2025-12-30T13:51:51Z&orderStatus=CANCELED" | json_pp
```

### List Orders (Admin)
Endpoint: GET /api/v1/admin/orders
```
curl -u admin1:admin_password1 http://localhost:8080/api/v1/admin/orders | json_pp
```

With query parameters (Optional: customerId, from, to, orderStatus):
```
curl -u admin1:admin_password1 "http://localhost:8080/api/v1/admin/orders?customerId=1&from=2025-08-01T13:46:44Z&to=2025-12-30T13:51:51Z&orderStatus=CANCELED" | json_pp
```

### Create Order (Customer)
Endpoint: POST /api/v1/orders

Response: HTTP 201 Created
```
curl -u customer1:password1 \
     -H "Content-Type: application/json" \
     -d '{
           "customerId": 1,
           "assetName": "TRY",
           "orderSide": "SELL",
           "size": 10,
           "price": 1
         }' \
     -X POST "http://localhost:8080/api/v1/orders" | json_pp
```

### Create Order (Admin)
Endpoint: POST /api/v1/admin/orders

Response: HTTP 201 Created
```
curl -u admin1:admin_password1 \
     -H "Content-Type: application/json" \
     -d '{
           "customerId": 1,
           "assetName": "TRY",
           "orderSide": "SELL",
           "size": 10,
           "price": 1
         }' \
     -X POST "http://localhost:8080/api/v1/admin/orders" | json_pp
```

### Delete Order (Customer)
Endpoint: DELETE /api/v1/orders/{orderId}

Response: HTTP 204 No Content (Successfully deleted)
```
curl -u customer1:password1 -X DELETE "http://localhost:8080/api/v1/orders/4" | json_pp
```

Response: HTTP 409 Conflict (orderStatus is not PENDING)
```
curl -u customer1:password1 -X DELETE "http://localhost:8080/api/v1/orders/4" | json_pp
```

### Delete Order (Admin)
Endpoint: DELETE /api/v1/admin/orders/{orderId}

Response: HTTP 204 No Content (Successfully deleted)
```
curl -u admin1:admin_password1 -X DELETE "http://localhost:8080/api/v1/admin/orders/9" | json_pp
```

### Match Order (Admin)
Endpoint: PUT /api/v1/admin/orders/{orderId}/match
```
curl -u admin1:admin_password1 -X PUT "http://localhost:8080/api/v1/admin/orders/5/match" | json_pp
```


### Technical Stack

#### Language & Platform
- Java 17+ – Leveraging the latest Spring Boot 3+ features and modern Java syntax.
- Spring Boot 3 – Simplified configuration, dependency injection, and embedded server support.

#### Frameworks & Libraries
Spring Framework Modules
- spring-boot-starter-web – REST APIs with Spring MVC
- spring-boot-starter-data-jpa – ORM and database access with JPA/Hibernate
- spring-boot-starter-security – Authentication & authorization
- spring-boot-starter-validation – Bean validation for request DTOs
- MapStruct – Mapping between entities and DTOs
- Spring HATEOAS – Hypermedia-driven REST responses
- Spring Test / MockMvc – Unit and integration testing of controllers and services

## Technical Debt:
- Need further clarification for createOrder - BUY. Currently it returns UnsupportedOperationException

- Need further clarification for matchOrder. Currently it only updates the OrderStatus (PENDING -> MATCHED)

- Profiles (dev, test, prod)
- The application-test.yml and application-prod.yml files will be created.

