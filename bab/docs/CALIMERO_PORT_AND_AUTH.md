# Calimero Server - Port and Authentication Guide

## Server Configuration

**Calimero HTTP API Server:**
- **Host:** `localhost` (default)
- **Port:** `8077` (MANDATORY - configured in Calimero)
- **Endpoint:** `/api/request` (POST)
- **Auth Token:** `test-token-123` (configurable in `config/http_server.json`)

## Authentication

**All API requests require Bearer token authentication.**

### Header Format
```
Authorization: Bearer test-token-123
```

### Example Request
```bash
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"system","data":{"operation":"processes"}}'
```

## Request Format (CRITICAL)

**Calimero uses nested `data` object with `operation` inside:**

```json
{
  "type": "system",
  "data": {
    "operation": "processes"
  }
}
```

**NOT this format:**
```json
{
  "type": "system",
  "operation": "processes"  ❌ WRONG - operation must be inside data
}
```

## Response JSON Keys (CRITICAL - FIX REQUIRED)

### 1. System Processes
**Request:**
```json
{"type": "system", "data": {"operation": "processes"}}
```

**Response:**
```json
{
  "type": "system",
  "data": {
    "processes": [...]  ✅ Key is "processes"
  }
}
```

### 2. Systemd Services  
**Request:**
```json
{"type": "systemservices", "data": {"operation": "list"}}
```

**Response:**
```json
{
  "type": "systemservices",
  "data": {
    "services": [...],      ✅ Key is "services" (NOT "systemservices")
    "count": 70,
    "totalCount": 180,
    "activeOnly": false,
    "runningOnly": false
  }
}
```

### 3. Web Service Discovery
**Request:**
```json
{"type": "webservice", "data": {"operation": "list"}}
```

**Response:**
```json
{
  "type": "webservice",
  "data": {
    "services": [...],  ✅ Key is "services" (NOT "systemservices")
    "count": 27,
    "version": "1.0.0"
  }
}
```

## Derbent BAB Project Configuration

**Entity:** `CProject_Bab`  
**Fields:**
- `ipAddress`: Target server IP (e.g., "localhost", "192.168.1.100")
- `authToken`: Bearer token for authentication (e.g., "test-token-123")

**HTTP Client:** `CClientProject`  
- Default port: `8077` (hardcoded constant)
- Automatically adds auth header if `authToken` is configured
- Connection check on every request with 30-second cooldown

## Testing

### Test Script
Use the official Calimero test script:
```bash
cd ~/git/calimero
./scripts/test_http_api.sh
```

### Manual Test
```bash
AUTH_TOKEN="test-token-123"

# Test system processes
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"system","data":{"operation":"processes"}}'

# Test systemd services  
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"systemservices","data":{"operation":"list"}}'

# Test web service discovery
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"type":"webservice","data":{"operation":"list"}}'
```

## Bug Fix Required

**Java clients are looking for WRONG JSON keys:**

### ❌ Current (BROKEN):
```java
// CSystemServiceCalimeroClient.java
if (data.has("systemservices") && data.get("systemservices").isJsonArray()) {
    final JsonArray serviceArray = data.getAsJsonArray("systemservices");
    // ...
}
```

### ✅ Should be:
```java
// CSystemServiceCalimeroClient.java
if (data.has("services") && data.get("services").isJsonArray()) {
    final JsonArray serviceArray = data.getAsJsonArray("services");
    // ...
}
```

**Same fix needed for:**
- `CSystemServiceCalimeroClient.java` - Change "systemservices" → "services"
- `CWebServiceDiscoveryCalimeroClient.java` - Change "systemservices" → "services"
- `CSystemProcessCalimeroClient.java` - Already correct ("processes")

## Verification

After fix, test with live Calimero server:
```bash
# Start Derbent BAB application
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=bab"

# Configure BAB project:
# 1. Navigate to BAB Projects
# 2. Set IP Address: localhost
# 3. Set Auth Token: test-token-123
# 4. Open dashboard - should show:
#    - 70 systemd services
#    - 27 web service endpoints
#    - 50+ processes
```

## Security Notes

1. **Change default token in production!**
2. **Use HTTPS in production**
3. **Store token securely** (not in plain text config)
4. **Implement token rotation**
5. **Use environment variables for sensitive data**

## Documentation Links

- Calimero test script: `~/git/calimero/scripts/test_http_api.sh`
- Curl examples: `~/git/calimero/src/http/docs/CURL_EXAMPLES.md`
- API reference: `~/git/calimero/src/http/docs/API_REFERENCE.md`
- Derbent BAB docs: `~/git/derbent/bab/docs/`
