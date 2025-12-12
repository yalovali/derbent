# Fail-Fast Test Rules for Copilot Agent

## 🚨 CRITICAL RULE: FAIL-FAST ON EXCEPTIONS

**When ANY exception or critical error occurs during testing, IMMEDIATELY stop execution and fail the test. Do NOT continue with subsequent test steps.**

## ✅ Why Fail-Fast?

1. **Faster Bug Identification** - Stops at the first problem, not after multiple cascading failures
2. **Cleaner Logs** - No noise from secondary failures caused by the first issue  
3. **Faster Development Cycle** - Fix one issue at a time, re-run, repeat
4. **Easier Debugging** - Root cause is clearly visible without distractions

## 🛑 Fail-Fast Implementation Rules

### 1. **Exception Handling Pattern**
```java
// ❌ WRONG - Log and continue
try {
    criticalOperation();
} catch (Exception e) {
    LOGGER.warn("Operation failed: {}", e.getMessage()); // Just log and continue
}

// ✅ CORRECT - Fail immediately  
try {
    criticalOperation();
} catch (Exception e) {
    LOGGER.error("❌ CRITICAL: Operation failed: {}", e.getMessage());
    throw new RuntimeException("FAIL-FAST: Critical operation failed", e);
}
```

### 2. **Critical Conditions That Must Fail-Fast**
- **Database reset/initialization failures**
- **Login failures** 
- **UI element not found** (buttons, dialogs, menus)
- **Network/connection errors**
- **Configuration/setup errors**
- **Data validation failures**

### 3. **Logging Levels for Fail-Fast**
- Use `LOGGER.error("❌ CRITICAL: ...")` for fail-fast conditions
- Include "FAIL-FAST:" prefix in exception messages
- Log debug information before throwing exception
- Use descriptive error messages that explain impact

### 4. **Test Structure Pattern**
```java
@Test
void testWorkflow() {
    try {
        // Step 1: Setup (fail-fast on setup issues)
        setupDatabase(); // Throws exception if fails
        
        // Step 2: Login (fail-fast on login issues)  
        login(); // Throws exception if fails
        
        // Step 3: Main test logic (fail-fast on business logic issues)
        performBusinessLogic(); // Throws exception if fails
        
        // Step 4: Assertions
        verifyResults();
        
    } catch (Exception e) {
        LOGGER.error("❌ Test failed at step: {}", e.getMessage());
        throw e; // Re-throw to fail the test immediately
    }
}
```

### 5. **Specific Fail-Fast Scenarios for Derbent**

#### Database Reset
```java
// If DB reset button not found
throw new RuntimeException("FAIL-FAST: Database reset button not available");

// If confirmation dialog doesn't appear  
throw new RuntimeException("FAIL-FAST: No confirmation dialog after DB reset click");

// If no companies created after reset
throw new RuntimeException("FAIL-FAST: Database reset failed - no companies created");
```

#### Login Process
```java
// If login form not found
throw new RuntimeException("FAIL-FAST: Login form not accessible");

// If login credentials rejected
throw new RuntimeException("FAIL-FAST: Login authentication failed");

// If post-login redirect fails
throw new RuntimeException("FAIL-FAST: Application not accessible after login");
```

#### UI Navigation  
```java
// If menu not rendered
throw new RuntimeException("FAIL-FAST: Main navigation menu not rendered");

// If page navigation fails
throw new RuntimeException("FAIL-FAST: Cannot navigate to required page");
```

## 🎯 Implementation Guidelines

### Maven Configuration
Add to `pom.xml` for immediate test failure:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <failFast>true</failFast>
    </configuration>
</plugin>
```

### Playwright Configuration
```java
// Set shorter timeouts to fail faster
page.setDefaultTimeout(5000); // 5 seconds instead of 30 seconds

// Fail immediately on navigation errors
page.onRequestFailed(request -> {
    throw new RuntimeException("FAIL-FAST: Request failed: " + request.url());
});
```

### Test Method Structure
1. **Preconditions**: Validate environment setup
2. **Setup**: Initialize required state (database, login, etc.)
3. **Execute**: Run test scenario  
4. **Verify**: Assert expected outcomes
5. **Each step must fail-fast on any error**

## 📋 Fail-Fast Checklist

Before running any test:
- [ ] All critical operations wrapped in fail-fast exception handling
- [ ] Timeout values set to fail quickly (5-10 seconds max)
- [ ] Clear error messages that explain what failed and why
- [ ] No `catch` blocks that suppress exceptions  
- [ ] No warnings logged without throwing exceptions for critical issues

## 🔧 Benefits for Copilot Agent Development

1. **Faster Iteration Cycles** - Fix one thing, test, repeat
2. **Cleaner Bug Reports** - One clear failure per test run
3. **Better Resource Usage** - Don't waste time on broken scenarios  
4. **Easier Automation** - Predictable failure points
5. **Improved Code Quality** - Forces handling of edge cases

## ⚡ Quick Reference

**REMEMBER**: If it's critical for the test to continue, it should throw an exception if it fails. Only log warnings for non-critical issues that don't affect test validity.

**FAIL-FAST = FAST FEEDBACK = FASTER DEVELOPMENT**