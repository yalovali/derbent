# Login Authentication Mechanism with Company Context

## Overview

The Derbent application implements a multi-tenant authentication system where users can have the same username across different companies. The unique constraint is on the combination of `login` + `company_id`. This document describes the authentication mechanism, class hierarchy, and call flow.

## Problem Statement

- **Multi-Tenant Requirement**: Users can have identical usernames in different companies
- **Session Issue**: During login, the session doesn't exist yet, so the company context cannot be retrieved from the session
- **Solution**: Pass the selected company ID from the login form through the Spring Security authentication chain

## Architecture

### Class Hierarchy and Relationships

```
┌─────────────────────────────┐
│  CCustomLoginView           │  @AnonymousAllowed
│  (Vaadin UI Component)      │  - Company dropdown (ComboBox<CCompany>)
│                             │  - Username field
└──────────────┬──────────────┘  - Password field
               │ submits form with companyId
               ↓
┌─────────────────────────────┐
│  Spring Security            │
│  /login endpoint            │  Standard form POST
└──────────────┬──────────────┘
               │ intercepted by
               ↓
┌─────────────────────────────┐
│ CCompanyAwareAuth           │  Custom filter
│ enticationFilter            │  - Extracts companyId from form
│ extends                     │  - Creates CCompanyAwareAuthenticationToken
│ UsernamePasswordAuth        │
│ enticationFilter            │
└──────────────┬──────────────┘
               │ creates token
               ↓
┌─────────────────────────────┐
│ CCompanyAwareAuth           │  Custom authentication token
│ enticationToken             │  - Extends UsernamePasswordAuthenticationToken
│ extends                     │  - Carries companyId field
│ UsernamePasswordAuth        │
│ enticationToken             │
└──────────────┬──────────────┘
               │ passed to
               ↓
┌─────────────────────────────┐
│ CCompanyAwareAuth           │  @Component
│ enticationProvider          │  - Validates credentials
│ implements                  │  - Calls loadUserByUsernameAndCompany()
│ AuthenticationProvider      │
└──────────────┬──────────────┘
               │ calls
               ↓
┌─────────────────────────────┐
│  CUserService               │  @Service implements UserDetailsService
│  implements                 │  - loadUserByUsernameAndCompany(username, companyId)
│  UserDetailsService         │  - Queries database with company context
└──────────────┬──────────────┘  - Returns UserDetails
               │ queries
               ↓
┌─────────────────────────────┐
│  IUserRepository            │  JPA Repository
│  extends                    │  - findByUsername(companyId, username)
│  IAbstractNamedRepository   │
└─────────────────────────────┘
```

### Configuration

```
┌─────────────────────────────┐
│  CSecurityConfig            │  @Configuration @EnableWebSecurity
│  extends                    │  - Configures custom authentication filter
│  VaadinWebSecurity          │  - Registers custom authentication provider
│                             │  - Sets up login view
└─────────────────────────────┘
```

## Authentication Flow

### Step-by-Step Process

1. **User Interaction**
   - User navigates to protected resource
   - Redirected to `/login` via `CCustomLoginView`
   - Selects company from dropdown
   - Enters username and password

2. **Form Submission**
   ```javascript
   // Form includes these parameters:
   POST /login
   {
     username: "admin",
     password: "test123",
     companyId: "1",  // Selected company ID
     redirect: "home"  // Post-login redirect
   }
   ```

3. **Filter Interception** (`CCompanyAwareAuthenticationFilter`)
   ```java
   @Override
   public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
       String username = obtainUsername(request);
       String password = obtainPassword(request);
       String companyIdStr = request.getParameter(COMPANY_ID_PARAMETER);  // "companyId"
       
       Long companyId = Long.parseLong(companyIdStr);
       
       // Create company-aware token
       CCompanyAwareAuthenticationToken authRequest = 
           new CCompanyAwareAuthenticationToken(username, password, companyId);
       
       return this.getAuthenticationManager().authenticate(authRequest);
   }
   ```

4. **Authentication Provider** (`CCompanyAwareAuthenticationProvider`)
   ```java
   @Override
   public Authentication authenticate(Authentication authentication) {
       String username = authentication.getName();
       String password = authentication.getCredentials().toString();
       
       // Extract company ID from custom token
       Long companyId = ((CCompanyAwareAuthenticationToken) authentication).getCompanyId();
       
       // Load user with company context
       UserDetails userDetails = userService.loadUserByUsernameAndCompany(username, companyId);
       
       // Validate password
       if (!passwordEncoder.matches(password, userDetails.getPassword())) {
           throw new BadCredentialsException("Invalid username or password");
       }
       
       // Return authenticated token
       return new CCompanyAwareAuthenticationToken(
           userDetails.getUsername(),
           userDetails.getPassword(),
           companyId,
           userDetails.getAuthorities()
       );
   }
   ```

5. **User Service** (`CUserService`)
   ```java
   @PreAuthorize("permitAll()")
   public UserDetails loadUserByUsernameAndCompany(String username, Long companyId) {
       // Query database with company context
       CUser loginUser = repository.findByUsername(companyId, username)
           .orElseThrow(() -> new UsernameNotFoundException("User not found"));
       
       // Convert to Spring Security UserDetails
       return User.builder()
           .username(loginUser.getUsername())
           .password(loginUser.getPassword())  // BCrypt encoded
           .authorities(getAuthorities("ADMIN,USER"))
           .build();
   }
   ```

