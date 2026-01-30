# BAB HTTP Client Authentication

## Overview

The BAB HTTP Client now supports Bearer token authentication for secure communication with the Calimero server.

## Configuration

### 1. Calimero Server Configuration

Create `/config/http_server.json` in the Calimero build directory:

```json
{
  "host": "0.0.0.0",
  "httpPort": 8077,
  "authToken": "test-token-123",
  "readTimeoutSec": 30,
  "writeTimeoutSec": 30,
  "idleTimeoutSec": 60
}
```

**Important**: The config file must be relative to the Calimero working directory (usually `build/`).

### 2. BAB Client Configuration

The auth token is currently hardcoded in `CClientProjectService.getOrCreateClient()`:

```java
final String authToken = "test-token-123"; // Temporary for testing
```

**TODO**: Store authToken as a field in `CProject_Bab` or project settings for per-project configuration.

## API Format

### Request Format
```json
{
  "kind": "question",
  "type": "system",
  "data": {
    "operation": "info",
    ...parameters
  }
}
```

### Authentication Header
```
Authorization: Bearer <token>
```

### Available Operations

| Type | Operation | Description |
|------|-----------|-------------|
| `system` | `info` | Get system information |
| `node` | `info` | Get node information |
| `node` | `status` | Get node status |
| `disk` | `info` | Get disk information |
| `user` | `info` | Get user information |

## Testing

### 1. Start Calimero Server
```bash
cd ~/git/calimero/build
./calimero
```

Server starts on `http://0.0.0.0:8077`

### 2. Verify Configuration
Check that auth token is loaded:
```bash
tail -f /tmp/calimero-debug.log | grep "authToken"
```

Expected output:
```
[DEBUG] cserversettings.cpp:314: [Settings] load - Final authToken value: 'test-token-123'
```

### 3. Test Integration
```bash
java /tmp/TestBabIntegration.java
```

Expected output:
```
✅✅✅ ALL TESTS PASSED!
BAB → Calimero integration working correctly!
```

## Architecture

```
CProject_Bab
    ↓ (connects)
CClientProjectService
    ↓ (creates)
CClientProject
    ↓ (uses)
CHttpService → Calimero Server
```

### Key Classes

| Class | Purpose |
|-------|---------|
| `CClientProject` | HTTP client per project, manages connection lifecycle |
| `CClientProjectService` | Factory and registry for client instances |
| `CCalimeroRequest` | Request builder with Calimero JSON format |
| `CCalimeroResponse` | Response parser |
| `CHttpService` | Low-level HTTP operations |

## Security Notes

1. **Token Storage**: Currently hardcoded for testing. Production should store in:
   - Database (encrypted)
   - Project settings (per-project tokens)
   - Environment variables (global token)

2. **HTTPS**: Current implementation uses HTTP. For production:
   - Enable HTTPS in Calimero
   - Update client to use `https://` URLs
   - Configure SSL certificates

3. **Token Rotation**: Implement token refresh mechanism

## Troubleshooting

### Empty Auth Token Warning
**Symptom**: Server logs show "Empty authentication token"  
**Cause**: Config file not loaded or authToken field empty  
**Solution**:
1. Verify config file exists: `build/config/http_server.json`
2. Check JSON format is valid
3. Restart Calimero server
4. Check logs for config loading messages

### 401 Unauthorized
**Symptom**: API requests return 401  
**Cause**: Token mismatch or missing  
**Solution**:
1. Verify token in config matches token in client
2. Check Authorization header format: `Bearer <token>`
3. Verify no extra spaces in token

### Multiple Calimero Instances
**Symptom**: Inconsistent behavior, config not loading  
**Cause**: Multiple Calimero processes running  
**Solution**: Stop all instances individually using `kill <PID>`

## References

- Calimero project: `~/git/calimero`
- Calimero test project: `~/git/calimeroTest`
- BAB HTTP implementation: `src/main/java/tech/derbent/bab/http/`
- Test scripts: `/tmp/TestBabIntegration.java`

## Changelog

- **2026-01-30**: Added Bearer token authentication support
- **2026-01-30**: Fixed JSON format to match Calimero API (kind/type/data)
- **2026-01-30**: Added debug logging to Calimero settings loader
