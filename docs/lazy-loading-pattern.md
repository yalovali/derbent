# Lazy Loading Pattern and Best Practices

## Overview
This document describes the proper patterns for handling lazy-loaded relationships in the Derbent application to avoid `LazyInitializationException` while maintaining optimal performance and avoiding PostgreSQL column limit issues.

## The Problem

### LazyInitializationException
When accessing lazy-loaded relationships outside of a Hibernate session/transaction, you'll encounter:
```
org.hibernate.LazyInitializationException: Could not initialize proxy [tech.derbent.projects.domain.CProject#1] - no session
```

### Why We Use LAZY Fetch Type
- **Prevents Circular Loading**: EAGER fetch can cause circular eager loading chains
- **PostgreSQL Column Limits**: Massive JOIN queries can exceed PostgreSQL's 1664 column limit
- **Performance**: Only load data when needed

### Example: CUserCompanySetting Lazy Loading Issue

**Problem:** In `CComponentSingleCompanyUserSetting.java`, line 128:
```java
CCompany company = setting.getCompany();
```

Where `setting` is obtained from `user.getCompanySettings()`, which is a `@OneToOne` relationship with `FetchType.LAZY`.

**Symptom:** When the UI component tries to access `company.getName()` outside a transaction, it throws `LazyInitializationException`.

**Root Cause:** 
1. `CUser.companySetting` is marked as `FetchType.LAZY`
2. `CUserCompanySetting.company` is also `@ManyToOne` with default LAZY fetch
3. UI components operate outside transaction boundaries
4. Hibernate cannot initialize the proxy when no session is active

## Solution Patterns

### 1. Repository-Level Eager Fetching (Preferred)

For fields that are **always needed** when displaying data (e.g., in grids), use `LEFT JOIN FETCH` in repository queries.

#### Example: User Company Settings Repository

```java
@Repository
public interface IUserCompanySettingsRepository extends IUserRelationshipRepository<CUserCompanySetting> {
    
    /** Find all user company settings for a specific user with eager loading of company and user */
    @Override
    @Query("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.company WHERE r.user.id = :userId")
    List<CUserCompanySetting> findByUserId(@Param("userId") Long userId);
}

/** Find user by ID with company setting eagerly loaded */
@Query("SELECT u FROM #{#entityName} u " +
       "LEFT JOIN FETCH u.userType " +
       "LEFT JOIN FETCH u.companySetting cs " +
       "LEFT JOIN FETCH cs.company " +
       "WHERE u.id = :userId")
Optional<CUser> findByIdWithCompanySetting(@Param("userId") Long userId);
```

**Service Layer:**
```java
@Service
public class CUserService extends CAbstractNamedEntityService<CUser> {
    
    @Transactional(readOnly = true)
    @PreAuthorize("permitAll()")
    public Optional<CUser> findByIdWithCompanySetting(final Long userId) {
        Check.notNull(userId, "User ID must not be null");
        return ((IUserRepository) repository).findByIdWithCompanySetting(userId);
    }
}
```

**UI Component Usage:**
```java
@Override
public void populateForm() {
    super.populateForm();
    // Reload user with company setting eagerly loaded to avoid LazyInitializationException
    CUser currentUser = getCurrentEntity();
    if (currentUser != null && currentUser.getId() != null) {
        Optional<CUser> userWithCompanySetting = ((CUserService) entityService)
            .findByIdWithCompanySetting(currentUser.getId());
        if (userWithCompanySetting.isPresent()) {
            setCurrentEntity(userWithCompanySetting.get());
        }
    }
    updateDisplay();
}
```

**When to use:**
- Fields displayed in grids or lists
- Fields needed immediately after fetching
- Core relationships that are part of the entity's identity

**Benefits:**
- Data available outside transaction
- No LazyInitializationException
- Single query fetches all needed data
- Efficient for UI display

### 2. Service-Level Transactional Initialization

For fields that are **sometimes needed**, use service methods within `@Transactional` context.

#### Example: Abstract Service Method

```java
@Transactional(readOnly = true)
public EntityClass initializeLazyFields(final EntityClass entity) {
    Check.notNull(entity, "Entity cannot be null");
    if (entity.getId() == null) {
        LOGGER.warn("Cannot initialize lazy fields for unsaved entity");
        return entity;
    }
    // Fetch entity from database to ensure it's managed
    final EntityClass managed = repository.findById(entity.getId()).orElse(entity);
    // Access lazy fields to trigger loading within transaction
    managed.initializeAllFields();
    return managed;
}
```

**When to use:**
- Fields needed only in specific scenarios
- Deep object graphs that aren't always required
- Complex initialization logic

**Usage:**
```java
// In service method
@Transactional
public void processSettings(CUserProjectSettings settings) {
    settings = userProjectSettingsService.initializeLazyFields(settings);
    // Now all fields are accessible
    String projectName = settings.getProject().getName();
}
```

### 3. Entity-Level initializeAllFields()

Each entity implements `initializeAllFields()` to manually trigger lazy loading **within a transaction**.

#### Example: CUserProjectSettings

```java
@Override
public void initializeAllFields() {
    // Access lazy fields to trigger loading
    if (user != null) {
        user.getLogin();
    }
    if (project != null) {
        project.getName();
    }
    if (role != null) {
        role.getName();
    }
}
```

