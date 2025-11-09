# 📸 GUI Testing & Screenshot Documentation

## 🎯 Quick Overview

This documentation provides everything needed to run Playwright browser automation tests that validate CRUD (Create, Read, Update, Delete) operations across all entities in the Derbent application, with automatic screenshot capture for visual verification.

---

## 📚 Documentation Files

### 1. 🚀 [GUI_TESTING_EXECUTION_GUIDE.md](./GUI_TESTING_EXECUTION_GUIDE.md)
**Start here if you want to RUN the tests**

**What's inside**:
- Step-by-step execution instructions
- Command reference for all test suites
- Prerequisites and environment setup
- Troubleshooting common issues
- Alternative execution methods

**Quick commands**:
```bash
./run-playwright-tests.sh all          # Run everything
./run-playwright-tests.sh status-types # Just CRUD operations
./run-playwright-tests.sh comprehensive # Dynamic views
```

---

### 2. 📊 [CRUD_TESTING_COVERAGE_SUMMARY.md](./CRUD_TESTING_COVERAGE_SUMMARY.md)
**Read this to understand WHAT is tested**

**What's inside**:
- Complete test architecture overview
- Detailed coverage of 10+ entities
- CRUD operation patterns and validations
- Test helper methods reference (25+ methods)
- Testing methodology and success criteria
- Execution metrics and expected results

**Coverage highlights**:
- ✅ 22 test methods across 6 test classes
- ✅ Activity Types, Meeting Types, Decision Types, Order Types
- ✅ Activity Status, Approval Status, Workflow Status
- ✅ Users, Projects, Activities, Meetings, Companies
- ✅ 100+ validation assertions per complete run

---

### 3. 📸 [VISUAL_CRUD_TESTING_EXAMPLES.md](./VISUAL_CRUD_TESTING_EXAMPLES.md)
**See this to understand WHAT SCREENSHOTS look like**

**What's inside**:
- Detailed visual examples of each CRUD operation
- Screenshot sequences for complete workflows
- Form state examples (empty, filled, validated, errors)
- Grid interaction screenshots (sort, filter, paginate)
- Notification examples (success, error, warning)
- Button state documentation
- Navigation flow examples
- Multi-step workflow sequences

**Example scenarios**:
- Creating "Sprint Planning" Activity Type (4 screenshots)
- Viewing Meeting Type details (1 screenshot)
- Updating Decision Type (3 screenshots)
- Delete validation for protected entities (2 screenshots)
- Complete new user workflow (13 screenshots)

---

## ⚡ Quick Start

### Running Tests (Local Machine)

```bash
# 1. Clone and navigate to repository
git clone https://github.com/yalovali/derbent.git
cd derbent

# 2. Build the application
mvn clean compile

# 3. Run all tests with screenshots
./run-playwright-tests.sh all

# 4. View generated screenshots
ls -lh target/screenshots/
```

**Expected output**: 50-80 screenshots in `target/screenshots/` directory

**Time required**: 10-15 minutes for complete test suite

---

## 🎨 What You'll Get

### Screenshot Categories

#### CRUD Operation Screenshots (per entity):
1. **Initial view** - Grid with existing data
2. **After New** - Empty form ready for input
3. **Form filled** - All fields populated with test data
4. **After Save** - Success notification + updated grid
5. **Entity selected** - Details displayed in form
6. **During Update** - Modified fields highlighted
7. **After Update** - Changes reflected in grid
8. **Delete attempt** - Validation/protection message
9. **After Refresh** - Data reloaded

#### Additional Screenshots:
- Form validation errors
- ComboBox dropdowns
- Grid sorting and filtering
- All button states
- Navigation menus
- Dialog confirmations
- Error messages

---

## 🧪 Available Test Suites

| Command | Test Suite | Entities Covered | Screenshots | Time |
|---------|-----------|------------------|-------------|------|
| `status-types` | Type & Status CRUD | 6 entity types | ~40-50 | 3-5 min |
| `comprehensive` | Dynamic Views | 5+ main entities | ~15-20 | 3-5 min |
| `buttons` | Button Functionality | All views | ~10-15 | 2-3 min |
| `workflow` | Workflow Status | Workflow entities | ~8-10 | 2-3 min |
| `all` | Complete Suite | All entities | 50-80 | 10-15 min |

---

## 📋 Test Coverage Summary

### Entities with Full CRUD Testing

