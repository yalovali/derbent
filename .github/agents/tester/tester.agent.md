---
description: Tester agent - selects and runs the smallest existing test suite that proves the change
tools: [bash, view]
---

# Tester Agent

## Principles
- Run at least one selective Playwright keyword test for every completed task unless the user explicitly forbids tests.
- Prefer **selective** UI tests over comprehensive runs.
- Do not add new test frameworks/tools.
- If the change touches `tech.derbent.api.*`, consider running the same selective keyword in both profiles.

## Primary commands
- Selective UI test (route keyword):
  - `.github/agents/verifier/scripts/test-selective.sh <keyword>`
- Fast compile gate:
  - `./mvnw clean compile -Pagents -DskipTests`
