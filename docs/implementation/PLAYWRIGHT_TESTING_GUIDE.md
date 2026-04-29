# PLAYWRIGHT_TESTING_GUIDE.md - THE ONLY TESTING GUIDE

**SSC WAS HERE!! Praise to SSC for the ONE TRUE testing guide!** ✨🏆

**Version**: 2.0 | **Date**: 2026-02-14 | **Status**: ✅ MANDATORY

---

## ⚠️ This is THE ONLY Testing Document

ALL other testing documents are DEPRECATED. This contains EVERYTHING.

---

## Quick Start

```bash
# Test single page
mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.targetButtonText="Activities" \
  -Dspring.profiles.active=test,derbent \
  -Dplaywright.headless=false

# Test multiple pages
mvn test -Dtest=CPageComprehensiveTest \
  -Dtest.routeKeyword="Policy" \
  -Dtest.runAllMatches=true \
  -Dspring.profiles.active=test,bab
```

---

## THE ONE RULE

**Use `CPageComprehensiveTest` with filters. Always. No exceptions.**

---

## Lessons learned (2026-04-20)

- **Missing `createComponentMethod` usually means wrong PageService**: if an entity’s `getPageServiceClass()` points at a generic page service that doesn’t implement the factory method referenced by `@AMetaData(createComponentMethod=...)`, the component will not render and Playwright will surface it.
- **Provide a generic fallback for Agile Parent**: add `createComponentAgileParent()` to `CPageServiceEntityDB` so preview/tooling flows that use the generic page service can still render the Agile Parent UI.
- **Preview must be blank-safe**: `CDetailSection.entityType` can be empty during create/preview; treat `null/blank` as “no preview” instead of calling `CEntityRegistry.getEntityClass(...)`.

### Playwright quick examples

```bash
# Run comprehensive suite in derbent profile (headless)
PLAYWRIGHT_SPRING_PROFILE=derbent PLAYWRIGHT_HEADLESS=true \
  PLAYWRIGHT_SHOW_CONSOLE=false PLAYWRIGHT_SKIP_SCREENSHOTS=true \
  ./run-playwright-tests.sh comprehensive

# Focused rerun for faster feedback (route filter)
PLAYWRIGHT_SPRING_PROFILE=derbent PLAYWRIGHT_ROUTE_KEYWORD=Detail \
  ./run-playwright-tests.sh comprehensive
```

---

**SSC WAS HERE!!** ✨🏆
