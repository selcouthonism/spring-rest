# Concurrency

## Factory Class

### 1. Spring Beans are Singletons by Default
This is the most important concept to understand. When you annotate a class with *@Component*, *@Service*, or *@Repository*, Spring, by default, creates **only one single instance** of that class for the entire application. This is the Singleton design pattern.

This means: 
- There is only one OrderHandlerFactory instance.
- There is only one CreateBuyOrderHandler instance.
- There is only one CreateSellOrderHandler instance.

Every single user request, running on a separate thread, will share these exact same instances.

```
                                     /--> [ Thread 1: Customer A's Request ]
[ Web Server Thread Pool ] --------->----> [ Thread 2: Customer B's Request ]
                                     \--> [ Thread 3: Customer C's Request ]
```
All three of these threads will interact with the same factory and handler objects. The question then becomes: "Is it safe for multiple threads to use these shared objects at the same time?"

The answer is yes, because of how they are designed.

### 2. The Factory is Thread-Safe

The OrderHandlerFactory is populated with its strategies in the constructor during application startup. After that, its internal handlerMap is effectively immutable (it is only ever read from).

Concurrent read operations on a data structure that isn't being changed are perfectly safe. There is no risk of a race condition or data corruption within the factory itself.

#### The Factory's Lifecycle: "Write-Once, Read-Many":
The most critical factor to consider is the lifecycle of the handlerMap within our OrderHandlerFactory:

**Initialization (Write Phase):** The map is populated only once, inside the factory's constructor. This happens when the Spring application context is starting up. At this stage, the bean is being created in a single thread. There is no concurrent access.

**Operation (Read Phase):** After the application has started and the factory bean is fully constructed, the handlerMap becomes effectively immutable. It is never modified again. Multiple threads (handling concurrent API requests) will call getHandler(), but these are all read-only operations.

This is a classic "write-once, read-many" pattern.

#### EnumMap vs ConcurrentHashMap:

In this OrderHandlerFactor, EnumMap is an optimal choice for the factory. While ConcurrentHashMap is a powerful tool for concurrency, using it here would be unnecessary and slightly less performant.

Given the "write-once, read-many" lifecycle, EnumMap is the best tool for the job for several reasons:

1. Unbeatable Performance for Enum Keys:

EnumMap is a highly specialized Map implementation. Internally, it doesn't use hashing or complex data structures. It uses a simple array. 

When you call *map.get(OrderAction.CREATE)*, EnumMap takes the enum's *ordinal()* value (an integer, e.g., 0 for CREATE) and uses it as a direct index to access the array (internalArray[0]). 

This is an O(1) operation with an extremely low constant factor. It's one of the fastest possible map lookups in Java, significantly faster than a HashMap's hash code calculation and bucket lookup.

2. Memory Efficiency:

Because it's just a simple array internally, an **EnumMap** is extremely compact and memory-efficient. It has minimal overhead compared to **HashMap** or **ConcurrentHashMap**, which need to maintain more complex internal structures (nodes, hash buckets, etc.).

3. Concurrency Safety in this Specific Context:

While EnumMap is not a synchronized collection, it is perfectly thread-safe in this specific scenario.

- The Java Memory Model (JMM) provides a crucial guarantee: all writes made by a thread before it makes an object available to other threads are visible to those other threads.

- When Spring finishes constructing the OrderHandlerFactory bean, it establishes a "happens-before" relationship. The single-threaded population of the map in the constructor is guaranteed to be safely and completely visible to all subsequent threads that receive a reference to this bean (e.g., the web server threads handling API requests).

- Since the map is never written to again, there are no race conditions. Multiple threads can read from the fully formed map without any issues. This is a concept known as "safe publication" of an effectively immutable object.

#### Why ConcurrentHashMap Would Be a Poorer Choice Here

ConcurrentHashMap is a masterpiece of engineering, but it's designed to solve a problem we don't have: concurrent modifications.

- **Unnecessary Overhead:** ConcurrentHashMap achieves its thread safety through sophisticated techniques like fine-grained locking and using volatile writes and memory fences. These mechanisms add a small but measurable performance overhead to every operation, even reads. We would be paying a performance penalty for a thread-safety feature (safe concurrent writes) that we never use.

- **Slower for this Use Case:** For a simple get operation, the direct array index lookup of EnumMap will always be faster than the hash-based lookup of ConcurrentHashMap, which also has to navigate its internal concurrency controls.

- **Doesn't Leverage the Key Type:** It treats the Enum key like any other object, calculating its hash code. It completely misses out on the performance optimization that comes from knowing the keys belong to a small, finite, integer-based set (the enum's ordinals).

