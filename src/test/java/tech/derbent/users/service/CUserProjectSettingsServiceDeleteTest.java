package tech.derbent.users.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;

/**
 * Unit test for CUserProjectSettingsService delete functionality. Tests that the delete operation properly calls the
 * repository with transaction support.
 */
@ExtendWith(MockitoExtension.class)
class CUserProjectSettingsServiceDeleteTest {

    @Mock
    private CUserProjectSettingsRepository repository;

    @Mock
    private Clock clock;

    private CUserProjectSettingsService service;

    private CUserProjectSettings createTestUserProjectSettings() {
        final CUserProjectSettings settings = new CUserProjectSettings();
        // Create mock user and project
        final CUser user = mock(CUser.class);
        final CProject project = mock(CProject.class);
        settings.setUser(user);
        settings.setProject(project);
        settings.setRole("Developer");
        settings.setPermission("READ_WRITE");

        // Use reflection to set the ID since it's protected
        try {
            final java.lang.reflect.Field idField = settings.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(settings, 1L);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to set ID for test", e);
        }
        return settings;
    }

    @BeforeEach
    void setUp() {
        service = new CUserProjectSettingsService(repository, clock);
    }

    @Test
    void testDeleteEntity_ShouldCallRepositoryDeleteById() {
        // Given
        final CUserProjectSettings settings = createTestUserProjectSettings();
        doNothing().when(repository).deleteById(anyLong());
        // When
        service.delete(settings);
        // Then
        verify(repository).deleteById(settings.getId());
    }

    @Test
    void testDeleteEntity_WithNullEntity_ShouldNotCallRepository() {
        // Given
        final CUserProjectSettings settings = null;
        // When
        service.delete(settings);
        // Then - no exception should be thrown and repository should not be called This
        // tests the null check in the delete method
        verify(repository, org.mockito.Mockito.never()).deleteById(anyLong());
    }

    @Test
    void testDeleteEntity_WithNullId_ShouldNotCallRepository() {
        // Given
        final CUserProjectSettings settings = new CUserProjectSettings();
        // settings.setId(null); // ID is null by default
        // When
        service.delete(settings);
        // Then - no exception should be thrown and repository should not be called This
        // tests the null ID check in the delete method
        verify(repository, org.mockito.Mockito.never()).deleteById(anyLong());
    }
}