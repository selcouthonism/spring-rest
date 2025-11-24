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
- firstName: customer1FN - lastName: lastName1
- firstName: customer2FN - lastName: lastName1
- firstName: customer3FN - lastName: lastName1
- firstName: admin1FN - lastName: lastName1

**UserCredentails:**
- username: customer1 - pass: password1 - Role: CUSTOMER
- username: customer2 - pass: password2 - Role: CUSTOMER
- username: customer3 - pass: password3 - Role: CUSTOMER
- username: admin1 - pass: admin_password1 - Role: ADMIN

**Asset:**
- customer: customer1 - assetName: TRY - size: 10000 - usableSize: 10000
- customer: customer2 - assetName: TRY - size: 10000 - usableSize: 10000
- customer: customer3 - assetName: TRY - size: 10000 - usableSize: 10000

- customer: customer1 - assetName: AAPL - size: 5000 - usableSize: 5000
- customer: customer2 - assetName: AAPL - size: 5000 - usableSize: 5000
- customer: customer3 - assetName: AAPL - size: 5000 - usableSize: 5000

**Order:**
- customer: customer1 - assetName: TRY - orderSide: SELL - size: 100 - price: 0.1 - orderStatus: CANCELED
- customer: customer2 - assetName: TRY - orderSide: SELL - size: 100 - price: 0.1 - orderStatus: CANCELED

### Authentication
Endpoint: POST /api/v1/auth

Login with username and password and retrive jwt token. Extract the jwtToken from the JSON response into a shell variable so you can reuse it in future API calls.

Display the response on Terminal for Customer1:
```
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer1", "password": "password1"}' \
  | json_pp
```

For Customer1:
```
CUST_TOKEN1=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer1", "password": "password1"}' \
  | jq -r '.jwtToken')
```

For Customer2:
```
CUST_TOKEN2=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "customer2", "password": "password2"}' \
  | jq -r '.jwtToken')
```

For Admin:
```
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin1", "password": "admin_password1"}' \
  | jq -r '.jwtToken')
```

Verify that it worked:
```
echo $CUST_TOKEN1 "\n" $CUST_TOKEN2 "\n" $ADMIN_TOKEN
```

### Assets:
#### Get Asset (Customer)
Endpoint: GET /api/v1/assets/{assetId}

Customer gets its own asset.
```
curl http://localhost:8080/api/v1/assets/1 -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

If a customer attempts to access another customer's asset, the API will respond with HTTP 403 Forbidden.
```
curl http://localhost:8080/api/v1/assets/2 -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

#### Get Asset (Admin)
Endpoint: GET /api/v1/assets/{assetId}

```
curl http://localhost:8080/api/v1/assets/2 -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```

#### List Assets (Customer)
Endpoint: GET /api/v1/assets

```
curl http://localhost:8080/api/v1/assets -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

With query parameters (Optional: customerId, assetName):
```
curl "http://localhost:8080/api/v1/assets?customerId=1&assetName=TRY" -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

#### List Assets (Admin)
Endpoint: GET /api/v1/assets

```
curl localhost:8080/api/v1/assets -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```

With query parameters (Optional: customerId, assetName):
```
curl "http://localhost:8080/api/v1/assets?customerId=1&assetName=TRY" -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```

### Orders:

#### Get Order (Customer)
Endpoint: GET /api/v1/customers/{customerId}/orders/{orderId}

Customer gets its own order.
```
curl http://localhost:8080/api/v1/customers/1/orders/1 -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

If a customer attempts to access another customer's order, the API will respond with HTTP 403 Forbidden.
```
curl http://localhost:8080/api/v1/customers/2/orders/2 -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

#### Get Order (Admin)
Endpoint: GET /api/v1/customers/{customerId}/orders/{orderId}
```
curl http://localhost:8080/api/v1/customers/2/orders/2 -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```

### List Orders (Customer)
Endpoint: GET /api/v1/customers/{customerId}/orders
```
curl http://localhost:8080/api/v1/customers/1/orders -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

With query parameters (Optional: from, to, orderStatus):
```
curl "http://localhost:8080/api/v1/customers/1/orders?from=2025-08-01T13:46:44Z&to=2025-12-30T13:51:51Z&orderStatus=CANCELED" -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

### List Orders for a Specific Customer (Admin)
Endpoint: GET /api/v1/customers/{customerId}/orders
```
curl http://localhost:8080/api/v1/customers/1/orders -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```

With query parameters (Optional: customerId, from, to, orderStatus):
```
curl "http://localhost:8080/api/v1/customers/1/orders?customerId=1&from=2025-08-01T13:46:44Z&to=2025-12-30T13:51:51Z&orderStatus=CANCELED" -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```

### Create Order (Customer)
Endpoint: POST /api/v1/customers/{customerId}/orders

Response: HTTP 201 Created
```
curl -X POST "http://localhost:8080/api/v1/customers/1/orders" \
     -H "Content-Type: application/json" \
     -d '{
           "customerId": 1,
           "assetName": "AAPL",
           "orderSide": "SELL",
           "size": 10,
           "price": 1
         }' \
     -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

### Create Order (Admin)
Endpoint: POST /api/v1/customers/{customerId}/orders

Response: HTTP 201 Created
```
curl -X POST "http://localhost:8080/api/v1/customers/2/orders" \
     -H "Content-Type: application/json" \
     -d '{
           "customerId": 2,
           "assetName": "AAPL",
           "orderSide": "BUY",
           "size": 10,
           "price": 1
         }' \
     -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```

### Delete Order (Customer)
Endpoint: DELETE /api/v1/customers/{customerId}/orders/{orderId}

Response: HTTP 204 No Content (Successfully deleted)
```
curl -X DELETE "http://localhost:8080/api/v1/customers/1/orders/4" -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

Response: HTTP 409 Conflict (orderStatus is not PENDING)
```
curl -X DELETE "http://localhost:8080/api/v1/customers/1/orders/4" -H "Authorization: Bearer $CUST_TOKEN1" | json_pp
```

### Delete Order (Admin)
Endpoint: DELETE /api/v1/customers/{customerId}/orders/{orderId}

Response: HTTP 204 No Content (Successfully deleted)
```
curl -X DELETE "http://localhost:8080/api/v1/customers/1/orders/9" -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```

### Match Order (Admin)
Endpoint: PUT /api/v1/admin/orders/{orderId}/match
```
curl -X PUT "http://localhost:8080/api/v1/admin/orders/5/match" -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```


### Technical Stack

#### Language & Platform
- Java 21 – Leveraging the latest Spring Boot 3+ features and modern Java syntax.
- Spring Boot 3 (3.5.5) – Simplified configuration, dependency injection, and embedded server support.

> Note: Spring Boot doesn’t enable virtual threads by default (as of 3.2.x), but you can configure it with: 
```
spring.threads.virtual.enabled=true
```

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
- Profiles (dev, test, prod)
- The application-test.yml and application-prod.yml files will be created.

