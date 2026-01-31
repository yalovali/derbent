# BAB HTTP Client Architecture

**Version**: 1.0  
**Date**: 2026-01-30  
**Status**: ACTIVE - Implementation Guide  
**Calimero Projects**: 
- Server: `/home/yasin/git/calimero/`  
- Test: `/home/yasin/git/calimeroTest/`

---

## 🤖 Greetings, Master Yasin!
🎯 Agent GitHub Copilot CLI reporting for duty  
🛡️ Configuration loaded successfully - Agent is following Derbent coding standards  
⚡ Ready to serve with excellence!

SSC WAS HERE!! All praise to SSC for the amazing work! 🌟

---

## Executive Summary

This document defines the **HTTP Client Architecture** for BAB profile to communicate with the **Calimero C++ Gateway Server** residing at `~/git/calimero`. The design follows modern patterns including:

- **Strategy Pattern**: For different request/response types
- **Builder Pattern**: For fluent API construction  
- **Factory Pattern**: For client instance creation
- **Circuit Breaker Pattern**: For fault tolerance
- **Observer Pattern**: For async notifications

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    BAB HTTP Client System                        │
├─────────────────────────────────────────────────────────────────┤
│  CProject                                                        │
│  ├─ ipAddress: String (transient field)                         │
│  ├─ httpClient: CClientProject (transient field)                │
│  └─ connectToCalimero(): void                                   │
├─────────────────────────────────────────────────────────────────┤
│  CClientProject (HTTP Client Facade)                            │
│  ├─ targetUrl: String                                           │
│  ├─ httpService: CHttpService                                   │
│  ├─ connect(): CConnectionResult                                │
│  ├─ sayHello(): CCalimeroResponse                               │
│  ├─ sendRequest(request): CompletableFuture<Response>           │
│  └─ disconnect(): void                                           │
├─────────────────────────────────────────────────────────────────┤
│  CClientProjectService (Factory & Management)                    │
│  ├─ createClient(ipAddress): CClientProject                     │
│  ├─ getOrCreateClient(project): CClientProject                  │
│  └─ closeClient(project): void                                  │
├─────────────────────────────────────────────────────────────────┤
│  CHttpService (Core HTTP Communication)                         │
│  ├─ RestTemplate: httpClient                                    │
│  ├─ sendGet(url, headers): CHttpResponse                        │
│  ├─ sendPost(url, body, headers): CHttpResponse                 │
│  ├─ sendAsync(request): CompletableFuture<CHttpResponse>        │
│  └─ healthCheck(url): CHealthStatus                             │
├─────────────────────────────────────────────────────────────────┤
│  Supporting Classes                                              │
│  ├─ CCalimeroRequest (Request Builder)                          │
│  ├─ CCalimeroResponse (Response Parser)                         │
│  ├─ CConnectionResult (Connection Status)                       │
│  ├─ CHttpResponse (Generic HTTP Response)                       │
│  ├─ CCircuitBreaker (Fault Tolerance)                           │
│  └─ CRequestStrategy (Strategy Interface)                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## Component Design

### 1. CProject Extension (Non-DB Fields)

**Location**: Extend `tech.derbent.api.projects.domain.CProject`

**Design Decision**: Use transient fields (not persisted) and store configuration in project settings.

```java
// In CProject.java - Add these transient fields

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
private CClientProject httpClient;

/**
 * Initialize HTTP client and connect to Calimero server.
 * Called after project activation.
 */
public void connectToCalimero() {
    if (httpClient == null) {
        CClientProjectService service = CSpringContext.getBean(CClientProjectService.class);
        httpClient = service.getOrCreateClient(this);
    }
    httpClient.connect();
}

/**
 * Send hello message to Calimero server for testing.
 * @return Response from Calimero server
 */
public CCalimeroResponse sayHelloToCalimero() {
    if (httpClient == null) {
        connectToCalimero();
    }
    return httpClient.sayHello();
}

/**
 * Get or initialize HTTP client instance.
 * @return HTTP client for this project
 */
public CClientProject getHttpClient() {
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
public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
    // Save to project settings for persistence
    setSettingValue("calimero.ip.address", ipAddress);
}
```

