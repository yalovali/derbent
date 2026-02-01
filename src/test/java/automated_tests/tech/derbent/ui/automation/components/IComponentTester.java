package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Page;

/**
 * Interface for component testers.
 * Component testers are NOT tests themselves - they are helpers called BY tests.
 * They extend CBaseComponentTester and implement this interface.
 */
public interface IComponentTester {
	
	/**
	 * Check if this component can be tested on the current page.
	 * 
	 * @param page the Playwright page
	 * @return true if the component exists and can be tested
	 */
	boolean canTest(Page page);
	
	/**
	 * Test the component functionality.
	 * 
	 * @param page the Playwright page
	 */
	void test(Page page);
	
	/**
	 * Get the name of this component for logging.
	 * 
	 * @return component name
	 */
	String getComponentName();
}
