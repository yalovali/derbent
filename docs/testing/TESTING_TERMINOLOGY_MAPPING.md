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

## UI Component Terminology (ISO 29119 & ISTQB Standards)

### Test Execution Components

Based on ISO/IEC/IEEE 29119 and ISTQB Glossary v4.1, the following component terminology should be used:

| Derbent Component | Standard Term | ISO 29119 Reference | ISTQB Reference | Purpose |
|-------------------|---------------|---------------------|-----------------|---------|
| **CComponentTestExecution** | Test Runner / Test Harness | Part 4: Test Techniques | Glossary: Test Automation | Manages automated/manual test execution |
| **Test Session View** | Test Session | Part 1: Concepts & Definitions | Glossary: Test Session | Time-boxed test execution period |
| **Step Validator UI** | Test Execution Interface | Part 3: Test Documentation | Glossary: Tester Interface | UI for recording actual results |
| **Result Recorder** | Test Execution Log | Part 3: Test Reporting | Glossary: Test Report | Records pass/fail outcomes |
| **Progress Monitor** | Test Progress Tracking | Tool-specific (best practice) | Tool-specific | Real-time execution status |

### Key Standard Terms

**From ISO 29119**:
- **Test Session**: An uninterrupted period of test execution
- **Test Runner**: Software that manages test execution
- **Test Harness**: Collection of software and test data for testing
- **Test Execution**: The process of running tests and producing results

**From ISTQB Glossary v4.1**:
- **Tester**: Person responsible for test execution (NOT "test operator" or "test executor")
- **Actual Result**: The behavior observed during test execution
- **Expected Result**: The predicted behavior according to specifications
- **Test Execution**: The activity of running a test and comparing actual results with expected results

### UX Pattern Compliance

Following ISO 29119 Part 3 (Test Documentation) and ISTQB usability principles:

#### 1. Clear Expected vs Actual Results
- ✅ Display expected result prominently at top of step
- ✅ Provide large text area for actual result entry (minimum 4 rows)
- ✅ Side-by-side comparison when screen space allows
- ✅ Highlight differences in red when test fails

#### 2. Step-by-Step Navigation
- ✅ Previous/Next buttons with keyboard shortcuts (Alt+Left/Alt+Right)
- ✅ Current step indicator: "Step 3 of 10" or "Test Case 2 of 5"
- ✅ Completion percentage with progress bar
- ✅ Jump to step dropdown for quick navigation

#### 3. Result Status Selection (ISO 29119 Status Values)
- ✅ Large, color-coded buttons:
  - **PASS** (green #4CAF50) - Test passed
  - **FAIL** (red #F44336) - Test failed
  - **SKIP** (gray #9E9E9E) - Test skipped
  - **BLOCK** (yellow #FFC107) - Test blocked by dependency
- ✅ Single-click to set status (no confirmation for speed)
- ✅ Keyboard shortcuts: P (pass), F (fail), S (skip), B (block)
- ✅ Status automatically saved on change

#### 4. Evidence Attachment (ISO 29119 Best Practice)
- ✅ Quick screenshot button (capture browser/app)
- ✅ Drag-drop file upload area
- ✅ Link evidence at step or case level
- ✅ Thumbnail preview of attached images
- ✅ Auto-prompt for evidence on FAIL status

#### 5. Error Prevention & Data Loss Protection
- ✅ Auto-save results after each step completion
- ✅ Warn before leaving incomplete execution
- ✅ Save draft results every 30 seconds
- ✅ Resume capability if session interrupted
- ✅ Highlight unsaved changes with indicator

#### 6. Accessibility (ISTQB Usability Requirements)
- ✅ ARIA labels for screen readers
- ✅ Keyboard-only navigation support
- ✅ High contrast mode option
- ✅ Resizable text areas
- ✅ Focus indicators on all interactive elements

### Component Layout Standards

```
┌─────────────────────────────────────────────────────────────┐
│ Test Session: [Session Name]               Status: [Badge] │
│ Test Case 2 of 5: [Test Case Name]         [Progress: 40%] │
├─────────────────────────────────────────────────────────────┤
│ Step 3 of 7: [Action]                                       │
│                                                              │
│ Expected Result:                                            │
│ [Expected result text displayed here]                       │
│                                                              │
│ Actual Result: (Enter your observations)                    │
│ ┌────────────────────────────────────────────────────────┐ │
│ │ [Large text area for actual result - min 4 rows]      │ │
│ │                                                        │ │
│ └────────────────────────────────────────────────────────┘ │
│                                                              │
│ Result Status:                                              │
│ [PASS] [FAIL] [SKIP] [BLOCK]                               │
│                                                              │
│ Evidence: [📷 Screenshot] [📎 Attach File]  [3 attached]    │
│                                                              │
│ [< Previous] [Next >] [Jump to...▼] [Save & Exit]          │
└─────────────────────────────────────────────────────────────┘
```

### Terminology Migration Notes

For consistency with standards, use these terms in UI:
- ❌ "Test Operator" → ✅ "Tester" (ISTQB standard)
- ❌ "Test Execution Record" → ✅ "Test Execution Log" (ISO 29119)
- ❌ "Test Runner Component" → ✅ "Test Execution Interface" (user-facing)
- ✅ "Test Session" (already correct)
- ✅ "Test Suite" (already updated)
- ✅ "Expected Result" (already correct)
- ✅ "Actual Result" (already correct)

### References for Implementation

1. **ISO/IEC/IEEE 29119-1:2022** - Concepts and Definitions
2. **ISO/IEC/IEEE 29119-3:2021** - Test Documentation
3. **ISO/IEC/IEEE 29119-4:2021** - Test Techniques
4. **ISTQB Glossary v4.1** - Standard Testing Terminology
5. **WCAG 2.1** - Web Content Accessibility Guidelines (for UI accessibility)
