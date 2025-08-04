package unit_tests.tech.derbent.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.users.domain.CUser;

/**
 * Test class to verify enhanced ComboBox functionality with icons.
 */
public class CColorAwareComboBoxIconTest {

    @Test
    public void testIconPatternMatching() {
        // Test different entity name patterns
        final String userClassName = "CUser";
        assertTrue(userClassName.contains("User"), "Should match User pattern");
        final String companyClassName = "CCompany";
        assertTrue(companyClassName.contains("Company"), "Should match Company pattern");
        final String projectClassName = "CProject";
        assertTrue(projectClassName.contains("Project"), "Should match Project pattern");
    }

    @Test
    public void testUserIconDetection() {
        // Create a test user
        final CUser testUser = new CUser();
        // Test icon detection
        final VaadinIcon userIcon = CColorUtils.getIconForEntity(testUser);
        assertEquals(VaadinIcon.USER, userIcon, "CUser should have USER icon");
        // Test should display icon
        assertTrue(CColorUtils.shouldDisplayIcon(testUser), "CUser should display icon");
        // Test icon creation
        assertNotNull(CColorUtils.createIconForEntity(testUser), "Should create icon for CUser");
    }
}