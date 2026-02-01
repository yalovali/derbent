package automated_tests.tech.derbent.ui.automation.tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.ActiveProfiles;

@DisplayName ("🧭 BAB Gateway Menu Navigation Test")
@ActiveProfiles (value = {
		"test", "bab"
}, inheritProfiles = false)
public class CBabMenuNavigationTest extends CMenuNavigationTest {

	@BeforeAll
	static void configureBabSchema() {
		System.setProperty("playwright.schema", "BAB Gateway");
		System.setProperty("playwright.forceSampleReload", "true");
	}
}
