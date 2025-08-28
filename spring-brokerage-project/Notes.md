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