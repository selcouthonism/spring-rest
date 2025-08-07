# Annotations:

## @SpringBootApplication: 
 **@SpringBootApplication** is a convenience annotation that adds all of the following:
 - **@Configuration**: Tags the class as a source of bean definitions for the application context.
 - **@EnableAutoConfiguration**: Tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings. For example, if spring-webmvc is on the classpath, this annotation flags the application as a web application and activates key behaviors, such as setting up a DispatcherServlet.
 - **@ComponentScan**: Tells Spring to look for other components, configurations, and services in the com/example package, letting it find the controllers. The main() method uses Spring Boot’s SpringApplication.run() method to launch an application.

## @Bean:
Retrieves all the beans that were created by your application or that were automatically added by Spring Boot.
The annotation combo **@Bean** with **@Profile("!test")** in Spring Boot is used to conditionally register a bean based on the active Spring profile.

**@Bean**: Defines a bean in the Spring context. This runs on start up

**@Profile("!test")**: Only for dev or prod. Activates the bean only when the active profile is not test.

**@Profile("test")**: Only for testing

**@RequiredArgsConstructor**: is a Lombok annotation used in Java to automatically generate a constructor for a class with all the final fields (and any fields marked @NonNull). It reduces boilerplate code — you don’t need to write constructors manually and works perfectly with Spring’s constructor injection (e.g., in @Service, @Component, or @Controller) (recommended over field injection).
```
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    // other methods...
}
```
Lombok generates this constructor for you at compile time:
```
public OrderService(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
}
```


## REST:
### @RestController: 
**@RestController** marks the class as a controller where every method returns a domain object (data) instead of a view. It is shorthand for including both **@Controller** and **@ResponseBody**.

### @RequestParam:
**@RequestParam** binds the value of the query string parameter name into the name parameter of the greeting() method. This query string parameter is implicitly not required because of the use of the defaultValue attribute. If it is absent in the request, the defaultValue of World is used.

### @RequestMapping:
The **@RequestMapping** annotation ensures that HTTP requests to /path are mapped to the method. This example does not specify GET vs. PUT, POST, and so forth, because **@RequestMapping** maps all HTTP operations by default. 
To narrow down, use **@GetMapping("/path")**. Because the **@RestController** annotation is present on the class, an implicit **@ResponseBody** annotation is added to the method. This causes Spring MVC to render the returned *HttpEntity* and its payload directly to the response.

Both *linkTo(…)* and *methodOn(…)* are static methods on *ControllerLinkBuilder* that let you fake a method invocation on the controller. The returned LinkBuilder will have inspected the controller method’s mapping annotation to build up exactly the URI to which the method is mapped.

### @RestControllerAdvice:
**@RestControllerAdvice** in Spring Boot is a powerful annotation used to handle exceptions globally and apply logic across multiple **@RestControllers** in a centralized and REST-friendly way.


## DTO:
### @JsonCreator: 
**@JsonCreator** annotation tells Jackson how to create an object from JSON during deserialization — especially when Jackson can't figure it out automatically.

This is especially useful when:
- the DTO is immutable (final fields, no setters) and
- you want to map JSON input with custom field names or no default constructor.

Not needed:
- If you have a default constructor and public setters — Jackson handles it automatically.
- For deserialization of basic POJOs with matching fields and methods.

**@JsonCreator** most commonly used with constructors or factory methods in classes that are:

#### A constructor-based DTO:
Jackson will use the constructor (via @JsonCreator) and assign values using the annotated parameter names.

Immutable objects: No setters, only constructor
- If your class has only ***final*** fields and no no-arg constructor or setters, Jackson needs **@JsonCreator** to know which constructor to use:

```
public class ProductDto {

    private final String name;
    private final int quantity;

    @JsonCreator
    public ProductDto(@JsonProperty("name") String name,
                      @JsonProperty("quantity") int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    // Getters only (no setters)
}
```

Without @JsonCreator, deserialization will fail because Jackson can’t find a default constructor or setters.

#### Static Factory Method Example
If you're using a static method instead of a constructor to create objects:


