# Verifier Agent Configuration

## Agent Settings
- **Mode**: Validation & Testing
- **Primary Tools**: grep, bash, view, glob
- **Output Format**: Verification report with pass/fail
- **Build Profile**: agents (Java 17)

## Verification Scripts

### Static Analysis Scripts

#### 1. C-Prefix Check
```bash
#!/bin/bash
echo "Checking C-Prefix convention..."
violations=$(grep -r "^public class [A-Z]" src/main/java --include="*.java" | grep -v "^public class C" | wc -l)
if [ $violations -eq 0 ]; then
  echo "✅ PASS: All classes have C-prefix"
else
  echo "❌ FAIL: $violations classes missing C-prefix"
  grep -r "^public class [A-Z]" src/main/java --include="*.java" | grep -v "^public class C"
fi
```

#### 2. Raw Types Check
```bash
#!/bin/bash
echo "Checking for raw types..."
violations=$(grep -r "extends C.*[^<>].*{" src/main/java --include="*.java" | grep -v "extends C.*<" | wc -l)
if [ $violations -eq 0 ]; then
  echo "✅ PASS: No raw types found"
else
  echo "❌ FAIL: $violations raw types found"
  grep -r "extends C.*[^<>].*{" src/main/java --include="*.java" | grep -v "extends C.*<"
fi
```

#### 3. Field Injection Check
```bash
#!/bin/bash
echo "Checking for field injection..."
violations=$(grep -r "@Autowired" src/main/java --include="*.java" | grep -v "Constructor" | wc -l)
if [ $violations -eq 0 ]; then
  echo "✅ PASS: No field injection found"
else
  echo "❌ FAIL: $violations @Autowired fields found"
  grep -r "@Autowired" src/main/java --include="*.java" | grep -v "Constructor"
fi
```

#### 4. Entity Constants Check
```bash
#!/bin/bash
echo "Checking entity constants..."
missing=0
for file in $(find src/main/java -name "C*.java" -path "*/domain/*"); do
  if grep -q "extends C.*<" "$file"; then
    entity=$(basename "$file" .java)
    if ! grep -q "DEFAULT_COLOR" "$file"; then
      echo "❌ Missing DEFAULT_COLOR: $file"
      ((missing++))
    fi
    if ! grep -q "DEFAULT_ICON" "$file"; then
      echo "❌ Missing DEFAULT_ICON: $file"
      ((missing++))
    fi
    if ! grep -q "ENTITY_TITLE_SINGULAR" "$file"; then
      echo "❌ Missing ENTITY_TITLE_SINGULAR: $file"
      ((missing++))
    fi
    if ! grep -q "ENTITY_TITLE_PLURAL" "$file"; then
      echo "❌ Missing ENTITY_TITLE_PLURAL: $file"
      ((missing++))
    fi
    if ! grep -q "VIEW_NAME" "$file"; then
      echo "❌ Missing VIEW_NAME: $file"
      ((missing++))
    fi
  fi
done

if [ $missing -eq 0 ]; then
  echo "✅ PASS: All entity constants present"
else
  echo "❌ FAIL: $missing missing constants"
fi
```

#### 5. Dialog Layout Overflow Check
```bash
#!/bin/bash
echo "Checking dialog layout overflow rules..."
.github/agents/verifier/scripts/check-dialog-layout-rules.sh
```

## Test Execution Commands

### Selective Playwright Tests
```bash
# Test by keyword
mvn test -Dtest=CPageTestComprehensive \
  -Dtest.routeKeyword={keyword} \
  2>&1 | tee /tmp/playwright-{keyword}.log

# Example keywords: activity, storage, meeting, user
```

### Unit Tests
```bash
# Test specific service
mvn test -Dtest=C{Entity}ServiceTest 2>&1 | tee /tmp/unit-{entity}.log
```

### Build Tests
```bash
# Compile only (fast)
mvn clean compile -Pagents -DskipTests 2>&1 | tee /tmp/build.log

# Full build with tests (slow)
mvn clean verify -Pagents 2>&1 | tee /tmp/verify.log
```

## Report Template

```markdown
# 🔍 Verification Report: [Entity/Feature Name]

## Verification Date
[Date and time]

## Files Analyzed
- [File 1]
- [File 2]

## Static Analysis
- [ ] ✅/❌ C-Prefix convention
- [ ] ✅/❌ Generic types (no raw types)
- [ ] ✅/❌ Constructor injection
- [ ] ✅/❌ Entity constants
- [ ] ✅/❌ Collections initialization
- [ ] ✅/❌ JPA constructor pattern
- [ ] ✅/❌ Validation methods

## Build Results
- [ ] ✅/❌ Compilation (mvn compile)
- [ ] ✅/❌ Formatting (mvn spotless:check)

## Test Results
- [ ] ✅/❌ Unit Tests: [Test count]
- [ ] ✅/❌ Playwright Tests: [Keyword]

## Violations
[List violations with file:line]

## Fixes Required
[List required fixes]

## Summary
- Passed: [N]/[Total]
- Status: ✅ APPROVED / ❌ REJECTED
```

## Test Keywords by Domain

| Domain | Keyword | Pages Tested |
|--------|---------|--------------|
| Activities | activity | Activities, Activity Types, Activity Priorities |
| Storage | storage | Storages, Storage Types, Storage Items |
| Meetings | meeting | Meetings, Meeting Types |
| Users | user | Users, User Roles, User Project Roles |
| Issues | issue | Issues, Issue Types, Issue Priorities |
| Products | product | Products, Product Types, Product Categories |

## Continuous Verification

Run this before every commit:
```bash
# Quick check (< 1 min)
mvn clean compile -Pagents -DskipTests && \
mvn spotless:check

# Full check (5-10 min)
mvn clean verify -Pagents
```