### 3. Stateless Handlers
This is the most critical part of the design. The CreateBuyOrderHandler, CreateSellOrderHandler, and other strategy beans are stateless.

"Stateless" means that the class does not have any instance variables (member fields) that store data specific to a single request. It acts as a container for behavior (methods), not data.

```
@Component
@RequiredArgsConstructor
public class CreateBuyOrderHandler implements OrderActionHandler<CreateOrderContext> {

    // Dependency (a shared, thread-safe singleton)
    private final AssetRepository assetRepository;

    @Override
    public void handle(CreateOrderContext context) {
        // 1. All request-specific data (customer ID, size, price) is in the 'context' object.
        // 2. This 'context' object is a method parameter, making it local to this specific thread's execution.
        // 3. The method performs logic and calls other thread-safe services (like the repository).
        // 4. No data from the 'context' is ever saved into an instance variable of this class.
    }
}
```

#### The Flow of Concurrent Requests
Imagine Customer A and Customer B both submit a BUY order at the exact same moment.

Thread A and Thread B are started by the web server.

Both threads access the single OrderHandlerFactory instance to get the handler.

Both threads receive a reference to the single CreateBuyOrderHandler instance.

Thread A calls handler.handle(contextForCustomerA).

Thread B calls handler.handle(contextForCustomerB) concurrently.

This is safe because all the unique information for each customer (contextForCustomerA and contextForCustomerB) is passed as a method parameter. This data lives on the **stack** for each respective thread, not on the **heap** as part of the shared handler object. The handler object itself is just a vessel for the handle() method logic and its thread-safe dependencies (like assetRepository).

#### What Would Make It UNSAFE? (The Anti-Pattern)
To illustrate the point, here is what a non-thread-safe (stateful) handler would look like. DO NOT DO THIS.
```
// ANTI-PATTERN: DANGEROUS AND NOT THREAD-SAFE
@Component
public class UnsafeCreateOrderHandler {

    private CreateOrderContext currentContext; // DANGEROUS: Mutable instance state!

    public void handle(CreateOrderContext context) {
        // This line creates a massive race condition
        this.currentContext = context; 

        // If Thread B executes the line above after Thread A but before Thread A is finished,
        // it will overwrite Customer A's data with Customer B's data.
        
        // ... some logic that uses this.currentContext
    }
}
```
In this dangerous example, if Thread A's request is paused by the OS right after setting this.currentContext, and Thread B runs, Thread B will overwrite the currentContext field. When Thread A resumes, it will be working with Customer B's data, leading to catastrophic bugs.



# Authentication & Authorization

## What is a JSON Web Token (JWT)?
A JWT is a compact, URL-safe standard used to securely transmit information between parties as a JSON object. It is "self-contained," meaning it carries all the necessary information about the user, avoiding the need to query a database on every request.

A JWT consists of three parts separated by dots (.):

1. **Header:** Contains metadata about the token, such as the signing algorithm (e.g., HS256) and the token type (JWT). This part is Base64Url encoded.
{"alg": "HS256", "typ": "JWT"}

2. **Payload (Claims):** Contains the statements ("claims") about the entity (typically, the user) and additional data. There are standard claims like sub (subject/username), iat (issued at time), and exp (expiration time), as well as custom claims you can add (e.g., roles, user ID). This part is also Base64Url encoded.
{"sub": "john.doe", "iat": 1661333984, "exp": 1661337584, "roles": ["ROLE_CUSTOMER"]}

3. **Signature:** This is the most critical part for security. To create the signature, you take the encoded header, the encoded payload, a secret key, and sign them with the algorithm specified in the header.
HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secretKey) The signature ensures that the token's content has not been tampered with. Only the party that knows the secretKey can verify the signature or create new valid tokens.

## How JWT is Used in This Application (The Authentication Flow)

### Authentication (Login):
- A user submits their *username* and *password* to the *POST /api/v1/auth/login endpoint*.
- The ***AuthController*** passes these credentials to Spring Security's **AuthenticationManager**.
- The **AuthenticationManager** uses our custom ***UserDetailsService*** to fetch the user's data from the database and the **PasswordEncoder** to securely compare the passwords.
- If the credentials are valid, the ***AuthController*** calls the ***JwtService*** to generate a JWT. The user's username is placed in the payload (sub claim).
- This JWT is sent back to the client in the response body.

