package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.ActiveProfiles;

/** Dedicated adaptive Playwright test that targets the BAB dashboard project view and walks every tab/section to detect interface components. */
@DisplayName ("🌐 BAB Dashboard Component Detection Test")
@ActiveProfiles (value = {
		"test", "bab"
}, inheritProfiles = false)
public class CBabDashboardComponentDetectionTest extends CAdaptivePageTest {

	private static final String BAB_DASHBOARD_TITLE_FILTER = "bab dashboard projects - view 2";

	@BeforeAll
	static void configureBabDashboardTarget() {
		System.setProperty("playwright.schema", "BAB Gateway");
		System.setProperty("playwright.forceSampleReload", "true");
		System.clearProperty("test.targetButtonId");
		System.setProperty("test.titleContains", BAB_DASHBOARD_TITLE_FILTER);
		System.setProperty("test.runAllMatches", "false");
	}
}
