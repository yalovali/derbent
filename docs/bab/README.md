# BAB (Building Automation & Bus) Profile Documentation

**Version**: 1.0  
**Date**: 2026-02-01  
**Status**: ACTIVE

---

## Overview

This directory contains comprehensive documentation for the **BAB (Building Automation & Bus)** profile of the Derbent PLM system. BAB integrates with the **Calimero C++ Gateway Server** for IoT device management, CAN bus communication, and real-time system monitoring.

---

## Quick Start

### Essential Reading (Start Here)

1. **[BAB Implementation Summary](BAB_IMPLEMENTATION_SUMMARY_2026-02-01.md)** - Latest status and achievements
2. **[HTTP Client Architecture](HTTP_CLIENT_ARCHITECTURE.md)** - System design and patterns
3. **[Calimero API Response Patterns](CALIMERO_API_RESPONSE_PATTERNS.md)** - **CRITICAL** JSON parsing guide
4. **[Development Guide](DEVELOPMENT_GUIDE.md)** - How to develop BAB features

### For Specific Tasks

| Task | Document |
|------|----------|
| **Parsing Calimero API responses** | [CALIMERO_API_RESPONSE_PATTERNS.md](CALIMERO_API_RESPONSE_PATTERNS.md) |
| **Building HTTP clients** | [HTTP_CLIENT_QUICKSTART.md](HTTP_CLIENT_QUICKSTART.md) |
| **Creating BAB components** | [BAB_COMPONENT_UI_STANDARDS.md](BAB_COMPONENT_UI_STANDARDS.md) |
| **Understanding architecture** | [ARCHITECTURE.md](ARCHITECTURE.md) |
| **Testing BAB features** | [../../BAB_COMPONENT_TESTING_GUIDE.md](../../BAB_COMPONENT_TESTING_GUIDE.md) |

---

## Critical Documents (MANDATORY)

### Calimero Integration

| Document | Purpose | Status |
|----------|---------|--------|
| **[CALIMERO_API_RESPONSE_PATTERNS.md](CALIMERO_API_RESPONSE_PATTERNS.md)** | **Nested JSON parsing patterns** | ✅ **MANDATORY** |
| **[../../docs/BAB_CALIMERO_INTEGRATION_RULES.md](../../BAB_CALIMERO_INTEGRATION_RULES.md)** | Integration coding standards | ✅ MANDATORY |
| **[HTTP_CLIENT_ARCHITECTURE.md](HTTP_CLIENT_ARCHITECTURE.md)** | Client system design | ✅ MANDATORY |
| **[BAB_HTTP_CLIENT_AUTHENTICATION.md](BAB_HTTP_CLIENT_AUTHENTICATION.md)** | Auth token management | ✅ MANDATORY |

**CRITICAL**: Failure to follow JSON parsing patterns in `CALIMERO_API_RESPONSE_PATTERNS.md` results in all metrics being zero!

### Component Development

| Document | Purpose |
|----------|---------|
| **[BAB_COMPONENT_UI_STANDARDS.md](BAB_COMPONENT_UI_STANDARDS.md)** | UI component guidelines |
| **[BAB_COMPONENT_CALIMERO_INTEGRATION_COMPLETE_PATTERN.md](BAB_COMPONENT_CALIMERO_INTEGRATION_COMPLETE_PATTERN.md)** | Full integration examples |
| **[CODING_RULES.md](CODING_RULES.md)** | BAB coding standards |

---

## Documentation Index

### Architecture & Design