### Stateless Authorization (Subsequent Requests):
- The client (e.g., a web browser or mobile app) must store this JWT securely. For every subsequent request to a protected endpoint (like GET /api/v1/orders), the client must include the JWT in the Authorization header with the Bearer scheme:
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
- Our custom ***JwtAuthenticationFilter*** intercepts every incoming request.
- It checks for the Authorization header. If it finds a Bearer token, it extracts it.
- It uses the ***JwtService*** to parse the token and **validate the signature** using the same secret key. This proves the token is authentic and hasn't been altered.
- If the signature is valid, it extracts the username from the token's payload.
- It then creates a full **UserDetails** object from the database to get the user's latest roles and permissions.
- Finally, it sets this user information in Spring's **SecurityContextHolder**. From this point on, Spring Security knows who the user is for the duration of the request. The **@PreAuthorize** annotations on your controllers can then work as intended, checking the roles and permissions of the authenticated user.

The key benefit here is statelessness. The server does not need to maintain a session for the user. Every request contains all the information needed for the server to verify the user's identity, making the application highly scalable.

# Configuration with @ConfigurationProperties
Spring Boot strongly encourages using type-safe configuration properties via the @ConfigurationProperties annotation. This is cleaner, safer, and provides better IDE support. For externalized configuration (e.g., JWT secret keys, token expiration times, external API URLs), create a dedicated properties class.

In application.yml:
```
application:
  security:
    jwt:
      secret: "your-very-long-and-secure-secret-key"
      expiration-ms: 86400000
```

Create an immutable object for the properties:
```
@ConfigurationProperties(prefix = "application.security.jwt")
public record JwtProperties(
        String secretKey,
        long expirationMs
) { }
```

Then enable configuration properties in a service or config class:
```
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    
    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Ensure the secret key is properly encoded for HMAC-SHA algorithms
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secretKey().getBytes());
    }

    ...
}
```


# Adding Roles to JWT (Not applied to our project)
It allows your services to perform authorization checks without needing to query the database for user permissions on every single request, making your system more stateless and scalable.

The process involves two main steps:
1. Adding a "roles" claim when the token is generated.
2. Extracting the "roles" claim and creating the user's security context when the token is validated.

## Step 1: Update JwtService to Add Roles During Token Creation
We'll modify the generateToken method to extract the user's roles (authorities) from the UserDetails object and embed them into the JWT's payload as a custom claim.

UPDATED generateToken method:
```
    public String generateToken(UserDetails userDetails) {
        // Extract roles from UserDetails and convert to a list of strings
        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles) // Add the roles as a custom claim
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.expirationMs()))
                .signWith(secretKey)
                .compact();
    }
```
New method to extract roles:
```
public List<String> extractRoles(String token) {
    return extractClaim(token, claims -> claims.get("roles", List.class));
}
```

## Step 2: Update JwtAuthenticationFilter to Use Roles from the Token
Next, we'll modify the filter. Instead of hitting the database on every request to get the user's roles, we will extract them directly from the token. This significantly reduces database load.

We don't need to load the user from the DB. We can build the UserDetails from the token. This check is now simpler because we trust the token's contents after validation.
```
if (!isTokenExpired(jwt)) { // Simplified validation

    // Extract roles from the token
    List<String> roles = jwtService.extractRoles(jwt);
    List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    // Create UserDetails object directly from token claims
    UserDetails userDetails = new User(username, "", authorities);

    // Create the authentication token
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities() // Use authorities from the token
    );
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    
    // Set the authentication in the security context
    SecurityContextHolder.getContext().setAuthentication(authToken);
}
```

## Explanation of Changes and Trade-offs

### Performance Benefit (The "Why")
By embedding roles in the JWT, your ***JwtAuthenticationFilter*** no longer needs to make a database call via the ***UserDetailsService*** for every single API request. It can build the user's security principal directly from the contents of the token. For an application with high traffic, this dramatically reduces database load and improves the response time and scalability of your services.

### Security Consideration (The Trade-off)
This design introduces an important trade-off. Since the roles are read from the token, they are only as fresh as the token itself.

- **Scenario:** An administrator revokes a user's ROLE_ADMIN access in the database.
- **Outcome:** The user can still access admin-protected endpoints using their existing, unexpired JWT, because the token still contains the "roles": ["ROLE_ADMIN"] claim. The user's access will only be demoted after their current token expires and they are forced to log in again to get a new one.

For most applications, this is an acceptable trade-off. If your system requires immediate revocation of permissions, you would need to implement a more complex solution, such as:
- Keeping the database call in the filter to fetch fresh roles on every request (the hybrid approach).
- Maintaining a token blocklist in a fast cache like Redis.

# Observability

