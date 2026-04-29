# CODING RULES ENFORCEMENT

**Mandatory enforcement mechanisms for Spring Boot Profile Coding Rules**

## Pre-Commit Hook Script

**File**: `.git/hooks/pre-commit`
**Status**: MANDATORY for all developers

```bash
#!/bin/bash
# Spring Boot Profile Compliance Pre-Commit Hook
# MANDATORY: Add this to .git/hooks/pre-commit

echo "🔍 Checking Spring Boot profile compliance..."

# Rule 1: PLM services must support default profile
echo "Checking PLM services for default profile support..."
plm_violations=0
for file in $(find src/main/java/tech/derbent/plm -name "*Service.java" 2>/dev/null); do
  if grep -q '@Profile.*"derbent"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
    echo "❌ VIOLATION: $file missing default profile"
    plm_violations=$((plm_violations + 1))
  fi
done

# Rule 2: BAB services must support default profile  
echo "Checking BAB services for default profile support..."
bab_violations=0
for file in $(find src/main/java/tech/derbent/bab -name "*Service.java" 2>/dev/null); do
  if grep -q '@Profile.*"bab"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
    echo "❌ VIOLATION: $file missing default profile"
    bab_violations=$((bab_violations + 1))
  fi
done

# Rule 3: Check for single-profile annotations
echo "Checking for single-profile violations..."
single_profile_plm=$(grep -r '@Profile("derbent")' src/main/java/tech/derbent/plm --include="*Service.java" 2>/dev/null | wc -l)
single_profile_bab=$(grep -r '@Profile("bab")' src/main/java/tech/derbent/bab --include="*Service.java" 2>/dev/null | wc -l)

# Summary
total_violations=$((plm_violations + bab_violations + single_profile_plm + single_profile_bab))

if [ $total_violations -gt 0 ]; then
    echo ""
    echo "❌ COMMIT REJECTED: $total_violations profile violations found"
    echo ""
    echo "Fix suggestions:"
    echo "1. Add 'default' to @Profile annotations: @Profile({\"profile\", \"default\", \"test\"})"
    echo "2. Run bulk fix script from SPRING_BOOT_PROFILE_CODING_RULES.md"
    echo "3. Verify with: mvn compile -q"
    echo ""
    exit 1
fi

echo "✅ Profile compliance check passed"
exit 0
```

## Code Review Checklist

**MANDATORY checklist for ALL pull requests touching service files**

### Profile Configuration Checklist

- [ ] **PLM Services**: All services in `tech.derbent.plm.**` include `"default"` in `@Profile` annotation
- [ ] **BAB Services**: All services in `tech.derbent.bab.**` include `"default"` in `@Profile` annotation  
- [ ] **Repository Profiles**: Repository profiles match corresponding service profiles
- [ ] **Test Configuration**: Tests include `spring.profiles.active=test` property
- [ ] **Interface Injection**: No abstract class injection in constructor parameters

### Automated Verification Commands

Run these commands during code review:

```bash
# 1. Check for profile violations (MUST return 0)
find src/main/java/tech/derbent/plm -name "*Service.java" | while read file; do
  if grep -q '@Profile.*"derbent"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
    echo "VIOLATION: $file"
  fi
done | wc -l

# 2. Check for single-profile PLM services (MUST return 0)
grep -r '@Profile("derbent")' src/main/java/tech/derbent/plm --include="*Service.java" | wc -l

# 3. Verify compilation succeeds
mvn clean compile -q && echo "✅ Compilation OK" || echo "❌ Compilation FAILED"
```

## CI/CD Pipeline Integration

**Add to Jenkins/GitHub Actions pipeline**

```yaml
# GitHub Actions example
name: Profile Compliance Check
on: [pull_request]

jobs:
  profile-compliance:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Check Profile Compliance
      run: |
        echo "Checking Spring Boot profile compliance..."
        
        # Count violations
        violations=0
        
        # Check PLM services
        for file in $(find src/main/java/tech/derbent/plm -name "*Service.java"); do
          if grep -q '@Profile.*"derbent"' "$file" && ! grep -q '@Profile.*"default"' "$file"; then
            echo "VIOLATION: $file missing default profile"
            violations=$((violations + 1))
          fi
        done
        
        # Check for single profiles
        single_profiles=$(grep -r '@Profile("derbent")' src/main/java/tech/derbent/plm --include="*Service.java" | wc -l)
        violations=$((violations + single_profiles))
        
        if [ $violations -gt 0 ]; then
          echo "❌ Profile compliance check failed: $violations violations"
          exit 1
        fi
        
        echo "✅ Profile compliance check passed"
    
    - name: Verify Compilation
      run: |
        mvn clean compile -q
```

