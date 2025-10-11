package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/** Playwright smoke test that verifies the application can be opened and authenticated with default credentials in headless environments. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
@DisplayName ("🔐 Basic Login Smoke Test")
public class CSimpleLoginTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSimpleLoginTest.class);

	@Test
	@DisplayName ("✅ Login succeeds with default admin credentials")
	void loginWithDefaultCredentials() {
		LOGGER.info("🔐 Starting basic login smoke test");
		loginToApplication();
	}
}
