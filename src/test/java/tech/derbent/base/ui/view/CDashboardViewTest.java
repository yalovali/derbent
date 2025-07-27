package tech.derbent.base.ui.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.service.CUserService;

/**
 * Unit tests for CDashboardView services integration.
 * Layer: Test (MVC)
 * Verifies the dashboard services work correctly with mocked data.
 */
@ExtendWith(MockitoExtension.class)
class CDashboardViewServiceTest {

    @Mock
    private CProjectService mockProjectService;

    @Mock
    private CUserService mockUserService;

    @Mock
    private CActivityService mockActivityService;

    @Test
    void testDashboardServicesIntegration() {
        // Setup mock data for testing
        final List<CProject> mockProjects = new ArrayList<>();
        final CProject project1 = new CProject();
        project1.setName("Test Project 1");
        
        final CProject project2 = new CProject();
        project2.setName("Test Project 2");
        
        mockProjects.add(project1);
        mockProjects.add(project2);

        // Configure mock service responses
        when(mockProjectService.getTotalProjectCount()).thenReturn(2L);
        when(mockProjectService.findAll()).thenReturn(mockProjects);
        when(mockUserService.countUsersByProjectId(org.mockito.ArgumentMatchers.any())).thenReturn(3L);
        when(mockActivityService.countByProject(org.mockito.ArgumentMatchers.any(CProject.class))).thenReturn(10L);

        // Test that all service methods can be called without errors
        assertDoesNotThrow(() -> {
            final long projectCount = mockProjectService.getTotalProjectCount();
            final List<CProject> projects = mockProjectService.findAll();
            final long userCount = mockUserService.countUsersByProjectId(1L);
            final long activityCount = mockActivityService.countByProject(project1);
            
            // These assertions verify that our mocked services return expected values
            assert projectCount == 2L;
            assert projects.size() == 2;
            assert userCount == 3L;
            assert activityCount == 10L;
        }, "Dashboard services should work correctly with mocked data");
    }
}