# Testing Methodology Architecture

## Overview

The Derbent testing module provides comprehensive manual and automated test case management capabilities, following industry standards from tools like ProjeQtOr, TestRail, and ISO/IEC/IEEE 29119 testing standards.

## Core Concepts

### Test Hierarchy

```
Test Scenario (Business workflow/feature)
  └─> Test Case (Specific test condition)
       └─> Test Step (Atomic test action)
```

### Entity Relationships

1. **CTestScenario**: Groups related test cases representing a business workflow or user journey
   - Contains multiple test cases
   - Defines overall testing objective and prerequisites
   - Project-scoped entity

2. **CTestCase**: Specific test condition with expected behavior
   - Belongs to optional test scenario
   - Contains ordered test steps
   - Has type (CTestCaseType) with workflow/status
   - Supports priority (LOW, MEDIUM, HIGH, CRITICAL)
   - Supports severity (TRIVIAL, MINOR, NORMAL, MAJOR, BLOCKER)
   - Can be manual or automated (with test path reference)
   - Project-scoped entity with workflow

3. **CTestStep**: Individual action within a test case
   - Ordered sequence (stepOrder field)
   - Action description (what to do)
   - Expected result (what should happen)
   - Test data (input values)
   - Notes (additional context)

4. **CTestRun**: Execution session of a test scenario
   - Executes all test cases in a scenario
   - Records overall result (PASSED, FAILED, BLOCKED, NOT_EXECUTED, SKIPPED)
   - Tracks execution timing (start, end, duration)
   - Records executed by user
   - Contains test case results and step results
   - Aggregates statistics (total/passed/failed counts)

5. **CTestCaseResult**: Result of executing one test case within a run
   - Links to test case and test run
   - Records result status
   - Contains test step results

6. **CTestStepResult**: Result of executing one test step
   - Links to test step and test case result
   - Records actual result vs expected
   - Captures evidence (screenshots, logs via attachments)

7. **CTestCaseType**: Categorizes test cases
   - Defines workflow and available statuses
   - Examples: Functional, Integration, Regression, UAT, Performance

## Test Case Lifecycle

### States (via Workflow)
- **New**: Newly created, not yet reviewed
- **Under Review**: Being reviewed for completeness
- **Approved**: Ready for execution
- **Deprecated**: No longer applicable
- **Active**: In regular use

### Priority Levels
- **LOW**: Nice to have, minor features
- **MEDIUM**: Standard features (default)
- **HIGH**: Important functionality
- **CRITICAL**: Core/critical functionality

### Severity Levels
- **TRIVIAL**: Cosmetic issues
- **MINOR**: Minor loss of function
- **NORMAL**: Functional issue (default)
- **MAJOR**: Major loss of function
- **BLOCKER**: Complete loss of function

## Test Execution Workflow

### 1. Test Planning
1. Create test scenarios for features/workflows
2. Define test cases within scenarios
3. Add test steps with actions and expected results
4. Assign test case types and priorities
5. Review and approve test cases

### 2. Test Execution
1. Create test run for a scenario
2. Execute each test case step by step
3. Record actual results for each step
4. Mark steps as PASSED/FAILED/BLOCKED/SKIPPED
5. Attach evidence (screenshots, logs)
6. Complete test run with overall result

### 3. Test Reporting
1. View test run statistics (pass/fail rates)
2. Analyze failed tests
3. Link failures to defects/issues
4. Track test coverage
5. Generate test reports

## Integration with Project Management

### Links to Other Entities
- **Requirements**: Test cases validate requirements
- **Issues/Bugs**: Failed tests create issues
- **Activities**: Test execution as project activities
- **Sprint Items**: Test cases in sprint backlogs
- **Attachments**: Test evidence and documentation
- **Comments**: Test execution notes and discussions

### Automated Testing Integration
- `automated` flag marks test cases with automation
- `automatedTestPath` links to Playwright test file/method
- Test runs can be triggered by CI/CD pipelines
- Automated test results can update test run entities

## UI Components

### Test Scenario View
- List all test scenarios in project
- Create/edit/delete scenarios
- View associated test cases
- Track scenario completion

### Test Case View
- List all test cases with filtering
- CRUD operations on test cases
- Manage test steps (inline editing)
- View test case execution history
- Filter by type, priority, severity, automated
- Search by name, description, preconditions

### Test Run View
- Create new test run from scenario
- Execute tests step-by-step
- Record results in real-time
- View execution progress
- Complete and save results

### Test Results Dashboard
- Overall test metrics
- Pass/fail trends over time
- Coverage by feature/scenario
- Failed test analysis
- Execution time trends

## Best Practices

### Test Case Design
1. **Atomic**: Each test case tests one specific behavior
2. **Independent**: Test cases should not depend on each other
3. **Repeatable**: Same inputs should produce same results
4. **Clear**: Steps and expectations are unambiguous
5. **Traceable**: Linked to requirements and features

### Test Step Guidelines
1. One action per step
2. Specific and measurable expected results
3. Include test data values
4. Clear preconditions
5. Logical ordering

### Test Execution Guidelines
1. Follow steps exactly as written
2. Record actual results completely
3. Attach evidence for failures
4. Note deviations or observations
5. Link failures to issues immediately

### Test Maintenance
1. Review test cases regularly
2. Deprecate obsolete tests
3. Update for feature changes
4. Maintain test data
5. Archive old test runs periodically

## Standards Compliance

### ISO/IEC/IEEE 29119
- Test process organization
- Test documentation standards
- Test techniques and measures

### ISTQB Best Practices
- Test case design techniques
- Test execution procedures
- Defect management integration

### Industry Tool Compatibility
- ProjeQtOr-style test management
- TestRail-compatible workflows
- Jira Test Management patterns

## Future Enhancements

### Phase 2
- Test suite organization (folders/tags)
- Test data management
- Parameterized test cases
- Test case reuse/templates

### Phase 3
- Advanced reporting and analytics
- CI/CD integration hooks
- Automated test synchronization
- Test coverage matrix

### Phase 4
- Requirements traceability matrix
- Risk-based testing
- Exploratory testing sessions
- Test estimation and planning

## Related Documentation
- `docs/implementation/TEST_MODULE_IMPLEMENTATION.md` - Implementation guide
- `docs/testing/comprehensive-page-testing.md` - UI testing patterns
- `docs/development/copilot-guidelines.md` - Development patterns
- `docs/architecture/coding-standards.md` - Code standards
