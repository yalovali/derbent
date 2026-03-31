package automated_tests.tech.derbent.ui.automation.debug;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.Application;

/**
 * Simple test to debug Spring Boot startup issues in test context.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource(properties = {
    "spring.profiles.active=test",
    "server.port=0",  // Let Spring Boot pick an available port
    "logging.level.org.springframework.boot=DEBUG",
    "logging.level.org.springframework.boot.web=DEBUG",
    "logging.level.org.apache.catalina=DEBUG"
})
public class CSimpleStartupTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSimpleStartupTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    public void testApplicationStartup() {
        LOGGER.info("🚀 Test started!");
        LOGGER.info("📡 Server port: {}", port);
        LOGGER.info("🌐 Application context: {}", applicationContext != null ? "LOADED" : "NULL");
        
        // Log available beans
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        LOGGER.info("📦 Total beans loaded: {}", beanNames.length);
        
        // Try to access localhost
        String url = "http://localhost:" + port + "/";
        LOGGER.info("🔗 Application should be available at: {}", url);
        
        LOGGER.info("✅ Test completed successfully!");
    }
}