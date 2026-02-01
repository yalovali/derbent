# BAB Component Testing Guide

**Version**: 1.0  
**Date**: 2026-02-01  
**Status**: ACTIVE  

## Overview

This guide describes the comprehensive testing strategy for BAB-specific UI components using the Derbent testing framework.

## Architecture

### Test Layers

| Layer | Purpose | Technology | Pattern |
|-------|---------|------------|---------|
| **Unit Tests** | Component structure validation | JUnit 5 | Interface/method verification |
| **Integration Tests** | Component behavior testing | Playwright | UI automation with fail-fast |
| **Component Testers** | Reusable test strategies | IComponentTester | Signature-based detection |

## BAB Components Under Test

### 1. CComponentCalimeroStatus

**Location**: `src/main/java/tech/derbent/bab/setup/view/CComponentCalimeroStatus.java`

**Purpose**: Manages Calimero HTTP service control with enable/disable, path configuration, and start/stop functionality.

**Features Tested**:
- ✅ Enable/Disable checkbox
- ✅ Executable path configuration
- ✅ Status indicator (running/stopped/disabled)
- ✅ Start/Stop button with full cycle testing
- ✅ Status consistency verification
- ✅ Async operation handling

**Unit Test**: `src/test/java/tech/derbent/bab/setup/view/CComponentCalimeroStatusTest.java`
- Verifies HasValue interface implementation
- Checks all component IDs for Playwright
- Validates public API methods

**Integration Test**: `CCalimeroStatusComponentTester.java`
- Tab/accordion navigation
- Component visibility verification
- Control interaction testing
- Status consistency validation

**Test Command**:
```bash
# Unit tests
mvn test -Dtest=CComponentCalimeroStatusTest

# Integration tests
MAVEN_OPTS="-Dtest.routeKeyword=system settings bab" ./run-playwright-tests.sh comprehensive
```

### 2. CComponentDashboardWidget_Bab

**Location**: `src/main/java/tech/derbent/bab/ui/component/CComponentDashboardWidget_Bab.java`

**Purpose**: Displays BAB project information and status in dashboard view.

**Features Tested**:
- ✅ Widget title display
- ✅ Project name display
- ✅ Active status indicator
- ✅ Dashboard type label
- ✅ Status indicator color (green/orange/gray)
- ✅ Refresh functionality

**Unit Test**: `src/test/java/tech/derbent/bab/ui/component/CComponentDashboardWidget_BabTest.java`
- Verifies CComponentDashboardWidget extension
- Checks component initialization pattern
- Validates refresh methods

**Integration Test**: `CDashboardWidgetBabTester.java`
- Dashboard tab detection
- Widget visibility verification
- Status color validation
- Refresh button testing

**Test Command**:
```bash
# Unit tests
mvn test -Dtest=CComponentDashboardWidget_BabTest

# Integration tests
MAVEN_OPTS="-Dtest.routeKeyword=bab dashboard" ./run-playwright-tests.sh comprehensive
```

## Test Pattern Standards

### Unit Test Pattern (JUnit 5)

```java
/**
 * Unit tests for CComponent[Name] to verify component structure and patterns.
 * 
 * Following Derbent test pattern: Verify component implements standard interfaces
 * and provides required methods without requiring full UI context initialization.
 * 
 * Component features tested:
 * - [List key features]
 * 
 * Integration testing via Playwright:
 * - Run: MAVEN_OPTS="-Dtest.routeKeyword=[keyword]" ./run-playwright-tests.sh comprehensive
 * - Tests [CRUD operations and features]
 */
class CComponent[Name]Test {

	@Test
	void testComponentDefinesStandardIDs() {
		// Verify all component IDs for Playwright testing
		assertEquals("expected-id", CComponent[Name].ID_CONSTANT);
	}

	@Test
	void testComponentExtendsProperBase() {
		// Verify component hierarchy
		assertTrue(CComponentBase.class.isAssignableFrom(CComponent[Name].class));
	}

	@Test
	void testImplementsHasValueInterface() {
		// For CComponentBase subclasses
		assertTrue(HasValue.class.isAssignableFrom(CComponent[Name].class));
	}

	@Test
	void testHasPublicMethods() throws Exception {
		// Verify required public API methods
		final Method method = CComponent[Name].class.getMethod("methodName");
		assertNotNull(method);
	}
}
```

### Integration Test Pattern (Playwright)

