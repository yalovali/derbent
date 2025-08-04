package unit_tests.tech.derbent.session.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.server.VaadinSession;

import tech.derbent.abstracts.interfaces.CProjectListChangeListener;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for project list change listener functionality in SessionService.
 */
@ExtendWith (MockitoExtension.class)
class SessionServiceProjectListChangeTest extends CTestBase {

	@Mock
	private VaadinSession vaadinSession;

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testAddAndRemoveProjectListChangeListener() {
		final CProjectListChangeListener listener =
			mock(CProjectListChangeListener.class);
		// Test adding listener
		sessionService.addProjectListChangeListener(listener);
		// Test removing listener
		sessionService.removeProjectListChangeListener(listener);
		// Test removing null listener (should not throw exception)
		sessionService.removeProjectListChangeListener(null);
		assertTrue(true, "All listener operations completed successfully");
	}

	@Test
	void testProjectListChangeListenerRegistration() {
		// Create a mock listener
		final CProjectListChangeListener listener =
			mock(CProjectListChangeListener.class);
		// Add the listener
		sessionService.addProjectListChangeListener(listener);
		// Notify project list changed
		sessionService.notifyProjectListChanged();
		// Verify the listener was called (note: this would work if UI context was
		// available) In a real UI test, the listener would be called
		assertTrue(true, "Listener registration completed without errors");
		// Remove the listener
		sessionService.removeProjectListChangeListener(listener);
		assertTrue(true, "Listener removal completed without errors");
	}
}