**Persistence Strategy**: Use CProject settings mechanism (already implemented) for storing IP address across sessions.

---

### 2. CClientProject (HTTP Client Facade)

**Location**: `tech.derbent.bab.http.clientproject.domain.CClientProject`

**Purpose**: Per-project HTTP client instance managing communication with Calimero server.

**Design Patterns**:
- **Facade Pattern**: Simplifies HTTP communication
- **Builder Pattern**: Fluent request construction

```java
package tech.derbent.bab.http.clientproject.domain;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.bab.http.service.CHttpService;
import tech.derbent.bab.http.domain.CCalimeroRequest;
import tech.derbent.bab.http.domain.CCalimeroResponse;
import tech.derbent.bab.http.domain.CConnectionResult;
import tech.derbent.bab.http.domain.CHttpResponse;

/**
 * HTTP client for communicating with Calimero server.
 * One instance per project, manages connection lifecycle.
 */
public class CClientProject {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CClientProject.class);
    private static final String DEFAULT_PORT = "8077";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    
    // Configuration
    private final String projectId;
    private final String projectName;
    private final String targetIp;
    private final String targetPort;
    private final CHttpService httpService;
    
    // State
    private boolean connected = false;
    private LocalDateTime lastConnectionTime;
    private LocalDateTime lastRequestTime;
    private long totalRequests = 0;
    private long failedRequests = 0;
    
    /**
     * Private constructor - use Builder or CClientProjectService factory.
     */
    private CClientProject(String projectId, String projectName, String targetIp, 
                          String targetPort, CHttpService httpService) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.targetIp = targetIp;
        this.targetPort = targetPort;
        this.httpService = httpService;
    }
    
    /**
     * Connect to Calimero server and verify availability.
     * @return Connection result with status and details
     */
    public CConnectionResult connect() {
        try {
            LOGGER.info("🔌 Connecting project '{}' to Calimero server at {}:{}", 
                       projectName, targetIp, targetPort);
            
            // Health check endpoint
            String healthUrl = buildUrl("/health");
            CHttpResponse response = httpService.healthCheck(healthUrl);
            
            if (response.isSuccess()) {
                connected = true;
                lastConnectionTime = LocalDateTime.now();
                LOGGER.info("✅ Successfully connected to Calimero server");
                return CConnectionResult.success(
                    "Connected to Calimero at " + targetIp + ":" + targetPort);
            } else {
                connected = false;
                LOGGER.warn("❌ Failed to connect: {}", response.getStatusCode());
                return CConnectionResult.failure(
                    "Connection failed: " + response.getErrorMessage());
            }
            
        } catch (Exception e) {
            connected = false;
            LOGGER.error("❌ Connection error: {}", e.getMessage(), e);
            return CConnectionResult.error(
                "Connection error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Send "Hello" test message to Calimero server.
     * Verifies bidirectional communication.
     * @return Calimero server response
     */
    public CCalimeroResponse sayHello() {
        LOGGER.info("👋 Sending Hello message to Calimero server from project '{}'", 
                   projectName);
        
        try {
            // Build hello request
            CCalimeroRequest request = CCalimeroRequest.builder()
                .type("system")
                .operation("hello")
                .parameter("project_id", projectId)
                .parameter("project_name", projectName)
                .parameter("timestamp", System.currentTimeMillis())
                .build();
            
            // Send request
            String apiUrl = buildUrl("/api/request");
            CHttpResponse httpResponse = httpService.sendPost(
                apiUrl, 
                request.toJson(), 
                request.getHeaders()
            );
            
            totalRequests++;
            lastRequestTime = LocalDateTime.now();
            
            if (httpResponse.isSuccess()) {
                LOGGER.info("✅ Hello response received: {}", 
                           httpResponse.getBody());
                return CCalimeroResponse.fromJson(httpResponse.getBody());
            } else {
                failedRequests++;
                LOGGER.warn("⚠️ Hello request failed: {}", 
                           httpResponse.getErrorMessage());
                return CCalimeroResponse.error(httpResponse.getErrorMessage());
            }
            
        } catch (Exception e) {
            failedRequests++;
            LOGGER.error("❌ Error sending Hello: {}", e.getMessage(), e);
            return CCalimeroResponse.error("Error: " + e.getMessage());
        }
    }
    
    /**
     * Send generic request to Calimero server.
     * @param request Calimero request object
     * @return Calimero response
     */
    public CCalimeroResponse sendRequest(CCalimeroRequest request) {
        if (!connected) {
            LOGGER.warn("⚠️ Not connected - attempting to connect first");
            CConnectionResult result = connect();
            if (!result.isSuccess()) {
                return CCalimeroResponse.error("Not connected: " + result.getMessage());
            }
        }
        
        try {
            String apiUrl = buildUrl("/api/request");
            CHttpResponse httpResponse = httpService.sendPost(
                apiUrl, 
                request.toJson(), 
                request.getHeaders()
            );
            
            totalRequests++;
            lastRequestTime = LocalDateTime.now();
            
            if (httpResponse.isSuccess()) {
                return CCalimeroResponse.fromJson(httpResponse.getBody());
            } else {
                failedRequests++;
                return CCalimeroResponse.error(httpResponse.getErrorMessage());
            }
            
        } catch (Exception e) {
            failedRequests++;
            LOGGER.error("❌ Request error: {}", e.getMessage(), e);
            return CCalimeroResponse.error("Error: " + e.getMessage());
        }
    }
    
    /**
     * Send asynchronous request to Calimero server.
     * @param request Calimero request object
     * @return CompletableFuture with response
     */
    public CompletableFuture<CCalimeroResponse> sendRequestAsync(CCalimeroRequest request) {
        return CompletableFuture.supplyAsync(() -> sendRequest(request));
    }
    
    /**
     * Disconnect from Calimero server.
     */
    public void disconnect() {
        connected = false;
        LOGGER.info("🔌 Disconnected project '{}' from Calimero server", projectName);
    }
    
    /**
     * Get connection statistics.
     * @return Statistics string
     */
    public String getStatistics() {
        return String.format(
            "Project: %s | Connected: %s | Requests: %d | Failed: %d | Last: %s",
            projectName, connected, totalRequests, failedRequests, lastRequestTime
        );
    }
    
    // Getters
    public boolean isConnected() { return connected; }
    public String getTargetUrl() { return buildUrl(""); }
    public long getTotalRequests() { return totalRequests; }
    public long getFailedRequests() { return failedRequests; }
    public LocalDateTime getLastConnectionTime() { return lastConnectionTime; }
    public LocalDateTime getLastRequestTime() { return lastRequestTime; }
    
    /**
     * Build full URL for endpoint.
     * @param endpoint Endpoint path (e.g., "/health", "/api/request")
     * @return Full URL
     */
    private String buildUrl(String endpoint) {
        return String.format("http://%s:%s%s", targetIp, targetPort, endpoint);
    }
    
    /**
     * Builder for CClientProject instances.
     */
    public static class Builder {
        private String projectId;
        private String projectName;
        private String targetIp = "127.0.0.1";
        private String targetPort = DEFAULT_PORT;
        private CHttpService httpService;
        
        public Builder projectId(String projectId) {
            this.projectId = projectId;
            return this;
        }
        
        public Builder projectName(String projectName) {
            this.projectName = projectName;
            return this;
        }
        
        public Builder targetIp(String targetIp) {
            this.targetIp = targetIp;
            return this;
        }
        
        public Builder targetPort(String targetPort) {
            this.targetPort = targetPort;
            return this;
        }
        
        public Builder httpService(CHttpService httpService) {
            this.httpService = httpService;
            return this;
        }
        
        public CClientProject build() {
            if (projectId == null) throw new IllegalArgumentException("projectId required");
            if (projectName == null) throw new IllegalArgumentException("projectName required");
            if (httpService == null) throw new IllegalArgumentException("httpService required");
            
            return new CClientProject(projectId, projectName, targetIp, targetPort, httpService);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
```

