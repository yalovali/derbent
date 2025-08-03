package tech.derbent.risks.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.risks.domain.CRiskStatus;

/**
 * Test class for CRiskStatusService functionality. Tests CRUD operations and entity creation.
 */
class CRiskStatusServiceTest extends CTestBase {

    @Override
    protected void setupForTest() {
        // Test setup if needed
    }

    @Test
    void testConstructor() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> new CRiskStatusService(riskStatusRepository, clock));
    }

    @Test
    void testCreateEntityWithValidName() {
        // Given
        final String validName = "IDENTIFIED";
        
        // When
        final CRiskStatus status = new CRiskStatus(validName, project);
        
        // Then
        assertNotNull(status);
        assertEquals(validName, status.getName());
        assertEquals(project, status.getProject());
    }

    @Test
    void testCreateEntityWithNameAndDescription() {
        // Given
        final String name = "MITIGATED";
        final String description = "Risk has been mitigated";
        final String color = "#00cc66";
        
        // When
        final CRiskStatus status = new CRiskStatus(name, project, description, color, false);
        
        // Then
        assertNotNull(status);
        assertEquals(name, status.getName());
        assertEquals(description, status.getDescription());
        assertEquals(project, status.getProject());
        assertEquals(false, status.isFinal());
    }

    @Test
    void testCreateEntityWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CRiskStatus(null, project);
        }, "Should throw IllegalArgumentException for null name");
    }

    @Test
    void testCreateEntityWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new CRiskStatus("", project);
        }, "Should throw IllegalArgumentException for empty name");
    }
}