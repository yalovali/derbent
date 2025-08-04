package unit_tests.tech.derbent.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.abstracts.components.CEntityLabel;
import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.users.domain.CUser;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.projects.domain.CProject;

/**
 * Test class for the enhanced entity label and icon functionality. Tests the new CEntityLabel component and expanded
 * icon support.
 */
public class CEntityLabelTest {

    private CUser testUser;
    private CCompany testCompany;
    private CProject testProject;

    @BeforeEach
    void setUp() {
        // Create test entities that are easy to construct
        testUser = new CUser();
        testUser.setName("Test User"); // Set name explicitly

        testCompany = new CCompany("Test Company");
        testProject = new CProject("Test Project");
    }

    @Test
    void testBasicEntitiesHaveIcons() {
        // Test that basic entity types now have icons
        assertNotNull(CColorUtils.getIconForEntity(testUser), "User should have an icon");
        assertNotNull(CColorUtils.getIconForEntity(testCompany), "Company should have an icon");
        assertNotNull(CColorUtils.getIconForEntity(testProject), "Project should have an icon");
    }

    @Test
    void testSpecificEntityIcons() {
        // Test specific icon assignments
        assertEquals(VaadinIcon.USER, CColorUtils.getIconForEntity(testUser), "User should have USER icon");
        assertEquals(VaadinIcon.BUILDING, CColorUtils.getIconForEntity(testCompany),
                "Company should have BUILDING icon");
        assertEquals(VaadinIcon.FOLDER, CColorUtils.getIconForEntity(testProject), "Project should have FOLDER icon");
    }

    @Test
    void testShouldDisplayIconForAllEntities() {
        // Test that shouldDisplayIcon returns true for all entities
        assertTrue(CColorUtils.shouldDisplayIcon(testUser), "Should display icon for User");
        assertTrue(CColorUtils.shouldDisplayIcon(testCompany), "Should display icon for Company");
        assertTrue(CColorUtils.shouldDisplayIcon(testProject), "Should display icon for Project");

        // Test null entity
        assertFalse(CColorUtils.shouldDisplayIcon(null), "Should not display icon for null entity");
    }

    @Test
    void testCreateIconForEntity() {
        // Test icon creation
        assertNotNull(CColorUtils.createIconForEntity(testUser), "Should create icon for User");
        assertNotNull(CColorUtils.createIconForEntity(testCompany), "Should create icon for Company");
        assertNotNull(CColorUtils.createIconForEntity(testProject), "Should create icon for Project");

        // Test null entity
        assertNull(CColorUtils.createIconForEntity(null), "Should not create icon for null entity");
    }

    @Test
    void testEntityLabelCreation() {
        // Test CEntityLabel creation with different entities
        CEntityLabel userLabel = new CEntityLabel(testUser);
        assertNotNull(userLabel, "Should create label for User");
        assertEquals(testUser, userLabel.getEntity(), "Label should contain the user entity");
        assertTrue(userLabel.hasIcon(), "User label should have an icon");

        CEntityLabel companyLabel = new CEntityLabel(testCompany);
        assertNotNull(companyLabel, "Should create label for Company");
        assertEquals(testCompany, companyLabel.getEntity(), "Label should contain the company entity");
        assertTrue(companyLabel.hasIcon(), "Company label should have an icon");

        // Test with null entity
        CEntityLabel nullLabel = new CEntityLabel(null);
        assertNotNull(nullLabel, "Should create label even for null entity");
        assertNull(nullLabel.getEntity(), "Label should have null entity");
        assertFalse(nullLabel.hasIcon(), "Null entity label should not have an icon");
    }

    @Test
    void testEntityLabelDisplayText() {
        CEntityLabel userLabel = new CEntityLabel(testUser);
        String displayText = userLabel.getDisplayText();
        assertNotNull(displayText, "Display text should not be null");
        assertTrue(displayText.contains("Test User"), "Display text should contain user name");

        CEntityLabel companyLabel = new CEntityLabel(testCompany);
        String companyDisplayText = companyLabel.getDisplayText();
        assertNotNull(companyDisplayText, "Company display text should not be null");
        assertTrue(companyDisplayText.contains("Test Company"), "Display text should contain company name");
    }

    @Test
    void testEntityLabelWithCustomStyling() {
        // Test CEntityLabel with custom styling
        CEntityLabel customLabel = new CEntityLabel(testUser, "10px 15px", false, false);
        assertNotNull(customLabel, "Should create label with custom styling");
        assertEquals(testUser, customLabel.getEntity(), "Custom label should contain the user entity");
        assertTrue(customLabel.hasIcon(), "Custom label should have an icon");
    }

    @Test
    void testEntityLabelRefresh() {
        CEntityLabel userLabel = new CEntityLabel(testUser);
        assertNotNull(userLabel, "Should create initial label");

        // Test refresh functionality
        assertDoesNotThrow(() -> userLabel.refresh(), "Refresh should not throw exception");
        assertEquals(testUser, userLabel.getEntity(), "Entity should remain the same after refresh");
    }

    @Test
    void testSimpleLabelCreation() {
        // Test simple label creation utility method
        var simpleLabel = CEntityLabel.createSimpleLabel(testUser);
        assertNotNull(simpleLabel, "Should create simple label for User");

        var nullSimpleLabel = CEntityLabel.createSimpleLabel(null);
        assertNotNull(nullSimpleLabel, "Should create simple label even for null");
        assertEquals("N/A", nullSimpleLabel.getText(), "Null simple label should show N/A");
    }

    @Test
    void testGenericEntityIconFallback() {
        // Create a simple test object to test the fallback behavior
        Object genericEntity = new Object() {
            @Override
            public String toString() {
                return "Generic Entity";
            }
        };

        // Should get a fallback icon for any object
        VaadinIcon icon = CColorUtils.getIconForEntity(genericEntity);
        assertEquals(VaadinIcon.RECORDS, icon, "Generic entity should get RECORDS fallback icon");

        assertTrue(CColorUtils.shouldDisplayIcon(genericEntity), "Should display icon for any entity");
        assertNotNull(CColorUtils.createIconForEntity(genericEntity), "Should create icon for any entity");
    }

    @Test
    void testEntityClassNamePatterns() {
        // Test different entity naming patterns to ensure icon detection works

        // Create mock objects with specific class names to test pattern matching
        class MockUserRole {
            @Override
            public String toString() {
                return "Admin";
            }
        }

        class MockOrderStatus {
            @Override
            public String toString() {
                return "Pending";
            }
        }

        class MockActivityType {
            @Override
            public String toString() {
                return "Task";
            }
        }

        MockUserRole userRole = new MockUserRole();
        MockOrderStatus orderStatus = new MockOrderStatus();
        MockActivityType activityType = new MockActivityType();

        // Test that pattern matching works
        assertEquals(VaadinIcon.USER_CARD, CColorUtils.getIconForEntity(userRole),
                "UserRole should get USER_CARD icon");
        assertEquals(VaadinIcon.CIRCLE, CColorUtils.getIconForEntity(orderStatus),
                "OrderStatus should get CIRCLE icon");
        assertEquals(VaadinIcon.LIST, CColorUtils.getIconForEntity(activityType), "ActivityType should get LIST icon");
    }
}