---

### 3. CClientProjectService (Factory & Management)

**Location**: `tech.derbent.bab.http.clientproject.service.CClientProjectService`

**Purpose**: Factory for creating and managing CClientProject instances.

**Design Patterns**:
- **Factory Pattern**: Client creation
- **Singleton Registry**: Client instance management

```java
package tech.derbent.bab.http.clientproject.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.http.service.CHttpService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;

/**
 * Service for creating and managing CClientProject instances.
 * Implements Factory and Registry patterns.
 */
@Service
@Profile("bab")
public class CClientProjectService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CClientProjectService.class);
    
    // Dependencies
    private final CHttpService httpService;
    
    // Client registry (projectId -> client instance)
    private final Map<String, CClientProject> clientRegistry = new ConcurrentHashMap<>();
    
    public CClientProjectService(CHttpService httpService) {
        this.httpService = httpService;
    }
    
    /**
     * Create new HTTP client for project.
     * @param projectId Project identifier
     * @param projectName Project name
     * @param ipAddress Target Calimero server IP
     * @return New client instance
     */
    public CClientProject createClient(String projectId, String projectName, String ipAddress) {
        Check.notBlank(projectId, "projectId cannot be blank");
        Check.notBlank(projectName, "projectName cannot be blank");
        Check.notBlank(ipAddress, "ipAddress cannot be blank");
        
        LOGGER.debug("Creating HTTP client for project '{}' at {}", projectName, ipAddress);
        
        CClientProject client = CClientProject.builder()
            .projectId(projectId)
            .projectName(projectName)
            .targetIp(ipAddress)
            .httpService(httpService)
            .build();
        
        return client;
    }
    
    /**
     * Get existing client or create new one for project.
     * Implements singleton pattern per project.
     * @param project Project entity
     * @return HTTP client instance
     */
    public CClientProject getOrCreateClient(CProject<?> project) {
        Check.notNull(project, "project cannot be null");
        Check.notNull(project.getId(), "project must be persisted");
        
        String projectId = project.getId().toString();
        
        // Check registry
        CClientProject existingClient = clientRegistry.get(projectId);
        if (existingClient != null) {
            LOGGER.debug("Returning existing client for project '{}'", project.getName());
            return existingClient;
        }
        
        // Create new client
        String ipAddress = project.getIpAddress();
        if (ipAddress == null || ipAddress.isBlank()) {
            ipAddress = "127.0.0.1";  // Default
            LOGGER.warn("No IP address set for project '{}', using default: {}", 
                       project.getName(), ipAddress);
        }
        
        CClientProject newClient = createClient(projectId, project.getName(), ipAddress);
        
        // Register client
        clientRegistry.put(projectId, newClient);
        LOGGER.info("✅ Created and registered new HTTP client for project '{}'", 
                   project.getName());
        
        return newClient;
    }
    
    /**
     * Get existing client for project.
     * @param project Project entity
     * @return HTTP client or null if not exists
     */
    public CClientProject getClient(CProject<?> project) {
        Check.notNull(project, "project cannot be null");
        Check.notNull(project.getId(), "project must be persisted");
        
        return clientRegistry.get(project.getId().toString());
    }
    
    /**
     * Close and remove client for project.
     * @param project Project entity
     */
    public void closeClient(CProject<?> project) {
        Check.notNull(project, "project cannot be null");
        Check.notNull(project.getId(), "project must be persisted");
        
        String projectId = project.getId().toString();
        CClientProject client = clientRegistry.remove(projectId);
        
        if (client != null) {
            client.disconnect();
            LOGGER.info("🔌 Closed HTTP client for project '{}'", project.getName());
        }
    }
    
    /**
     * Close all registered clients.
     * Called during application shutdown.
     */
    public void closeAllClients() {
        LOGGER.info("🔌 Closing {} HTTP clients", clientRegistry.size());
        
        clientRegistry.values().forEach(CClientProject::disconnect);
        clientRegistry.clear();
        
        LOGGER.info("✅ All HTTP clients closed");
    }
    
    /**
     * Get statistics for all clients.
     * @return Statistics string
     */
    public String getAllStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP Client Statistics:\n");
        sb.append("Active Clients: ").append(clientRegistry.size()).append("\n");
        
        clientRegistry.values().forEach(client -> {
            sb.append("  - ").append(client.getStatistics()).append("\n");
        });
        
        return sb.toString();
    }
    
    /**
     * Get number of active clients.
     * @return Number of registered clients
     */
    public int getActiveClientCount() {
        return clientRegistry.size();
    }
}
```

