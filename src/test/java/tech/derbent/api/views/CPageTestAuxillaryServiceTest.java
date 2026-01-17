package tech.derbent.api.views;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import tech.derbent.Application;


@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CPageTestAuxillaryServiceTest {

    @Autowired
    private CPageTestAuxillaryService service;

    @Test
    void testServiceInitialization() {
        assertNotNull(service, "CPageTestAuxillaryService should be autowired");
    }

    @Test
    void testUserIconTestRouteIsRegistered() {
        assertNotNull(service.getRoutes(), "Routes list should not be null");
        assertFalse(service.getRoutes().isEmpty(), "Routes list should not be empty");
        
        boolean foundUserIconTest = service.getRoutes().stream()
            .anyMatch(route -> "User Icon Test".equals(route.title) 
                            && "user-icon-test".equals(route.route));
        
        assertTrue(foundUserIconTest, "User Icon Test route should be registered");
        
        System.out.println("=== Registered Routes ===");
        service.getRoutes().forEach(route -> {
            System.out.println("Title: " + route.title + ", Route: " + route.route + 
                             ", Icon: " + route.iconName + ", Color: " + route.iconColor);
        });
    }
}
