# Fix Summary: Repeated Application Startup Log Messages

## Issue Description
You reported seeing repeated "Application started in X ms" log messages like:
```
[35m19:58:29.2[0;39m [32mINFO [0;39m ([36mApplication.java:56[0;39m) [31mApplication started in 93232 ms[0;39m
[35m19:58:29.4[0;39m [32mINFO [0;39m ([36mApplication.java:56[0;39m) [31mApplication started in 93444 ms[0;39m
[35m19:58:29.5[0;39m [32mINFO [0;39m ([36mApplication.java:56[0;39m) [31mApplication started in 93452 ms[0;39m
...
```

## Root Cause
**This issue is NOT related to your Java version changes.** The problem was in `Application.java` (line 53-56):

```java
app.addListeners(_ -> {
    final long endTime = System.nanoTime();
    final long durationMs = (endTime - startTime) / 1_000_000;
    LOGGER.info("Application started in {} ms", durationMs);
});
```

The `addListeners()` method adds a generic `ApplicationListener` that gets invoked for **EVERY Spring application event**, not just when the application starts. This caused the log message to be printed multiple times for different events:
- ContextRefreshedEvent
- ServletWebServerInitializedEvent  
- ApplicationStartedEvent
- ApplicationReadyEvent
- And others...

## Solution Applied
Changed the code to specifically listen for `ApplicationReadyEvent`:

```java
app.addListeners(event -> {
    if (event instanceof ApplicationReadyEvent) {
        final long endTime = System.nanoTime();
        final long durationMs = (endTime - startTime) / 1_000_000;
        LOGGER.info("Application started in {} ms", durationMs);
    }
});
```

This ensures the log message is only printed ONCE when the application is fully ready.

## Java Version Note
The codebase requires **Java 24** (or at least Java 21+) because it uses the `_` identifier for unused lambda parameters, which is a Java 21+ feature. The pom.xml has been restored to:

```xml
<java.version>24</java.version>
<maven.compiler.source>24</maven.compiler.source>
<maven.compiler.target>24</maven.compiler.target>
```

If you need to use Java 17, you would need to replace all `_` lambda parameters with a named identifier (e.g., `ignored` or `event`).

## Changes Made
1. **Application.java**: Added import for `ApplicationReadyEvent` and filtered events in the listener
2. **pom.xml**: Restored Java 24 configuration (as required by the codebase)

## Testing
You can verify the fix by running the application and checking that you only see ONE "Application started in X ms" message instead of multiple repeated messages.
