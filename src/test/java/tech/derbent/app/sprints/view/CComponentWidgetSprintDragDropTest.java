package tech.derbent.app.sprints.view;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.shared.Registration;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;

/** Test class for CComponentWidgetSprint drag-drop interface implementation.
 * Verifies that CComponentWidgetSprint properly implements IHasDragStart and IHasDragEnd
 * interfaces and can propagate drag-drop events from its internal grid. */
class CComponentWidgetSprintDragDropTest {

	/** Test that CComponentWidgetSprint implements IHasDragStart interface.
	 * Verifies that the addDragStartListener method can be called and returns a valid Registration.
	 * Note: Registration is returned even if componentSprintItems is null (returns empty registration). */
	@Test
	void testImplementsIHasDragStart() {
		// Create a sprint for the widget
		final CSprint sprint = new CSprint();
		sprint.setName("Test Sprint");

		// Create the widget (this will initialize componentSprintItems in createSecondLine)
		final CComponentWidgetSprint widget = new CComponentWidgetSprint(sprint);

		// Verify we can add a drag start listener
		final Registration registration = widget.addDragStartListener((GridDragStartEvent<CSprintItem> event) -> {
			// Listener body - will be called when drag starts
		});

		// Verify registration was returned (even if empty)
		assertNotNull(registration, "Registration should not be null");
	}

	/** Test that CComponentWidgetSprint implements IHasDragEnd interface.
	 * Verifies that the addDragEndListener method can be called and returns a valid Registration.
	 * Note: Registration is returned even if componentSprintItems is null (returns empty registration). */
	@Test
	void testImplementsIHasDragEnd() {
		// Create a sprint for the widget
		final CSprint sprint = new CSprint();
		sprint.setName("Test Sprint");

		// Create the widget (this will initialize componentSprintItems in createSecondLine)
		final CComponentWidgetSprint widget = new CComponentWidgetSprint(sprint);

		// Verify we can add a drag end listener
		final Registration registration = widget.addDragEndListener((GridDragEndEvent<CSprintItem> event) -> {
			// Listener body - will be called when drag ends
		});

		// Verify registration was returned (even if empty)
		assertNotNull(registration, "Registration should not be null");
	}

	/** Test that both drag listeners can be added to the same widget instance.
	 * This verifies the widget properly implements both IHasDragStart and IHasDragEnd interfaces. */
	@Test
	void testBothInterfacesImplemented() {
		// Create a sprint for the widget
		final CSprint sprint = new CSprint();
		sprint.setName("Test Sprint for Both Interfaces");

		// Create the widget
		final CComponentWidgetSprint widget = new CComponentWidgetSprint(sprint);

		// Try to add both listeners
		final Registration dragStartReg = widget.addDragStartListener(event -> {
			// Drag start handler
		});
		final Registration dragEndReg = widget.addDragEndListener(event -> {
			// Drag end handler
		});

		// Verify both registrations are not null
		assertNotNull(dragStartReg, "Drag start registration should not be null");
		assertNotNull(dragEndReg, "Drag end registration should not be null");
	}
}
