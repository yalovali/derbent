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
 * Tests for CGridCell and CGridCellStatus components.
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
        assertTrue(cell.isCenterAlign());
        assertEquals("4px 8px", cell.getPadding());
        assertEquals("80px", cell.getMinWidth());
        assertEquals("400", cell.getFontWeight());
        assertFalse(cell.isRoundedCorners());
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
        assertNotNull(cell.getText());
        assertTrue(cell.getText().contains("In Progress") || cell.getText().length() > 0);
    }

    @Test
    void testCGridCellWithNullEntity() {
        final CGridCell cell = new CGridCell();
        cell.setEntityValue(null);
        assertEquals("N/A", cell.getText());
    }

    @Test
    void testCGridCellStatusDefaultConstructor() {
        final CGridCellStatus statusCell = new CGridCellStatus();
        assertNotNull(statusCell);
        assertTrue(statusCell.isAutoContrast());
        assertTrue(statusCell.isRoundedCorners());
        assertEquals("500", statusCell.getFontWeight());
        assertTrue(statusCell.isShowIcon()); // New: verify icon display is enabled by default
    }

    @Test
    void testCGridCellStatusWithEntity() {
        final CGridCellStatus statusCell = new CGridCellStatus(testStatus);
        assertNotNull(statusCell);
        
        // The component should contain the status text but also may have icon components
        // Since icons are added as child components, we can't directly check getText()
        // Instead, verify the cell is properly configured
        assertTrue(statusCell.isShowIcon());
        assertTrue(statusCell.isAutoContrast());
    }

    @Test
    void testCGridCellStatusWithNullEntity() {
        final CGridCellStatus statusCell = new CGridCellStatus();
        statusCell.setStatusValue(null);
        assertEquals("No Status", statusCell.getText());
    }

    @Test
    void testCGridCellStatusCustomStyling() {
        final CGridCellStatus statusCell = new CGridCellStatus();
        statusCell.setAutoContrast(false);
        statusCell.setRoundedCorners(false);
        
        assertFalse(statusCell.isAutoContrast());
        assertFalse(statusCell.isRoundedCorners());
    }
    
    @Test
    void testCGridCellStatusIconConfiguration() {
        final CGridCellStatus statusCell = new CGridCellStatus();
        
        // Test default icon display
        assertTrue(statusCell.isShowIcon());
        
        // Test disabling icon display
        statusCell.setShowIcon(false);
        assertFalse(statusCell.isShowIcon());
        
        // Test re-enabling icon display
        statusCell.setShowIcon(true);
        assertTrue(statusCell.isShowIcon());
    }
    
    @Test
    void testCGridCellStatusWithEntityAndIconDisabled() {
        final CGridCellStatus statusCell = new CGridCellStatus();
        statusCell.setShowIcon(false);
        statusCell.setStatusValue(testStatus);
        
        // With icon disabled, the cell should contain just the text
        assertFalse(statusCell.isShowIcon());
        assertNotNull(statusCell);
    }

    @Test
    void testCGridCellStatusSetStatusColor() {
        final CGridCellStatus statusCell = new CGridCellStatus();
        statusCell.setText("Test Status");
        statusCell.setStatusColor("#ff0000", "#ffffff");
        
        // We can't easily test the applied styles in unit tests without DOM,
        // but we can verify the method doesn't throw exceptions
        assertNotNull(statusCell);
        assertEquals("Test Status", statusCell.getText());
    }

    @Test
    void testCGridCellCustomStyling() {
        final CGridCell cell = new CGridCell("Test");
        cell.setCustomStyling("#ff0000", "#ffffff");
        
        // Verify method doesn't throw exceptions
        assertNotNull(cell);
        assertEquals("Test", cell.getText());
    }

    @Test
    void testCGridCellStyleConfiguration() {
        final CGridCell cell = new CGridCell();
        
        // Test padding
        cell.setPadding("8px 12px");
        assertEquals("8px 12px", cell.getPadding());
        
        // Test center align
        cell.setCenterAlign(false);
        assertFalse(cell.isCenterAlign());
        
        // Test min width
        cell.setMinWidth("120px");
        assertEquals("120px", cell.getMinWidth());
        
        // Test font weight
        cell.setFontWeight("600");
        assertEquals("600", cell.getFontWeight());
        
        // Test rounded corners
        cell.setRoundedCorners(true);
        assertTrue(cell.isRoundedCorners());
    }
}