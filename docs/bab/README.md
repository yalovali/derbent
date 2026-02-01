# BAB Gateway Documentation

This directory contains comprehensive documentation for the BAB (Building Automation Bus) Gateway implementation in Derbent.

## Quick Links

### 🚀 Getting Started
- **[BAB_QUICK_START_CALIMERO_FIX.md](../../BAB_QUICK_START_CALIMERO_FIX.md)** - 15-minute guide to get Calimero working
- **[BAB_FINAL_STATUS_2026-02-01.md](BAB_FINAL_STATUS_2026-02-01.md)** - Current implementation status

### 📚 Complete Patterns
- **[BAB_COMPONENT_CALIMERO_INTEGRATION_COMPLETE_PATTERN.md](BAB_COMPONENT_CALIMERO_INTEGRATION_COMPLETE_PATTERN.md)** - MANDATORY patterns for ALL BAB components (52KB)
- **[BAB_IMPLEMENTATION_SUMMARY_2026-02-01.md](BAB_IMPLEMENTATION_SUMMARY_2026-02-01.md)** - Implementation summary and metrics

### 🎯 Key Rules

**ALL BAB components MUST**:
1. ✅ Extend `CComponentBabBase`
2. ✅ Use dedicated `C*CalimeroClient` helpers
3. ✅ Have component IDs for Playwright testing
4. ✅ Implement three-layer error handling
5. ✅ Show real data from Calimero server

## Architecture

```
CComponent*              (UI - Vaadin components)
    ↓
C*CalimeroClient         (API Client - Request/Response)
    ↓
CClientProject           (HTTP Client - Auth + Connection)
    ↓
CHttpService             (Low-level HTTP - Java 11 HttpClient)
    ↓
Calimero Server          (C++ HTTP Server - Real system data)
```

## Current Status (2026-02-01)

✅ **Calimero Server**: Operational on port 8077  
✅ **Authentication**: Working with token `test-token-123`  
✅ **Components**: 9/9 components compliant with pattern  
✅ **Client Helpers**: 7/7 Calimero clients implemented  
✅ **Real Data**: System metrics, network interfaces, processes, etc.  
✅ **Tests**: Playwright tests passing  
✅ **Documentation**: 77KB comprehensive guides  

## Component Inventory

| Component | Client Helper | Real Data | Tests |
|-----------|---------------|-----------|-------|
| CComponentSystemMetrics | CSystemMetricsCalimeroClient | ✅ CPU/Mem/Disk | ✅ |
| CComponentInterfaceList | CNetworkInterfaceCalimeroClient | ✅ Interfaces | ✅ |
| CComponentCpuUsage | CCpuInfoCalimeroClient | ✅ CPU details | ⚠️ |
| CComponentDiskUsage | CDiskUsageCalimeroClient | ✅ Filesystems | ⚠️ |
| CComponentDnsConfiguration | CNetworkRoutingCalimeroClient | ✅ DNS servers | ⚠️ |
| CComponentSystemProcessList | CSystemProcessCalimeroClient | ✅ Processes | ⚠️ |
| CComponentSystemServices | CSystemServiceCalimeroClient | ✅ Services | ⚠️ |
| CComponentRoutingTable | CNetworkRoutingCalimeroClient | ✅ Routes | ⚠️ |
| CComponentNetworkRouting | CNetworkRoutingCalimeroClient | ✅ Routes+DNS | ⚠️ |

**Legend**: ✅ Complete, ⚠️ Needs enhanced testing

## Testing

### Run Playwright Tests
```bash
cd /home/yasin/git/derbent
./run-playwright-tests.sh bab
```

### Test Calimero API
```bash
curl -X POST http://localhost:8077/api/request \
  -H "Authorization: Bearer test-token-123" \
  -H "Content-Type: application/json" \
  -d '{"type":"system","data":{"operation":"metrics"}}' | python3 -m json.tool
```

## Development

### Create New BAB Component

1. **Extend CComponentBabBase**
```java
public class CComponentMyWidget extends CComponentBabBase {
    public static final String ID_ROOT = "custom-my-widget-component";
    // ... implementation
}
```

2. **Create Calimero Client Helper**
```java
public class CMyDomainCalimeroClient {
    public Optional<CMyData> fetchData() {
        // ... implementation
    }
}
```

3. **Follow Three-Layer Error Handling**
- Client: Return `Optional`, never throw
- Component: Handle empty, show empty state
- Notification: User-friendly messages

4. **Add Playwright Tests**
```java
public static void testMyComponent(Page page) {
    // ... test implementation
}
```

## Troubleshooting

### Calimero Returns 401 Unauthorized
```bash
# Check config and restart
cat ~/git/calimero/build/config/http_server.json | grep authToken
cd ~/git/calimero/build && ./calimero > /tmp/calimero_server.log 2>&1 &
```

### Components Show N/A
```bash
# Verify Calimero running
ps aux | grep calimero | grep -v grep
curl -s http://localhost:8077/health
```

## Reference

- **Calimero Source**: `~/git/calimero/src/http/`
- **BAB Components**: `src/main/java/tech/derbent/bab/dashboard/view/`
- **Calimero Clients**: `src/main/java/tech/derbent/bab/dashboard/service/`
- **Playwright Tests**: `src/test/java/tech/derbent/tests/bab/`

---

**Maintained by**: SSC + Master Yasin  
**Last Updated**: 2026-02-01  
**Status**: ✅ OPERATIONAL - All systems go!
