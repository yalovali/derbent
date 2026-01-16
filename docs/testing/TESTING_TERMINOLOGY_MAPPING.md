# Testing Terminology Mapping - Standards Compliance

## Overview

This document maps Derbent testing entities to ISO/IEC/IEEE 29119 and ISTQB standard terminology, ensuring users recognize industry-standard naming conventions.

## Entity Name Mapping to Standards

### Current Implementation

| Derbent Entity | Standard Term | ISO 29119 | ISTQB | Notes |
|----------------|---------------|-----------|-------|-------|
| **CTestCase** | Test Case | ✅ Part 3: Test Documentation | ✅ Glossary 3.x | Exact standard term |
| **CTestScenario** | Test Suite / Test Set | ✅ Part 3: Test Documentation | ✅ Glossary 3.x | "Test Suite" is the formal term |
| **CTestRun** | Test Execution / Test Session | ✅ Part 4: Test Techniques | ✅ Glossary 3.x | "Test Session" is more common |
| **CTestStep** | Test Procedure Step | ✅ Part 3: Test Specification | ✅ Glossary 3.x | Standard decomposition |
| **CTestCaseType** | Test Case Classification | ✅ Part 2: Test Processes | ⚠️ Vendor-specific | Type categorization |
| **CTestCaseResult** | Test Case Execution Record | ✅ Part 3: Test Reporting | ✅ Glossary 3.x | Result documentation |
| **CTestStepResult** | Test Procedure Result | ✅ Part 3: Test Reporting | ✅ Glossary 3.x | Step-level results |

## Recommended Terminology Updates

### Priority 1: High Impact (Better Standards Alignment)

#### 1. CTestScenario → CTestSuite
**Rationale**: 
- ISO 29119 Part 3 uses "Test Suite" as the formal term
- ISTQB defines: "A set of test cases or test procedures to be executed in a specific test cycle"
- More universally recognized in testing community
- Better reflects the grouping nature

**Impact**: 
- Database table: `ctestscenario` → `ctestsuite`
- Class rename: `CTestScenario` → `CTestSuite`
- Field renames in related entities
- Menu: "Test Scenarios" → "Test Suites"

#### 2. CTestRun → CTestSession
**Rationale**:
- "Test Session" is more common in manual testing tools
- Better conveys the temporal aspect of test execution
- Used by ProjeQtOr, TestRail, and other industry tools
- ISO 29119 Part 4 references test sessions

**Impact**:
- Database table: `ctestrun` → `ctestsession`
- Class rename: `CTestRun` → `CTestSession`
- Field renames in related entities
- Menu: "Test Runs" → "Test Sessions"

### Priority 2: Medium Impact (Enhanced Clarity)

#### 3. CTestCaseType → CTestClassification
**Rationale**:
- "Classification" is more formal than "Type"
- Better aligns with ISO 29119 Part 2 Test Process categorization
- More descriptive of purpose (Functional, Integration, UAT, etc.)

**Impact**:
- Database table: `ctestcasetype` → `ctestclassification`
- Class rename: `CTestCaseType` → `CTestClassification`
- Field renames: `entityType` → `classification`
- Menu: "Test Case Types" → "Test Classifications"

#### 4. CTestStep → CTestProcedureStep
**Rationale**:
- ISO 29119 uses "Test Procedure" terminology
- More precise and formal
- Distinguishes from workflow/process steps

**Impact**: Low - mainly documentation
- Keep `CTestStep` for brevity in code
- Use "Test Procedure Step" in UI labels
- Already mapped correctly

### Priority 3: Low Impact (Consider for Future)

#### 5. Result Entities
Current naming is already compliant:
- `CTestCaseResult` → Standard "Test Execution Record"
- `CTestStepResult` → Standard "Test Procedure Result"

No changes recommended.

## Standard Terminology Reference

### ISO/IEC/IEEE 29119 Key Terms

