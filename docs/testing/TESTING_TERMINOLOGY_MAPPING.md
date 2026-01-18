# Testing Terminology Mapping - Validation Module

## Overview

Derbent uses **Validation** terminology for the business module (entities, menus, tables) to avoid confusion with automated tests. This document maps those entities to ISO/IEC/IEEE 29119 and ISTQB standard testing terms.

**Policy**: Validation naming is mandatory in the application and database; Test terminology is reserved for automated tests and standards references.

## Entity Mapping to Standards

| Derbent Entity | Standard Term | ISO 29119 | ISTQB | Notes |
|----------------|---------------|-----------|-------|-------|
| **CValidationCase** | Test Case | ✅ Part 3: Test Documentation | ✅ Glossary 3.x | Standard term for a defined test condition |
| **CValidationSuite** | Test Suite / Test Scenario | ✅ Part 3: Test Documentation | ✅ Glossary 3.x | Group of related test cases |
| **CValidationSession** | Test Session / Test Execution | ✅ Part 4: Test Techniques | ✅ Glossary 3.x | Time-boxed execution period |
| **CValidationStep** | Test Step / Test Procedure Step | ✅ Part 3: Test Specification | ✅ Glossary 3.x | Atomic action within a case |
| **CValidationCaseType** | Test Case Classification | ✅ Part 2: Test Processes | ⚠️ Vendor-specific | Categorization of cases |
| **CValidationCaseResult** | Test Execution Record | ✅ Part 3: Test Reporting | ✅ Glossary 3.x | Case-level result log |
| **CValidationStepResult** | Test Step Result | ✅ Part 3: Test Reporting | ✅ Glossary 3.x | Step-level result log |

## Database Naming

Current tables and key columns use Validation naming:

- Tables: `cvalidationcase`, `cvalidationcasetype`, `cvalidationsuite`, `cvalidationstep`, `cvalidationsession`, `cvalidationexecution`, `cvalidationcaseresult`, `cvalidationstepresult`
- PK/FK columns: `validationcase_id`, `validationcasetype_id`, `validationsuite_id`, `validationstep_id`, `validationsession_id`, `validationexecution_id`

## UI Component Terminology

| Derbent Component | Standard Term | Purpose |
|-------------------|---------------|---------|
| **CComponentValidationExecution** | Test Execution Interface / Test Runner | Manages session execution and result capture |
| **Validation Session View** | Test Session | Session management and execution entry |
| **Step Validator UI** | Test Execution Interface | Recording actual results per step |
| **Result Recorder** | Test Execution Log | Stores pass/fail outcomes |

## Documentation Guidance

- Module docs and menu examples must use **Validation** naming.
- Automated test documentation uses **Test** naming (unit, integration, Playwright).
- Standards references keep the official **Test** terminology and map to `CValidation*` entities.
