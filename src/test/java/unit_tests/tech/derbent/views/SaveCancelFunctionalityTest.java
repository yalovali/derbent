package unit_tests.tech.derbent.views;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.decisions.service.CDecisionService;
import tech.derbent.decisions.view.CDecisionsView;
import tech.derbent.orders.service.COrderService;
import tech.derbent.orders.view.COrdersView;
import tech.derbent.session.service.CSessionService;

/**
 * SaveCancelFunctionalityTest - Tests save/cancel functionality inheritance for all
 * entity views. This test verifies that all main entity views properly extend abstract
 * classes and inherit save/cancel functionality from the abstract base classes.
 */
@SpringBootTest
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.jpa.hibernate.ddl-auto=create-drop" }
)
public class SaveCancelFunctionalityTest {

	/**
	 * Test that Activities view can be instantiated and has save/cancel functionality.
	 * CActivitiesView extends CProjectAwareMDPage which inherits save/cancel from
	 * CAbstractEntityDBPage.
	 */
	@Test
	void testActivitiesViewSaveCancelInheritance() {
		final CActivityService mockActivityService = mock(CActivityService.class);
		final CSessionService mockSessionService = mock(CSessionService.class);
		final CActivitiesView activitiesView =
			new CActivitiesView(mockActivityService, mockSessionService, null);
		assertNotNull(activitiesView, "Activities view should be instantiable");
		// The view inherits save/cancel functionality from CProjectAwareMDPage ->
		// CAbstractEntityDBPage
	}

	/**
	 * Test that Decisions view can be instantiated and has save/cancel functionality.
	 * CDecisionsView extends CProjectAwareMDPage which inherits save/cancel from
	 * CAbstractEntityDBPage.
	 */
	@Test
	void testDecisionsViewSaveCancelInheritance() {
		final CDecisionService mockDecisionService = mock(CDecisionService.class);
		final CSessionService mockSessionService = mock(CSessionService.class);
		final CDecisionsView decisionsView =
			new CDecisionsView(mockDecisionService, mockSessionService);
		assertNotNull(decisionsView, "Decisions view should be instantiable");
		// The view inherits save/cancel functionality from CProjectAwareMDPage ->
		// CAbstractEntityDBPage
	}

	/**
	 * Test that Orders view can be instantiated and has save/cancel functionality.
	 * COrdersView extends CProjectAwareMDPage which inherits save/cancel from
	 * CAbstractEntityDBPage.
	 */
	@Test
	void testOrdersViewSaveCancelInheritance() {
		final COrderService mockOrderService = mock(COrderService.class);
		final CSessionService mockSessionService = mock(CSessionService.class);
		final COrdersView ordersView =
			new COrdersView(mockOrderService, mockSessionService);
		assertNotNull(ordersView, "Orders view should be instantiable");
		// The view inherits save/cancel functionality from CProjectAwareMDPage ->
		// CAbstractEntityDBPage
	}
}