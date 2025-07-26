package tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * Test class for CPanelUserBasicInfo to ensure proper field grouping.
 */
@ExtendWith(MockitoExtension.class)
class CPanelUserBasicInfoTest {

    @Mock
    private CUserService userService;

    private CPanelUserBasicInfo panel;
    private CUser testUser;
    private BeanValidationBinder<CUser> binder;

    @BeforeEach
    void setUp() {
        testUser = new CUser();
        testUser.setName("Test User");
        
        binder = new BeanValidationBinder<>(CUser.class);
    }

    @Test
    void testPanelCreation() {
        // When
        panel = new CPanelUserBasicInfo(testUser, binder, userService);
        
        // Then
        assertNotNull(panel, "Panel should be created successfully");
        assertNotNull(panel.getBaseLayout(), "Base layout should be initialized");
        assertEquals("Basic Information", panel.getAccordionTitle(), "Panel should have correct title");
    }

    @Test
    void testFieldGrouping() {
        // When
        panel = new CPanelUserBasicInfo(testUser, binder, userService);
        
        // Then
        assertNotNull(panel.getEntityFields(), "Entity fields should be set");
        assertEquals(3, panel.getEntityFields().size(), "Should have exactly 3 basic info fields");
        
        // Verify correct fields are included
        assertEquals("name", panel.getEntityFields().get(0), "First field should be name");
        assertEquals("lastname", panel.getEntityFields().get(1), "Second field should be lastname");
        assertEquals("login", panel.getEntityFields().get(2), "Third field should be login");
    }

    @Test
    void testPopulateFormWithValidEntity() {
        // Given
        panel = new CPanelUserBasicInfo(testUser, binder, userService);
        final CUser newUser = new CUser();
        newUser.setName("New User");
        
        // When
        panel.populateForm(newUser);
        
        // Then - Should not throw exception
        // Test passes if no exception is thrown
    }
}