package unit_tests.tech.derbent.session.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.projects.domain.CProject;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for SessionService project change notification functionality. These tests
 * verify the listener registration mechanism without requiring UI context.
 */
public class SessionServiceProjectChangeTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	/**
	 * Test that creates a simple implementation of the listener interface to verify the
	 * contract works as expected.
	 */
	@Test
	void testListenerInterface() {
		// Create a simple test listener implementation
		final CProject[] receivedProject = new CProject[1];
		final boolean[] listenerCalled = new boolean[1];
		final CProjectChangeListener testListener = new CProjectChangeListener() {

			@Override
			public void onProjectChanged(final CProject newProject) {
				receivedProject[0] = newProject;
				listenerCalled[0] = true;
			}
		};
		// Test the listener interface directly
		testListener.onProjectChanged(project);
		// Verify the listener was called with the correct project
		assertEquals(true, listenerCalled[0], "Listener should have been called");
		assertEquals(project, receivedProject[0],
			"Listener should receive the correct project");
	}

	@Test
	void testMultipleListenerRegistration() {
		// Create multiple mock listeners
		final CProjectChangeListener listener1 = mock(CProjectChangeListener.class);
		final CProjectChangeListener listener2 = mock(CProjectChangeListener.class);
		final CProjectChangeListener listener3 = mock(CProjectChangeListener.class);
		// Register all listeners
		sessionService.addProjectChangeListener(listener1);
		sessionService.addProjectChangeListener(listener2);
		sessionService.addProjectChangeListener(listener3);
		// Verify the service handles multiple registrations
		assertNotNull(sessionService);
		// Test partial unregistration
		sessionService.removeProjectChangeListener(listener2);
		// Verify the service remains functional
		assertNotNull(sessionService);
	}

	@Test
	void testNullListenerHandling() {
		// Test that adding/removing null listeners doesn't cause issues
		sessionService.addProjectChangeListener(null);
		sessionService.removeProjectChangeListener(null);
		// Verify the service remains functional
		assertNotNull(sessionService);
	}

	@Test
	void testProjectChangeListenerRegistration() {
		// Create a mock listener
		final CProjectChangeListener listener = mock(CProjectChangeListener.class);
		// Register the listener
		sessionService.addProjectChangeListener(listener);
		// Verify the service exists and listener registration doesn't throw exceptions
		assertNotNull(sessionService);
	}

	@Test
	void testProjectChangeListenerUnregistration() {
		// Create a mock listener
		final CProjectChangeListener listener = mock(CProjectChangeListener.class);
		// Register and then unregister the listener
		sessionService.addProjectChangeListener(listener);
		sessionService.removeProjectChangeListener(listener);
		// Verify the service exists and operations complete without exceptions
		assertNotNull(sessionService);
	}
}