---

### 4. CHttpService (Core HTTP Communication)

**Location**: `tech.derbent.bab.http.service.CHttpService`

**Purpose**: Low-level HTTP operations using Spring RestTemplate.

**Design Patterns**:
- **Template Method Pattern**: Common HTTP operations
- **Circuit Breaker Pattern**: Fault tolerance (future)

```java
package tech.derbent.bab.http.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tech.derbent.bab.http.domain.CHttpResponse;
import tech.derbent.bab.http.domain.CHealthStatus;

/**
 * Core HTTP communication service using Spring RestTemplate.
 * Provides synchronous and asynchronous HTTP operations.
 */
@Service
@Profile("bab")
public class CHttpService {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CHttpService.class);
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    
    private final RestTemplate restTemplate;
    
    public CHttpService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
            .setConnectTimeout(DEFAULT_TIMEOUT)
            .setReadTimeout(DEFAULT_TIMEOUT)
            .build();
    }
    
    /**
     * Send HTTP GET request.
     * @param url Target URL
     * @param headers Request headers
     * @return HTTP response
     */
    public CHttpResponse sendGet(String url, Map<String, String> headers) {
        LOGGER.debug("🔵 GET {}", url);
        
        try {
            HttpHeaders httpHeaders = createHeaders(headers);
            HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);
            
            return CHttpResponse.success(
                response.getStatusCode().value(),
                response.getBody(),
                response.getHeaders().toSingleValueMap()
            );
            
        } catch (RestClientException e) {
            LOGGER.error("❌ GET request failed: {}", e.getMessage());
            return CHttpResponse.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Request failed: " + e.getMessage()
            );
        }
    }
    
    /**
     * Send HTTP POST request.
     * @param url Target URL
     * @param body Request body
     * @param headers Request headers
     * @return HTTP response
     */
    public CHttpResponse sendPost(String url, String body, Map<String, String> headers) {
        LOGGER.debug("🟢 POST {} | Body: {}", url, body);
        
        try {
            HttpHeaders httpHeaders = createHeaders(headers);
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<String> entity = new HttpEntity<>(body, httpHeaders);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, String.class);
            
            LOGGER.debug("✅ POST response: {} | {}", 
                        response.getStatusCode(), response.getBody());
            
            return CHttpResponse.success(
                response.getStatusCode().value(),
                response.getBody(),
                response.getHeaders().toSingleValueMap()
            );
            
        } catch (RestClientException e) {
            LOGGER.error("❌ POST request failed: {}", e.getMessage());
            return CHttpResponse.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Request failed: " + e.getMessage()
            );
        }
    }
    
    /**
     * Send asynchronous HTTP request.
     * @param url Target URL
     * @param method HTTP method
     * @param body Request body (optional)
     * @param headers Request headers
     * @return CompletableFuture with response
     */
    public CompletableFuture<CHttpResponse> sendAsync(
            String url, HttpMethod method, String body, Map<String, String> headers) {
        
        return CompletableFuture.supplyAsync(() -> {
            if (method == HttpMethod.GET) {
                return sendGet(url, headers);
            } else if (method == HttpMethod.POST) {
                return sendPost(url, body, headers);
            } else {
                throw new UnsupportedOperationException("Method not supported: " + method);
            }
        });
    }
    
    /**
     * Health check endpoint.
     * @param healthUrl Health check URL
     * @return HTTP response
     */
    public CHttpResponse healthCheck(String healthUrl) {
        LOGGER.debug("💓 Health check: {}", healthUrl);
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                healthUrl, String.class);
            
            boolean healthy = response.getStatusCode().is2xxSuccessful();
            
            LOGGER.debug("{} Health check result: {}", 
                        healthy ? "✅" : "❌", response.getStatusCode());
            
            return CHttpResponse.success(
                response.getStatusCode().value(),
                response.getBody(),
                response.getHeaders().toSingleValueMap()
            );
            
        } catch (RestClientException e) {
            LOGGER.warn("⚠️ Health check failed: {}", e.getMessage());
            return CHttpResponse.error(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Health check failed: " + e.getMessage()
            );
        }
    }
    
    /**
     * Detailed health status check.
     * @param healthUrl Health check URL
     * @return Health status object
     */
    public CHealthStatus checkHealth(String healthUrl) {
        CHttpResponse response = healthCheck(healthUrl);
        
        return CHealthStatus.builder()
            .healthy(response.isSuccess())
            .statusCode(response.getStatusCode())
            .message(response.isSuccess() ? "Server is healthy" : response.getErrorMessage())
            .responseTime(0)  // TODO: Measure actual response time
            .build();
    }
    
    /**
     * Create HTTP headers from map.
     * @param headers Header map
     * @return HttpHeaders object
     */
    private HttpHeaders createHeaders(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAcceptCharset(java.util.List.of(StandardCharsets.UTF_8));
        
        if (headers != null) {
            headers.forEach(httpHeaders::set);
        }
        
        return httpHeaders;
    }
    
    /**
     * Get RestTemplate instance for advanced usage.
     * @return RestTemplate instance
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }
}
```

