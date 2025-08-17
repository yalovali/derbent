package tech.derbent.screens.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;

@ExtendWith(MockitoExtension.class)
class CScreenServiceTest {

    @Mock
    private CScreenRepository screenRepository;

    @Mock
    private Clock clock;

    private CScreenService screenService;

    @BeforeEach
    void setUp() {
        screenService = new CScreenService(screenRepository, clock);
    }

    @Test
    void testNewEntityWithNameAndProject() {
        // Given
        final String screenName = "Test Screen";
        final CProject project = new CProject("Test Project");

        // When
        final CScreen screen = screenService.newEntity(screenName, project);

        // Then
        assertNotNull(screen);
        assertEquals(screenName, screen.getName());
        assertEquals(project, screen.getProject());
        assertTrue(screen.getIsActive());
    }

    @Test
    void testNewEntityWithNameOnly() {
        // Given
        final String screenName = "Test Screen";

        // When
        final CScreen screen = screenService.newEntity(screenName);

        // Then
        assertNotNull(screen);
        assertEquals(screenName, screen.getName());
        assertTrue(screen.getIsActive());
    }

    @Test
    void testFindByProjectAndEntityType() {
        // Given
        final CProject project = new CProject("Test Project");
        final String entityType = "CActivity";
        final List<CScreen> expectedScreens = Arrays.asList(new CScreen("Screen 1", project),
                new CScreen("Screen 2", project));

        when(screenRepository.findByProjectAndEntityType(project, entityType)).thenReturn(expectedScreens);

        // When
        final List<CScreen> actualScreens = screenService.findByProjectAndEntityType(project, entityType);

        // Then
        assertEquals(expectedScreens, actualScreens);
        verify(screenRepository).findByProjectAndEntityType(project, entityType);
    }

    @Test
    void testFindActiveByProject() {
        // Given
        final CProject project = new CProject("Test Project");
        final List<CScreen> expectedScreens = Arrays.asList(new CScreen("Active Screen 1", project),
                new CScreen("Active Screen 2", project));

        when(screenRepository.findActiveByProject(project)).thenReturn(expectedScreens);

        // When
        final List<CScreen> actualScreens = screenService.findActiveByProject(project);

        // Then
        assertEquals(expectedScreens, actualScreens);
        verify(screenRepository).findActiveByProject(project);
    }

    @Test
    void testGetEntityClass() {
        // When
        final Class<CScreen> entityClass = screenService.getEntityClass();

        // Then
        assertEquals(CScreen.class, entityClass);
    }
}