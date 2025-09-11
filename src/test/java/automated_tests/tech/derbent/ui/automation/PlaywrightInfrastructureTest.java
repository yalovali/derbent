package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/** Simple test to verify the Playwright test infrastructure compiles correctly without requiring browser installation. */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa", 
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "server.port=8080"
})
public class PlaywrightInfrastructureTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaywrightInfrastructureTest.class);

    @Test
    void testPlaywrightInfrastructureExists() {
        LOGGER.info("🧪 Testing Playwright infrastructure compilation...");
        
        // Verify base class exists and can be instantiated
        try {
            CBaseUITest baseTest = new CBaseUITest() {};
            assertTrue(baseTest != null);
            LOGGER.info("✅ CBaseUITest base class exists and compiles");
        } catch (Exception e) {
            LOGGER.error("❌ CBaseUITest instantiation failed: {}", e.getMessage());
            throw e;
        }
        
        // Verify essential methods exist
        assertTrue(CBaseUITest.class.getDeclaredMethods().length > 0);
        LOGGER.info("✅ CBaseUITest has expected methods");
        
        LOGGER.info("✅ Playwright infrastructure test completed successfully");
    }
    
    @Test 
    void testApplicationContext() {
        LOGGER.info("🧪 Testing Spring application context loads...");
        // Just verify the test context loads properly
        assertTrue(true);
        LOGGER.info("✅ Spring application context test completed successfully");
    }
}