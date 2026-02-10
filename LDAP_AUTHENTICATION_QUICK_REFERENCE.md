# LDAP Authentication Quick Reference

**Version**: 1.0  
**Date**: 2026-02-10

## Quick Configuration

### System Settings (Enable LDAP)

```yaml
Enable LDAP Authentication: ✅ Checked
LDAP Server URL: ldap://ldap.company.com:389
LDAP Bind DN: cn=admin,dc=company,dc=com
LDAP Bind Password: [your password]
LDAP Search Base: ou=users,dc=company,dc=com
LDAP User Filter: (uid={0})
```

### User Settings (Mark as LDAP User)

```yaml
User: john.doe
Login: john
Is LDAP User: ✅ Checked
Password: [leave empty for LDAP users]
```

## Common LDAP Configurations

### OpenLDAP (Most Common)

```properties
ldapServerUrl=ldap://localhost:389
ldapBindDn=cn=admin,dc=company,dc=com
ldapBindPassword=secret
ldapSearchBase=ou=users,dc=company,dc=com
ldapUserFilter=(uid={0})
```

**User DN**: `uid=john,ou=users,dc=company,dc=com`

### Active Directory

```properties
ldapServerUrl=ldap://ad.company.com:389
ldapBindDn=cn=LDAP Admin,cn=Users,dc=company,dc=com
ldapBindPassword=secret
ldapSearchBase=cn=Users,dc=company,dc=com
ldapUserFilter=(sAMAccountName={0})
```

**User DN**: `cn=John Doe,cn=Users,dc=company,dc=com`

### LDAPS (Secure - Recommended)

```properties
ldapServerUrl=ldaps://ldap.company.com:636
ldapBindDn=cn=admin,dc=company,dc=com
ldapBindPassword=secret
ldapSearchBase=ou=users,dc=company,dc=com
ldapUserFilter=(uid={0})
```

**Note**: Uses SSL/TLS on port 636

## Field Descriptions

| Field | Description | Example | Required |
|-------|-------------|---------|----------|
| **Enable LDAP Authentication** | Master switch for LDAP | `true` | Yes |
| **LDAP Server URL** | LDAP server address | `ldap://server:389` | Yes |
| **LDAP Bind DN** | Admin account for binding | `cn=admin,dc=company,dc=com` | No* |
| **LDAP Bind Password** | Password for bind DN | `secret123` | No* |
| **LDAP Search Base** | Base DN for user searches | `ou=users,dc=company,dc=com` | Yes |
| **LDAP User Filter** | Filter for user lookup | `(uid={0})` | Yes |

*Anonymous bind used if not provided (if server allows)

## Validation Rules

### ✅ Valid Configurations

```properties
# Minimal (anonymous bind)
enableLdapAuthentication=true
ldapServerUrl=ldap://localhost:389
ldapSearchBase=ou=users,dc=company,dc=com
ldapUserFilter=(uid={0})

# Full (authenticated bind)
enableLdapAuthentication=true
ldapServerUrl=ldaps://ldap.company.com:636
ldapBindDn=cn=admin,dc=company,dc=com
ldapBindPassword=secret123
ldapSearchBase=ou=users,dc=company,dc=com
ldapUserFilter=(uid={0})
```

### ❌ Invalid Configurations

```properties
# Missing {0} placeholder
ldapUserFilter=(uid=john)  # ❌ Error: Must contain {0}

# Invalid URL format
ldapServerUrl=http://ldap.com  # ❌ Error: Must start with ldap:// or ldaps://

# Invalid DN format
ldapSearchBase=users  # ❌ Error: Must be valid DN (e.g., ou=users,dc=company,dc=com)

# Empty required fields (when LDAP enabled)
ldapServerUrl=  # ❌ Error: Required when LDAP enabled
```

## Testing Procedures

### 1. Test LDAP Connection

**Command** (Linux/Mac):
```bash
ldapsearch -x -H ldap://server:389 -D "cn=admin,dc=company,dc=com" -w password -b "" -s base
```

**Expected**: Connection successful, server info returned

### 2. Test User Lookup

**Command**:
```bash
ldapsearch -x -H ldap://server:389 -D "cn=admin,dc=company,dc=com" -w password \
  -b "ou=users,dc=company,dc=com" "(uid=john)"
```

**Expected**: User entry returned with DN

### 3. Test User Authentication

**Command**:
```bash
ldapsearch -x -H ldap://server:389 -D "uid=john,ou=users,dc=company,dc=com" -w userpassword \
  -b "uid=john,ou=users,dc=company,dc=com"
```

**Expected**: Authentication successful, user entry returned

### 4. Test Application Login

**Steps**:
1. Enable LDAP in System Settings
2. Create user with `isLDAPUser = true`
3. Login with LDAP credentials
4. Check logs for authentication flow

**Expected Logs**:
```
🔐 LDAP authentication attempt: username=john
DEBUG CLdapAuthenticator: Connecting to LDAP server: ldap://server:389
DEBUG CLdapAuthenticator: User DN: uid=john,ou=users,dc=company,dc=com
✅ LDAP authentication successful: username=john
```

