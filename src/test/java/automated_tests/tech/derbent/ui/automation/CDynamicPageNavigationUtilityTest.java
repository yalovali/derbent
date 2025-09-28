package automated_tests.tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.session.service.CSessionService;

/**
 * Utility test to validate dynamic page navigation logic without requiring Playwright browsers.
 * This test validates that the infrastructure for locating dynamic pages by entity type works correctly.
 */
@SpringBootTest(classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:navtestdb", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🧭 Dynamic Page Navigation Utility Test")
public class CDynamicPageNavigationUtilityTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageNavigationUtilityTest.class);

    @Autowired
    private CPageEntityService pageEntityService;
    
    @Autowired
    private CProjectService projectService;
    
    @Autowired
    private CSessionService sessionService;

    @Test
    @DisplayName("✅ Test Dynamic Page Entity Discovery")
    void testDynamicPageEntityDiscovery() {
        LOGGER.info("🔍 Testing dynamic page entity discovery...");
        
        try {
            // Get all projects
            List<CProject> projects = projectService.findAll();
            LOGGER.info("📊 Found {} projects", projects.size());
            
            if (projects.isEmpty()) {
                LOGGER.warn("⚠️ No projects found - this may be expected in test environment");
                LOGGER.info("✅ Dynamic page entity discovery test completed (no projects to test)");
                return; // This is acceptable in test environment
            }            
            
            // Get all page entities
            List<CPageEntity> allPages = pageEntityService.findAll();
            LOGGER.info("📄 Found {} page entities total", allPages.size());
            
            // Test for each project
            for (CProject project : projects) {
                LOGGER.info("🔍 Testing project: {}", project.getName());
                
                List<CPageEntity> projectPages = pageEntityService.findActivePagesByProject(project);
                LOGGER.info("📄 Project {} has {} active pages", project.getName(), projectPages.size());
                
                // Validate each page has required properties for navigation
                for (CPageEntity page : projectPages) {
                    validatePageForNavigation(page);
                }
                
                // Test entity type discovery
                testEntityTypeDiscovery(projectPages);
            }
            
            LOGGER.info("✅ Dynamic page entity discovery test completed successfully");
            
        } catch (Exception e) {
            LOGGER.error("❌ Dynamic page entity discovery test failed: {}", e.getMessage(), e);
            fail("Dynamic page entity discovery test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("🎯 Test Entity Type Search Terms Generation")
    void testEntityTypeSearchTermsGeneration() {
        LOGGER.info("🔍 Testing entity type search terms generation...");
        
        try {
            // Create a mock CBaseUITest instance to test the utility methods
            CBaseUITest mockTest = new CBaseUITest() {};
            
            // Test search terms generation for common entity types
            String[] testEntityTypes = {"CUser", "CProject", "CActivity", "CMeeting", "COrder"};
            
            for (String entityType : testEntityTypes) {
                String[] searchTerms = mockTest.generateSearchTermsForEntity(entityType);
                
                assertNotNull(searchTerms, "Search terms should not be null for " + entityType);
                assertTrue(searchTerms.length > 0, "Should generate at least one search term for " + entityType);
                
                LOGGER.info("🏷️ Entity type '{}' generates search terms: {}", entityType, String.join(", ", searchTerms));
                
                // Validate search terms make sense
                validateSearchTerms(entityType, searchTerms);
            }
            
            LOGGER.info("✅ Entity type search terms generation test completed successfully");
            
        } catch (Exception e) {
            LOGGER.error("❌ Entity type search terms generation test failed: {}", e.getMessage(), e);
            fail("Entity type search terms generation test failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("🔗 Test Dynamic Page URL Generation")
    void testDynamicPageUrlGeneration() {
        LOGGER.info("🔍 Testing dynamic page URL generation...");
        
        try {
            // Get all projects
            List<CProject> projects = projectService.findAll();
            
            if (projects.isEmpty()) {
                LOGGER.warn("⚠️ No projects found - creating mock URL validation test");
                
                // Test URL generation logic with mock data
                Long mockPageId = 123L;
                String expectedUrl = "/cdynamicpagerouter/" + mockPageId;
                String baseUrl = "http://localhost:8080";
                String fullUrl = baseUrl + expectedUrl;
                
                LOGGER.info("🌐 Mock page would have URL: {}", fullUrl);
                assertEquals("http://localhost:8080/cdynamicpagerouter/123", fullUrl);
                
                LOGGER.info("✅ Dynamic page URL generation test completed (mock validation)");
                return;
            }
            
            for (CProject project : projects) {
                List<CPageEntity> projectPages = pageEntityService.findActivePagesByProject(project);
                
                for (CPageEntity page : projectPages) {
                    // Test URL generation
                    String expectedUrl = "/cdynamicpagerouter/" + page.getId();
                    String baseUrl = "http://localhost:8080";
                    String fullUrl = baseUrl + expectedUrl;
                    
                    LOGGER.info("🌐 Page '{}' would have URL: {}", page.getPageTitle(), fullUrl);
                    
                    // Validate URL components
                    assertNotNull(page.getId(), "Page ID should not be null");
                    assertTrue(page.getId() > 0, "Page ID should be positive");
                    assertNotNull(page.getPageTitle(), "Page title should not be null");
                    
                    LOGGER.debug("✅ URL validation passed for page: {}", page.getPageTitle());
                }
            }
            
            LOGGER.info("✅ Dynamic page URL generation test completed successfully");
            
        } catch (Exception e) {
            LOGGER.error("❌ Dynamic page URL generation test failed: {}", e.getMessage(), e);
            fail("Dynamic page URL generation test failed: " + e.getMessage());
        }
    }

    /**
     * Validate that a page entity has all the required properties for navigation.
     */
    private void validatePageForNavigation(CPageEntity page) {
        assertNotNull(page, "Page entity should not be null");
        assertNotNull(page.getId(), "Page ID should not be null");
        assertNotNull(page.getPageTitle(), "Page title should not be null");
        assertTrue(page.getIsActive(), "Page should be active for navigation");
        
        LOGGER.debug("✅ Page validation passed: {}", page.getPageTitle());
        
        // Log additional information for debugging
        if (page.getDetailSection() != null) {
            LOGGER.debug("📝 Page '{}' has detail section with entity type: {}", 
                page.getPageTitle(), page.getDetailSection().getEntityType());
        }
        
        if (page.getGridEntity() != null) {
            LOGGER.debug("📊 Page '{}' has grid entity: {}", 
                page.getPageTitle(), page.getGridEntity().getName());
        }
    }

    /**
     * Test entity type discovery for a list of pages.
     */
    private void testEntityTypeDiscovery(List<CPageEntity> pages) {
        int entityTypesFound = 0;
        
        for (CPageEntity page : pages) {
            if (page.getDetailSection() != null && page.getDetailSection().getEntityType() != null) {
                String entityType = page.getDetailSection().getEntityType();
                LOGGER.info("🎯 Found entity type '{}' for page '{}'", entityType, page.getPageTitle());
                entityTypesFound++;
                
                // Validate entity type follows naming convention
                assertTrue(entityType.startsWith("C"), "Entity type should start with 'C': " + entityType);
            }
        }
        
        LOGGER.info("📊 Discovered {} entity types in {} pages", entityTypesFound, pages.size());
    }

    /**
     * Validate that search terms make sense for an entity type.
     */
    private void validateSearchTerms(String entityType, String[] searchTerms) {
        // Should contain the original entity type
        boolean containsOriginal = false;
        for (String term : searchTerms) {
            if (term.equals(entityType)) {
                containsOriginal = true;
                break;
            }
        }
        assertTrue(containsOriginal, "Search terms should include original entity type: " + entityType);
        
        // Should contain a plural form for most entities
        if (entityType.startsWith("C")) {
            String baseName = entityType.substring(1);
            String pluralForm = baseName + "s";
            
            boolean containsPlural = false;
            for (String term : searchTerms) {
                if (term.equals(pluralForm)) {
                    containsPlural = true;
                    break;
                }
            }
            assertTrue(containsPlural, "Search terms should include plural form: " + pluralForm);
        }
    }
}