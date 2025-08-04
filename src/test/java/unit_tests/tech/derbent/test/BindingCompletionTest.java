package unit_tests.tech.derbent.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.meetings.domain.CMeetingStatus;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class to verify that CEntityFormBuilder properly completes all forField bindings
 * for status entities before readBean is called, preventing IllegalStateException.
 */
@ExtendWith (MockitoExtension.class)
class BindingCompletionTest extends CTestBase {

	@Override
	@BeforeEach
	protected void setupForTest() {
		// Mock Vaadin environment
		final VaadinRequest request = mock(VaadinRequest.class);
		final VaadinService service = mock(VaadinService.class);
		final VaadinSession session = mock(VaadinSession.class);
		VaadinSession.setCurrent(session);
		UI.setCurrent(new UI());
	}

	@Test
	void testActivityStatusFormBindingCompletion() {
		assertDoesNotThrow(() -> {
			final var binder = CBinderFactory.createEnhancedBinder(CActivityStatus.class);
			final var formLayout =
				CEntityFormBuilder.buildForm(CActivityStatus.class, binder);
			assertNotNull(formLayout, "Form layout should be created");
			// Create test entity
			final var testEntity = new CActivityStatus();
			testEntity.setName("Test Status");
			testEntity.setDescription("Test Description");
			testEntity.setColor("#00FF00");
			testEntity.setSortOrder(1);
			testEntity.setFinalStatus(false);
			// This should not throw IllegalStateException
			binder.readBean(testEntity);
		}, "Activity status form binding should complete all forField bindings");
	}

	@Test
	void testActivityStatusFormWriteBack() {
		assertDoesNotThrow(() -> {
			final var binder = CBinderFactory.createEnhancedBinder(CActivityStatus.class);
			final var formLayout =
				CEntityFormBuilder.buildForm(CActivityStatus.class, binder);
			assertNotNull(formLayout, "Form layout should be created");
			// Create test entity
			final var testEntity = new CActivityStatus();
			binder.readBean(testEntity);
			// This should not throw IllegalStateException during write back
			binder.writeBean(testEntity);
		}, "Activity status form should allow write back without binding errors");
	}

	@Test
	void testMeetingStatusFormBindingCompletion() {
		assertDoesNotThrow(() -> {
			final var binder = CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			final var formLayout =
				CEntityFormBuilder.buildForm(CMeetingStatus.class, binder);
			assertNotNull(formLayout, "Form layout should be created");
			// Create test entity
			final var testEntity = new CMeetingStatus();
			testEntity.setName("Test Status");
			testEntity.setDescription("Test Description");
			testEntity.setColor("#FF0000");
			testEntity.setSortOrder(1);
			testEntity.setFinalStatus(false);
			// This should not throw IllegalStateException
			binder.readBean(testEntity);
		}, "Meeting status form binding should complete all forField bindings");
	}

	@Test
	void testMeetingStatusFormWriteBack() {
		assertDoesNotThrow(() -> {
			final var binder = CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			final var formLayout =
				CEntityFormBuilder.buildForm(CMeetingStatus.class, binder);
			assertNotNull(formLayout, "Form layout should be created");
			// Create test entity
			final var testEntity = new CMeetingStatus();
			binder.readBean(testEntity);
			// This should not throw IllegalStateException during write back
			binder.writeBean(testEntity);
		}, "Meeting status form should allow write back without binding errors");
	}
}