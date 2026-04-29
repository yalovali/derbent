# Validation Entity Renaming Summary (Completed)

## Overview

The former Test Management entities have been renamed to Validation entities to avoid confusion with automated tests (unit/integration/Playwright) and to make ownership of manual validation data explicit.

**Policy**: Use **Validation** for business entities, menus, routes, tables, and packages. Reserve **Test** terminology for automated test execution and testing methodology documentation.

## Final Naming (Old → New)

- Test Case → Validation Case (`CValidationCase`)
- Test Case Type → Validation Case Type (`CValidationCaseType`)
- Test Scenario / Test Suite → Validation Suite (`CValidationSuite`)
- Test Step → Validation Step (`CValidationStep`)
- Test Run / Test Session → Validation Session (`CValidationSession`)
- Test Execution → Validation Execution (`CValidationExecution`)
- Test Case Result → Validation Case Result (`CValidationCaseResult`)
- Test Step Result → Validation Step Result (`CValidationStepResult`)

## Package Structure

- `src/main/java/tech/derbent/app/validation/validationcase`
- `src/main/java/tech/derbent/app/validation/validationcasetype`
- `src/main/java/tech/derbent/app/validation/validationsuite`
- `src/main/java/tech/derbent/app/validation/validationstep`
- `src/main/java/tech/derbent/app/validation/validationsession`
- `src/main/java/tech/derbent/app/validation/validationsession/validationexecution`

## Database Schema

### Tables

- `cvalidationcase`
- `cvalidationcasetype`
- `cvalidationsuite`
- `cvalidationstep`
- `cvalidationsession`
- `cvalidationexecution`
- `cvalidationcaseresult`
- `cvalidationstepresult`

### Primary Keys and Foreign Keys

- `validationcase_id`, `validationcasetype_id`, `validationsuite_id`, `validationstep_id`, `validationsession_id`, `validationexecution_id`
- FK fields follow the same naming pattern (for example: `validationsuite_id`, `validationsession_id`).

## Documentation Enforcement

- Use Validation naming in module docs, view-layer patterns, and menu examples.
- Automated test guides continue to use Test terminology.
- Mapping to ISO/IEC/IEEE 29119 and ISTQB is captured in `docs/testing/TESTING_TERMINOLOGY_MAPPING.md`.