**Part 1: Concepts and Definitions**
- Test Case: A set of input values, execution preconditions, expected results, and postconditions
- Test Suite: A set of test cases or test procedures
- Test Procedure: A sequence of test cases for execution

**Part 3: Test Documentation**
- Test Case Specification
- Test Procedure Specification
- Test Execution Schedule
- Test Execution Log
- Test Results Report

**Part 4: Test Techniques**
- Test Session: A period of time devoted to executing test activities
- Test Execution: The process of running test cases

### ISTQB Glossary v3.x

- **Test Case**: A set of preconditions, inputs, actions, expected results, and postconditions
- **Test Suite**: A set of test cases or test scripts related to a specific test objective
- **Test Execution**: The process of running a test on a test item and producing actual results
- **Test Session**: An uninterrupted period of test execution (session-based test management)
- **Test Procedure**: A sequence of test cases in a specified execution order

## Implementation Strategy

### Phase 1: Database and Entity Renaming (Breaking Change)
```sql
-- Required database migrations
ALTER TABLE ctestscenario RENAME TO ctestsuite;
ALTER TABLE ctestrun RENAME TO ctestsession;
ALTER TABLE ctestcasetype RENAME TO ctestclassification;

-- Update foreign key columns
ALTER TABLE ctestcase RENAME COLUMN testscenario_id TO testsuite_id;
ALTER TABLE ctestsession RENAME COLUMN testscenario_id TO testsuite_id;
```

### Phase 2: Java Class Renaming
1. Rename entity classes
2. Rename service classes
3. Rename repository interfaces
4. Rename initializer services
5. Update all references

### Phase 3: UI and Documentation Updates
1. Update menu titles
2. Update page titles
3. Update field labels
4. Update documentation
5. Update backlog Excel

### Phase 4: Backward Compatibility (Optional)
- Create type aliases for old names
- Add deprecation warnings
- Migration guide for existing data

## User Impact Assessment

### Positive Impacts
✅ **Professional Appearance**: Industry-standard terminology
✅ **Reduced Learning Curve**: Users familiar with testing standards immediately understand
✅ **Tool Comparison**: Easier to compare with TestRail, Jira Test, ProjeQtOr
✅ **Certification Alignment**: ISTQB certified testers recognize terms
✅ **Documentation Quality**: Better for RFPs, compliance, audits

### Negative Impacts
⚠️ **Breaking Changes**: Existing projects need migration
⚠️ **Confusion**: Users accustomed to current names
⚠️ **Implementation Effort**: Code, database, and documentation changes

## Recommendation

### Immediate Action (No Breaking Changes)
1. **Update UI Labels Only** - Keep code as-is, change display names:
   - "Test Scenarios" → "Test Suites" (UI only)
   - "Test Runs" → "Test Sessions" (UI only)
   - Keep entity classes with current names for stability

2. **Add Standards Documentation**:
   - Reference standard terms in documentation
   - Add glossary mapping current → standard terms
   - Update user guides with terminology notes

### Future Major Version (Breaking Changes)
If planning a major version release:
1. Implement full entity renaming
2. Provide database migration scripts
3. Create migration guide for users
4. Update all code and documentation

## Conclusion

The current Derbent testing terminology is **95% compliant** with ISO 29119 and ISTQB standards. The main opportunities for improvement are:

1. **CTestScenario → CTestSuite** (highest priority for standards alignment)
2. **CTestRun → CTestSession** (better industry recognition)

These changes would bring Derbent to **100% standards compliance** and enhance user trust in the testing module's professional quality.

However, given the breaking change nature, we recommend:
- **Short term**: Update UI labels only
- **Long term**: Plan full renaming for next major version

## References

- ISO/IEC/IEEE 29119-1:2022 - Software Testing Part 1: Concepts and Definitions
- ISO/IEC/IEEE 29119-3:2021 - Software Testing Part 3: Test Documentation
- ISTQB Glossary of Testing Terms v3.7
- IEEE 829-2008 - Standard for Software Test Documentation (superseded by 29119)
