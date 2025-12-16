package tech.derbent.api.interfaces;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.utils.Check;

public interface IHasDragControl {

	static final Logger LOGGER = LoggerFactory.getLogger(IHasDragControl.class);

	default void addEventListener_dragDrop(ComponentEventListener<CDragDropEvent<?>> listener) {
		getDropListeners().add(listener);
	}

	default void addEventListener_dragEnd(ComponentEventListener<CDragEndEvent> listener) {
		getDragEndListeners().add(listener);
	}

	default void addEventListener_dragStart(ComponentEventListener<CDragStartEvent<?>> listener) {
		getDragStartListeners().add(listener);
	}

	public Set<ComponentEventListener<CDragEndEvent>> getDragEndListeners();
	public Set<ComponentEventListener<CDragStartEvent<?>>> getDragStartListeners();
	public Set<ComponentEventListener<CDragDropEvent<?>>> getDropListeners();

	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void notifyDragEndListeners(final CDragEndEvent event) {
		if (getDragEndListeners().isEmpty()) {
			return;
		}
		// LOGGER.debug("[DragDebug] {} notifying {} drag end listeners", getClass().getSimpleName(), getDragEndListeners().size());
		for (final ComponentEventListener listener : getDragEndListeners()) {
			try {
				listener.onComponentEvent(event);
			} catch (final Exception e) {
				LOGGER.error("[DragDebug] Error notifying drag end listener in {}: {}", getClass().getSimpleName(), e.getMessage());
			}
		}
	}

	/** Notifies all registered drag start listeners.
	 * <p>
	 * This method is called when a drag operation starts. It automatically notifies all registered drag start listeners with proper error handling.
	 * Implementations can call this method to propagate drag start events up the component hierarchy.
	 * @param event The drag start event to notify listeners about */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void notifyDragStartListeners(final CDragStartEvent<?> event) {
		if (getDragStartListeners().isEmpty()) {
			return;
		}
		for (final ComponentEventListener listener : getDragStartListeners()) {
			try {
				listener.onComponentEvent(event);
			} catch (final Exception e) {
				LOGGER.error("[DragDebug] Error notifying drag start listener in {}: {}", getClass().getSimpleName(), e.getMessage());
			}
		}
	}

	/** Notifies all registered drop listeners.
	 * <p>
	 * This method is called when items are dropped onto this component. It automatically notifies all registered drop listeners with proper error
	 * handling. Implementations can call this method to propagate drop events up the component hierarchy.
	 * @param event The drop event to notify listeners about */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void notifyDropListeners(final CDragDropEvent<?> event) {
		if (getDropListeners().isEmpty()) {
			return;
		}
		for (final ComponentEventListener listener : getDropListeners()) {
			try {
				LOGGER.debug("[DragDebug] {} notifying drop listener {}", getClass().getSimpleName(), listener.getClass().getSimpleName());
				listener.onComponentEvent(event);
			} catch (final Exception e) {
				LOGGER.error("[DragDebug] Error notifying drop listener in {}: {}", getClass().getSimpleName(), e.getMessage());
			}
		}
	}

	@SuppressWarnings ({})
	default void notifyEvents(final CEvent event) {
		try {
			event.addSource(this);
			if (event instanceof CDragStartEvent<?>) {
				notifyDragStartListeners((CDragStartEvent<?>) event);
			} else if (event instanceof CDragDropEvent<?>) {
				notifyDropListeners((CDragDropEvent<?>) event);
			} else if (event instanceof CDragEndEvent) {
				notifyDragEndListeners((CDragEndEvent) event);
			}
		} catch (final Exception e) {
			LOGGER.error("Error in notifyEvents for event: {}", event.toString(), e);
			throw e;
		}
	}

	default void on_dragDrop(CDragDropEvent<?> event) {
		notifyEvents(event);
	}

	default void on_dragEnd(CDragEndEvent event) {
		notifyEvents(event);
	}

	default void on_dragStart(CDragStartEvent<?> event) {
		notifyEvents(event);
	}

	void setDragEnabled(boolean enabled);
	void setDropEnabled(boolean enabled);

	@SuppressWarnings ({})
	default void setupChildDragDropForwarding(final IHasDragControl child) {
		// LOGGER.debug("Setting up drag-drop event forwarding from child {} to parent {} parent: {} child:{}",
		// child.getClass().getSimpleName(),getClass().getSimpleName(), toString(), child.toString());
		Check.notNull(child, "Child component cannot be null");
		// Forward drag start events from child to parent
		child.addEventListener_dragStart(event -> {
			on_dragStart(event);
		});
		// Forward drag end events from child to parent
		child.addEventListener_dragEnd(event -> {
			on_dragEnd(event);
		});
		// Forward drop events from child to parent
		child.addEventListener_dragDrop(event -> {
			on_dragDrop(event);
		});
	}
}
