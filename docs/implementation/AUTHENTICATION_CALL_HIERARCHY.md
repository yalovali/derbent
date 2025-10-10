# Authentication Call Hierarchy - Detailed Flow

## Overview
This document provides a detailed call hierarchy showing how the authentication mechanism works from user interaction to database query.

## Complete Call Stack

```
┌────────────────────────────────────────────────────────────────────────┐
│                    USER INTERACTION LAYER                               │
│                                                                         │
│  User Action: Selects company, enters username/password, clicks Login  │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│                     VAADIN UI LAYER                                     │
│                                                                         │
│  CCustomLoginView.handleLogin()                                        │
│  ├─ Validates company selection                                        │
│  ├─ Validates username and password                                    │
│  └─ Creates HTML form with:                                            │
│     ├─ username                                                         │
│     ├─ password                                                         │
│     ├─ companyId  ← NEW: Selected company ID                          │
│     └─ redirect                                                         │
│                                                                         │
│  JavaScript: form.submit() → POST /login                               │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 │ HTTP POST
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│                  SPRING SECURITY LAYER                                  │
│                                                                         │
│  1. FilterChainProxy                                                   │
│     └─ Routes request through security filter chain                    │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│                 CUSTOM AUTHENTICATION FILTER                            │
│                                                                         │
│  2. CCompanyAwareAuthenticationFilter.attemptAuthentication()          │
│     ├─ Extracts username from request                                  │
│     ├─ Extracts password from request                                  │
│     ├─ Extracts companyId from request  ← NEW                         │
│     ├─ Creates CCompanyAwareAuthenticationToken                        │
│     │  └─ Token contains: (username, password, companyId)              │
│     └─ Delegates to AuthenticationManager.authenticate()               │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│               AUTHENTICATION MANAGER                                    │
│                                                                         │
│  3. ProviderManager.authenticate()                                     │
│     └─ Finds appropriate AuthenticationProvider                        │
│        └─ Selects CCompanyAwareAuthenticationProvider                  │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│            CUSTOM AUTHENTICATION PROVIDER                               │
│                                                                         │
│  4. CCompanyAwareAuthenticationProvider.authenticate()                 │
│     ├─ Extracts username from token                                    │
│     ├─ Extracts password from token                                    │
│     ├─ Extracts companyId from token  ← NEW                           │
│     │                                                                   │
│     ├─ Calls CUserService.loadUserByUsernameAndCompany()              │
│     │  └─ Parameters: (username, companyId)  ← NEW                    │
│     │                                                                   │
│     ├─ Validates password using PasswordEncoder.matches()              │
│     │  ├─ Compares submitted password with stored BCrypt hash          │
│     │  └─ Throws BadCredentialsException if mismatch                   │
│     │                                                                   │
│     └─ Returns authenticated CCompanyAwareAuthenticationToken          │
│        └─ Token contains: (username, password, companyId, authorities) │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│                    USER SERVICE LAYER                                   │
│                                                                         │
│  5. CUserService.loadUserByUsernameAndCompany(username, companyId)    │
│     ├─ Logs authentication attempt                                     │
│     ├─ Validates companyId is not null                                 │
│     │  └─ Falls back to session-based lookup if null                   │
│     │                                                                   │
│     ├─ Calls IUserRepository.findByUsername(companyId, username)       │
│     │  └─ Parameters: (companyId, username)  ← Both required          │
│     │                                                                   │
│     ├─ Throws UsernameNotFoundException if user not found              │
│     │                                                                   │
│     ├─ Converts user roles to Spring Security GrantedAuthority         │
│     │  └─ Calls getAuthorities("ADMIN,USER")                           │
│     │                                                                   │
│     └─ Returns Spring Security UserDetails                             │
│        └─ User.builder()                                                │
│           ├─ username(loginUser.getUsername())                          │
│           ├─ password(loginUser.getPassword())  ← BCrypt encoded       │
│           ├─ authorities(authorities)                                   │
│           ├─ accountExpired(false)                                      │
│           ├─ accountLocked(false)                                       │
│           ├─ credentialsExpired(false)                                  │
│           └─ disabled(!loginUser.isEnabled())                           │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│                   JPA REPOSITORY LAYER                                  │
│                                                                         │
│  6. IUserRepository.findByUsername(companyId, username)                │
│     └─ JPQL Query:                                                      │
│        SELECT u FROM CUser u                                            │
│        LEFT JOIN FETCH u.userType                                       │
│        LEFT JOIN FETCH u.projectSettings ps                             │
│        LEFT JOIN FETCH ps.project                                       │
│        WHERE u.login = :username                                        │
│          AND u.company.id = :CompanyId  ← Multi-tenant isolation       │
│                                                                         │
│     └─ Returns Optional<CUser>                                          │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│                     DATABASE LAYER                                      │
│                                                                         │
│  7. SQL Query Execution                                                │
│     SELECT u.*, ut.*, ps.*, p.*                                         │
│     FROM cuser u                                                        │
│     LEFT JOIN cusertype ut ON u.user_type_id = ut.user_type_id         │
│     LEFT JOIN cuserprojectsettings ps ON u.user_id = ps.user_id        │
│     LEFT JOIN cproject p ON ps.project_id = p.project_id               │
│     WHERE u.login = ?                                                   │
│       AND u.company_id = ?  ← Enforces company isolation               │
│                                                                         │
│     CONSTRAINT: UNIQUE (login, company_id)  ← DB-level enforcement     │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 │ Result Set
                                 ↓
                    [Authentication Success Path]
                                 │
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│              AUTHENTICATION SUCCESS HANDLER                             │
│                                                                         │
│  8. CAuthenticationSuccessHandler.onAuthenticationSuccess()            │
│     ├─ Logs successful authentication                                  │
│     ├─ Determines target URL from:                                     │
│     │  ├─ 1. redirect parameter from form                              │
│     │  ├─ 2. Originally requested URL from session                     │
│     │  ├─ 3. Default view from system settings                         │
│     │  └─ 4. Fallback: /home                                           │
│     │                                                                   │
│     ├─ Clears requested URL from session                               │
│     │                                                                   │
│     └─ Redirects to target URL                                         │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ↓
┌────────────────────────────────────────────────────────────────────────┐
│                    SESSION MANAGEMENT                                   │
│                                                                         │
│  9. Spring Security creates HttpSession                                │
│     ├─ Stores SecurityContext                                          │
│     │  └─ Contains authenticated Authentication object                 │
│     ├─ Stores user principal                                           │
│     └─ Session attributes:                                             │
│        ├─ SPRING_SECURITY_CONTEXT                                      │
│        └─ Company context (set by application after login)             │
└────────────────────────────────┬───────────────────────────────────────┘
                                 │
                                 ↓
                      [User is authenticated]
                                 │
                                 ↓
                        [Redirect to home page]
```

