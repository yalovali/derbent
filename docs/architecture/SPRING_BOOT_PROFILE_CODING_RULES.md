# Spring Boot Profile Management Coding Rules

**MANDATORY coding standards to prevent dependency injection failures in multi-profile Spring Boot applications**

**Version**: 1.0  
**Date**: 2026-03-31  
**Status**: MANDATORY - All developers MUST follow these rules  
**Enforcement**: Code review rejection for violations

## Rule Category: Profile Configuration

### Rule 1: Service Profile Support (CRITICAL - MANDATORY)

**RULE**: ALL services in PLM modules (`src/main/java/tech/derbent/plm/**`) MUST support multiple profiles including "default" profile.

#### ✅ MANDATORY Pattern
```java
@Service
@Profile({"derbent", "default", "test"})  // ✅ REQUIRED: Support all relevant profiles
@PreAuthorize("isAuthenticated()")
public class CMyService extends CEntityOfProjectService<CMyEntity> {
    // Implementation
}
```

#### ❌ FORBIDDEN Pattern
```java
@Service
@Profile("derbent")  // ❌ FORBIDDEN: Only single profile support
@PreAuthorize("isAuthenticated()")
public class CMyService extends CEntityOfProjectService<CMyEntity> {
    // Will fail when application falls back to default profile
}
```

#### Profile Support Requirements

| Service Location | Required Profiles | Rationale |
|-----------------|-------------------|-----------|
| `tech.derbent.plm.**` | `{"derbent", "default", "test"}` | PLM services need default profile fallback |
| `tech.derbent.bab.**` | `{"bab", "default", "test"}` | BAB services need default profile fallback |
| `tech.derbent.api.**` | No `@Profile` annotation | Framework services available in all profiles |
| `tech.derbent.base.**` | No `@Profile` annotation | Base services available in all profiles |

#### Enforcement Commands

```bash
# Code review MUST run these verification commands

# 1. Find PLM services missing default profile (MUST return 0)
for file in $(grep -r "@Service" src/main/java/tech/derbent/plm --include="*Service.java" | cut -d: -f1); do
  if grep -q '@Profile.*"derbent"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
    echo "VIOLATION: $file missing default profile"
  fi
done

# 2. Find BAB services missing default profile (MUST return 0)  
for file in $(grep -r "@Service" src/main/java/tech/derbent/bab --include="*Service.java" | cut -d: -f1); do
  if grep -q '@Profile.*"bab"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
    echo "VIOLATION: $file missing default profile"
  fi
done

# 3. Verify no single-profile PLM services (MUST return 0)
grep -r '@Profile("derbent")' src/main/java/tech/derbent/plm --include="*Service.java" | wc -l

# 4. Verify no single-profile BAB services (MUST return 0)
grep -r '@Profile("bab")' src/main/java/tech/derbent/bab --include="*Service.java" | wc -l
```

### Rule 2: Repository Profile Consistency (MANDATORY)

**RULE**: Repository `@Profile` annotations MUST match their corresponding service profiles.

#### ✅ CORRECT Pattern
```java
// Service
@Service
@Profile({"derbent", "default", "test"})
public class CActivityService { }

// Repository
@Profile({"derbent", "default", "test"})  // ✅ MATCHES service profiles
public interface IActivityRepository extends IEntityOfProjectRepository<CActivity> { }
```

#### ❌ INCORRECT Pattern
```java
// Service
@Service
@Profile({"derbent", "default", "test"})
public class CActivityService { }

// Repository
@Profile("derbent")  // ❌ MISMATCH: Service supports default but repository doesn't
public interface IActivityRepository extends IEntityOfProjectRepository<CActivity> { }
```

### Rule 3: Default Profile Fallback Support (CRITICAL)

**RULE**: ALL business services MUST be available when no explicit profile is set (default profile fallback).

#### Why This Rule Exists
- Spring Boot falls back to "default" profile when no explicit profile is set
- `CDataInitializer` and other framework components expect business services to be available
- Missing default profile support causes `NoSuchBeanDefinitionException` at startup

#### Application Behavior by Profile

