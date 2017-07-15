# Chains

Dead simple circuit breaker written just for fun.

Chains features a three-state circuit breaker mechanism with the ability to run calls synchronously or asynchronously.

## Usage

First create a `CircuitDescriptor` that describes the attributes of a circuit, most importantly a `ThrowingRunnable`:

~~~~Java
final CircuitDescriptor descriptor = CircuitDescriptor.wrapping(() -> { throw new NullPointerException(); })
            .withThreshold(3)
            .withDelay(Duration.of(1, MINUTES))
            .build();
~~~~

Then construct the actual `Circuit` using the previously created descriptor:

~~~~Java
final Circuit circuit = CircuitFactory.fromDescriptor(descriptor);
~~~~

Yaaay! You're now able to dispatch calls through the circuit!

~~~~Java
circuit.executeSync().ifPresent(System.out::println);
~~~~

If you're using async calls too (through `Circuit.executeAsync()`), then don't forget to call 

~~~~Java
Chains.shutdown();
~~~~

somewhere at the end of your application.
