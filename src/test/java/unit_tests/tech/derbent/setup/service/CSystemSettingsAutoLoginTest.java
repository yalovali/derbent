package unit_tests.tech.derbent.setup.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.session.service.CSessionService;
import tech.derbent.setup.domain.CSystemSettings;
import tech.derbent.setup.service.CSystemSettingsRepository;
import tech.derbent.setup.service.CSystemSettingsService;

/**
 * Unit tests for auto-login functionality in CSystemSettingsService.
 * Tests the new auto-login settings management features.
 */
@ExtendWith(MockitoExtension.class)
class CSystemSettingsAutoLoginTest {

    @Mock
    private CSystemSettingsRepository repository;

    @Mock
    private Clock clock;

    @Mock
    private CSessionService sessionService;

    private CSystemSettingsService service;
    private CSystemSettings testSettings;

    @BeforeEach
    void setUp() throws Exception {
        service = new CSystemSettingsService(repository, clock, sessionService);
        testSettings = new CSystemSettings();
        
        // Use reflection to set ID since it's private with no setter
        java.lang.reflect.Field idField = testSettings.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(testSettings, 1L);
        
        testSettings.setAutoLoginEnabled(false);
        testSettings.setDefaultLoginView("home");
    }

    @Test
    void testIsAutoLoginEnabled_WhenEnabled_ReturnsTrue() {
        // Arrange
        testSettings.setAutoLoginEnabled(true);
        when(repository.findSystemSettings()).thenReturn(Optional.of(testSettings));

        // Act
        boolean result = service.isAutoLoginEnabled();

        // Assert
        assertTrue(result);
        verify(repository).findSystemSettings();
    }

    @Test
    void testIsAutoLoginEnabled_WhenDisabled_ReturnsFalse() {
        // Arrange
        testSettings.setAutoLoginEnabled(false);
        when(repository.findSystemSettings()).thenReturn(Optional.of(testSettings));

        // Act
        boolean result = service.isAutoLoginEnabled();

        // Assert
        assertFalse(result);
        verify(repository).findSystemSettings();
    }

    @Test
    void testIsAutoLoginEnabled_WhenNoSettings_ReturnsFalse() {
        // Arrange
        when(repository.existsSystemSettings()).thenReturn(false);
        when(repository.saveAndFlush(any(CSystemSettings.class))).thenReturn(testSettings);

        // Act
        boolean result = service.isAutoLoginEnabled();

        // Assert
        assertFalse(result);
        verify(repository).saveAndFlush(any(CSystemSettings.class));
    }

    @Test
    void testGetDefaultLoginView_ReturnsCorrectView() {
        // Arrange
        testSettings.setDefaultLoginView("cprojectsview");
        when(repository.findSystemSettings()).thenReturn(Optional.of(testSettings));

        // Act
        String result = service.getDefaultLoginView();

        // Assert
        assertEquals("cprojectsview", result);
        verify(repository).findSystemSettings();
    }

    @Test
    void testGetDefaultLoginView_WhenNull_ReturnsHome() {
        // Arrange
        testSettings.setDefaultLoginView(null);
        when(repository.findSystemSettings()).thenReturn(Optional.of(testSettings));

        // Act
        String result = service.getDefaultLoginView();

        // Assert
        assertEquals("home", result);
        verify(repository).findSystemSettings();
    }

    @Test
    void testUpdateAutoLoginSettings_SavesCorrectly() {
        // Arrange
        when(repository.findSystemSettings()).thenReturn(Optional.of(testSettings));
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.saveAndFlush(any(CSystemSettings.class))).thenReturn(testSettings);

        // Act
        CSystemSettings result = service.updateAutoLoginSettings(true, "cactivitiesview");

        // Assert
        assertNotNull(result);
        verify(repository).findSystemSettings();
        verify(repository).saveAndFlush(any(CSystemSettings.class));
    }

    @Test
    void testUpdateAutoLoginSettings_TrimsViewName() {
        // Arrange
        when(repository.findSystemSettings()).thenReturn(Optional.of(testSettings));
        when(repository.existsById(1L)).thenReturn(true);
        when(repository.saveAndFlush(any(CSystemSettings.class))).thenAnswer(invocation -> {
            CSystemSettings settings = invocation.getArgument(0);
            assertEquals("cmeetingsview", settings.getDefaultLoginView());
            assertEquals(true, settings.getAutoLoginEnabled());
            return settings;
        });

        // Act
        service.updateAutoLoginSettings(true, "  cmeetingsview  ");

        // Assert
        verify(repository).saveAndFlush(any(CSystemSettings.class));
    }

    @Test
    void testAutoLoginDefaultValues() {
        // Test that new CSystemSettings instance has correct defaults
        CSystemSettings newSettings = new CSystemSettings();
        
        assertFalse(newSettings.getAutoLoginEnabled());
        assertEquals("home", newSettings.getDefaultLoginView());
    }
}