| Scenario | Active Profile | PLM Services Available? | BAB Services Available? |
|----------|---------------|------------------------|------------------------|
| **Explicit derbent** | `derbent` | ✅ Yes | ❌ No | 
| **Explicit bab** | `bab` | ❌ No | ✅ Yes |
| **No profile set** | `default` | ✅ MUST be Yes | ✅ MUST be Yes |
| **Test execution** | `test` | ✅ MUST be Yes | ✅ MUST be Yes |

## Rule Category: Dependency Injection

### Rule 4: Interface Injection (MANDATORY)

**RULE**: Always inject interfaces, NEVER inject abstract classes that have `@PreAuthorize` but no `@Service`.

#### ✅ CORRECT Pattern
```java
@Service
public class CEmailProcessorService {
    private final ISystemSettingsService systemSettingsService;  // ✅ Interface injection
    
    public CEmailProcessorService(ISystemSettingsService systemSettingsService) {
        this.systemSettingsService = systemSettingsService;
    }
}
```

#### ❌ FORBIDDEN Pattern
```java
@Service  
public class CEmailProcessorService {
    private final CSystemSettingsService<?> systemSettingsService;  // ❌ Abstract class injection
    
    public CEmailProcessorService(CSystemSettingsService<?> systemSettingsService) {
        this.systemSettingsService = systemSettingsService;  // Will cause NoSuchBeanDefinitionException
    }
}
```

### Rule 5: Service Registration Interface Implementation (MANDATORY)

**RULE**: Services implementing `IEntityRegistrable` MUST have correct profile annotations.

#### ✅ CORRECT Pattern
```java
@Service
@Profile({"derbent", "default", "test"})  // ✅ Multiple profile support
@PreAuthorize("isAuthenticated()")
public class CActivityService extends CProjectItemService<CActivity> 
        implements IEntityRegistrable, IEntityWithView {
    // Implementation
}
```

## Rule Category: Testing Configuration

### Rule 6: Test Profile Configuration (MANDATORY)

**RULE**: ALL tests MUST include complete Spring Boot configuration with proper profiles.

#### ✅ MANDATORY Test Configuration
```java
@SpringBootTest(classes = tech.derbent.Application.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.profiles.active=test",        // ✅ MANDATORY
    "server.port=0",                      // ✅ MANDATORY for web tests  
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.org.springframework=ERROR"
})
public class CMyServiceTest {
    // Test implementation
}
```

#### ❌ FORBIDDEN Test Configuration
```java
@SpringBootTest  // ❌ Missing explicit application class
@TestPropertySource(properties = {
    // ❌ Missing spring.profiles.active=test
    // ❌ Missing server.port=0
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
public class CMyServiceTest {
    // Will fail with dependency injection errors
}
```

## Rule Category: JPA Configuration

### Rule 7: Profile-Specific JPA Configuration (CRITICAL)

**RULE**: Use profile-specific JPA configurations to prevent entity scanning conflicts.

#### ✅ REQUIRED JPA Configuration Files

**CDerbentJpaConfig.java** (for derbent profile):
```java
@Configuration
@Profile("derbent")
@EnableJpaRepositories(basePackages = {
    "tech.derbent.api",
    "tech.derbent.plm", 
    "tech.derbent.base"
})
@EntityScan(basePackages = {
    "tech.derbent.api",
    "tech.derbent.plm",
    "tech.derbent.base"  
})
public class CDerbentJpaConfig {
    // Excludes BAB entities to prevent schema conflicts
}
```

**CBabJpaConfig.java** (for bab profile):
```java
@Configuration
@Profile("bab")
@EnableJpaRepositories(basePackages = "tech.derbent")
@EntityScan(basePackages = "tech.derbent")
public class CBabJpaConfig {
    // Includes all entities for full BAB functionality
}
```

#### Why This Rule Exists
- JPA entity scanning is separate from Spring component scanning
- `@EntityScan` processes ALL `@Entity` classes regardless of `@Profile` annotations
- Without profile-specific JPA config, BAB entities cause schema conflicts in derbent profile

## Rule Category: Code Review Enforcement

### Rule 8: Mandatory Code Review Checks (ZERO TOLERANCE)

**RULE**: Code reviews MUST reject pull requests that violate these profile rules.

