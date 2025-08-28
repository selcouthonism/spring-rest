# UnitTest (Mockito)
Mocking is the practice of replacing real dependencies (DB, external APIs, services) with fake objects that simulate their behavior during testing. The pupose is to isolate the unit under test (class/method) from external systems. For example, instead of calling a real OrderRepository that connects to a database, you mock it and tell it what to return when called.

1. Creating a Mock
```
@Mock
private OrderRepository orderRepository; // a fake repository
```
or programmatically:
```
OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
```

2. Stubbing with when()

You define what the mock should do when a method is called. For example, if the method is called with 1L, it returns a fake Order. If it’s called with another value (and no stub defined), it returns default (e.g., null, 0, empty list).
```
when(orderRepository.findById(1L)).thenReturn(Optional.of(new Order(1L, "BUY")));
when(orderRepository.findAll()).thenThrow(new RuntimeException("DB error"));
when(orderRepository.count()).thenAnswer(invocation -> 42L);
```

3. Verifying with verify()

verify() is used to check that a method was called on a mock object (and how many times). It doesn’t stub or control behavior. It only asserts interactions. If the method wasn’t called as expected or arguments don’t match, the test fails. (Think of it as: “Did my service call the dependency as expected?”)
> Conditions: times(n), never(), atLeast(n), atMost(n), atLeastOnce()

```
verify(mock).methodName(arguments);             // The syntax
verify(orderRepository).findById(1L);           // called once
verify(orderRepository, times(2)).save(any());  // called twice
verify(orderRepository, never()).delete(any()); // never called
```

4. Argument Matchers

It is one of the most powerful features when you don’t know or don’t care about the exact arguments your mocks are called with. When you stub (**when**) or verify (**verify**) a method call, you often need to match the arguments. Without matchers, Mockito requires exact values. With matchers, for example you can say things like “I don’t care what string is passed” or “match only if the argument is > 10”.
>Common matchers: any(), anyString(), eq(value), argThat(predicate), anyInt(), isNull(), notNull(), 
```
when(orderRepository.save(any(Order.class))).thenReturn(new Order(99L, "MOCKED"));
```

Argument Matching in Verify: The following example confirms save() was called with an Order whose side = "BUY". (argThat(): assert call arguments)
```
verify(orderRepository).save(argThat(order -> order.getSide().equals("BUY")));
```

The following example verifies that only positive-usable-size orders were saved:
```
verify(orderRepository).save(
    argThat(order -> order.getUsableSize().compareTo(BigDecimal.ZERO) > 0)
);
```

5. Spies

Spies are partial mocks — real objects wrapped with Mockito, allowing some methods to behave normally and others to be stubbed.

6. ArgumentCaptor

ArgumentCaptor lets you capture the actual arguments that were passed to a mock method call so that you can inspect them in your test. This is useful when you care about the values of arguments, not just whether a method was called.
```
ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

verify(orderRepository).save(captor.capture());
Order saved = captor.getValue();

assertEquals("BUY", saved.getSide());
```

Multiple Captures:
```
ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

verify(orderRepository, times(2)).save(captor.capture());

List<Order> allOrders = captor.getAllValues();
assertEquals(2, allOrders.size());
assertEquals("BUY", allOrders.get(0).getSide());
assertEquals("SELL", allOrders.get(1).getSide());
```

With multiple parameters: If a method takes multiple arguments, you can capture one and use matchers for others:
```
ArgumentCaptor<BigDecimal> amountCaptor = ArgumentCaptor.forClass(BigDecimal.class);
verify(paymentService).transfer(eq("USD"), amountCaptor.capture());
assertTrue(amountCaptor.getValue().compareTo(BigDecimal.ZERO) > 0);
```

7. doReturn / doThrow / doAnswer

Useful when stubbing void methods or avoiding when(...).thenReturn(...) on spies.
```
doThrow(new RuntimeException("fail"))
    .when(notificationService).send(any());

doReturn("SAFE")
    .when(spyObject).dangerousMethod();
```

## Cheatsheet

| **Feature**              | **Usage**                                            | **Example**                                                                                                                                                                 |
| ------------------------ | ---------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Create a mock**        | Create a fake object for dependency                  | `OrderRepository repo = mock(OrderRepository.class);` <br> or `@Mock private OrderRepository repo;`                                                                         |
| **Inject mocks**         | Automatically inject mocks into the class under test | `@InjectMocks private OrderService orderService;`                                                                                                                           |
| **Stub method** (`when`) | Define return value or behavior                      | `when(repo.findById(1L)).thenReturn(Optional.of(order));`                                                                                                                   |
| **Stub exceptions**      | Simulate throwing exception                          | `when(repo.findAll()).thenThrow(new RuntimeException("DB down"));`                                                                                                          |
| **Verify interaction**   | Check if a method was called                         | `verify(repo).findById(1L);`                                                                                                                                                |
| **Verify times**         | Check number of invocations                          | `verify(repo, times(2)).save(any());`                                                                                                                                       |
| **Verify never**         | Ensure method wasn’t called                          | `verify(repo, never()).delete(any());`                                                                                                                                      |
| **Argument matchers**    | Flexible stubbing/verification                       | `when(repo.save(any(Order.class))).thenReturn(order);` <br> `verify(repo).save(eq(order));`                                                                                 |
| **Custom matcher**       | Match with condition                                 | `verify(repo).save(argThat(o -> o.getSide().equals("BUY")));`                                                                                                               |
| **ArgumentCaptor**       | Capture arguments for assertion                      | `ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);` <br> `verify(repo).save(captor.capture());` <br> `assertEquals("BUY", captor.getValue().getSide());` |
| **Spy**                  | Wrap real object, stub selectively                   | `List<String> spyList = spy(new ArrayList<>());` <br> `when(spyList.size()).thenReturn(100);`                                                                               |
| **doReturn / doThrow**   | Alternative stubbing for void or spies               | `doThrow(new RuntimeException()).when(service).notify();` <br> `doReturn("safe").when(spy).riskyMethod();`                                                                  |
| **doAnswer**             | Custom logic on stub                                 | `doAnswer(inv -> "Hello " + inv.getArgument(0)) .when(service).greet(any());`                                                                                               |
| **Reset mock**           | Clear interactions/stubs                             | `reset(repo);`                                                                                                                                                              |
| **Check order**          | Verify call order                                    | `InOrder inOrder = inOrder(repo);` <br> `inOrder.verify(repo).save(any());` <br> `inOrder.verify(repo).findAll();`                                                          |

