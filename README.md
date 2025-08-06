# spring-rest

Overview
This project showcases the fundamental building blocks of a Spring-based application. It is designed to be a clean and modular example using an H2 in-memory database for lightweight testing and development.
Features

- **RESTful API** – Built with Spring's @RestController to expose endpoints.
- **Service Layer** – Encapsulates business logic for clean separation of concerns.
- **DTOs (Data Transfer Objects)** – Used for structured communication between the API and internal layers.
- **Repository Layer** – Utilizes Spring Data JPA to interact with the database.
- **Model Layer** – Defines domain entities representing the application's core data structures.
- **Assembler** – Converts between domain models and DTOs for cleaner API design.
- **Exception Handling** – Implements centralized exception management with custom error responses.
- **Validation** – Ensures data integrity using Jakarta Bean Validation (@Valid, @NotNull, etc.).
- **Unit Tests** – Includes basic tests to verify the correctness of core components.



## DTO:
**Record** is preferred in newer Spring Boot apps for simplicity and immutability. It can be used on Java 16+ and using Spring Boot 3.x.

***Jackson JSON library*** to automatically marshal instances of type OrderDto into JSON. Jackson is included by default by the web starter. 

The OrderDto object must be converted to JSON. Thanks to Spring’s HTTP message converter support, you need not do this conversion manually. Because Jackson 2 is on the classpath, Spring’s MappingJackson2HttpMessageConverter is automatically chosen to convert the OrderDto instance to JSON.

>Note: Use OrderDto in your Controller + Assembler.
You hide internal database fields (e.g., audit fields, lazy-loaded relations)

## Assembler:
*RepresentationModelAssembler* interface has one method: **toModel()**.
 It is based on converting a non-model object (Order) into a model-based object (EntityModel<OrderDto>).

 By applying Spring Framework’s **@Component** annotation, the assembler is automatically created when the app starts.
 
 Spring HATEOAS’s abstract base class for all models is *RepresentationModel*.
 However, for simplicity, Spring recommends using EntityModel<T> as your mechanism to easily wrap all POJOs as models.
 
 ### EntityModel<T>:
 EntityModel<T> is a generic container from Spring HATEOAS that includes not only the data but a collection of links.

 - linkTo(methodOn(OrderController.class).getById(id)).withSelfRel() asks that Spring HATEOAS build a link to the getById() method of OrderController and flag it as a self link.

 - linkTo(methodOn(OrderController.class).getAllOrders()).withRel("orders") asks Spring HATEOAS to build a link to the aggregate root, all(), and call it "orders".

 This demo is based on Spring MVC and uses the static helper methods from WebMvcLinkBuilder to build these links.
 If you are using Spring WebFlux in your project, you must instead use WebFluxLinkBuilder.

## Mapper:
Use ***MapStruct*** to avoid writing mappers manually.
MapStruct is a fast, compile-time mapper that generates efficient code to map between your entities and DTOs automatically.

## Exceptions:
*exception.specific:* For domain-specific exceptions that represent business rules.

*exception.general:* For application-wide, reusable, or system-level exceptions.

*exception.handler:* For your **@RestControllerAdvice** classes that handle and format exceptions globally.

## Unit Test:

**OrderControllerTest**: 
Uses Mockito annotations to mock dependencies and inject into the controller.

Tests each controller method:
- Mocks service calls and assembler transformations.
- Asserts the returned values and status codes.
- Verifies interactions with mocks.

***Mockito*** is the most popular mocking framework for Java. It allows you to fake the behavior of classes (usually dependencies) in your unit tests so that you can test your code in isolation.

**OrderControllerIntegrationTest**: 
It uses real components: 
- OrderController, OrderService, OrderRepository, OrderModelAssembler, etc.
- Uses real H2 DB in memory (Spring Boot auto-configures it)
- Validates full flow including database persistence
- Calls real endpoints like /orders/{id}, /orders, etc.

***@SpringBootTest*** is a powerful annotation in Spring Boot used to write integration tests. It tells Spring to start the full application context, just like it would in production — including all beans, configurations, repositories, controllers, and more.