## Troubleshooting Checklist

### Login Fails

- [ ] Is LDAP enabled in System Settings?
- [ ] Is user marked as LDAP user (`isLDAPUser = true`)?
- [ ] Is LDAP server running and accessible?
- [ ] Can you connect to LDAP server from application server?
- [ ] Are LDAP credentials correct?
- [ ] Check application logs for errors

### Connection Issues

- [ ] Firewall allows port 389 (ldap) or 636 (ldaps)?
- [ ] Network route exists from app server to LDAP server?
- [ ] DNS resolution works for LDAP server hostname?
- [ ] LDAP server accepts connections from app server IP?

### Authentication Issues

- [ ] User exists in LDAP directory?
- [ ] User password is correct?
- [ ] ldapSearchBase is correct?
- [ ] ldapUserFilter matches user attribute?
- [ ] User DN construction is correct?

## Common Errors

### Error: "Cannot connect to LDAP server"

**Cause**: Network/firewall issue  
**Solution**: Check network connectivity, firewall rules

### Error: "Invalid credentials"

**Cause**: Wrong username or password  
**Solution**: Verify credentials with LDAP admin

### Error: "LDAP authentication disabled"

**Cause**: `enableLdapAuthentication = false`  
**Solution**: Enable LDAP in System Settings

### Error: "LDAP Server URL must start with ldap:// or ldaps://"

**Cause**: Invalid URL format  
**Solution**: Use correct protocol prefix

### Error: "LDAP User Filter must contain {0} placeholder"

**Cause**: Missing placeholder in filter  
**Solution**: Add `{0}` for username substitution

## Security Best Practices

### ✅ DO

- ✅ Use `ldaps://` (SSL/TLS) in production
- ✅ Use read-only LDAP bind account
- ✅ Restrict LDAP bind account permissions
- ✅ Monitor failed authentication attempts
- ✅ Use strong LDAP bind passwords

### ❌ DON'T

- ❌ Use `ldap://` (plain text) in production
- ❌ Use admin account for bind DN
- ❌ Store LDAP passwords in plain text (encrypt!)
- ❌ Ignore failed authentication logs
- ❌ Use weak LDAP bind passwords

## Log Monitoring

### Successful Authentication

```log
INFO  CUserService: Loading user by username: john@1
DEBUG CLdapAuthenticator: 🔐 LDAP authentication attempt: username=john
DEBUG CLdapAuthenticator: Using LDAP settings from system configuration
DEBUG CLdapAuthenticator: LDAP Server URL: ldap://server:389
DEBUG CLdapAuthenticator: Search Base: ou=users,dc=company,dc=com
DEBUG CLdapAuthenticator: User Filter: (uid={0})
DEBUG CLdapAuthenticator: Constructing user DN for username: john
DEBUG CLdapAuthenticator: User DN: uid=john,ou=users,dc=company,dc=com
DEBUG CLdapAuthenticator: Creating LDAP context for authentication
INFO  CLdapAuthenticator: ✅ LDAP authentication successful: username=john, dn=uid=john,ou=users,dc=company,dc=com
```

### Failed Authentication

```log
WARN  CLdapAuthenticator: ❌ LDAP authentication failed: username=john
WARN  CLdapAuthenticator: Reason: Invalid credentials - check username and password
```

### Connection Failure

```log
ERROR CLdapAuthenticator: ❌ LDAP connection failed: url=ldap://server:389
ERROR CLdapAuthenticator: Reason: Connection timed out after 5000ms
ERROR CLdapAuthenticator: Check network connectivity and firewall rules
```

## Implementation Status

### ✅ Completed (All Phases)

- [x] **Phase 1**: Entity fields (CSystemSettings, CUser)
- [x] **Phase 2**: LDAP authenticator (CLdapAuthenticator)
- [x] **Phase 2**: Authentication provider (CLdapAwareAuthenticationProvider)
- [x] **Phase 2**: Security configuration (CSecurityConfig)
- [x] **Phase 3**: Validation logic (CSystemSettingsService, CUserService)
- [x] **Phase 4**: UI initializers (System Settings - BAB & Derbent)
- [x] **Phase 4**: UI initializers (User Management)
- [x] **Phase 4**: Sample data (LDAP examples with disabled defaults)
- [x] **Phase 5**: Documentation (LDAP_AUTHENTICATION_IMPLEMENTATION.md)
- [x] **Phase 5**: Quick reference (LDAP_AUTHENTICATION_QUICK_REFERENCE.md)

### 🎯 Implementation Complete

**All planned phases have been completed successfully!**

LDAP authentication is now fully integrated into the Derbent platform and ready for use. UI fields are available in System Settings and User Management. Sample data includes disabled LDAP configuration and a test LDAP user.

## Support

**Full Documentation**: `/LDAP_AUTHENTICATION_IMPLEMENTATION.md`  
**Contact**: Derbent Development Team

---

**Last Updated**: 2026-02-10  
**Implementation Status**: Core complete ✅
