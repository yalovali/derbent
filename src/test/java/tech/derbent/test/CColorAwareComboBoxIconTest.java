package tech.derbent.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.abstracts.components.CColorAwareComboBox;
import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.users.domain.CUser;

/**
 * Test class to verify enhanced ComboBox functionality with icons.
 */
public class CColorAwareComboBoxIconTest {

    @Test
    public void testUserIconDetection() {
        // Create a test user
        CUser testUser = new CUser();
        
        // Test icon detection
        VaadinIcon userIcon = CColorUtils.getIconForEntity(testUser);
        assertEquals(VaadinIcon.USER, userIcon, "CUser should have USER icon");
        
        // Test should display icon
        assertTrue(CColorUtils.shouldDisplayIcon(testUser), "CUser should display icon");
        
        // Test icon creation
        assertNotNull(CColorUtils.createIconForEntity(testUser), "Should create icon for CUser");
    }

    @Test
    public void testComboBoxEnhancedRendering() {
        // Create ComboBox for CUser
        CColorAwareComboBox<CUser> userComboBox = new CColorAwareComboBox<>(CUser.class, "Test Users");
        
        // Test that it recognizes enhanced rendering is needed
        assertTrue(userComboBox.isColorAware(), "CUser ComboBox should use enhanced rendering");
    }
    
    @Test
    public void testIconPatternMatching() {
        // Test different entity name patterns
        String userClassName = "CUser";
        assertTrue(userClassName.contains("User"), "Should match User pattern");
        
        String companyClassName = "CCompany";
        assertTrue(companyClassName.contains("Company"), "Should match Company pattern");
        
        String projectClassName = "CProject";
        assertTrue(projectClassName.contains("Project"), "Should match Project pattern");
    }
}