## Maven Plugin Integration

**Add to pom.xml for automatic checking**

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>profile-compliance-check</id>
            <phase>validate</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>bash</executable>
                <arguments>
                    <argument>scripts/check-profile-compliance.sh</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Emergency Response Procedures

### When Profile Violations Are Detected

1. **Stop Development** - Do not merge until fixed
2. **Apply Emergency Fix**:
   ```bash
   # Quick fix for specific service
   sed -i 's/@Profile("derbent")/@Profile({"derbent", "default", "test"})/g' path/to/service.java
   ```
3. **Verify Fix**:
   ```bash
   mvn compile -q && echo "Fix successful"
   ```
4. **Update Documentation** if new patterns are discovered

### Production Emergency Response

If production fails with `NoSuchBeanDefinitionException`:

1. **Identify Profile Issue**:
   ```bash
   grep "No active profile set" /var/log/application/*.log
   ```
2. **Apply Hotfix**:
   ```bash
   # Emergency profile fix
   sed -i 's/@Profile("derbent")/@Profile({"derbent", "default"})/g' failing-service.java
   ```
3. **Redeploy** with fixed service
4. **Create Post-Incident** action item to update rules

## Developer Training & Onboarding

### New Developer Checklist

When onboarding new developers:

- [ ] **Read** `SPRING_BOOT_PROFILE_CODING_RULES.md`
- [ ] **Install** pre-commit hooks
- [ ] **Review** historical examples of profile fixes
- [ ] **Practice** with bulk fix scripts on test branch
- [ ] **Verify** understanding with code review simulation

### Common Mistakes Training

**Scenario-based training examples:**

1. **Mistake**: `@Profile("derbent")` only
   **Fix**: `@Profile({"derbent", "default", "test"})`
   **Why**: Application falls back to default profile

2. **Mistake**: Repository profile doesn't match service
   **Fix**: Sync repository and service profiles
   **Why**: Service available but repository isn't

3. **Mistake**: Abstract class injection
   **Fix**: Use interface injection
   **Why**: Abstract classes with @PreAuthorize aren't beans

## Monitoring & Metrics

### Success Metrics to Track

- **Profile Violation Rate**: Target 0% (zero violations in code reviews)
- **Startup Failure Rate**: Target 0% (no dependency injection failures)
- **Fix Application Time**: Target <5 minutes (when violations found)
- **Developer Compliance**: Target 100% (all developers follow rules)

### Monitoring Dashboard

Track these metrics in development dashboard:

```bash
# Daily compliance report
echo "=== Daily Profile Compliance Report ===" 
echo "PLM single-profile violations: $(grep -r '@Profile("derbent")' src/main/java/tech/derbent/plm --include="*Service.java" | wc -l)"
echo "BAB single-profile violations: $(grep -r '@Profile("bab")' src/main/java/tech/derbent/bab --include="*Service.java" | wc -l)"
echo "Services without default profile: $(find . -name "*Service.java" | while read f; do grep -q '@Profile.*"derbent"' "$f" && ! grep -q '@Profile.*"default"' "$f" && echo "$f"; done | wc -l)"
echo "Last compliance check: $(date)"
```

---

## Implementation Timeline

**Phase 1**: Install enforcement tools (Week 1)
- Add pre-commit hooks to all developer machines
- Update code review checklist
- Add CI/CD pipeline checks

**Phase 2**: Developer training (Week 2)  
- Conduct training sessions on profile rules
- Review historical examples and fixes
- Practice with bulk fix scripts

**Phase 3**: Monitoring setup (Week 3)
- Implement compliance metrics dashboard
- Set up automated monitoring alerts
- Create incident response procedures

**Phase 4**: Continuous improvement (Ongoing)
- Review and update rules based on new discoveries
- Enhance automation tools
- Share success stories and lessons learned

**Success Criteria**: Zero profile-related dependency injection failures in production after implementation.