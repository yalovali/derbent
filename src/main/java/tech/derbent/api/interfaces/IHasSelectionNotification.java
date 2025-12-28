package tech.derbent.api.interfaces;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ComponentEventListener;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.utils.Check;

public interface IHasSelectionNotification {

	static final Logger LOGGER = LoggerFactory.getLogger(IHasSelectionNotification.class);

	default void addEventListener_select(ComponentEventListener<CSelectEvent> listener) {
		if (listener == null) {
			return;
		}
		// LOGGER.debug("[DragDebug] {} adding drag start listener {}", getClass().getSimpleName(), listener.getClass().getSimpleName());
		select_getSelectListeners().add(listener);
	}

	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void notifySelectListeners(CSelectEvent event) {
		for (final ComponentEventListener listener : select_getSelectListeners()) {
			try {
				listener.onComponentEvent(event);
			} catch (final Exception e) {
				LOGGER.error("[SelectDebug] Error notifying select listener in {}: {}", getClass().getSimpleName(), e.getMessage());
			}
		}
	}

	default void on_select(CSelectEvent event) {
		select_notifyEvents(event);
	}

	public default void select_checkEventAfterPass(@SuppressWarnings ("unused") CEvent event) {}

	public default void select_checkEventBeforePass(@SuppressWarnings ("unused") CEvent event) {}

	public Set<ComponentEventListener<CSelectEvent>> select_getSelectListeners();

	default void select_notifyEvents(final CEvent event) {
		try {
			// event.addSource(this);
			select_checkEventBeforePass(event);
			if (event instanceof CSelectEvent) {
				notifySelectListeners((CSelectEvent) event);
			}
			select_checkEventAfterPass(event);
		} catch (final Exception e) {
			LOGGER.error("Error in notifyEvents for event: {}", event.toString(), e);
			throw e;
		}
	}

	default void setupSelectionNotification(final IHasSelectionNotification child) {
		Check.notNull(child, "Child component cannot be null");
		child.addEventListener_select(event -> {
			on_select(event);
		});
	}
}