```java
/**
 * Tests the [Component Name] component.
 * 
 * Component features tested:
 * - [Feature 1]
 * - [Feature 2]
 * 
 * Integration testing:
 * - Run: MAVEN_OPTS="-Dtest.routeKeyword=[keyword]" ./run-playwright-tests.sh comprehensive
 */
public class C[Component]Tester extends CBaseComponentTester {

	private static final String COMPONENT_NAME = "[Component Name]";
	private static final String COMPONENT_TAB_LABEL = "[Tab/Section Name]";
	private static final String ROOT_ID = "custom-[component]-root";
	
	private static final String[] ROOT_SELECTORS = {
		"#" + ROOT_ID,
		".[css-class]",
		"[fallback-selector]"
	};

	@Override
	public boolean canTest(final Page page) {
		return locateRoot(page) != null;
	}

	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	private Locator locateRoot(final Page page) {
		// Component detection logic
		for (final String selector : ROOT_SELECTORS) {
			final Locator root = page.locator(selector);
			if (root.count() > 0 && root.isVisible()) {
				return root;
			}
		}
		return null;
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      🔧 Validating {} ...", COMPONENT_NAME);
		
		// Open tab/accordion if needed
		openTabOrAccordionIfNeeded(page, COMPONENT_TAB_LABEL);
		
		final Locator root = locateRoot(page);
		if (root == null) {
			throw new AssertionError(COMPONENT_NAME + " not found on page " + safePageTitle(page));
		}
		
		scrollIntoView(root);
		
		// Test component features
		verifyFeature1(page, root);
		verifyFeature2(page, root);
		testInteractions(page, root);
		
		LOGGER.info("      ✅ {} validation complete", COMPONENT_NAME);
	}
}
```

## Running Tests

### Unit Tests (Fast)

```bash
# All BAB component unit tests
mvn test -Dtest=CComponent*Test

# Specific component
mvn test -Dtest=CComponentCalimeroStatusTest
mvn test -Dtest=CComponentDashboardWidget_BabTest
```

**Benefits**:
- ⚡ Fast execution (milliseconds)
- 🔧 No UI context required
- ✅ Verifies component structure
- 📊 Interface compliance checking

### Integration Tests (Comprehensive)

```bash
# Calimero Status component
MAVEN_OPTS="-Dtest.routeKeyword=system settings bab" ./run-playwright-tests.sh comprehensive

# Dashboard Widget
MAVEN_OPTS="-Dtest.routeKeyword=bab dashboard" ./run-playwright-tests.sh comprehensive

# Visible browser for debugging
PLAYWRIGHT_HEADLESS=false MAVEN_OPTS="-Dtest.routeKeyword=calimero" ./run-playwright-tests.sh comprehensive

# Run all matching routes (not just first)
MAVEN_OPTS="-Dtest.routeKeyword=bab -Dtest.runAllMatches=true" ./run-playwright-tests.sh comprehensive
```

**Benefits**:
- 🎯 Real browser interaction
- ✅ Complete CRUD testing
- 🔄 Async operation validation
- 📸 Screenshots on failure
- 🚨 Fail-fast exception detection

## Test Coverage

### CComponentCalimeroStatus

| Feature | Unit Test | Integration Test | Status |
|---------|-----------|------------------|--------|
| Component IDs | ✅ | ✅ | COMPLETE |
| HasValue interface | ✅ | ✅ | COMPLETE |
| Enable/Disable checkbox | ✅ | ✅ | COMPLETE |
| Executable path field | ✅ | ✅ | COMPLETE |
| Status indicator | ✅ | ✅ | COMPLETE |
| Start/Stop button | ✅ | ✅ | COMPLETE |
| Full cycle test | ❌ | ✅ | COMPLETE |
| Refresh method | ✅ | ✅ | COMPLETE |

### CComponentDashboardWidget_Bab

| Feature | Unit Test | Integration Test | Status |
|---------|-----------|------------------|--------|
| Component hierarchy | ✅ | ✅ | COMPLETE |
| Widget title | ✅ | ✅ | COMPLETE |
| Project name display | ✅ | ✅ | COMPLETE |
| Active status | ✅ | ✅ | COMPLETE |
| Dashboard type | ✅ | ✅ | COMPLETE |
| Status indicator color | ❌ | ✅ | COMPLETE |
| Refresh functionality | ✅ | ✅ | COMPLETE |

## Integration with CPageTestComprehensive

The component testers are automatically registered in the comprehensive test framework:

```java
public class CPageTestComprehensive extends CBaseUITest {
	
	// Component testers are auto-discovered via control signatures
	private final IComponentTester calimeroStatusTester = new CCalimeroStatusComponentTester();
	private final IComponentTester dashboardWidgetTester = new CDashboardWidgetBabTester();
	
	// Testers are invoked when their canTest() returns true
	// This happens during tab walking and component detection
}
```

**Detection Flow**:
1. Navigate to page via CPageTestAuxillary button
2. Walk all tabs and accordions
3. For each view: run `tester.canTest(page)`
4. If true: run `tester.test(page)`
5. Continue with CRUD and grid testing

## Component ID Standards (MANDATORY)

**RULE**: All interactive components MUST define public static final String constants for IDs.