## Key Points in the Flow

### 1. Company Context Propagation
The company ID flows through the entire authentication chain:
```
Login Form → Filter → Token → Provider → Service → Repository → Database
```

### 2. Security Layers
- **Database**: Unique constraint on (login, company_id)
- **Repository**: WHERE clause filters by company_id
- **Service**: Validates company context exists
- **Provider**: Passes company context to service
- **Filter**: Extracts company from form

### 3. Error Handling Points
- **Filter**: Invalid company ID format
- **Provider**: Password mismatch
- **Service**: User not found
- **Repository**: Database error

### 4. Critical Design Choices
1. **Custom Token**: Carries company ID through auth chain
2. **Custom Provider**: Handles company-aware tokens
3. **Custom Filter**: Extracts company from form
4. **New Service Method**: Accepts company parameter
5. **Lazy Injection**: Breaks circular dependency

## Comparison: Before vs After

### Before (Broken)
```
Login Form → Spring Security → loadUserByUsername(username)
                                        ↓
                                getCurrentCompany() → Session
                                        ↓
                                    (null) → Exception!
```

### After (Working)
```
Login Form → Custom Filter → Custom Provider → loadUserByUsernameAndCompany(username, companyId)
                                                                      ↓
                                                            Repository Query
                                                                      ↓
                                                              User Found!
```

## Security Considerations

1. **Multi-Tenant Isolation**: Company ID enforced at every layer
2. **Password Security**: BCrypt hashing with automatic salt
3. **Session Security**: Created only after successful authentication
4. **SQL Injection**: Prevented by JPA parameter binding
5. **Timing Attacks**: Generic error messages for invalid credentials

## Performance Considerations

1. **Eager Loading**: User type and project settings loaded with user
2. **Query Optimization**: Single query with LEFT JOIN FETCH
3. **Password Validation**: BCrypt comparison is intentionally slow
4. **Session Creation**: Happens after authentication succeeds
5. **Database Constraint**: Unique index speeds up lookups

## Debugging Tips

Enable debug logging to see the flow:
```properties
logging.level.tech.derbent.login=DEBUG
logging.level.tech.derbent.users=DEBUG
logging.level.org.springframework.security=DEBUG
```

Look for these log messages:
- "Creating company-aware authentication token for user"
- "Authenticating user '{}' for company ID"
- "Attempting to load user by username: {} and company ID: {}"
- "User '{}' authenticated successfully"
- "Redirecting user {} to"