**Status and Type Entities**:
- ✅ Activity Types (`CActivityType`)
- ✅ Meeting Types (`CMeetingType`)
- ✅ Decision Types (`CDecisionType`)
- ✅ Order Types (`COrderType`)
- ✅ Activity Status (`CProjectItemStatus`)
- ✅ Approval Status (`CApprovalStatus`)

**Workflow Entities**:
- ✅ Workflow Status with transitions

**Main Business Entities**:
- ✅ Users (`CUser`)
- ✅ Projects (`CProject`)
- ✅ Activities (`CActivity`)
- ✅ Meetings (`CMeeting`)
- ✅ Companies (`CCompany`)

### Operations Validated for Each Entity

- ✅ **CREATE**: Form fill → Save → Notification → Grid update
- ✅ **READ**: Select → Display data in form fields
- ✅ **UPDATE**: Edit → Modify → Save → Verify changes
- ✅ **DELETE**: Attempt delete → Validate protection
- ✅ **REFRESH**: Reload data → Maintain selection

---

## 🔍 Test Architecture

### Base Test Class: `CBaseUITest.java`

**Key Features**:
- 25+ helper methods for common test operations
- Automatic screenshot capture
- Standardized CRUD patterns
- Comprehensive validation checks

**Helper Methods Include**:
```java
// Navigation
navigateToViewByText(String viewText)
navigateToDynamicPageByEntityType(String entityType)
navigateToProjects()

// CRUD Operations
clickNew()
clickSave()
clickEdit()
clickDelete()
clickRefresh()

// Form Interaction
fillFirstTextField(String value)
fillFirstTextArea(String value)
selectFirstComboBoxOption()
fillFieldById(Class<?> entityClass, String fieldName, String value)

// Grid Operations
clickFirstGridRow()
getGridRowCount()
verifyGridHasData()
applyGridSearchFilter(String query)

// Validation
verifyAccessibility()
verifyNotification(String type)
verifyButtonState(String buttonText, boolean shouldBeEnabled)

// Screenshots
takeScreenshot(String name)
takeViewScreenshot(Class<?> viewClass, String scenario, boolean success)
```

---

## 🎯 Success Criteria

After running tests, you should have:

✅ **50-80 screenshots** documenting all CRUD operations  
✅ **Visual proof** of Create, Read, Update, Delete functionality  
✅ **Form state documentation** (empty, filled, validated, errors)  
✅ **Grid interaction proof** (sorting, filtering, selection)  
✅ **Notification captures** (success, error messages)  
✅ **Button state documentation** (enabled, disabled states)  
✅ **Complete workflow sequences** (multi-step operations)  

---

## ⚠️ Known Limitation

**CI Environment Issue**: Maven repositories (`maven.vaadin.com`, `storedobject.com`) are blocked in GitHub Actions environment, preventing automated build/test execution.

**Solution**: Run tests on local machine where Maven repositories are accessible.

---

## 📖 Additional Resources

**Existing Documentation**:
- `PLAYWRIGHT_TEST_EXECUTION_REPORT.md` - Previous test results
- `TESTING_GUIDE.md` - General testing guidelines
- `docs/testing/PLAYWRIGHT_TEST_SUMMARY.md` - Detailed test documentation

**Test Implementation**:
- `src/test/java/automated_tests/tech/derbent/ui/automation/` - Test source code
- `src/test/java/automated_tests/tech/derbent/ui/automation/CBaseUITest.java` - Base test class

**Test Execution Scripts**:
- `run-playwright-tests.sh` - Main test runner
- `run-all-playwright-tests.sh` - Alternative runner

---

## 🚀 Next Steps

1. **Read** [GUI_TESTING_EXECUTION_GUIDE.md](./GUI_TESTING_EXECUTION_GUIDE.md) for detailed instructions
2. **Understand** [CRUD_TESTING_COVERAGE_SUMMARY.md](./CRUD_TESTING_COVERAGE_SUMMARY.md) to know what's tested
3. **Preview** [VISUAL_CRUD_TESTING_EXAMPLES.md](./VISUAL_CRUD_TESTING_EXAMPLES.md) to see what screenshots look like
4. **Run** `./run-playwright-tests.sh all` to generate screenshots
5. **Review** `target/screenshots/` directory for results

---

## 📞 Support

For questions or issues:
- Check the troubleshooting section in `GUI_TESTING_EXECUTION_GUIDE.md`
- Review test implementation in `src/test/java/automated_tests/`
- Examine existing test results in `PLAYWRIGHT_TEST_EXECUTION_REPORT.md`

---

**Ready to test?** Start with [GUI_TESTING_EXECUTION_GUIDE.md](./GUI_TESTING_EXECUTION_GUIDE.md) →
