# BAB Authentication Token Storage

## Overview

Authentication tokens for Calimero HTTP API are stored in **cleartext** in the database for development purposes. This document explains the current implementation and future production considerations.

## Current Implementation (Development)

### Database Storage

**Table**: `cproject_bab`  
**Column**: `auth_token VARCHAR(255)`  
**Storage**: Cleartext (no encryption)

```sql
-- Column definition
ALTER TABLE cproject_bab ADD COLUMN auth_token VARCHAR(255);

-- Example data
auth_token = "test-token-123"
```

### Entity Field

**File**: `src/main/java/tech/derbent/bab/project/domain/CProject_Bab.java`

```java
@Column(name = "auth_token", length = 255)
@AMetaData(
    displayName = "Auth Token",
    required = false,
    readOnly = false,
    description = "Bearer authentication token for Calimero HTTP API",
    hidden = false,
    maxLength = 255
)
private String authToken;
```

### Sample Data Initialization

**File**: `src/main/java/tech/derbent/bab/project/service/CProject_BabInitializerService.java`

```java
public static CProject_Bab initializeSampleBab(final CCompany company, final boolean minimal) {
    // ...
    item.setIpAddress("127.0.0.1");
    item.setAuthToken("test-token-123"); // Default auth token
    // ...
}
```

### Default Value

**File**: `CProject_Bab.initializeDefaults()`

```java
private final void initializeDefaults() {
    ipAddress = "127.0.0.1";
    authToken = "test-token-123"; // Default for testing
    // ...
}
```

## UI Integration

### Form Field

Auth token field automatically appears in project edit form via `@AMetaData` annotation:

- **Display Name**: "Auth Token"
- **Type**: Text input (255 char max)
- **Visible**: Yes (hidden = false)
- **Editable**: Yes (readOnly = false)
- **Required**: No

### Grid Column

Token is visible in project list grid for easy reference:

```java
grid.setColumnFields(List.of(
    "id", "name", "description", 
    "ipAddress", "authToken",  // ← Visible in grid
    "active", "createdDate", "lastModifiedDate"
));
```

## Security Considerations

### Development (Current)

✅ **Acceptable for development**:
- Quick setup and testing
- Easy debugging
- No encryption overhead
- Visible in UI for configuration

### Production (Future)

❌ **NOT acceptable for production**:
- Tokens visible in database dumps
- No protection against SQL injection exposure
- No audit trail for token access
- No token rotation mechanism

## Future Production Enhancements

### 1. Token Encryption

**Priority**: HIGH  
**Timeline**: Before production deployment

```java
// Encrypt before storing
@PrePersist
@PreUpdate
private void encryptToken() {
    if (authToken != null && !authToken.startsWith("ENC:")) {
        authToken = "ENC:" + encryptionService.encrypt(authToken);
    }
}

// Decrypt after loading
@PostLoad
private void decryptToken() {
    if (authToken != null && authToken.startsWith("ENC:")) {
        authToken = encryptionService.decrypt(authToken.substring(4));
    }
}
```

### 2. UI Password Field

**Priority**: MEDIUM  
**Timeline**: Before production deployment

```java
@AMetaData(
    displayName = "Auth Token",
    fieldType = FieldType.PASSWORD,  // ← Mask input
    hidden = false
)
```

### 3. Token Rotation

**Priority**: MEDIUM  
**Timeline**: Post-MVP

```java
@Column(name = "token_expires_at")
private LocalDateTime tokenExpiresAt;

public boolean isTokenExpired() {
    return tokenExpiresAt != null && LocalDateTime.now().isAfter(tokenExpiresAt);
}
```

### 4. Audit Trail

**Priority**: LOW  
**Timeline**: Post-MVP

```java
@Column(name = "token_last_used")
private LocalDateTime tokenLastUsed;

@Column(name = "token_usage_count")
private Integer tokenUsageCount;
```

### 5. Token Validation

**Priority**: MEDIUM  
**Timeline**: Post-MVP

```java
@Override
protected void validateEntity(final CProject_Bab entity) {
    super.validateEntity(entity);
    
    // Validate token format
    if (entity.getAuthToken() != null) {
        if (entity.getAuthToken().length() < 8) {
            throw new CValidationException("Token must be at least 8 characters");
        }
        if (entity.getAuthToken().contains(" ")) {
            throw new CValidationException("Token cannot contain spaces");
        }
    }
}
```

## Migration Path

### Step 1: Add Encryption (Before Production)

1. Create `CTokenEncryptionService`
2. Add `@PrePersist`/`@PostLoad` hooks to `CProject_Bab`
3. Migrate existing tokens: `UPDATE cproject_bab SET auth_token = encrypt(auth_token)`
4. Test with encrypted tokens

### Step 2: Update UI (Before Production)

1. Change `@AMetaData` to use `PASSWORD` field type
2. Add "Test Connection" button to verify token
3. Show masked token in grid (e.g., "test-...123")

### Step 3: Add Rotation (Post-MVP)

1. Add expiration fields
2. Implement automatic token refresh
3. Handle 401 responses with token re-request

## Testing

### Verify Current Implementation

```bash
# 1. Start application
cd ~/git/derbent
mvn spring-boot:run -Dspring-boot.run.profiles=bab

# 2. Check sample data
# Login: admin@1 / admin
# Navigate to: BAB Gateway Projects
# Verify: Token field visible and editable

# 3. Test token in database
psql -d derbent -c "SELECT name, ip_address, auth_token FROM cproject_bab;"

# Expected output:
# name              | ip_address  | auth_token
# ------------------+-------------+---------------
# BAB Gateway Core  | 127.0.0.1   | test-token-123
```

### Verify HTTP Integration

```bash
# Test with default token
curl -X POST http://127.0.0.1:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"kind":"question","type":"system","data":{"operation":"info"}}'

# Expected: 200 OK with system info
```

## Decision Log

### 2026-01-30: Cleartext Storage Approved

**Decision**: Store tokens in cleartext for development  
**Rationale**: 
- Faster development iteration
- Easy debugging and configuration
- No encryption overhead during testing
- Production encryption can be added later

**Approved By**: User (project owner)  
**Status**: Implemented  
**Future Action**: Add encryption before production deployment

## Related Documentation

- [BAB HTTP Client Authentication](BAB_HTTP_CLIENT_AUTHENTICATION.md) - Complete authentication setup
- [Calimero Server Configuration](../../git/calimero/docs/HTTP_API.md) - Server-side auth config
- [BAB HTTP Client Design](BAB_HTTP_CLIENT_DESIGN.md) - Overall client architecture

## Summary

✅ **Current**: Cleartext storage for development  
⏳ **Future**: Encrypted storage for production  
🎯 **Goal**: Balance development speed with production security

---

**Note**: This is a temporary implementation. Encryption MUST be implemented before production deployment.
