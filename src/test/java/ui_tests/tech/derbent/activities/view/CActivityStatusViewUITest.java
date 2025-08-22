package ui_tests.tech.derbent.activities.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.activities.service.CActivityStatusService;
import tech.derbent.activities.view.CActivityStatusView;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

/** Test for CActivityStatusView to detect and prevent the binding issue that prevents the view from opening. This test specifically addresses the
 * issue mentioned in the problem statement: "the activity status view cannot be opened" */
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class CActivityStatusViewUITest {

	@Autowired
	private CActivityStatusService activityStatusService;
	@Autowired
	private CSessionService sessionService;
	@Autowired
	private CScreenService screenService;

	/** Test that CActivityStatusView can be instantiated without throwing binding errors. This test reproduces the exact issue: "All bindings created
	 * with forField must be completed before calling readBean" */
	@Test
	void testActivityStatusViewCanBeInstantiated() {
		// This should not throw java.lang.IllegalStateException: All bindings created
		// with forField must be completed before calling readBean
		assertDoesNotThrow(() -> {
			final CActivityStatusView view = new CActivityStatusView(activityStatusService, sessionService, screenService);
			assertNotNull(view, "CActivityStatusView should be instantiated successfully");
		}, "CActivityStatusView should be instantiated without binding errors");
	}

	/** Test that the view can create its details layout without errors. This simulates what happens when the view is navigated to. */
	@Test
	void testActivityStatusViewDetailsLayoutCreation() {
		assertDoesNotThrow(() -> {
			final CActivityStatusView view = new CActivityStatusView(activityStatusService, sessionService, screenService);
			// This simulates the layout creation that happens during navigation
			view.createDetailsLayout();
		}, "Details layout creation should not throw binding errors");
	}

	/** Test that reproduces the exact error: populate form after grid selection. This simulates selecting an item in the grid which triggers
	 * populateForm. */
	@Test
	void testActivityStatusViewPopulateForm() {
		assertDoesNotThrow(() -> {
			final CActivityStatusView view = new CActivityStatusView(activityStatusService, sessionService, screenService);
			view.createDetailsLayout();
			// Create a test entity
			final tech.derbent.activities.domain.CActivityStatus testStatus = new tech.derbent.activities.domain.CActivityStatus();
			testStatus.setName("Test Status");
			testStatus.setDescription("Test Description");
			// This should not throw "All bindings created with forField must be completed
			// before calling readBean"
			view.testPopulateForm(testStatus);
		}, "Populate form should not throw binding errors");
	}
}
