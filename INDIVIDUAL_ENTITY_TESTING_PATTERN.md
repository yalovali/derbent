# Individual Entity Testing Pattern - Mandatory Rules
**Version:** 1.0  
**Date:** 2026-01-16  
**Audience:** GitHub Copilot Agents, Developers  
**Status:** MANDATORY

## 🎯 Purpose

This document establishes the **mandatory pattern** for testing entities individually, identifying issues, fixing them, and iterating until all entities pass without errors.

## 📋 The Pattern: Test → Fix → Commit → Repeat

### Step 1: Test Single Entity

```bash
# Test one entity at a time
mvn test \
  -Dtest=CPageTestNewEntities#testSingleEntity \
  -Dentity.name=<entity-name> \
  -Dspring.profiles.active=h2 \
  -Dplaywright.headless=false \
  2>&1 | tee target/test-logs/entity-<entity-name>.log
```

**Required Parameters:**
- `-Dtest=CPageTestNewEntities#testSingleEntity` - Runs single entity test method
- `-Dentity.name=<entity-name>` - Entity route name (e.g., "budgets", "invoices")
- `-Dspring.profiles.active=h2` - Use H2 database for testing
- `-Dplaywright.headless=false` - Browser MUST be visible

### Step 2: Analyze Error

**Look for these error patterns:**

#### A. Lazy Loading Exception
```
LazyInitializationException: could not initialize proxy - no Session
```

**Solution:** Add eager loading to repository

#### B. Field Not Found Exception
```
UnknownPathException: Could not resolve attribute 'entityType' of 'X'
```

