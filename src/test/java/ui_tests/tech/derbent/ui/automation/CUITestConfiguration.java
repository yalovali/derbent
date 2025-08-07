package ui_tests.tech.derbent.ui.automation;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;

/**
 * Test configuration to override beans that should not run during UI tests.
 * This prevents sample data initialization and other startup processes.
 */
@TestConfiguration
@Profile("uitest")
public class CUITestConfiguration {

    /**
     * Override any ApplicationRunner beans to prevent sample data initialization
     */
    @Bean
    @Primary
    public ApplicationRunner noOpApplicationRunner() {
        return args -> {
            // Do nothing - prevents any ApplicationRunner from executing
        };
    }
    
    /**
     * Override any CommandLineRunner beans to prevent initialization
     */
    @Bean
    @Primary  
    public CommandLineRunner noOpCommandLineRunner() {
        return args -> {
            // Do nothing - prevents any CommandLineRunner from executing
        };
    }
}