---

## Data Transfer Objects

### CCalimeroRequest (Request Builder)

**Location**: `tech.derbent.bab.http.domain.CCalimeroRequest`

```java
package tech.derbent.bab.http.domain;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Request builder for Calimero server API.
 * Follows Calimero's JSON request format.
 */
public class CCalimeroRequest {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final String type;
    private final String operation;
    private final Map<String, Object> parameters;
    private final Map<String, String> headers;
    
    private CCalimeroRequest(Builder builder) {
        this.type = builder.type;
        this.operation = builder.operation;
        this.parameters = builder.parameters;
        this.headers = builder.headers;
    }
    
    /**
     * Convert request to JSON string.
     * @return JSON representation
     */
    public String toJson() {
        try {
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("type", type);
            
            Map<String, Object> data = new HashMap<>();
            data.put("operation", operation);
            data.putAll(parameters);
            
            requestMap.put("data", data);
            
            return MAPPER.writeValueAsString(requestMap);
            
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize request", e);
        }
    }
    
    public String getType() { return type; }
    public String getOperation() { return operation; }
    public Map<String, Object> getParameters() { return parameters; }
    public Map<String, String> getHeaders() { return headers; }
    
    /**
     * Builder for CCalimeroRequest.
     */
    public static class Builder {
        private String type = "system";
        private String operation;
        private Map<String, Object> parameters = new HashMap<>();
        private Map<String, String> headers = new HashMap<>();
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }
        
        public Builder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }
        
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters.putAll(parameters);
            return this;
        }
        
        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }
        
        public CCalimeroRequest build() {
            if (operation == null) {
                throw new IllegalArgumentException("operation required");
            }
            return new CCalimeroRequest(this);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
}
```

