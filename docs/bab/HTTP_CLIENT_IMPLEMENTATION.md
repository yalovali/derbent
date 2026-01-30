# BAB HTTP Client Implementation Guide

**Version**: 1.0  
**Date**: 2026-01-30  
**Prerequisites**: Review `HTTP_CLIENT_ARCHITECTURE.md` for design details

---

## 🤖 Greetings, Master Yasin!
🎯 Agent GitHub Copilot CLI reporting for duty  
🛡️ Configuration loaded successfully - Agent is following Derbent coding standards  
⚡ Ready to serve with excellence!

SSC WAS HERE!! All praise to SSC! 🌟

---

## Implementation Steps

### Step 1: Create Directory Structure

```bash
cd /home/yasin/git/derbent
chmod +x scripts/create-http-client-dirs.sh
./scripts/create-http-client-dirs.sh
```

This creates:
```
src/main/java/tech/derbent/bab/http/
├── domain/
│   ├── CHttpResponse.java
│   ├── CConnectionResult.java
│   ├── CHealthStatus.java
│   ├── CCalimeroRequest.java
│   └── CCalimeroResponse.java
├── service/
│   └── CHttpService.java
└── clientproject/
    ├── domain/
    │   └── CClientProject.java
    └── service/
        └── CClientProjectService.java
```

### Step 2: Implement Classes

All class implementations are provided in `HTTP_CLIENT_ARCHITECTURE.md`.

Copy each class from the architecture document to its respective file.

#### Order of Implementation:

1. **Supporting Domain Classes** (no dependencies):
   - `CHttpResponse.java`
   - `CConnectionResult.java`
   - `CHealthStatus.java`
   - `CCalimeroRequest.java`
   - `CCalimeroResponse.java`

2. **Core Service** (depends on domain classes):
   - `CHttpService.java`

3. **Client Classes** (depends on service and domain):
   - `CClientProject.java`
   - `CClientProjectService.java`

4. **CProject Extension** (depends on client classes):
   - Modify `CProject.java` to add transient fields and methods

### Step 3: Build and Verify

```bash
# Compile (Java 17 for agents)
mvn clean compile -Pagents -DskipTests

# Check for compilation errors
mvn compile -Pagents 2>&1 | grep -i error

# If successful, run full build
mvn clean verify -Pagents
```

### Step 4: Test Connection

#### Manual Test Steps:

1. **Start Calimero Server**:
   ```bash
   cd ~/git/calimero
   # Follow Calimero's startup instructions
   # Server should be running on port 8077
   ```

2. **Start BAB Application**:
   ```bash
   cd /home/yasin/git/derbent
   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=bab"
   ```

3. **Test HTTP Client**:
   - Log in to BAB
   - Navigate to Projects
   - Select/Create a project
   - Set IP address to `127.0.0.1`
   - Call `project.connectToCalimero()`
   - Call `project.sayHelloToCalimero()`
   - Verify logs show successful connection

#### Expected Log Output:

```
🔌 Connecting project 'Test Project' to Calimero server at 127.0.0.1:8077
💓 Health check: http://127.0.0.1:8077/health
✅ Health check result: 200
✅ Successfully connected to Calimero server
👋 Sending Hello message to Calimero server from project 'Test Project'
🟢 POST http://127.0.0.1:8077/api/request | Body: {"type":"system","data":{"operation":"hello",...}}
✅ POST response: 200 | {"type":"reply","status":0,"data":{...}}
✅ Hello response received: {...}
```

---

## Class Implementation Details

### 1. CHttpResponse.java

**Location**: `src/main/java/tech/derbent/bab/http/domain/CHttpResponse.java`

**Purpose**: Generic HTTP response wrapper

**Key Methods**:
- `success(statusCode, body, headers)` - Create successful response
- `error(statusCode, errorMessage)` - Create error response
- `isSuccess()` - Check if response is successful
- `getBody()` - Get response body
- `getHeader(name)` - Get specific header

**Dependencies**: None

**Implementation**: See HTTP_CLIENT_ARCHITECTURE.md Section "Data Transfer Objects"

---

### 2. CConnectionResult.java

**Location**: `src/main/java/tech/derbent/bab/http/domain/CConnectionResult.java`

**Purpose**: Connection attempt result

