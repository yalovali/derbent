# Quick Reference: Spring Boot Dependency Injection - Emergency Checklist

**Quick debugging guide based on successful resolution of 74 PLM services dependency injection issue**

## 🚨 EMERGENCY: `NoSuchBeanDefinitionException`

### Problem: Application falls back to default profile but services only support derbent profile

**Quick Diagnosis:**
```bash
# Check what profile is active (should show "default" if issue exists)
grep "No active profile set" logs/*.log

# Find services missing default profile support
grep -r "@Service" src/main/java/tech/derbent/plm --include="*Service.java" | \
  cut -d: -f1 | while read file; do
    if grep -q '@Profile.*"derbent"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
      echo "$file"
    fi
  done
```

**Bulk Fix (PROVEN):**
```bash
# Apply to ALL PLM services missing default profile
for file in $(grep -r "@Service" src/main/java/tech/derbent/plm --include="*Service.java" | cut -d: -f1); do
  if grep -q '@Profile.*"derbent"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
    echo "Fixing $file"
    sed -i 's/@Profile.*"derbent".*/@Profile({"derbent", "default"})/g' "$file"
  fi
done
```

**Verification:**
```bash
# Compile should succeed
mvn compile -q && echo "✅ Compilation successful"

# Run test to verify beans are available  
mvn test -Dtest=CBeanAvailabilityTest -q
```

## 📊 Success Metrics for this Session

**Fixed in this session:**
- ✅ **74 PLM services** updated to support default profile
- ✅ **CDataInitializer** no longer fails with NoSuchBeanDefinitionException  
- ✅ **Bean availability test** passes in default profile
- ✅ **CSpringContext.getBean(CActivityService.class)** pattern works
- ✅ **Application compiles** without dependency injection errors

**Root Cause:** PLM services only had `@Profile("derbent")` but application was falling back to "default" profile when no explicit profile was set.

**Solution Pattern:** Add "default" to all PLM service profiles: `@Profile({"derbent", "default"})`

---

## Other Common Patterns (See Full Guidelines)

### Pattern 1: Interface vs Abstract Class
```java
// ❌ WRONG: private final CAbstractService<?> service;
// ✅ CORRECT: private final IAbstractService service;
```

### Pattern 2: Missing Test Profile
```java
// ❌ WRONG: @Profile("derbent")  
// ✅ CORRECT: @Profile({"derbent", "test", "default"})
```

### Pattern 3: JPA Entity Conflicts
- Create profile-specific JPA configurations
- Use CDerbentJpaConfig and CBabJpaConfig

---

**See SPRING_BOOT_DEPENDENCY_INJECTION_GUIDELINES.md for complete troubleshooting guide**