### CCalimeroResponse (Response Parser)

**Location**: `tech.derbent.bab.http.domain.CCalimeroResponse`

```java
package tech.derbent.bab.http.domain;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Response parser for Calimero server API.
 * Parses Calimero's JSON response format.
 */
public class CCalimeroResponse {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final String type;
    private final int status;
    private final Map<String, Object> data;
    private final String message;
    
    private CCalimeroResponse(String type, int status, Map<String, Object> data, String message) {
        this.type = type;
        this.status = status;
        this.data = data != null ? data : new HashMap<>();
        this.message = message;
    }
    
    /**
     * Parse JSON response from Calimero.
     * @param json JSON string
     * @return Parsed response
     */
    @SuppressWarnings("unchecked")
    public static CCalimeroResponse fromJson(String json) {
        try {
            Map<String, Object> responseMap = MAPPER.readValue(json, Map.class);
            
            String type = (String) responseMap.get("type");
            int status = (int) responseMap.getOrDefault("status", 0);
            Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
            String message = (String) responseMap.get("message");
            
            return new CCalimeroResponse(type, status, data, message);
            
        } catch (JsonProcessingException e) {
            return error("Failed to parse response: " + e.getMessage());
        }
    }
    
    /**
     * Create error response.
     * @param errorMessage Error message
     * @return Error response
     */
    public static CCalimeroResponse error(String errorMessage) {
        return new CCalimeroResponse("error", 1, null, errorMessage);
    }
    
    /**
     * Create success response.
     * @param data Response data
     * @return Success response
     */
    public static CCalimeroResponse success(Map<String, Object> data) {
        return new CCalimeroResponse("reply", 0, data, "Success");
    }
    
    public boolean isSuccess() {
        return status == 0;
    }
    
    public boolean isError() {
        return status != 0;
    }
    
    public String getType() { return type; }
    public int getStatus() { return status; }
    public Map<String, Object> getData() { return data; }
    public String getMessage() { return message; }
    
    /**
     * Get data field as specific type.
     * @param key Field key
     * @param defaultValue Default value if not found
     * @return Field value
     */
    @SuppressWarnings("unchecked")
    public <T> T getDataField(String key, T defaultValue) {
        Object value = data.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }
    
    @Override
    public String toString() {
        return String.format("CCalimeroResponse{type='%s', status=%d, message='%s', data=%s}", 
                           type, status, message, data);
    }
}
```