### ✅ CORRECT Pattern
```java
public class CComponentCalimeroStatus extends CComponentBase<CSystemSettings_Bab> {
	
	// Public constants for Playwright testing
	public static final String ID_ROOT = "custom-calimero-status-component";
	public static final String ID_CARD = "custom-calimero-control-card";
	public static final String ID_ENABLE_CHECKBOX = "custom-calimero-enable-checkbox";
	public static final String ID_EXECUTABLE_PATH_FIELD = "custom-calimero-executable-path";
	public static final String ID_STATUS_INDICATOR = "custom-calimero-status-indicator";
	public static final String ID_START_STOP_BUTTON = "custom-calimero-start-stop-button";
	
	private void initializeComponents() {
		setId(ID_ROOT);
		checkbox.setId(ID_ENABLE_CHECKBOX);
		textField.setId(ID_EXECUTABLE_PATH_FIELD);
		// ...
	}
}
```

### ❌ INCORRECT Pattern
```java
// Missing public constants
private void initializeComponents() {
	setId("my-component");  // Hardcoded - not testable!
	checkbox.setId("checkbox" + System.currentTimeMillis());  // Dynamic - not stable!
}
```

**Benefits**:
- ✅ Type-safe ID references
- ✅ Compile-time verification
- ✅ Easy refactoring
- ✅ Self-documenting component structure
- ✅ Stable Playwright selectors

## Fail-Fast Exception Detection

All integration tests include automatic exception detection:

```java
// After each action
performFailFastCheck(page);

// Checks for:
// - "Error" notifications
// - Exception dialogs
// - Stack traces in console
// - Vaadin error messages
```

**Benefits**:
- 🚨 Immediate test failure on exception
- 📝 Full exception stack trace logged
- 🎯 Pinpoints exact failing operation
- ⏱️ No timeout waiting - instant feedback

## Adding New Component Tests

### Step 1: Create Unit Test

```bash
# Create test file in matching package
src/test/java/tech/derbent/bab/[package]/view/CComponent[Name]Test.java
```

### Step 2: Create Integration Tester

```bash
# Create tester in automation components
src/test/java/automated_tests/tech/derbent/ui/automation/components/C[Name]Tester.java
```

### Step 3: Register in CPageTestComprehensive

```java
// Add to component tester fields
private final IComponentTester myNewTester = new CMyNewTester();

// Add to control signatures (if needed)
private List<IControlSignature> initializeControlSignatures() {
	signatures.add(new CControlSignature("My Component", 
		"#custom-my-component-root", myNewTester));
	return signatures;
}
```

### Step 4: Run Tests

```bash
# Unit test
mvn test -Dtest=CComponent[Name]Test

# Integration test
MAVEN_OPTS="-Dtest.routeKeyword=[keyword]" ./run-playwright-tests.sh comprehensive
```

## Best Practices

### Component IDs
- ✅ Use `public static final String ID_*` constants
- ✅ Prefix with `custom-` to avoid conflicts
- ✅ Use kebab-case: `custom-calimero-enable-checkbox`
- ✅ Set IDs in `initializeComponents()`

### Test Methods
- ✅ One verification per method: `verifyEnableCheckbox()`
- ✅ Descriptive logging: `LOGGER.info("🔧 Testing feature...")`
- ✅ Fail-fast assertions: Throw AssertionError immediately
- ✅ Graceful warnings: Log non-critical issues

### Wait Strategies
- ✅ `wait_500(page)` after clicks
- ✅ `wait_1000(page)` after state changes
- ✅ `wait_2000(page)` for async operations
- ✅ Use `isVisible()` before interactions

### Locator Strategies
- ✅ Primary: ID selector (`#custom-component-id`)
- ✅ Fallback: CSS class (`.component-class`)
- ✅ Last resort: Text content (`:has-text('Label')`)
- ✅ Always check `count() > 0` before use

## Troubleshooting

### Test Fails to Find Component

**Problem**: `canTest()` returns false

**Solutions**:
1. Check component ID is set correctly
2. Verify tab/accordion navigation
3. Add fallback CSS selectors
4. Check component visibility conditions

### Button Click Has No Effect

**Problem**: Click succeeds but no state change

**Solutions**:
1. Increase wait time after click
2. Check for async operations
3. Verify button is enabled
4. Check for confirmation dialogs

### Status Verification Fails

**Problem**: Status text doesn't match expected

**Solutions**:
1. Check status update timing
2. Use contains() instead of equals()
3. Wait for status to stabilize
4. Log actual vs expected values

## Future Enhancements

### Planned Features
- 🔄 Component refresh cycle testing
- 📊 Performance metrics collection
- 🎨 Visual regression testing
- 🧪 Property-based testing
- 📈 Coverage report generation

### Component Roadmap
- [ ] CComponentBabBase tests
- [ ] CComponentInterfaceList enhanced tests
- [ ] CComponentNetworkTopology tests
- [ ] CComponentDeviceConfiguration tests

## References

- **AGENTS.md**: Master playbook with coding standards
- **PLAYWRIGHT_TESTING_GUIDE.md**: Fail-fast testing patterns
- **CPageTestComprehensive.java**: Main test framework
- **IComponentTester.java**: Component tester interface

## Contact

**Test Framework Owner**: Master Yasin  
**Component Owners**: SSC (BAB components)  
**Last Updated**: 2026-02-01