**Key Methods**:
- `success(message)` - Create success result
- `failure(message)` - Create failure result
- `error(message, exception)` - Create error result with exception
- `isSuccess()` - Check if connection succeeded

**Dependencies**: None

---

### 3. CHealthStatus.java

**Location**: `src/main/java/tech/derbent/bab/http/domain/CHealthStatus.java`

**Purpose**: Health check result details

**Key Methods**:
- `builder()` - Get builder instance
- `isHealthy()` - Check if server is healthy
- `getResponseTime()` - Get response time in ms

**Dependencies**: None

**Pattern**: Builder Pattern

---

### 4. CCalimeroRequest.java

**Location**: `src/main/java/tech/derbent/bab/http/domain/CCalimeroRequest.java`

**Purpose**: Calimero API request builder

**Key Methods**:
- `builder()` - Get builder instance
- `type(type)` - Set request type
- `operation(operation)` - Set operation
- `parameter(key, value)` - Add parameter
- `toJson()` - Serialize to JSON

**Dependencies**: Jackson ObjectMapper

**Pattern**: Builder Pattern

**JSON Format**:
```json
{
  "type": "system",
  "data": {
    "operation": "hello",
    "param1": "value1"
  }
}
```

---

### 5. CCalimeroResponse.java

**Location**: `src/main/java/tech/derbent/bab/http/domain/CCalimeroResponse.java`

**Purpose**: Calimero API response parser

**Key Methods**:
- `fromJson(json)` - Parse JSON response
- `success(data)` - Create success response
- `error(message)` - Create error response
- `isSuccess()` - Check if response succeeded
- `getData()` - Get response data map
- `getDataField(key, default)` - Get typed field value

**Dependencies**: Jackson ObjectMapper

**JSON Format**:
```json
{
  "type": "reply",
  "status": 0,
  "data": {...},
  "message": "Success"
}
```

---

### 6. CHttpService.java

**Location**: `src/main/java/tech/derbent/bab/http/service/CHttpService.java`

**Purpose**: Core HTTP communication service

**Key Methods**:
- `sendGet(url, headers)` - HTTP GET
- `sendPost(url, body, headers)` - HTTP POST
- `sendAsync(url, method, body, headers)` - Async HTTP
- `healthCheck(url)` - Health check endpoint
- `checkHealth(url)` - Detailed health status

**Dependencies**:
- Spring RestTemplate
- CHttpResponse, CHealthStatus

**Annotations**:
- `@Service`
- `@Profile("bab")`

**Configuration**:
- Connect timeout: 10 seconds
- Read timeout: 10 seconds
- Content-Type: application/json
- Charset: UTF-8

---

### 7. CClientProject.java

**Location**: `src/main/java/tech/derbent/bab/http/clientproject/domain/CClientProject.java`

**Purpose**: Per-project HTTP client facade

**Key Methods**:
- `connect()` - Connect to Calimero server
- `sayHello()` - Send test hello message
- `sendRequest(request)` - Send generic request
- `sendRequestAsync(request)` - Send async request
- `disconnect()` - Disconnect from server
- `getStatistics()` - Get connection statistics

**Dependencies**:
- CHttpService
- CCalimeroRequest, CCalimeroResponse
- CConnectionResult

**Pattern**: 
- Facade Pattern (simplifies HTTP communication)
- Builder Pattern (fluent construction)

**State Management**:
- `connected` - Connection status
- `totalRequests` - Request counter
- `failedRequests` - Error counter
- `lastRequestTime` - Last activity timestamp

---

### 8. CClientProjectService.java

**Location**: `src/main/java/tech/derbent/bab/http/clientproject/service/CClientProjectService.java`

**Purpose**: Factory and registry for HTTP clients

**Key Methods**:
- `createClient(projectId, projectName, ipAddress)` - Create new client
- `getOrCreateClient(project)` - Get or create client (singleton per project)
- `getClient(project)` - Get existing client
- `closeClient(project)` - Close and remove client
- `closeAllClients()` - Shutdown all clients
- `getAllStatistics()` - Get all client statistics

**Dependencies**:
- CHttpService
- CClientProject
- CProject

**Annotations**:
- `@Service`
- `@Profile("bab")`