**Solution:** Remove field from query (entity doesn't have it)

#### C. Spring Context Failure
```
Failed to load ApplicationContext
```

**Solution:** Check repository query syntax, entity relationships

### Step 3: Fix Based on Entity Hierarchy

#### Pattern: Check Entity Inheritance First

```bash
# Check what the entity extends
grep "class C<EntityName>\|extends\|implements" src/main/java/.../C<EntityName>.java
```

**Decision Tree:**

```
What does entity extend?
├─ CEntityDB<T>
│  └─ Fields: id, name, description
│     Query: LEFT JOIN FETCH e.attachments, e.comments
│
├─ CEntityOfCompany<T>
│  └─ Fields: company + CEntityDB fields
│     Query: LEFT JOIN FETCH e.company, e.attachments, e.comments
│
├─ CEntityOfProject<T>
│  └─ Fields: project + CEntityDB fields
│     Query: LEFT JOIN FETCH e.project, e.attachments, e.comments
│
├─ CProjectItem<T>
│  └─ Fields: project, assignedTo, createdBy
│     Query: LEFT JOIN FETCH e.project, e.assignedTo, e.createdBy, 
│            e.attachments, e.comments
│
└─ CProjectItem<T> + IHasStatusAndWorkflow<T>
   └─ Fields: project, assignedTo, createdBy, status, entityType, workflow
      Query: LEFT JOIN FETCH e.project, e.assignedTo, e.createdBy,
             e.status, e.entityType et, et.workflow, 
             e.attachments, e.comments
```

### Step 4: Apply Fix to Repository

**Location:** `src/main/java/tech/derbent/app/<module>/<entity>/service/I<Entity>Repository.java`

**Template for CProjectItem + IHasStatusAndWorkflow:**
```java
@Override
@Query("""
    SELECT e FROM #{#entityName} e
    LEFT JOIN FETCH e.attachments
    LEFT JOIN FETCH e.comments
    LEFT JOIN FETCH e.project
    LEFT JOIN FETCH e.assignedTo
    LEFT JOIN FETCH e.createdBy
    LEFT JOIN FETCH e.status
    LEFT JOIN FETCH e.entityType et
    LEFT JOIN FETCH et.workflow
    WHERE e.id = :id
    """)
Optional<CEntity> findById(@Param("id") Long id);
```

**Template for CEntityOfProject (no workflow):**
```java
@Override
@Query("""
    SELECT e FROM #{#entityName} e
    LEFT JOIN FETCH e.attachments
    LEFT JOIN FETCH e.comments
    LEFT JOIN FETCH e.project
    WHERE e.id = :id
    """)
Optional<CEntity> findById(@Param("id") Long id);
```

**Template for CEntityOfCompany:**
```java
@Override
@Query("""
    SELECT e FROM #{#entityName} e
    LEFT JOIN FETCH e.attachments
    LEFT JOIN FETCH e.comments
    LEFT JOIN FETCH e.company
    WHERE e.id = :id
    """)
Optional<CEntity> findById(@Param("id") Long id);
```

### Step 5: Compile and Verify Fix

```bash
# Compile to check for syntax errors
mvn compile

# If compilation fails, check entity fields:
grep -n "@Column\|@ManyToOne\|@OneToMany" src/main/java/.../C<Entity>.java
```

### Step 6: Commit Fix

```bash
git add <changed-files>
git commit -m "fix: Add eager loading for <Entity> attachments/comments

Issue: LazyInitializationException when loading <entity> forms
Entity: C<Entity> extends <ParentClass>
Fix: Added LEFT JOIN FETCH for attachments, comments, <other-fields>

Query now eagerly loads:
- attachments
- comments
- <list other fetched fields>

Pattern: <entity-hierarchy-type>
Test: mvn test -Dtest=CPageTestNewEntities#testSingleEntity -Dentity.name=<entity-name>"
```

### Step 7: Retest

```bash
# Run test again to verify fix
mvn test \
  -Dtest=CPageTestNewEntities#testSingleEntity \
  -Dentity.name=<entity-name> \
  -Dspring.profiles.active=h2 \
  -Dplaywright.headless=false

# Check result:
# ✅ If SUCCESS → Move to next entity
# ❌ If FAILURE → Return to Step 2
```

### Step 8: Move to Next Entity

Once entity passes, move to the next one from the list.

## 📊 Entity Testing Checklist

### New Entities to Test (Priority Order)

```bash
# Financial Entities (7)
1. budgets          ✅ FIXED
2. budget-types     ⏳ TO TEST
3. invoices         ✅ FIXED
4. invoice-items    ⏳ TO TEST
5. payments         ⏳ TO TEST
6. orders           ⏳ TO TEST
7. currencies       ⏳ TO TEST

# Test Management Entities (5)
8.  test-cases       ✅ FIXED
9.  test-scenarios   ✅ FIXED
10. test-runs        ✅ FIXED
11. test-steps       ⏳ TO TEST
12. test-case-results ⏳ TO TEST

# Team/Issue Entities (3)
13. issues          ⏳ TO TEST
14. issue-types     ⏳ TO TEST
15. teams           ✅ FIXED
```

## 🔍 Quick Reference Commands

### Test Single Entity
```bash
mvn test -Dtest=CPageTestNewEntities#testSingleEntity \
  -Dentity.name=<entity-name> -Dspring.profiles.active=h2
```

### Check Entity Structure
```bash
grep "class C<Entity>\|extends\|implements" \
  src/main/java/tech/derbent/app/*/*/domain/C<Entity>.java
```

### Find Repository
```bash
find src/main/java -name "I<Entity>Repository.java"
```

### Check Existing Query
```bash
grep -A10 "findById" \
  src/main/java/tech/derbent/app/*/*/service/I<Entity>Repository.java
```

### View Entity Fields
```bash
grep "@Column\|@ManyToOne\|@OneToMany\|@OneToOne" \
  src/main/java/tech/derbent/app/*/*/domain/C<Entity>.java
```

## 🚨 Common Pitfalls to Avoid

### ❌ DON'T: Copy-paste queries without checking entity hierarchy
```java
// WRONG - Assuming all entities have same fields
LEFT JOIN FETCH e.status
LEFT JOIN FETCH e.entityType et
```

### ✅ DO: Check entity inheritance first
```bash
grep "extends\|implements" path/to/Entity.java
```

### ❌ DON'T: Add fields that don't exist
```java
// WRONG - CTestRun extends CEntityOfProject (no assignedTo)
LEFT JOIN FETCH tr.assignedTo
```

### ✅ DO: Only fetch fields that exist
```java
// CORRECT - Check entity class for actual fields
LEFT JOIN FETCH tr.project
LEFT JOIN FETCH tr.attachments
```

### ❌ DON'T: Test multiple entities at once when debugging
```bash
# WRONG - Hard to identify which entity failed
./run-playwright-tests.sh comprehensive
```

### ✅ DO: Test one entity at a time
```bash
# CORRECT - Clear which entity has issue
mvn test -Dtest=CPageTestNewEntities#testSingleEntity -Dentity.name=budgets
```

## 🎯 Success Criteria

An entity test is considered **PASSED** when:

1. ✅ Test compiles without errors
2. ✅ Spring context loads successfully
3. ✅ Browser opens and is visible
4. ✅ Test navigates to entity page
5. ✅ Grid loads without LazyInitializationException
6. ✅ No errors in console or logs
7. ✅ Test completes with status: "Tests run: 1, Failures: 0, Errors: 0"

## 📝 Example: Complete Test Cycle for "Orders"

```bash
# Step 1: Test
mvn test -Dtest=CPageTestNewEntities#testSingleEntity \
  -Dentity.name=orders -Dspring.profiles.active=h2

# Step 2: Analyze Error (if any)
tail -100 target/test-logs/entity-orders.log | grep -E "Exception|Error"

# Step 3: Check Entity Structure
grep "class COrder\|extends\|implements" \
  src/main/java/tech/derbent/app/orders/order/domain/COrder.java
# Output: public class COrder extends CProjectItem<COrder> 
#         implements IHasStatusAndWorkflow<COrder>, IHasAttachments, IHasComments

# Step 4: Fix Repository (already done for Order)
# Check: src/main/java/tech/derbent/app/orders/order/service/IOrderRepository.java
# Should have: LEFT JOIN FETCH o.attachments, o.comments, etc.

# Step 5: Compile
mvn compile

# Step 6: Commit
git commit -m "fix: Verify Order repository eager loading"

# Step 7: Retest
mvn test -Dtest=CPageTestNewEntities#testSingleEntity \
  -Dentity.name=orders -Dspring.profiles.active=h2

# Step 8: If SUCCESS → Move to next entity
# Next: payments
```

## 🤖 Instructions for Copilot Agents

When asked to test new entities:

1. **Start with this document** - Review the pattern
2. **Test ONE entity at a time** - Never batch test during debugging
3. **Follow the 8-step pattern** - Don't skip steps
4. **Check entity hierarchy FIRST** - Before writing queries
5. **Use reference patterns** - CActivity, CIssue, COrder are correct
6. **Commit after each fix** - Don't accumulate changes
7. **Document the fix** - Use commit message template
8. **Retest immediately** - Verify fix works
9. **Mark entity as done** - Update checklist
10. **Move to next entity** - Continue until all pass

## 📚 Related Documentation

- `LAZY_LOADING_FIX_SUMMARY.md` - Technical details of lazy loading fixes
- `TESTING_RULES.md` - Overall testing standards
- `docs/architecture/coding-standards.md` - General coding guidelines
- Entity hierarchy guide (in LAZY_LOADING_FIX_SUMMARY.md)

---

**This pattern is MANDATORY for all entity testing. Follow it precisely to ensure consistent, reliable results.**