6. **Repository Query** (`IUserRepository`)
   ```java
   @Query(
       "SELECT u FROM #{#entityName} u " +
       "LEFT JOIN FETCH u.userType " +
       "LEFT JOIN FETCH u.projectSettings ps " +
       "LEFT JOIN FETCH ps.project " +
       "WHERE u.login = :username AND u.company.id = :CompanyId"
   )
   Optional<CUser> findByUsername(
       @Param("CompanyId") Long companyId,
       @Param("username") String username
   );
   ```

7. **Success Handler** (`CAuthenticationSuccessHandler`)
   - User is authenticated
   - Session is created
   - User is redirected to appropriate view

## Key Design Decisions

### 1. Custom Authentication Token
**Why**: Standard `UsernamePasswordAuthenticationToken` doesn't support additional context like company ID.

**Solution**: `CCompanyAwareAuthenticationToken` extends the standard token and adds a `companyId` field.

### 2. Custom Authentication Filter
**Why**: Need to extract company ID from the login form and create custom authentication tokens.

**Solution**: `CCompanyAwareAuthenticationFilter` extends `UsernamePasswordAuthenticationFilter` to intercept the login POST request.

### 3. Custom Authentication Provider
**Why**: Need to handle company-aware authentication tokens and pass company context to user service.

**Solution**: `CCompanyAwareAuthenticationProvider` validates credentials with company context.

### 4. Lazy PasswordEncoder Injection
**Why**: Circular dependency between `CSecurityConfig` (creates PasswordEncoder) and `CCompanyAwareAuthenticationProvider` (needs PasswordEncoder).

**Solution**: Use `@Lazy` injection in the provider to break the circular dependency:
```java
@Autowired
@Lazy
private PasswordEncoder passwordEncoder;
```

### 5. Backward Compatibility
The original `loadUserByUsername(String username)` method is retained for backward compatibility, but it requires an active session with company context.

The new `loadUserByUsernameAndCompany(String username, Long companyId)` method is used during authentication when session doesn't exist yet.

## Security Considerations

1. **Company Isolation**: Each user is strictly associated with one company
2. **Username Uniqueness**: Enforced at database level with unique constraint on (login, company_id)
3. **Password Security**: BCrypt encoding with per-password salt
4. **Session Management**: Spring Security handles session creation after successful authentication
5. **Authorization**: User roles and authorities control access to resources

## Database Schema

```sql
CREATE TABLE cuser (
    user_id BIGINT PRIMARY KEY,
    login VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,  -- BCrypt encoded
    company_id BIGINT NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    -- other fields...
    CONSTRAINT uk_user_company UNIQUE (login, company_id),
    CONSTRAINT fk_user_company FOREIGN KEY (company_id) REFERENCES ccompany(company_id)
);
```

## Configuration Summary

### Spring Security Configuration (`CSecurityConfig`)

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
    super.configure(http);  // Vaadin defaults
    
    setLoginView(http, CCustomLoginView.class);
    
    // Get authentication manager
    AuthenticationManager authManager = http.getSharedObject(AuthenticationManager.class);
    
    // Create custom filter
    CCompanyAwareAuthenticationFilter authFilter = 
        new CCompanyAwareAuthenticationFilter(authManager);
    authFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
    authFilter.setFilterProcessesUrl("/login");
    
    // Replace default filter
    http.addFilterAt(authFilter, UsernamePasswordAuthenticationFilter.class);
}

protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(companyAwareAuthenticationProvider);
}
```

## Testing the Authentication

### Manual Test Steps

1. Start the application
2. Navigate to http://localhost:8080
3. Should redirect to `/login`
4. Select a company from dropdown
5. Enter username (e.g., "admin")
6. Enter password (e.g., "test123")
7. Click "Login"
8. Verify authentication succeeds
9. Verify redirect to appropriate view

### Automated Test Approach

```java
// Test with Playwright or similar
1. Navigate to login page
2. Select company "Test Company" from dropdown
3. Enter username "admin"
4. Enter password "test123"
5. Click login button
6. Assert redirect to home page
7. Assert user is authenticated
```

## Troubleshooting

### Common Issues

1. **"No active company in session"**
   - Cause: Using old `loadUserByUsername()` method during login
   - Solution: Ensure `CCompanyAwareAuthenticationProvider` is being used

2. **Circular Dependency Exception**
   - Cause: PasswordEncoder circular dependency
   - Solution: Use `@Lazy` injection in provider

3. **401 Unauthorized on login page**
   - Cause: Login view not marked as `@AnonymousAllowed`
   - Solution: Verify `CCustomLoginView` has `@AnonymousAllowed` annotation

4. **User not found error**
   - Cause: Wrong company selected or user doesn't exist in that company
   - Solution: Verify user exists for the selected company in database

## Future Enhancements

1. **Role-Based Authentication**: Implement dynamic role loading from user entity
2. **Multi-Factor Authentication**: Add MFA support for enhanced security
3. **Remember Me**: Implement persistent login functionality
4. **Account Lockout**: Add brute-force protection
5. **Password Reset**: Implement secure password reset flow
6. **Audit Logging**: Log all authentication attempts

## References

- Spring Security Documentation: https://docs.spring.io/spring-security/
- Vaadin Security: https://vaadin.com/docs/latest/security
- BCrypt Algorithm: https://en.wikipedia.org/wiki/Bcrypt