```
public class Currency {

    private final String code;

    private Currency(String code) {
        this.code = code;
    }

    @JsonCreator
    public static Currency from(@JsonProperty("code") String code) {
        return new Currency(code.toUpperCase());
    }

    public String getCode() {
        return code;
    }
}
```

If your class has no default constructor or no setters, Jackson will fail:
>com.fasterxml.jackson.databind.exc.MismatchedInputException: Cannot construct instance...

**@JsonCreator** resolves this by showing Jackson how to construct your object.

#### Custom parameter mapping (when names don’t match):
If the JSON field names don't match the Java field or parameter names:

```
public class Product {

    private final String productName;

    @JsonCreator
    public Product(@JsonProperty("product_name") String productName) {
        this.productName = productName;
    }
}
```

Without @JsonProperty, Jackson won't know how to map "product_name" to productName.

#### Java Records:
Java records work out-of-the-box, but **@JsonCreator** can be added for clarity or when using **@JsonAlias** or advanced mapping.

```
public record Item(@JsonProperty("id") int id, @JsonProperty("desc") String desc) { }
```

####  Multiple constructors
If your class has more than one constructor, Jackson needs to be told which one to use.
```
public class Order {
    private final Long id;
    private final String status;

    public Order(Long id) {
        this.id = id;
        this.status = "NEW";
    }

    @JsonCreator
    public Order(@JsonProperty("id") Long id, @JsonProperty("status") String status) {
        this.id = id;
        this.status = status;
    }
}
```

### @JsonProperty: 
**@JsonProperty** maps JSON keys to method/constructor parameters

### @JsonIgnoreProperties: 
UserDto class is annotated with **@JsonIgnoreProperties** from the Jackson JSON processing library to indicate that any properties not bound in this type should be ignored. 

To directly bind your data to your custom types, you need to specify the variable name to be exactly the same as the key in the JSON document returned from the API. In case your variable name and key in JSON doc do not match, you can use **@JsonProperty** annotation to specify the exact key of the JSON document.

Ignore unknown fields in incoming JSON:
If your API receives JSON with extra fields not present in your class, and you want to avoid deserialization errors (ignore extra fields)
```
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private String name;
    private int age;
}
```

Ignore specific fields during serialization or deserialization:
You can tell Jackson to completely ignore certain fields (even if present in JSON or in the Java class)
```
@JsonIgnoreProperties({"password", "internalId"})
public class User {
    private String username;
    private String password;
    private Long internalId;
}
```
These fields will be skipped both during read and write.

> Note: Use @JsonProperty(access = Access.WRITE_ONLY) if you want more control (e.g. write-only passwords)

Ignore Hibernate/JPA lazy-loaded fields:
Useful in Spring Data JPA entities to avoid serializing unwanted proxy objects:
```
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Product {
    @Id private Long id;
    private String name;
}
```

> @JsonIgnore: Ignore a specific field (Field or getter)
> @JsonIgnoreProperties: Ignore one or more fields globally (Class-level)

## Database Related:

### @Version: 
**@Version** is used in JPA to enable optimistic locking. On conflict (e.g., stale data during concurrent update), an OptimisticLockException is thrown. Catch and retry or inform the user.

### @Lock(LockModeType.PESSIMISTIC_WRITE):
This locks the row in DB until the transaction completes.
```
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT o FROM Order o WHERE o.id = :id")
Seat findByIdForUpdate(@Param("id") Long id);
```

### @Transactional:
To ensure data consistency and rollback on failure, annotate service methods with **@Transactional**. Use **@Transactional(readOnly = true)** for pure reads, and plain **@Transactional** for write operations.

### @EnableJpaAuditing: 
**@EnableJpaAuditing** is a Spring Boot annotation that enables automatic population of auditing fields (like createdAt, updatedAt, createdBy, updatedBy) in your JPA entities.

It activates Spring Data JPA's auditing mechanism, so you can annotate your entity fields with:

- **@CreatedDate**: Auto-sets creation timestamp
- **@LastModifiedDate**: Auto-sets update timestamp
- **@CreatedBy**: Auto-sets creator
- **@LastModifiedBy**: Auto-sets last modifier