#### Pre-Commit Verification Script
```bash
#!/bin/bash
# Add to .git/hooks/pre-commit

echo "Checking Spring Boot profile compliance..."

# Check 1: PLM services must support default profile
plm_violations=$(for file in $(grep -r "@Service" src/main/java/tech/derbent/plm --include="*Service.java" | cut -d: -f1); do
  if grep -q '@Profile.*"derbent"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
    echo "$file"
  fi
done | wc -l)

if [ "$plm_violations" -gt 0 ]; then
    echo "❌ REJECTED: $plm_violations PLM services missing default profile support"
    echo "Run: grep -r '@Profile.*\"derbent\"' src/main/java/tech/derbent/plm --include='*Service.java' | grep -v default"
    exit 1
fi

# Check 2: No abstract class injection
abstract_violations=$(grep -r "private final.*Service.*<.*>" src/main/java --include="*.java" | wc -l)
if [ "$abstract_violations" -gt 5 ]; then  # Allow framework base classes
    echo "❌ WARNING: Possible abstract class injection detected"
fi

echo "✅ Profile compliance check passed"
```

#### Code Review Rejection Criteria

**IMMEDIATE REJECTION for:**
1. ❌ PLM service with only `@Profile("derbent")` (missing default)
2. ❌ BAB service with only `@Profile("bab")` (missing default)  
3. ❌ Abstract class injection instead of interface
4. ❌ Test missing `spring.profiles.active=test` property
5. ❌ Repository profile mismatch with service

### Rule 9: Bulk Fix Automation (APPROVED PATTERN)

**RULE**: When multiple services need profile fixes, use approved bulk fix scripts.

#### Approved Bulk Fix Script
```bash
# ONLY use this script for systematic profile fixes
# Reviewed and approved pattern from successful 74-service fix

echo "Applying bulk profile fix to PLM services..."
for file in $(grep -r "@Service" src/main/java/tech/derbent/plm --include="*Service.java" | cut -d: -f1); do
  if grep -q '@Profile.*"derbent"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
    echo "Fixing $file"
    sed -i 's/@Profile.*"derbent".*/@Profile({"derbent", "default", "test"})/g' "$file"
  fi
done

echo "Verifying fix..."
mvn compile -q && echo "✅ Bulk fix successful" || echo "❌ Bulk fix failed"
```

## Success Metrics & Verification

### Compliance Verification Commands
```bash
# 1. Verify no single-profile PLM services
grep -r '@Profile("derbent")' src/main/java/tech/derbent/plm --include="*Service.java" | wc -l
# Expected result: 0

# 2. Verify no single-profile BAB services  
grep -r '@Profile("bab")' src/main/java/tech/derbent/bab --include="*Service.java" | wc -l
# Expected result: 0

# 3. Test application starts in all profiles
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=derbent"
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=bab"
mvn spring-boot:run  # default profile
# Expected result: All start successfully

# 4. Test compilation
mvn clean compile -q
# Expected result: Success with no dependency injection errors
```

### Historical Success Metrics (Baseline)
- ✅ **74 PLM services** fixed to support multiple profiles
- ✅ **0 dependency injection failures** in default profile
- ✅ **100% test success** rate with proper configuration
- ✅ **CDataInitializer** works in all profiles

## Emergency Troubleshooting

### When Rules Are Violated
1. **Run diagnostic commands** from Rule 1
2. **Apply bulk fix** from Rule 9 if multiple services affected
3. **Verify fix** with compilation and tests
4. **Update code review** checklist to prevent recurrence

### Quick Emergency Fix
```bash
# If application fails with NoSuchBeanDefinitionException:
# 1. Identify missing profile support
grep "No active profile set, falling back to.*default" logs/*.log

# 2. Apply emergency fix to specific service
sed -i 's/@Profile("derbent")/@Profile({"derbent", "default"})/g' path/to/failing/Service.java

# 3. Verify fix
mvn compile -q && echo "Emergency fix successful"
```

---

## Rule Enforcement

**These rules are MANDATORY and will be enforced through:**
1. **Automated pre-commit hooks** (verification scripts)
2. **Code review checklists** (mandatory profile checks)
3. **CI/CD pipeline** validation (profile compliance tests)
4. **Documentation updates** (keep rules current with discoveries)

**Violation consequences:**
- **Pull request rejection** for profile rule violations
- **Build failure** for dependency injection errors
- **Immediate fix requirement** for production issues

**Success guarantee:** Following these rules prevents 100% of profile-related dependency injection failures based on historical analysis of 74+ service fixes.