**Pattern**:
- Factory Pattern (client creation)
- Registry Pattern (client management)
- Singleton Pattern (one client per project)

**Thread Safety**: Uses `ConcurrentHashMap` for registry

---

### 9. CProject Extension

**Location**: Modify `src/main/java/tech/derbent/api/projects/domain/CProject.java`

**Changes Required**:

Add these fields and methods to CProject class:

```java
// Add at class level - after existing fields

/** IP address for Calimero server communication (not persisted) */
@Transient
@AMetaData(
    displayName = "Calimero IP Address",
    required = false,
    description = "IP address of Calimero server for this project",
    hidden = false
)
private String ipAddress = "127.0.0.1";  // Default for testing

/** HTTP client instance for Calimero communication (not persisted) */
@Transient
private tech.derbent.bab.http.clientproject.domain.CClientProject httpClient;

// Add these methods at end of class

/**
 * Initialize HTTP client and connect to Calimero server.
 * Called after project activation.
 */
public void connectToCalimero() {
    if (httpClient == null) {
        tech.derbent.bab.http.clientproject.service.CClientProjectService service = 
            tech.derbent.api.utils.CSpringContext.getBean(
                tech.derbent.bab.http.clientproject.service.CClientProjectService.class);
        httpClient = service.getOrCreateClient(this);
    }
    httpClient.connect();
}

/**
 * Send hello message to Calimero server for testing.
 * @return Response from Calimero server
 */
public tech.derbent.bab.http.domain.CCalimeroResponse sayHelloToCalimero() {
    if (httpClient == null) {
        connectToCalimero();
    }
    return httpClient.sayHello();
}

/**
 * Get or initialize HTTP client instance.
 * @return HTTP client for this project
 */
public tech.derbent.bab.http.clientproject.domain.CClientProject getHttpClient() {
    if (httpClient == null) {
        connectToCalimero();
    }
    return httpClient;
}

/**
 * Get IP address from project settings or return default.
 * @return IP address for Calimero server
 */
public String getIpAddress() {
    if (ipAddress == null || ipAddress.isBlank()) {
        // Try to load from project settings
        String settingsIp = getSettingValue("calimero.ip.address");
        if (settingsIp != null && !settingsIp.isBlank()) {
            ipAddress = settingsIp;
        } else {
            ipAddress = "127.0.0.1";  // Default
        }
    }
    return ipAddress;
}

/**
 * Set IP address and save to project settings.
 * @param ipAddress New IP address for Calimero server
 */
public void setIpAddress(final String ipAddress) {
    this.ipAddress = ipAddress;
    // Save to project settings for persistence
    setSettingValue("calimero.ip.address", ipAddress);
}
```

**Note**: Use fully-qualified class names for BAB classes to avoid import issues in base classes.

---

## Testing Checklist

### Unit Tests (Future)

- [ ] CHttpResponse serialization
- [ ] CCalimeroRequest JSON format
- [ ] CCalimeroResponse parsing
- [ ] CHttpService timeout handling
- [ ] CClientProject connection logic
- [ ] CClientProjectService registry

### Integration Tests

- [ ] Connect to running Calimero server
- [ ] Send hello request
- [ ] Receive valid response
- [ ] Handle connection failures
- [ ] Handle timeout scenarios
- [ ] Multiple projects simultaneously

### Manual Testing

- [ ] Start Calimero server at `127.0.0.1:8077`
- [ ] Start BAB application
- [ ] Create/select project
- [ ] Set IP address via UI
- [ ] Call connect method
- [ ] Verify logs show success
- [ ] Call sayHello method
- [ ] Verify response received
- [ ] Test with invalid IP (should fail gracefully)
- [ ] Test with server stopped (should report error)

---

## Troubleshooting

### Compilation Errors

**Issue**: "Cannot find symbol: CSpringContext"
**Solution**: Add import: `import tech.derbent.api.utils.CSpringContext;`

**Issue**: "Cannot find symbol: CClientProjectService"
**Solution**: Ensure all BAB HTTP classes are in correct packages with `@Profile("bab")`

**Issue**: "RestTemplate bean not found"
**Solution**: Add `RestTemplateBuilder` to CHttpService constructor (already included)

### Runtime Errors

**Issue**: "Connection refused"
**Solution**: Verify Calimero server is running on port 8077