and have Spring automatically fill those fields when the entity is saved or updated.

#### AuditorAware:
The AuditorAware bean in Spring Data JPA is used with **@CreatedBy** and **@LastModifiedBy** to automatically capture who created or modified an entity. This is part of Spring’s auditing support enabled by **@EnableJpaAuditing**. It tells Spring which user (or system identity) is performing the current operation.

***Entity Example Using AuditorAware:***
```
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    // getters, setters
}
```

***Minimal Static Implementation:*** 

Useful for non-authenticated apps or background jobs.
```
@Bean
public AuditorAware<String> auditorProvider() {
    return () -> Optional.of("system");  // hardcoded
}
```

***Spring Security Integration:***
To automatically use the current authenticated username:
```
@Bean
public AuditorAware<String> auditorProvider() {
    return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                         .filter(Authentication::isAuthenticated)
                         .map(Authentication::getName);
}
```
This allows: 
```
@CreatedBy
private String createdBy;

@LastModifiedBy
private String updatedBy;
```
...to be filled with admin, johndoe, etc., from the logged-in user's identity.

***With Custom User Objects:***
If you store a custom UserDetails or principal:
```
@Bean
public AuditorAware<String> auditorProvider() {
    return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                         .filter(Authentication::isAuthenticated)
                         .map(auth -> {
                             CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
                             return userDetails.getUserId(); // or getEmail(), getUsername()
                         });
}
```

### @Entity: 
**@Entity** is a JPA annotation to make this object ready for storage in a JPA-based data store. Spring Data JPA repositories are interfaces with methods that support creating, reading, updating, and deleting records against a back end data store. Some repositories also support data paging and sorting, where appropriate. Spring Data synthesizes implementations based on conventions found in the naming of the methods in the interface.

### @Transactional: 
To ensure data consistency and rollback on failure, annotate service methods with @Transactional.

The **@Transactional(readOnly = true)** annotation tells Spring that the method only performs read operations (no changes to the database).

Spring can apply optimizations, such as:
- Skipping dirty checks (no need to track changes).
- Using read-only database connections (if supported).
- Reducing lock contention (depending on the DB and transaction isolation level).

## Unit Test:
### @SpringBootTest: 
- Loads entire Spring Boot context (Loads all beans: @Service, @Repository, @Component, etc.)
- Test full app (controllers, services, DB, etc.)
- Good for testing end-to-end functionality (integration tests)

### @WebMvcTest: 
- Loads only web layer (controllers) (Loads only web-related beans: @Controller, @ControllerAdvice, @JsonComponent, etc.)
- Does NOT load services or repositories unless you explicitly mock them with @MockBean.
- Test controller logic in isolation (unit tests of controllers)
- Faster to run — ideal for unit tests targeting the web layer only.

### @AutoWire:

## Security:
### @EnableMethodSecurity:

**@PreAuthorize("hasRole('ADMIN')")**:  Role or permission check.
- When: Before the method execution.
- Purpose: Checks if the caller has the required permissions before the method runs.
- Use case: Prevent unauthorized access early.

If the expression in @PreAuthorize evaluates to false, the method won't execute, and an access denied error is thrown immediately.

**@PostAuthorize("hasRole('ADMIN')")**: Result-based authorization (ownership)
- When: After the method execution.
- Purpose: Checks the authorization based on the returned object or method outcome.
- Use case: When authorization depends on the result of the method call (e.g., owner-based access).

If the expression is false, the method result is discarded, and an access denied error is thrown.
```
@PostAuthorize("returnObject.user == authentication.name")
public Order getOrder(Long id) {
    // The method runs, then this checks if the returned order's user matches the authenticated user
    return orderRepository.findById(id);
}
```

# Additional Notes:
## WebClient:
WebClient vs RestTemplate:
The most modern and preferred way is with *WebClient* (introduced in Spring 5). You can also use *RestTemplate* (older but still valid). 
Look at SpringApiApplication *RestTemplate* is in maintenance mode, so prefer WebClient for new development.

Mono<T> is a reactive container (similar to Optional<T> or Future<T>). Non-blocking and asynchronous.
