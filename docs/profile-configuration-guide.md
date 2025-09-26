# Profile Configuration Best Practices

## Quick Reference for Preventing Bean Dependency Issues

### Problem Symptoms
- `NoSuchBeanDefinitionException` during test execution
- Tests fail with "No qualifying bean of type" errors
- Application starts successfully but tests fail
- Services can't find expected dependencies

### Root Cause Analysis
1. **Profile Gaps**: Service only available in specific profiles
2. **Type Mismatches**: Service expects concrete class, gets interface
3. **Missing Bridges**: No configuration to connect different implementations

### Solution Patterns

#### Pattern 1: Complete Profile Coverage
```java
// ✅ Reset-db profile
@Profile("reset-db")
@Service
public class CSessionService implements ISessionService { }

// ✅ All other profiles  
@Profile("!reset-db")
@ConditionalOnWebApplication
@Service("CSessionService") 
public class CWebSessionService implements ISessionService { }
```

#### Pattern 2: Configuration Bridge
```java
// ✅ Type compatibility bridge
@Configuration
public class ServiceConfig {
    @Bean
    @Primary
    @Profile("!reset-db")
    public CSessionService bridge(CWebSessionService web, ...) {
        return new CSessionService(...) { /* delegate to web */ };
    }
}
```

### Testing Validation
```bash
# Must all pass:
mvn test -Dtest=PlaywrightInfrastructureTest  # Default profile
mvn test -Dspring.profiles.active=test        # Test profile  
mvn clean compile -Preset-db                  # Reset-db profile
```

### Prevention Checklist
- [ ] Every profile has required service beans
- [ ] Type compatibility maintained across profiles
- [ ] Configuration bridges added where needed
- [ ] All profile combinations tested
- [ ] Documentation updated with profile requirements

### Common Profiles in Derbent
- **default**: Web application, full features
- **test**: Web application for testing
- **reset-db**: Database reset utility, no web features
- **production**: Optimized web application

Keep this document handy when working with profile-based configurations!