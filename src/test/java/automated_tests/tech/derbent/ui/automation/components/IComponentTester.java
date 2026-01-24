package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Page;

/** Base interface for component-specific test strategies. Each implementation tests a specific UI component type (e.g., attachment, comment, status,
 * grid). */
public interface IComponentTester {

	/** Check if this tester can test components on the current page.
	 * @param page Playwright page
	 * @return true if component is detected and testable */
	boolean canTest(Page page);
	/** Get component name for logging.
	 * @return Component name */
	String getComponentName();
	/** Execute tests for this component.
	 * @param page Playwright page */
	void test(Page page);
}