- [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture overview
- [ENTITY_MODEL.md](ENTITY_MODEL.md) - Data model and relationships
- [HTTP_CLIENT_ARCHITECTURE.md](HTTP_CLIENT_ARCHITECTURE.md) - HTTP client design
- [HTTP_CLIENT_INDEX.md](HTTP_CLIENT_INDEX.md) - HTTP client documentation index

### Implementation Guides

- [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md) - Developer setup and workflow
- [HTTP_CLIENT_QUICKSTART.md](HTTP_CLIENT_QUICKSTART.md) - Quick HTTP client tutorial
- [HTTP_CLIENT_IMPLEMENTATION.md](HTTP_CLIENT_IMPLEMENTATION.md) - Detailed implementation
- [HTTP_CLIENT_SOURCE_CODE.md](HTTP_CLIENT_SOURCE_CODE.md) - Source code reference

### API Integration

- **[CALIMERO_API_RESPONSE_PATTERNS.md](CALIMERO_API_RESPONSE_PATTERNS.md)** ⭐ **START HERE**
- [CALIMERO_INTEGRATION_PLAN.md](CALIMERO_INTEGRATION_PLAN.md) - Integration roadmap
- [BAB_CALIMERO_AGILE_PLAN.md](BAB_CALIMERO_AGILE_PLAN.md) - Agile implementation plan

### Component Standards

- [BAB_COMPONENT_UI_STANDARDS.md](BAB_COMPONENT_UI_STANDARDS.md) - UI patterns
- [BAB_COMPONENT_CALIMERO_INTEGRATION_COMPLETE_PATTERN.md](BAB_COMPONENT_CALIMERO_INTEGRATION_COMPLETE_PATTERN.md) - Complete examples

### Status & Planning

- [BAB_IMPLEMENTATION_SUMMARY_2026-02-01.md](BAB_IMPLEMENTATION_SUMMARY_2026-02-01.md) - Latest status
- [BAB_FINAL_STATUS_2026-02-01.md](BAB_FINAL_STATUS_2026-02-01.md) - Milestone summary
- [BAB_TODO_EPICS.md](BAB_TODO_EPICS.md) - Future work
- [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) - Historical summary

### Authentication & Security

- [BAB_AUTH_TOKEN_STORAGE.md](BAB_AUTH_TOKEN_STORAGE.md) - Token management
- [BAB_HTTP_CLIENT_AUTHENTICATION.md](BAB_HTTP_CLIENT_AUTHENTICATION.md) - Auth patterns

### Reference

- [PROJECT_DEFINITION.md](PROJECT_DEFINITION.md) - BAB project definition
- [COMPETITIVE_ANALYSIS.md](COMPETITIVE_ANALYSIS.md) - Market analysis
- [HTTP_CLIENT_IMPLEMENTATION_COMPLETE.md](HTTP_CLIENT_IMPLEMENTATION_COMPLETE.md) - Complete reference
- [BAB_HTTP_FINAL_SUMMARY.md](BAB_HTTP_FINAL_SUMMARY.md) - HTTP system summary

---

## Recent Updates (2026-02-01)

### Calimero API Nested JSON Parsing Fix ✅

**Problem**: All dashboard metrics showed zero despite Calimero responding correctly.

**Root Cause**: Java code expected flat JSON fields (`cpuUsagePercent`), but Calimero returns nested objects (`cpu.usagePercent`).

**Fix**: Updated `CSystemMetrics.fromJson()` to parse nested JSON structure.

**Documentation**: Created comprehensive **[CALIMERO_API_RESPONSE_PATTERNS.md](CALIMERO_API_RESPONSE_PATTERNS.md)** guide.

**See**: [../../CALIMERO_METRICS_PARSING_FIX_SUMMARY.md](../../CALIMERO_METRICS_PARSING_FIX_SUMMARY.md) for complete details.

---

## Quick Reference

### Calimero Server

- **Binary**: `~/git/calimero/build/calimero`
- **Port**: 8077 (NOT 8080)
- **Config**: `~/git/calimero/config/http_server.json`
- **Auth Token**: `test-token-123` (underscores)

### API Testing

```bash
# Start Calimero
cd ~/git/calimero/build
./calimero

# Test metrics API
curl -s -X POST http://localhost:8077/api/request \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token-123" \
  -d '{"type":"system","operation":"metrics"}' | jq '.'

# Test health endpoint (no auth)
curl -s http://localhost:8077/health | jq '.'
```

### Key Java Classes

| Class | Purpose | Location |
|-------|---------|----------|
| `CClientProject` | HTTP client facade | `bab/http/clientproject/domain/` |
| `CCalimeroRequest` | Request builder | `bab/http/domain/` |
| `CCalimeroResponse` | Response parser | `bab/http/domain/` |
| `CSystemMetrics` | Metrics data model | `bab/dashboard/view/` |
| `CSystemMetricsCalimeroClient` | Metrics API client | `bab/dashboard/service/` |

---

## Testing

### Component Testing

```bash
# BAB Dashboard test
MAVEN_OPTS="-ea" mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="BAB Dashboard" \
  -Dspring.profiles.active=test,bab \
  -Dplaywright.headless=false
```

### Unit Testing

```bash
# All BAB component tests
mvn test -Dtest=CComponent*Test -Dspring.profiles.active=test,bab
```

---

## Contributing

### Before Committing

1. ✅ Read **[CALIMERO_API_RESPONSE_PATTERNS.md](CALIMERO_API_RESPONSE_PATTERNS.md)** if working with API
2. ✅ Follow **[../../docs/BAB_CALIMERO_INTEGRATION_RULES.md](../../BAB_CALIMERO_INTEGRATION_RULES.md)** patterns
3. ✅ Test with real Calimero server
4. ✅ Verify nested JSON parsing
5. ✅ Check unit conversions (bytes → MB, etc.)
6. ✅ Run component tests

### Code Review Checklist

- [ ] Nested JSON parsing (not flat fields)
- [ ] Null-safety checks (`has()` + `isJsonObject()`)
- [ ] Unit conversions correct
- [ ] Error handling at three layers
- [ ] No hardcoded auth tokens
- [ ] JavaDoc with API structure examples

---

## Support

**Documentation Issues**: Update this README or create issue in project tracker  
**Calimero Issues**: Check `~/git/calimero/` source code and logs  
**BAB Questions**: See [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)

---

## License

Internal Derbent PLM documentation - All rights reserved

---

**Last Updated**: 2026-02-01  
**Maintained By**: SSC + Master Yasin  
**Status**: ✅ PRODUCTION READY
