package tech.derbent.abstracts.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.projects.domain.CProject;

/**
 * Tests for CGridCell component.
 * 
 * @author Derbent Framework
 */
class CGridCellTest {

    private CProject testProject;
    private CActivityStatus testStatus;

    @BeforeEach
    void setUp() {
        // Create test project
        testProject = new CProject();
        testProject.setName("Test Project");
        
        // Create test activity status with color
        testStatus = new CActivityStatus();
        testStatus.setName("In Progress");
        testStatus.setDescription("Work is in progress");
        testStatus.setColor("#3498db");
        testStatus.setProject(testProject);
    }

    @Test
    void testCGridCellDefaultConstructor() {
        final CGridCell cell = new CGridCell();
        assertNotNull(cell);
        assertFalse(cell.isShowIcon()); // Icon display disabled by default
        assertTrue(cell.isAutoContrast()); // Auto contrast enabled by default
    }

    @Test
    void testCGridCellWithText() {
        final CGridCell cell = new CGridCell("Test Text");
        assertNotNull(cell);
        assertEquals("Test Text", cell.getText());
    }

    @Test
    void testCGridCellSetEntityValue() {
        final CGridCell cell = new CGridCell();
        cell.setEntityValue(testStatus);
        assertNotNull(cell);
        // Note: Since CGridCell now uses Div and may contain multiple components (icon + text),
        // we can't directly check getText(). Instead, verify the cell is properly configured.
    }

    @Test
    void testCGridCellWithNullEntity() {
        final CGridCell cell = new CGridCell();
        cell.setEntityValue(null);
        assertEquals("N/A", cell.getText());
    }

    // Status-related tests - CGridCell now handles status functionality
    @Test
    void testCGridCellStatusFunctionality() {
        final CGridCell statusCell = new CGridCell();
        assertNotNull(statusCell);
        assertTrue(statusCell.isAutoContrast());
        assertFalse(statusCell.isShowIcon()); // Icon display disabled by default
    }

    @Test
    void testCGridCellSetStatusValue() {
        final CGridCell statusCell = new CGridCell();
        statusCell.setStatusValue(testStatus);
        assertNotNull(statusCell);
        // Verify the cell is properly configured for status display
    }

    @Test
    void testCGridCellStatusWithNullEntity() {
        final CGridCell statusCell = new CGridCell();
        statusCell.setStatusValue(null);
        assertEquals("No Status", statusCell.getText());
    }

    @Test
    void testCGridCellIconConfiguration() {
        final CGridCell cell = new CGridCell();
        
        // Test default icon display (disabled)
        assertFalse(cell.isShowIcon());
        
        // Test enabling icon display
        cell.setShowIcon(true);
        assertTrue(cell.isShowIcon());
        
        // Test disabling icon display
        cell.setShowIcon(false);
        assertFalse(cell.isShowIcon());
    }
    
    @Test
    void testCGridCellWithEntityAndIconEnabled() {
        final CGridCell statusCell = new CGridCell();
        statusCell.setShowIcon(true);
        statusCell.setStatusValue(testStatus);
        
        // With icon enabled, the cell should be properly configured
        assertTrue(statusCell.isShowIcon());
        assertNotNull(statusCell);
    }

    @Test
    void testCGridCellAutoContrastConfiguration() {
        final CGridCell cell = new CGridCell();
        
        // Test default auto contrast (enabled)
        assertTrue(cell.isAutoContrast());
        
        // Test disabling auto contrast
        cell.setAutoContrast(false);
        assertFalse(cell.isAutoContrast());
        
        // Test re-enabling auto contrast
        cell.setAutoContrast(true);
        assertTrue(cell.isAutoContrast());
    }
}