**When to use:**
- Called from `@Transactional` service methods
- Part of service-level initialization pattern
- NOT to be called from UI layer

**Important:** Do NOT call this method outside of a Hibernate session/transaction!

## Anti-Patterns (Don't Do This!)

### ❌ Calling initializeAllFields() in UI Layer

**BAD:**
```java
// In UI component or view
protected void createStandardDataAccessors(Supplier<List<RelationalClass>> settingsSupplier) {
    final List<RelationalClass> settings = settingsSupplier.get();
    settings.forEach(setting -> {
        setting.initializeAllFields();  // ❌ LazyInitializationException!
    });
}
```

**Why it fails:**
- `settingsSupplier.get()` completes within transaction
- Transaction ends before `initializeAllFields()` is called
- Lazy fields are no longer accessible

**GOOD:**
```java
// Repository eagerly fetches needed fields
@Query("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.project WHERE r.user.id = :userId")
List<CUserProjectSettings> findByUserId(@Param("userId") Long userId);

// UI layer just uses the data
protected void setupDataAccessors() {
    createStandardDataAccessors(() -> userProjectSettingsService.findByUser(getCurrentEntity()));
    // Data already loaded, no need for initializeAllFields()
}
```

### ❌ Using EAGER Fetch Type

**BAD:**
```java
@ManyToOne(fetch = FetchType.EAGER)  // ❌ Causes circular loading!
@JoinColumn(name = "role_id")
private CUserProjectRole role;
```

**Why it fails:**
- Can create circular eager loading chains
- Generates massive JOIN queries
- May exceed PostgreSQL's 1664 column limit
- Loads data even when not needed

**GOOD:**
```java
@ManyToOne(fetch = FetchType.LAZY)  // ✅ Explicit LAZY
@JoinColumn(name = "role_id")
private CUserProjectRole role;
```

## Implementation Checklist

When adding new entity relationships:

- [ ] Define relationship as `FetchType.LAZY`
- [ ] Identify which fields are always needed for display
- [ ] Add `LEFT JOIN FETCH` to repository queries for those fields
- [ ] Override base repository methods if needed
- [ ] Implement `initializeAllFields()` in entity class
- [ ] Add service-level initialization method if needed
- [ ] Test with `@SpringBootTest` and H2 database
- [ ] Verify no LazyInitializationException in UI
- [ ] Document any special fetch requirements

## Testing Lazy Loading

### Why Playwright Tests May Not Detect Lazy Loading Issues

Playwright tests might not catch `LazyInitializationException` for several reasons:

1. **Transaction Boundaries**: Spring Boot test infrastructure may keep transactions open longer than production runtime, masking the issue
2. **Test Data Initialization**: Fresh test data created within a transaction might be eagerly loaded through the persistent context
3. **H2 Database Behavior**: H2's in-memory database may have different query optimization than PostgreSQL
4. **Mock Data Paths**: If tests use mock data or simplified data flows, they may not exercise the same code paths as production
5. **Vaadin Session Management**: UI framework session handling in tests vs production may differ

**Example of a test that WOULD catch the issue:**
```java
@Test
@DisplayName("Test company can be accessed outside transaction")
public void testCompanyAccessibleOutsideTransaction() {
    // Fetch user with company setting eagerly loaded (simulating UI access)
    Optional<CUser> userOpt = userService.findByIdWithCompanySetting(testUser.getId());
    
    assertTrue(userOpt.isPresent(), "User should be found");
    CUser user = userOpt.get();
    
    // Access company outside transaction - should NOT throw LazyInitializationException
    CCompany company = user.getCompanySettings().getCompany();
    assertNotNull(company.getName(), "Company name should be accessible");
}
```

### Test with H2 Database

```java
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class LazyLoadingTest {
    
    @Test
    @Transactional
    public void testLazyFieldsAccessibleWithinTransaction() {
        // Create and save entity
        CUserProjectSettings settings = createTestSettings();
        settings = service.save(settings);
        
        // Access lazy fields within transaction
        String projectName = settings.getProject().getName();
        assertNotNull(projectName);
    }
    
    @Test
    public void testRepositoryEagerlyFetchesRequiredFields() {
        // Save entity
        CUserProjectSettings settings = createTestSettings();
        settings = service.save(settings);
        
        // Fetch outside transaction
        List<CUserProjectSettings> fetched = repository.findByUserId(settings.getUser().getId());
        
        // Fields should be accessible (no LazyInitializationException)
        assertNotNull(fetched.get(0).getProject().getName());
        assertNotNull(fetched.get(0).getUser().getLogin());
    }
}
```

## Related Documentation

- `docs/CComponentUserProjectSettings_Pattern.md` - Comprehensive component pattern
- `src/test/java/tech/derbent/users/domain/CUserProjectSettingsFetchTypeTest.java` - FetchType validation tests
- `src/main/java/tech/derbent/api/annotations/CSpringAuxillaries.java` - Hibernate utilities

## Summary

1. **Default to LAZY fetch type** for all relationships
2. **Use `LEFT JOIN FETCH` in repositories** for fields needed in UI
3. **Provide service-level initialization** for complex scenarios
4. **Never call `initializeAllFields()` outside transactions**
5. **Test thoroughly** with H2 database and integration tests

This pattern ensures optimal performance, avoids LazyInitializationException, and maintains compatibility with PostgreSQL's constraints.
