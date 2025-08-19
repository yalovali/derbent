package tech.derbent.screens.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Clock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;

@ExtendWith(MockitoExtension.class)
class CScreenLinesServiceTest {

    @Mock
    private CScreenLinesRepository screenLinesRepository;

    @Mock
    private Clock clock;

    private CScreenLinesService screenLinesService;

    @BeforeEach
    void setUp() {
        screenLinesService = new CScreenLinesService(screenLinesRepository, clock);
    }

    @Test
    void testGetEntityClass() {
        // When
        final Class<CScreenLines> entityClass = screenLinesService.getEntityClass();
        // Then
        assertEquals(CScreenLines.class, entityClass);
    }

    @Test
    void testNewEntitySetsDefaultMaxLength() {
        // Given
        final CProject project = new CProject("Test Project");
        final CScreen screen = new CScreen("Test Screen", project);
        final String fieldCaption = "Test Field";
        final String entityProperty = "testField";
        // Mock the getNextLineOrder method
        when(screenLinesRepository.getNextLineOrder(screen)).thenReturn(1);
        // When
        final CScreenLines screenLine = screenLinesService.newEntity(screen, fieldCaption, entityProperty);
        // Then
        assertNotNull(screenLine);
        assertEquals(screen, screenLine.getScreen());
        assertEquals(fieldCaption, screenLine.getFieldCaption());
        assertEquals(entityProperty, screenLine.getEntityProperty());
        assertEquals(Integer.valueOf(255), screenLine.getMaxLength()); // This should be
                                                                       // set to default
                                                                       // value
        assertEquals(Integer.valueOf(1), screenLine.getLineOrder());
        assertTrue(screenLine.getIsActive());
    }

    @Test
    void testNewEntityValidation() {
        // Given
        final CProject project = new CProject("Test Project");
        final CScreen screen = new CScreen("Test Screen", project);
        final String fieldCaption = "Test Field";
        final String entityFieldName = "testField";
        when(screenLinesRepository.getNextLineOrder(screen)).thenReturn(1);
        // When
        final CScreenLines screenLine = screenLinesService.newEntity(screen, fieldCaption, entityFieldName);
        // Then - verify that maxLength satisfies validation constraints
        assertNotNull(screenLine.getMaxLength());
        assertTrue(screenLine.getMaxLength() >= 1, "Max length should be at least 1");
        assertTrue(screenLine.getMaxLength() <= 10000, "Max length should not exceed 10000");
    }
}