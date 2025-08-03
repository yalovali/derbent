package tech.derbent.base.ui.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.service.CUserService;

/**
 * Integration test for dashboard data availability. This test verifies that the database is properly initialized with
 * sample data and that the dashboard services can retrieve the data correctly.
 */
@SpringBootTest
@TestPropertySource(properties = { "spring.sql.init.mode=always", "spring.jpa.defer-datasource-initialization=true" })
@Transactional
class CDashboardViewIntegrationTest extends CTestBase {

    @Autowired
    private CProjectService projectService;

    @Autowired
    private CUserService userService;

    @Autowired
    private CActivityService activityService;

    @Test
    void testDashboardDataInitialization() {
        System.out.println("Testing dashboard data initialization...");
        // Test project count - should be 4 from CSampleDataInitializer
        final long projectCount = projectService.getTotalProjectCount();
        System.out.println("Project count: " + projectCount);
        assertTrue(projectCount > 0, "Project count should be greater than 0");
        // Test project list retrieval
        final List<CProject> projects = projectService.findAll();
        assertNotNull(projects, "Projects list should not be null");
        System.out.println("Projects found: " + projects.size());
        assertTrue(projects.size() > 0, "Should have at least one project");

        // Log each project
        for (final CProject project : projects) {
            System.out.println("  - " + project.getName() + " (ID: " + project.getId() + ")");
            // Test user count for this project
            final long userCount = userService.countUsersByProjectId(project.getId());
            System.out.println("    Users: " + userCount);
            // Test activity count for this project
            final long activityCount = activityService.countByProject(project);
            System.out.println("    Activities: " + activityCount);
        }
        // Verify we have the expected projects from CSampleDataInitializer
        final boolean hasDigitalTransformation = projects.stream()
                .anyMatch(p -> "Digital Transformation Initiative".equals(p.getName()));
        assertTrue(hasDigitalTransformation,
                "Should have 'Digital Transformation Initiative' from CSampleDataInitializer");
        final boolean hasProductDevelopment = projects.stream()
                .anyMatch(p -> "Product Development Phase 2".equals(p.getName()));
        assertTrue(hasProductDevelopment, "Should have 'Product Development Phase 2' from CSampleDataInitializer");
        final boolean hasInfrastructureModernization = projects.stream()
                .anyMatch(p -> "Infrastructure Modernization".equals(p.getName()));
        assertTrue(hasInfrastructureModernization,
                "Should have 'Infrastructure Modernization' from CSampleDataInitializer");
        final boolean hasCustomerExperience = projects.stream()
                .anyMatch(p -> "Customer Experience Enhancement".equals(p.getName()));
        assertTrue(hasCustomerExperience, "Should have 'Customer Experience Enhancement' from CSampleDataInitializer");
    }

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub

    }
}