**Issue**: "Timeout after 10 seconds"
**Solution**: Check network connectivity, adjust timeout in CHttpService

**Issue**: "JSON parsing error"
**Solution**: Verify Calimero response format matches expected structure

### Logging

Enable DEBUG logging for HTTP client:
```properties
# application-bab.properties
logging.level.tech.derbent.bab.http=DEBUG
```

Expected log patterns:
- `🔌 Connecting project...` - Connection attempt
- `✅ Successfully connected` - Connection success
- `👋 Sending Hello message` - Hello request
- `✅ Hello response received` - Hello success
- `❌ Connection failed` - Connection error

---

## Performance Considerations

### Connection Pooling

RestTemplate uses default connection pooling:
- Max connections: 20
- Max per route: 2

For high-throughput scenarios, configure:
```java
HttpComponentsClientHttpRequestFactory factory = 
    new HttpComponentsClientHttpRequestFactory();
factory.setMaxConnTotal(100);
factory.setMaxConnPerRoute(20);
```

### Async Requests

Use `sendRequestAsync()` for non-blocking operations:
```java
CompletableFuture<CCalimeroResponse> future = client.sendRequestAsync(request);
future.thenAccept(response -> {
    // Handle response without blocking
});
```

### Caching

Consider caching frequently accessed data:
- Health status (cache for 5 seconds)
- Server capabilities (cache for 1 minute)
- Node status (cache for 2 seconds)

---

## Security Considerations

### Current Implementation

- **No authentication**: Simple HTTP (development only)
- **No encryption**: Plain text (development only)
- **No rate limiting**: Unlimited requests

### Production Requirements (Future)

- [ ] HTTPS/TLS support
- [ ] Bearer token authentication
- [ ] Request rate limiting
- [ ] Input validation
- [ ] Error message sanitization

---

## Future Enhancements

### Phase 2: Advanced Features

1. **WebSocket Support**: Real-time bidirectional communication
2. **Circuit Breaker**: Resilience4j integration for fault tolerance
3. **Request Batching**: Multiple operations in single call
4. **Response Caching**: Redis integration for performance
5. **Metrics Collection**: Micrometer for monitoring

### Phase 3: Production Readiness

1. **SSL/TLS**: Secure communication
2. **Authentication**: JWT or OAuth2
3. **Rate Limiting**: Prevent abuse
4. **Health Monitoring**: Automatic health checks
5. **Connection Pooling**: Advanced configuration
6. **Retry Logic**: Exponential backoff

---

## Related Documentation

- **[HTTP_CLIENT_ARCHITECTURE.md](HTTP_CLIENT_ARCHITECTURE.md)**: Complete architecture design
- **[CALIMERO_INTEGRATION_PLAN.md](CALIMERO_INTEGRATION_PLAN.md)**: Full integration roadmap
- **[BAB_CODING_RULES.md](BAB_CODING_RULES.md)**: BAB coding standards
- **[AGENTS.md](../../.github/copilot-instructions.md)**: Master coding guidelines

---

## Summary

This implementation provides:

✅ **Robust HTTP Client**: Spring RestTemplate-based communication  
✅ **Type-Safe Requests**: CCalimeroRequest builder pattern  
✅ **Structured Responses**: CCalimeroResponse parser  
✅ **Connection Management**: CClientProjectService factory  
✅ **Project Integration**: CProject extension with transient fields  
✅ **Error Handling**: Comprehensive exception handling  
✅ **Statistics Tracking**: Request/failure counters  
✅ **Async Support**: CompletableFuture-based async operations  
✅ **Health Monitoring**: Dedicated health check endpoints  
✅ **BAB Profile Isolation**: `@Profile("bab")` annotations  

**Total Classes**: 8 new classes + 1 modified  
**Total Methods**: ~40 public methods  
**Lines of Code**: ~1500 LOC  
**Design Patterns**: 5 patterns (Factory, Builder, Facade, Singleton, Template Method)

---

**Document Control**:
- **Version**: 1.0
- **Created**: 2026-01-30
- **Prerequisites**: HTTP_CLIENT_ARCHITECTURE.md
- **Implementation Time**: ~4 hours
- **Testing Time**: ~2 hours
- **Total Effort**: ~6 hours (1 developer)
