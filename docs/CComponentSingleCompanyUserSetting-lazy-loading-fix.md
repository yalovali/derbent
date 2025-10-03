# CComponentSingleCompanyUserSetting Lazy Loading Fix

## Issue Description

**File:** `src/main/java/tech/derbent/api/views/components/CComponentSingleCompanyUserSetting.java`  
**Line:** 128  
**Error:** `LazyInitializationException` when accessing `setting.getCompany()`

### Symptom
```java
CCompany company = setting.getCompany();
```
Throws `org.hibernate.LazyInitializationException: could not initialize proxy - no Session` when the UI component tries to access company data.

### Root Cause

The issue occurs due to a chain of lazy-loaded relationships:

1. **CUser.companySetting** (line 32 in CUser.java)
   ```java
   @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
   @JoinColumn(name = "single_company_settings_id", nullable = true)
   private CUserCompanySetting companySetting;
   ```

2. **CUserCompanySetting.company** (line 38-44 in CUserCompanySetting.java)
   ```java
   @ManyToOne
   @JoinColumn(name = "company_id", nullable = false)
   private CCompany company;
   ```

3. **UI Layer Access**: When `CComponentSingleCompanyUserSetting` calls `setting.getCompany()` in the `updateDisplay()` method, it's outside any Hibernate session/transaction, causing the LazyInitializationException.

## Solution

The fix implements the **Repository-Level Eager Fetching** pattern as documented in `docs/lazy-loading-pattern.md`.

### Changes Made

#### 1. Entity: Implement initializeAllFields()

**File:** `CUserCompanySetting.java`

```java
@Override
public void initializeAllFields() {
    // Access lazy fields to trigger loading within transaction
    if (user != null) {
        user.getLogin(); // Trigger user loading
    }
    if (company != null) {
        company.getName(); // Trigger company loading
    }
}
```

This method can be called within a transaction to manually initialize lazy fields.

#### 2. Repository: Add Eager Fetch Query

**File:** `IUserRepository.java`

```java
/** Find user by ID with company setting eagerly loaded. 
 * This is used when the UI needs to access company data 
 * to avoid LazyInitializationException. */
@Query("SELECT u FROM #{#entityName} u " +
       "LEFT JOIN FETCH u.userType " +
       "LEFT JOIN FETCH u.companySetting cs " +
       "LEFT JOIN FETCH cs.company " +
       "WHERE u.id = :userId")
Optional<CUser> findByIdWithCompanySetting(@Param("userId") Long userId);
```

This query uses `LEFT JOIN FETCH` to eagerly load:
- User type
- Company setting
- Company (nested within company setting)

#### 3. Service: Expose Repository Method

**File:** `CUserService.java`

```java
/** Find user by ID with company setting eagerly loaded to avoid 
 * LazyInitializationException in UI. This method should be used 
 * when the UI needs to access user's company settings and company data.
 * @param userId the user ID
 * @return Optional containing the user with company setting loaded, 
 *         or empty if not found */
@Transactional(readOnly = true)
@PreAuthorize("permitAll()")
public Optional<CUser> findByIdWithCompanySetting(final Long userId) {
    Check.notNull(userId, "User ID must not be null");
    return ((IUserRepository) repository).findByIdWithCompanySetting(userId);
}
```

#### 4. UI Component: Use Eager Loading

**File:** `CComponentSingleCompanyUserSetting.java`

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

This ensures the user entity is reloaded with all required data before the UI tries to display it.

#### 5. Test: Validate the Fix

**File:** `CUserCompanySettingLazyLoadingTest.java`

Three test cases validate the fix:

1. **testCompanyAccessibleOutsideTransaction()**: Verifies company can be accessed outside a transaction
2. **testInitializeAllFieldsWithinTransaction()**: Validates initializeAllFields() works within transaction
3. **testRepositoryEagerlyFetchesCompany()**: Confirms repository query eagerly fetches company data

All tests pass successfully.

## Why Playwright Tests Don't Detect This

Playwright UI tests may not catch this lazy loading issue for several reasons:

### 1. Transaction Boundaries
Spring Boot's test infrastructure (@SpringBootTest) may keep transactions open longer than production runtime, masking the issue. The test transaction may span the entire test method, keeping the Hibernate session active.

### 2. Test Data Initialization
Fresh test data created within a transaction might be eagerly loaded through the persistent context. If test data is created and immediately used within the same transaction, lazy proxies are already initialized.

### 3. H2 Database Behavior
H2's in-memory database used in tests may have different query optimization than PostgreSQL used in production. H2 might eagerly fetch relationships even when marked as LAZY.

### 4. Mock Data Paths
If Playwright tests use simplified data flows or mock data providers, they may not exercise the exact same code paths as production users.

### 5. Vaadin Session Management
The UI framework's session handling in tests versus production may differ. Test environments may maintain longer-lived sessions that keep Hibernate contexts active.

### Example: Test That WOULD Catch The Issue

```java
@Test
@DisplayName("Test company accessible outside transaction")
public void testCompanyAccessibleOutsideTransaction() {
    // This test method is NOT @Transactional - simulating UI behavior
    
    // Fetch user with eager loading
    Optional<CUser> userOpt = userService.findByIdWithCompanySetting(testUser.getId());
    
    // Access company outside transaction
    CCompany company = userOpt.get().getCompanySettings().getCompany();
    assertNotNull(company.getName()); // Would fail without eager loading
}
```

## Best Practices

1. **Always use repository-level eager fetching** for fields that are displayed in UI
2. **Never call initializeAllFields() outside a transaction** - it will throw the same exception
3. **Use service methods with @Transactional** to wrap repository calls
4. **Test lazy loading with non-transactional test methods** to catch these issues
5. **Document which fields are eagerly loaded** in repository query comments

## Related Documentation

- `docs/lazy-loading-pattern.md` - Comprehensive lazy loading patterns and best practices
- `src/test/java/tech/derbent/users/domain/CUserProjectSettingsFetchTypeTest.java` - Similar lazy loading fix for project settings
- `src/main/java/tech/derbent/api/services/CAbstractService.java` - Base service with initializeLazyFields() method

## Testing the Fix

To verify the fix works:

```bash
# Run the specific test
mvn test -Dtest=CUserCompanySettingLazyLoadingTest

# Or run all tests
mvn test

# Start the application and manually test the user edit page
mvn spring-boot:run -Dspring.profiles.active=h2
# Navigate to Users view and edit a user with a company setting
```

## Summary

The fix ensures that when `CComponentSingleCompanyUserSetting` displays user company settings, all required data (user, company setting, and company) is eagerly fetched from the database within a transaction. This prevents `LazyInitializationException` when the UI component tries to access company details outside the transaction boundary.

The solution follows the repository-level eager fetching pattern, which is the preferred approach for data that is always needed in the UI.
