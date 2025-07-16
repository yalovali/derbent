package tech.derbent.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tech.derbent.security.service.CLoginService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * Data initialization for CLogin-based authentication system.
 * Creates default admin and user accounts if they don't exist.
 */
@Configuration
@ConditionalOnProperty(name = "application.security.use-clogin", havingValue = "true")
public class CLoginDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(CLoginDataInitializer.class);

    @Bean
    public ApplicationRunner initializeDefaultUsers(CLoginService loginService, CUserService userService) {
        return args -> {
            log.info("Initializing default login users...");
            
            // Create admin user if doesn't exist
            if (!loginService.existsByUsername("admin")) {
                CUser adminUser = new CUser();
                adminUser.setName("Admin");
                adminUser.setLastname("User");
                adminUser.setEmail("admin@derbent.tech");
                adminUser.setLogin("admin");
                adminUser.setPhone("+1234567890");
                userService.save(adminUser);
                
                var adminLogin = loginService.createLogin("admin", "admin123", "ADMIN,USER");
                adminLogin.setUser(adminUser);
                loginService.save(adminLogin);
                
                log.info("Created default admin user: username=admin, password=admin123");
            }
            
            // Create regular user if doesn't exist
            if (!loginService.existsByUsername("user")) {
                CUser regularUser = new CUser();
                regularUser.setName("Regular");
                regularUser.setLastname("User");
                regularUser.setEmail("user@derbent.tech");
                regularUser.setLogin("user");
                regularUser.setPhone("+1234567891");
                userService.save(regularUser);
                
                var userLogin = loginService.createLogin("user", "user123", "USER");
                userLogin.setUser(regularUser);
                loginService.save(userLogin);
                
                log.info("Created default regular user: username=user, password=user123");
            }
            
            log.info("Default users initialization completed");
        };
    }
}