package unit_tests.tech.derbent.activities.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.view.CPanelActivityDescription;
import tech.derbent.session.service.CSessionService;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CPanelActivityDescription to ensure it follows the same pattern as CPanelUserDescription. Updated to
 * use annotation-based data provider resolution.
 */
@ExtendWith(MockitoExtension.class)
class CPanelActivityDescriptionTest extends CTestBase {

    @Mock
    private CActivityService activityService;

    @Mock
    private CSessionService sessionService;

    private CAccordionDBEntity<CActivity> panel;

    private CActivity testActivity;

    private CEnhancedBinder<CActivity> binder;

    @Override
    protected void setupForTest() {
        testActivity = new CActivity("Test Activity", project);
        testActivity.setName("Test Activity");
        binder = new CEnhancedBinder<>(CActivity.class);
    }

    @Test
    void testPanelCreation()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        // When
        panel = new CPanelActivityDescription(testActivity, binder, activityService);
        // Then
        assertNotNull(panel, "Panel should be created successfully");
    }

    @Test
    void testPopulateFormWithNullEntity()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        // Given
        panel = new CPanelActivityDescription(testActivity, binder, activityService);
        panel.populateForm(null);
    }

    @Test
    void testPopulateFormWithValidEntity()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        panel = new CPanelActivityDescription(testActivity, binder, activityService);
        final CActivity newActivity = new CActivity("Test Activity", project);
        newActivity.setName("New Activity");
        panel.populateForm(newActivity);
    }

    @Test
    void testSaveEventHandler()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        panel = new CPanelActivityDescription(testActivity, binder, activityService);
        panel.saveEventHandler();
    }
}