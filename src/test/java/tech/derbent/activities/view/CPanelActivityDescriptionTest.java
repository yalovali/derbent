package tech.derbent.activities.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

/**
 * Test class for CPanelActivityDescription to ensure it follows the same pattern as CPanelUserDescription.
 * Updated to use annotation-based data provider resolution.
 */
@ExtendWith(MockitoExtension.class)
class CPanelActivityDescriptionTest {

    @Mock
    private CActivityService activityService;

    private CPanelActivityDescription panel;
    private CActivity testActivity;
    private BeanValidationBinder<CActivity> binder;

    @BeforeEach
    void setUp() {
        testActivity = new CActivity();
        testActivity.setName("Test Activity");
        
        binder = new BeanValidationBinder<>(CActivity.class);
    }

    @Test
    void testPanelCreation() {
        // When
        panel = new CPanelActivityDescription(testActivity, binder, activityService);
        
        // Then
        assertNotNull(panel, "Panel should be created successfully");
        assertNotNull(panel.getBaseLayout(), "Base layout should be initialized");
    }

    @Test
    void testPopulateFormWithNullEntity() {
        // Given
        panel = new CPanelActivityDescription(testActivity, binder, activityService);
        
        // When - Should not throw exception when entity is null
        panel.populateForm(null);
        
        // Then - No exception should be thrown
        // Test passes if no exception is thrown
    }

    @Test
    void testPopulateFormWithValidEntity() {
        // Given
        panel = new CPanelActivityDescription(testActivity, binder, activityService);
        final CActivity newActivity = new CActivity();
        newActivity.setName("New Activity");
        
        // When
        panel.populateForm(newActivity);
        
        // Then - Should not throw exception
        // Test passes if no exception is thrown
    }

    @Test
    void testSaveEventHandler() {
        // Given
        panel = new CPanelActivityDescription(testActivity, binder, activityService);
        
        // When - Should not throw exception
        panel.saveEventHandler();
        
        // Then - Test passes if no exception is thrown
    }
}