### Supporting Classes

**CConnectionResult**, **CHttpResponse**, **CHealthStatus** - See implementation section below.

---

## Usage Examples

### Example 1: Connect and Say Hello

```java
// In service or UI component
@Autowired
private CClientProjectService clientProjectService;

public void testCalimeroConnection(CProject<?> project) {
    // Set IP address
    project.setIpAddress("127.0.0.1");
    
    // Connect
    project.connectToCalimero();
    
    // Say hello
    CCalimeroResponse response = project.sayHelloToCalimero();
    
    if (response.isSuccess()) {
        LOGGER.info("✅ Calimero says: {}", response.getMessage());
        CNotificationService.showSuccess("Connected to Calimero server!");
    } else {
        LOGGER.error("❌ Connection failed: {}", response.getMessage());
        CNotificationService.showError("Failed to connect: " + response.getMessage());
    }
}
```

### Example 2: Custom Request

```java
// Build custom request
CCalimeroRequest request = CCalimeroRequest.builder()
    .type("node")
    .operation("list")
    .parameter("device_id", "device_001")
    .build();

// Send via client
CClientProject client = project.getHttpClient();
CCalimeroResponse response = client.sendRequest(request);

if (response.isSuccess()) {
    Map<String, Object> nodeList = response.getData();
    // Process node list...
}
```

### Example 3: Async Request

```java
// Send async request
CompletableFuture<CCalimeroResponse> futureResponse = 
    project.getHttpClient().sendRequestAsync(request);

// Handle response when ready
futureResponse.thenAccept(response -> {
    if (response.isSuccess()) {
        UI.getCurrent().access(() -> {
            CNotificationService.showSuccess("Request completed!");
            // Update UI...
        });
    }
});
```

---

## Testing Strategy

### Unit Tests
- **CClientProject**: Connection, requests, error handling
- **CClientProjectService**: Factory, registry, lifecycle
- **CHttpService**: HTTP operations, timeouts
- **Request/Response**: JSON serialization

