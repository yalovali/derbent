# Spring Boot Dependency Injection & Testing Guidelines

**Version**: 1.0  
**Date**: 2026-03-31  
**Status**: MANDATORY - All developers MUST follow these rules  
**Based on**: Comprehensive debugging session that resolved critical dependency injection issues

## Table of Contents

1. [Spring Dependency Injection Rules](#1-spring-dependency-injection-rules)
2. [Profile-Based Service Configuration](#2-profile-based-service-configuration)
3. [JPA vs Component Scanning Separation](#3-jpa-vs-component-scanning-separation)
4. [Email Services Pattern](#4-email-services-pattern)
5. [PageService Simplification Pattern](#5-pageservice-simplification-pattern)
6. [Testing Configuration Rules](#6-testing-configuration-rules)
7. [Playwright Test Configuration](#7-playwright-test-configuration)
8. [Troubleshooting Checklist](#8-troubleshooting-checklist)

---

## 1. Spring Dependency Injection Rules

### 1.1 Interface vs Abstract Class Injection (CRITICAL)

**RULE**: Always inject interfaces, never abstract classes with Spring annotations.

#### ✅ CORRECT - Inject Interface
```java
@Service
public class CEmailProcessorService {
    private final ISystemSettingsService systemSettingsService;  // ✅ Interface injection
    
    public CEmailProcessorService(
        final IEmailRepository repository,
        final ISessionService sessionService,
        final ISystemSettingsService systemSettingsService) {  // ✅ Interface parameter
        
        this.systemSettingsService = systemSettingsService;
    }
}
```

#### ❌ INCORRECT - Inject Abstract Class
```java
@Service
public class CEmailProcessorService {
    private final CSystemSettingsService<?> systemSettingsService;  // ❌ Abstract class injection
    
    public CEmailProcessorService(
        final IEmailRepository repository,
        final ISessionService sessionService,
        final CSystemSettingsService<?> systemSettingsService) {  // ❌ Abstract class parameter
        
        this.systemSettingsService = systemSettingsService;
    }
}
```

**Why**: Abstract classes with `@PreAuthorize` but no `@Service` annotation cannot be injected by Spring. Interfaces provide the correct abstraction layer.

### 1.2 Service Implementation Pattern

**RULE**: Use `@Service` on concrete implementations, implement interfaces, avoid `@PreAuthorize` on abstract classes without `@Service`.

#### ✅ CORRECT Pattern
```java
// Interface - defines contract
public interface ISystemSettingsService {
    String getSystemSetting(String key);
    void setSystemSetting(String key, String value);
}

// Concrete implementation - injectable bean
@Service
@Profile({"derbent", "test", "default"})  // ✅ Multiple profiles supported
@PreAuthorize("isAuthenticated()")
public class CSystemSettings_DerbentService extends CEntityOfCompanyService<CSystemSettings_Derbent> 
        implements ISystemSettingsService {
    
    @Override
    public String getSystemSetting(String key) {
        // Implementation
    }
}
```

#### ❌ INCORRECT Pattern
```java
// ❌ Abstract class with @PreAuthorize but no @Service - NOT injectable!
@PreAuthorize("isAuthenticated()")
public abstract class CSystemSettingsService<T> extends CAbstractService<T> {
    // Cannot be injected by Spring
}
```

---

## 2. Profile-Based Service Configuration

### 2.1 Profile Support Rules (MANDATORY)

**RULE**: Services must explicitly declare which profiles they support. Include "test" and "default" profiles for services needed in testing.

#### ✅ CORRECT - Complete Profile Support
```java
@Service
@Profile({"derbent", "test", "default"})  // ✅ Supports multiple profiles including test
@PreAuthorize("isAuthenticated()")
public class CSystemSettings_DerbentService extends CEntityOfCompanyService<CSystemSettings_Derbent> {
    // Available in derbent, test, and default profiles
}

@Service
@Profile({"bab"})  // ✅ BAB-specific service
@PreAuthorize("isAuthenticated()")
public class CBabGatewayService extends CEntityOfProjectService<CBabGateway> {
    // Only available in BAB profile
}
```

#### ❌ INCORRECT - Missing Test Profile
```java
@Service
@Profile("derbent")  // ❌ Missing "test" and "default" - will break tests!
@PreAuthorize("isAuthenticated()")
public class CSystemSettings_DerbentService extends CEntityOfCompanyService<CSystemSettings_Derbent> {
    // NOT available in test profile - dependency injection fails in tests
}
```

### 2.2 Profile Architecture

| Profile | Purpose | Services Included | JPA Entity Scanning |
|---------|---------|-------------------|---------------------|
| **`derbent`** | Production PLM deployment | PLM entities + API base | PLM packages only |
| **`bab`** | BAB Gateway deployment | BAB entities + API base | All packages |
| **`test`** | Unit & integration testing | All services needed for tests | Test-specific configuration |
| **`default`** | Fallback profile | Core services | Core packages |

### 2.3 Repository Profile Rules

**RULE**: Repositories must match their service profiles and include `default` profile.

```java
@Profile({"derbent", "test", "default"})  // ✅ Matches service profiles
public interface ISystemSettings_DerbentRepository extends IEntityOfCompanyRepository<CSystemSettings_Derbent> {
    // Repository implementation
}
```

---

## 3. JPA vs Component Scanning Separation

### 3.1 The Critical Discovery

**CRITICAL INSIGHT**: JPA entity scanning and Spring component scanning are **separate systems**:
- **Component scanning** respects `@Profile` annotations
- **JPA entity scanning** processes ALL `@Entity` classes regardless of profile
- This causes schema conflicts when BAB entities are processed in derbent deployments

### 3.2 Profile-Specific JPA Configuration (MANDATORY)

**RULE**: Create separate JPA configurations for each profile to prevent entity conflicts.

#### ✅ CORRECT - Derbent Profile JPA Config
```java
@Configuration
@Profile("derbent")
@EnableJpaRepositories(
    basePackages = {
        "tech.derbent.api",
        "tech.derbent.plm",
        "tech.derbent.base"
    }
)
@EntityScan(
    basePackages = {
        "tech.derbent.api",
        "tech.derbent.plm", 
        "tech.derbent.base"
    }
)
public class CDerbentJpaConfig {
    // Only scans derbent packages - excludes BAB entities
}
```

#### ✅ CORRECT - BAB Profile JPA Config
```java
@Configuration
@Profile("bab")
@EnableJpaRepositories(basePackages = "tech.derbent")
@EntityScan(basePackages = "tech.derbent")
public class CBabJpaConfig {
    // Scans all packages - includes BAB entities
}
```

#### ❌ INCORRECT - Global JPA Config
```java
// ❌ DON'T put @EnableJpaRepositories and @EntityScan on Application class
@SpringBootApplication
@EnableJpaRepositories(basePackages = "tech.derbent")  // ❌ Causes conflicts!
@EntityScan(basePackages = "tech.derbent")             // ❌ Scans ALL entities!
public class Application {
    // This processes BAB entities even in derbent profile
}
```

### 3.3 Application Class Configuration

**RULE**: Keep Application class clean, delegate to profile-specific configurations.

```java
@SpringBootApplication
public class Application {
    // ✅ Clean - no JPA annotations
    // JPA configuration handled by profile-specific @Configuration classes
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 4. Email Services Pattern

### 4.1 Email Service Dependencies (LEARNED PATTERN)

**ISSUE DISCOVERED**: Email services with complex PageService dependencies create cascading injection failures.

#### ✅ CORRECT - Simplified Email Service Pattern
```java
@Service
@Profile({"derbent", "test", "default"})
@PreAuthorize("isAuthenticated()")
public class CEmailQueuedService extends CEntityOfCompanyService<CEmailQueued> 
        implements IEntityRegistrable, IEntityWithView {
    
    // ✅ Use generic PageService instead of custom one
    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceEntityDB.class;  // ✅ Generic, no complex dependencies
    }
    
    @Override
    public Class<?> getInitializerServiceClass() {
        return CEmailQueuedInitializerService.class;
    }
}
```

#### ❌ INCORRECT - Complex PageService Dependencies
```java
@Service
public class CEmailQueuedService extends CEntityOfCompanyService<CEmailQueued> {
    
    // ❌ Custom PageService with complex View dependencies
    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceEmailQueued.class;  // ❌ Complex dependencies cause failures
    }
}

// ❌ Complex PageService that requires specific View implementations
@Service
public class CPageServiceEmailQueued extends CPageServiceEntityDB<CEmailQueued> {
    // ❌ Requires CEmailQueuedView which may not be available in test context
    public CPageServiceEmailQueued(final CEmailQueuedView view) {
        super(view);  // ❌ Circular dependency risk
    }
}
```

### 4.2 Email Service Simplification Strategy

When email services fail with PageService dependency issues:

1. **Remove custom PageService classes** (CPageServiceEmailQueued, CPageServiceEmailSent)
2. **Use generic `CPageServiceEntityDB`** for basic functionality
3. **Keep service registration** (`IEntityRegistrable`, `IEntityWithView`)
4. **Maintain required imports** (add missing repository imports)

---

## 5. PageService Simplification Pattern

### 5.1 When to Use Generic vs Custom PageServices

#### ✅ Use Generic `CPageServiceEntityDB` When:
- Basic CRUD operations are sufficient
- Entity doesn't require complex view logic
- Service is primarily used for data access
- Testing/development environment needs

#### ✅ Use Custom PageService When:
- Complex view interactions required
- Specialized UI behavior needed
- Custom validation or workflow logic
- Production-specific features

### 5.2 Generic PageService Pattern

```java
@Service
public class CSimpleEntityService extends CEntityOfCompanyService<CSimpleEntity> 
        implements IEntityRegistrable, IEntityWithView {
    
    @Override
    public Class<?> getPageServiceClass() {
        return CPageServiceEntityDB.class;  // ✅ Generic - always available
    }
    
    @Override
    public Class<?> getServiceClass() {
        return this.getClass();
    }
    
    @Override
    public Class<?> getInitializerServiceClass() {
        return CSimpleEntityInitializerService.class;
    }
}
```

---

## 6. Testing Configuration Rules

### 6.1 Spring Boot Test Configuration (MANDATORY)

**RULE**: All Spring Boot tests MUST include complete configuration with profiles and ports.

#### ✅ CORRECT - Complete Test Configuration
```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource(properties = {
    "spring.profiles.active=test",           // ✅ MANDATORY - Sets test profile
    "server.port=0",                         // ✅ MANDATORY - Random port assignment
    "spring.datasource.url=jdbc:h2:mem:testdb", 
    "spring.datasource.username=sa", 
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver", 
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Test Description")
public class CMyTest {
    
    @LocalServerPort
    private int port;  // ✅ MANDATORY - Injected random port
    
    // Test methods
}
```

#### ❌ INCORRECT - Incomplete Test Configuration
```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource(properties = {
    // ❌ MISSING: spring.profiles.active=test
    // ❌ MISSING: server.port=0
    "spring.datasource.url=jdbc:h2:mem:testdb", 
    "spring.datasource.username=sa", 
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver", 
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class CMyTest {
    // ❌ Will fail: No profile set, port conflicts possible
}
```

### 6.2 Test Profile Requirements

**RULE**: Services and repositories needed in tests MUST support "test" profile.

**Verification Command**:
```bash
# Find services missing test profile support
grep -r "@Service" src/main/java --include="*Service.java" | \
  xargs -I {} grep -L "@Profile.*test" {} | \
  grep -v "CAbstractService\|CPageService"
```

---

## 7. Playwright Test Configuration

### 7.1 Playwright Spring Boot Integration

**RULE**: Playwright tests MUST use proper Spring Boot test configuration and timeouts.

#### ✅ CORRECT - Playwright Test Setup
```java
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource(properties = {
    "spring.profiles.active=test",           // ✅ Critical for proper service loading
    "server.port=0",                         // ✅ Critical for port availability
    "spring.datasource.url=jdbc:h2:mem:testdb", 
    "spring.datasource.username=sa", 
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver", 
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("UI Test Description")
public class CPlaywrightTest extends CBaseUITest {
    // Playwright test implementation
}
```

### 7.2 Playwright Timeout Configuration

**RULE**: Increase timeouts to accommodate Spring Boot startup time (~25 seconds).

#### ✅ CORRECT - Adequate Timeouts
```java
// In CBaseUITest.java
protected void wait_loginscreen() {
    try {
        page.waitForSelector("#custom-username-input, #custom-password-input, #" + LOGIN_BUTTON_ID,
                new Page.WaitForSelectorOptions().setTimeout(45000));  // ✅ 45 seconds for Spring Boot startup
    } catch (final Exception e) {
        LOGGER.warn("⚠️ Login screen not detected: {}", e.getMessage());
    }
}

private Locator waitForResetDbFullButton() {
    try {
        page.waitForSelector("#" + RESET_DB_FULL_BUTTON_ID, 
            new Page.WaitForSelectorOptions().setTimeout(45000));  // ✅ 45 seconds timeout
    } catch (final Exception e) {
        LOGGER.warn("⚠️ Reset button not detected: {}", e.getMessage());
    }
    return page.locator("#" + RESET_DB_FULL_BUTTON_ID);
}
```

#### ❌ INCORRECT - Insufficient Timeouts
```java
// ❌ 15 seconds is too short for Spring Boot startup
page.waitForSelector("#login-form", new Page.WaitForSelectorOptions().setTimeout(15000));
```

---

## 8. Troubleshooting Checklist

### 8.1 Dependency Injection Failure Checklist

When you see: `required a bean of type 'X' that could not be found`

**Check in order**:

1. **✅ Interface vs Abstract Class**: Are you injecting an interface or abstract class?
   - Fix: Change to interface injection

2. **✅ Service Profile Support**: Does the service support the active profile?
   - Fix: Add missing profile (especially "test" and "default")

3. **✅ Repository Profile Match**: Does the repository match service profiles?
   - Fix: Update repository `@Profile` annotation

4. **✅ JPA Configuration**: Are entities being scanned in wrong profile?
   - Fix: Check profile-specific JPA configurations

5. **✅ Circular Dependencies**: Do services have circular PageService dependencies?
   - Fix: Use generic `CPageServiceEntityDB`

6. **✅ Missing Imports**: Are all required imports present?
   - Fix: Add missing repository imports

### 8.2 Test Failure Checklist

When Playwright tests fail to start:

1. **✅ Test Configuration**: Does test include `spring.profiles.active=test` and `server.port=0`?
2. **✅ Service Availability**: Are all required services available in "test" profile?
3. **✅ Timeout Configuration**: Are timeouts sufficient for Spring Boot startup (~25s)?
4. **✅ JPA Test Config**: Is there a test-specific JPA configuration if needed?

### 8.3 Profile Configuration Verification

**Verification Commands**:
```bash
# 1. Find services missing test profile
find src/main/java -name "*Service.java" -exec grep -l "implements.*IEntity" {} \; | \
  xargs grep -L "@Profile.*test"

# 2. Find repositories missing profile annotations
find src/main/java -name "*Repository.java" -exec grep -L "@Profile" {} \;

# 3. Check for global JPA annotations on Application class
grep -n "@EnableJpaRepositories\|@EntityScan" src/main/java/**/Application.java

# 4. Verify profile-specific JPA configurations exist
ls src/main/java/tech/derbent/api/config/*JpaConfig.java
```

---

## 9. Success Verification

### 9.1 Application Startup Verification

**Test all profiles work**:
```bash
# Test derbent profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=derbent"

# Test bab profile  
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=bab"

# Test default profile
mvn spring-boot:run

# Test compilation with test profile
mvn clean compile -Dspring.profiles.active=test
```

### 9.2 Test Verification

**Verify test infrastructure**:
```bash
# Run simple Spring Boot test
mvn test -Dtest=*StartupTest -Dspring.profiles.active=test

# Run Playwright tests
./run-playwright-tests.sh menu
```

---

## 10. Key Lessons Learned

### 10.1 Critical Insights

1. **JPA ≠ Component Scanning**: These are separate systems with different profile handling
2. **Interface Injection**: Always inject interfaces, not abstract classes with Spring annotations
3. **Profile Completeness**: Services must explicitly declare ALL profiles they support
4. **Test Configuration**: Complete configuration including profiles and ports is mandatory
5. **Service Simplification**: Use generic PageServices when complex dependencies cause issues

### 10.2 Development Workflow

1. **Define Interface First**: Create service interfaces before implementations
2. **Configure Profiles Early**: Add profile support from the beginning
3. **Test Profile Support**: Always include "test" in service profiles
4. **Verify JPA Scanning**: Ensure entities are scanned only in appropriate profiles
5. **Start Simple**: Use generic services first, specialize only when needed

---

## 11. Emergency Fixes

### 11.1 Quick Dependency Injection Fix

If you see dependency injection errors:

```bash
# 1. Quick check - find the failing service
grep -r "required a bean of type" target/

# 2. Check if it's interface vs abstract class issue
grep -n "private final.*Service" src/main/java/path/to/FailingService.java

# 3. Quick fix - change to interface injection
# From: private final CAbstractService service;
# To:   private final IAbstractService service;

# 4. Verify profiles
grep -A5 -B5 "@Profile" src/main/java/path/to/FailingService.java
```

### 11.2 Quick Test Fix

If tests fail to start:

```bash
# 1. Add missing test configuration
# Add to @TestPropertySource:
# "spring.profiles.active=test",
# "server.port=0",

# 2. Check service profile support
grep -r "@Profile" src/main/java/path/to/service/ | grep -v test
```

---

## Conclusion

Following these guidelines prevents the complex dependency injection and configuration issues we encountered. The key is understanding the separation between JPA entity scanning and Spring component scanning, proper profile configuration, and complete test setup.

**Remember**: When in doubt, inject interfaces, support multiple profiles, and configure tests completely!