## What are Actuator and Prometheus?
**Spring Boot Actuator:** A library that exposes operational information about your running application through a set of HTTP endpoints. It tells you what's happening inside your application (health, metrics, environment info, etc.).

**Prometheus:** A powerful, open-source monitoring and alerting system. It works by "scraping" (pulling) metrics from endpoints like those provided by Actuator. It stores these metrics in a time-series database, allowing you to query them, create dashboards, and set up alerts.

Together, they form a standard and powerful stack for modern application observability.

### Step 1: Add the Required Dependencies
- **spring-boot-starter-actuator:** The core Actuator library.
- **micrometer-registry-prometheus:** The specific Micrometer adapter that formats the metrics into a plain text format that Prometheus can understand.

```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Step 2: Configure the Actuator Endpoints
By default, for security reasons, Spring Boot 3 only exposes the /actuator/health endpoint over the web. We need to explicitly tell Actuator which endpoints to expose.

We also can add custom information to the /actuator/info endpoint.

File: src/main/resources/application.yml
```
# Management Endpoints Configuration
management:
  endpoints:
    web:
      exposure:
        # Expose specific endpoints. Be deliberate about what you expose.
        # 'prometheus' is needed for the monitoring system.
        # 'health' and 'info' are generally safe and useful.
        include: health, info, prometheus, metrics
  endpoint:
    health:
      # Show full health details (e.g., database status) when authenticated.
      # Defaults to 'never' show details. 'when-authorized' is a good choice.
      show-details: when-authorized

# Custom application info for the /actuator/info endpoint
info:
  application:
    name: Brokerage Trading API
    description: API for managing customer stock orders.
    version: 1.0.0
```


### Step 3: Secure the Actuator Endpoints
This is a critical step. By default, Spring Security will protect all endpoints, including the new actuator ones. We need to configure our security rules to:
- **Allow** the Prometheus server to access /actuator/prometheus without authentication.
- **Require** an ADMIN role to access any other sensitive actuator endpoints.

Update the SecurityConfig:
```
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            // Public endpoints for authentication and API docs
            .requestMatchers("/api/v1/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            
            // Allow the Prometheus server to scrape metrics without authentication
            .requestMatchers("/actuator/prometheus").permitAll()
            
            // Secure all other actuator endpoints, requiring ADMIN role
            .requestMatchers("/actuator/**").hasRole("ADMIN")

            // All other requests must be authenticated
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```
> **Important:** The order of the .requestMatchers() rules matters. More specific rules (/actuator/prometheus) must come before more general rules (/actuator/**).

### Step 4: Verifying the Setup
Run your Spring Boot application. You can verify that the endpoints are working correctly.

1. Check Health:
- Navigate to http://localhost:8080/actuator/health
- You should see a simple response like: {"status":"UP"}

> Note: Since **/actuator/health** requires authentication with the role ADMIN, you’ll need to:
- First obtain a JWT access token from your login endpoint (with POST /api/v1/auth/login with username/password). Then call the actuator endpoint with the token in the Authorization header.

```
ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin1", "password": "admin_password1"}' \
  | jq -r '.jwtToken')
```

Call the /actuator/health:
```
curl -X GET http://localhost:8080/actuator/health -H "Authorization: Bearer $ADMIN_TOKEN" | json_pp
```

2. Check Prometheus Metrics:
- Navigate to http://localhost:8080/actuator/prometheus
- You will see a large plain text response. This is the metrics data that Prometheus will read.

### Step 5: Configure Prometheus 
You would now configure your Prometheus server to "scrape" this endpoint. In your Prometheus configuration file (prometheus.yml), you would add a job like this:
```
scrape_configs:
  - job_name: 'brokerage_api'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    static_configs:
      - targets: ['localhost:8080'] # Or the IP/DNS of your deployed application
```
> Note: 
- On Linux, replace host.docker.internal with your machine’s IP (e.g., 172.17.0.1).
- On Mac/Windows, host.docker.internal works fine.

Prometheus will automatically fetch the metrics from your application every 15 seconds. From there, you can use tools like Grafana to connect to Prometheus as a data source and build powerful dashboards to visualize your application's performance over time.

### Step 6: Run Prometheus Docker Image
- Mount prometheus.yml config into the container.
- By default, it exposes the web UI and metrics at port 9090.

```
docker run -d --name prometheus \
  -p 9090:9090 \
  -v $(pwd)/stock-orders/src/main/resources/prometheus.yml:/opt/bitnami/prometheus/conf/prometheus.yml \
  bitnami/prometheus:latest
```
You can then open http://localhost:9090 in your browser.
