# Calimero Services API - Java Client Documentation

**Created**: 2026-02-03  
**Status**: Ready for implementation  
**Backend**: ✅ Complete (Calimero C++)  
**Client**: 📋 Specification ready

---

## Overview

This directory contains complete documentation for implementing a Java HTTP client to manage systemd services on remote Linux systems via the Calimero HTTP API.

The backend C++ implementation is complete and tested. The Java client implementation follows the established patterns used by other Calimero clients (System, Network, etc.).

---

## Documentation Files

### 1. CALIMERO_SERVICES_API.md (Primary Reference)

**Contents**:
- Complete API specification for all 8 operations
- Request/response formats with examples
- Java Data Transfer Objects (DTOs) with Jackson annotations
- Complete `CalimeroServicesClient` implementation
- Error handling patterns
- Security considerations
- Manual testing commands (curl)

**Operations Documented**:
1. `list` - List all services with filters
2. `status` - Get detailed service status
3. `start` - Start a service
4. `stop` - Stop a service
5. `restart` - Restart a service
6. `reload` - Reload service configuration
7. `enable` - Enable service at boot
8. `disable` - Disable service at boot

**Use this for**: Complete API reference, DTO definitions, client patterns

---

### 2. CALIMERO_SERVICES_QUICK_GUIDE.md (Implementation Guide)

**Contents**:
- Quick start checklist with time estimates
- Phase-by-phase implementation plan (4-5 hours total)
- Complete code templates for:
  - `ServicesView.java` (Vaadin Grid UI)
  - `ServiceDetailsDialog.java` (Details popup)
  - Grid configuration with filters
  - Action buttons (start/stop/restart)
  - Auto-refresh logic (5-second polling)
- Testing checklist
- Integration steps with Spring/CDI

**Use this for**: Step-by-step implementation, code templates, UI patterns

---

## Implementation Timeline

| Phase | Task | Duration | Files |
|-------|------|----------|-------|
| 1 | Create DTOs | 30 min | ServiceInfo, ServiceStatus, ServiceActionResult, ServiceListRequest |
| 2 | Implement Client | 45 min | CalimeroServicesClient extends CalimeroClientBase |
| 3 | Write Tests | 30 min | Unit tests with mocked responses |
| 4 | Build UI | 2-3 hours | ServicesView, ServiceDetailsDialog |
| **Total** | | **4-5 hours** | |

---

## Java Client Architecture

```
User Interface (Vaadin)
         ↓
   ServicesView.java
         ↓
CalimeroServicesClient.java (extends CalimeroClientBase)
         ↓
   HTTP POST /api/request
   {"type": "systemservices", "data": {...}}
         ↓
   Calimero HTTP Server (C++)
         ↓
   CSystemServicesRequestHandler
         ↓
   CSystemServicesProcessor
         ↓
   systemctl commands
```

---

## Quick Start

### 1. Read the Documentation

1. Start with `CALIMERO_SERVICES_API.md` to understand the API
2. Review `CALIMERO_SERVICES_QUICK_GUIDE.md` for implementation steps
3. Look at existing clients (CalimeroSystemClient) for patterns

### 2. Implement DTOs (Phase 1)

Create in `src/main/java/io/github/cenk1cenk2/bab/dto/calimero/`:
- `ServiceInfo.java`
- `ServiceStatus.java`
- `ServiceActionResult.java`
- `ServiceListRequest.java`

Copy from CALIMERO_SERVICES_API.md, add getters/setters.

### 3. Implement Client (Phase 2)

Create `CalimeroServicesClient.java` in `src/main/java/io/github/cenk1cenk2/bab/client/calimero/`:
- Extend `CalimeroClientBase`
- Message type: `"systemservices"`
- Implement all 8 operations

Register in `CalimeroClientFactory`:
```java
public CalimeroServicesClient createServicesClient() {
    return new CalimeroServicesClient(baseUrl, authToken);
}
```

### 4. Test (Phase 3)

Create `CalimeroServicesClientTest.java`:
- Mock HTTP responses
- Test all operations
- Test error handling

Manual testing with curl:
```bash
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"systemservices","data":{"operation":"list"}}'
```

### 5. Build UI (Phase 4)

Create Vaadin views:
- `ServicesView.java` - Main grid with services list
- `ServiceDetailsDialog.java` - Details popup

Features:
- Grid with status badges (green=running, red=failed, gray=stopped)
- Filters: active only, running only, search box
- Action buttons: start/stop/restart
- Auto-refresh every 5 seconds
- Details dialog with logs and boot configuration

---

## Code Templates

Complete code templates are provided in `CALIMERO_SERVICES_QUICK_GUIDE.md`:

- **ServicesView.java** (200+ lines) - Full Vaadin Grid implementation
- **ServiceDetailsDialog.java** (100+ lines) - Details popup with logs
- **Grid setup** - Columns, filters, action buttons
- **Auto-refresh** - Polling logic
- **Error handling** - User-friendly notifications

---

## Testing the Backend

The C++ backend is complete and ready to test. When Calimero is running:

```bash
# Test list operation
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"systemservices","data":{"operation":"list","runningOnly":true}}'

# Test status operation
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"systemservices","data":{"operation":"status","serviceName":"sshd.service"}}'

# Test start operation
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"systemservices","data":{"operation":"start","serviceName":"apache2.service"}}'
```

---

## Integration Checklist

- [ ] DTOs created with Jackson annotations
- [ ] CalimeroServicesClient implemented
- [ ] Client registered in CalimeroClientFactory
- [ ] Unit tests written and passing
- [ ] Manual testing with curl successful
- [ ] ServicesView created with Grid
- [ ] ServiceDetailsDialog created
- [ ] Navigation menu item added
- [ ] Auto-refresh implemented
- [ ] Status badges styled correctly
- [ ] Action buttons working (start/stop/restart)
- [ ] Filters working (active/running/search)
- [ ] Error handling displays notifications
- [ ] Security/roles configured (if needed)
- [ ] End-to-end testing complete

---

## Support & References

**Backend Implementation**:
- Processor: `/home/yasin/git/calimero/src/http/webservice/processors/cservicesprocessor.cpp`
- Handler: `/home/yasin/git/calimero/src/http/webservice/handlers/cservicesrequesthandler.cpp`
- Registration: `/home/yasin/git/calimero/src/http/webservice/base/cwebservice.cpp`

**Existing Patterns**:
- Look at `CalimeroSystemClient.java` for similar implementation
- Look at `SystemMetricsView.java` for similar UI patterns

**Documentation**:
- `CALIMERO_SERVICES_API.md` - Complete API reference
- `CALIMERO_SERVICES_QUICK_GUIDE.md` - Implementation guide

---

## Notes

- Backend uses systemctl commands under the hood
- Operations require appropriate system permissions
- Some operations (start/stop/restart) may need sudo/root
- Authentication via Bearer token (existing mechanism)
- Auto-refresh recommended for live status monitoring
- Filter and search improve UX for systems with many services

---

**Ready to implement!** 🚀

Start with `CALIMERO_SERVICES_API.md` for complete details.