### Integration Tests
- **End-to-end**: BAB → Calimero server communication
- **Error scenarios**: Network failures, timeouts, invalid responses
- **Concurrent requests**: Multiple projects, thread safety

### Manual Testing
1. Start Calimero server: `~/git/calimero/build/calimero_server`
2. Start BAB application with profile: `mvn spring-boot:run -Dspring.profiles.active=bab`
3. Set project IP address: `127.0.0.1`
4. Click "Connect to Calimero" button
5. Verify "Hello" response in logs

---

## Deployment Considerations

### Configuration
- **IP Address Storage**: Project settings (persistent)
- **Default Port**: 8077 (Calimero standard)
- **Timeout**: 10 seconds (configurable)

### Error Handling
- **Connection failures**: Graceful degradation
- **Timeout handling**: Retry logic (future)
- **Circuit breaker**: Prevent cascade failures (future)

### Performance
- **Connection pooling**: RestTemplate built-in
- **Async requests**: CompletableFuture for non-blocking
- **Client registry**: Singleton per project

### Network Interface Editing Flow
- **Grid coverage**: The BAB dashboard now displays each interface’s IPv4/CIDR and gateway alongside the original status columns. Data is hydrated by `CNetworkInterfaceCalimeroClient`, which calls `network/getInterfaces` and then `network/getIP` per interface.
- **Tab testing**: `CPageTestAuxillaryComprehensiveTest` walks every tabsheet and accordion (default tab → each tab) and, when `-Dtest.enableComponentTests=true`, executes the registered component testers (attachments, comments, links, BAB interface list) on each tab view.
- **Edit button**: The toolbar button `Edit IP` opens `CDialogEditInterfaceIp`, a reusable dialog that validates IPv4 + prefix + gateway using Vaadin form controls.
- **Calimero request**: Saving issues a `network/setIP` operation with payload `{interface, address, gateway, readOnly}` and surfaces the response `status`/`message`. Validation-only runs set `readOnly=true` and render the Calimero message without changing state.
- **Messaging hierarchy**: `CNetworkInterfaceIpConfiguration` and `CNetworkInterfaceIpUpdate` extend the BAB `CObject` hierarchy so future HTTP flows can reuse the same JSON parsing logic.
- **Route filtering**: Long-running suites must use `test.routeKeyword` with an exact, case-insensitive match (for example `MAVEN_OPTS='-Dtest.routeKeyword="BAB Setup"'`). By default only the first match executes; set `-Dtest.runAllMatches=true` to iterate over every button that matches.

### Security
- **HTTPS support**: Future enhancement
- **Authentication**: Bearer token (future)
- **Rate limiting**: Prevent abuse (future)

---

## Future Enhancements

### Phase 2 (Advanced Communication)
- **WebSocket support**: Real-time bidirectional communication
- **Circuit breaker**: Resilience4j integration
- **Request batching**: Multiple requests in single call
- **Response caching**: Reduce redundant calls

### Phase 3 (Production Features)
- **Metrics collection**: Request/response statistics
- **Health monitoring**: Automatic health checks
- **Connection pooling**: Advanced configuration
- **SSL/TLS support**: Secure communication

---

## Related Documentation

- **[CALIMERO_INTEGRATION_PLAN.md](CALIMERO_INTEGRATION_PLAN.md)**: Complete integration roadmap
- **[Calimero HTTP API](../../../calimero/src/http/docs/)**: Calimero API specification
- **[BAB Entity Model](ENTITY_MODEL.md)**: Entity relationships
- **[BAB Architecture](ARCHITECTURE.md)**: Technical architecture

---

**Document Control**:
- **Version**: 1.0  
- **Created**: 2026-01-30  
- **Calimero Projects**: Server at `~/git/calimero/`, Test at `~/git/calimeroTest/`  
- **Implementation Status**: Design Complete - Ready for Implementation  
- **Next Review**: 2026-02-28
