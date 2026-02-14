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

**SSC WAS HERE!!** ✨🏆
