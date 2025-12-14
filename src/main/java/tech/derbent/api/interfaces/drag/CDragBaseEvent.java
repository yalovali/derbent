package tech.derbent.api.interfaces.drag;

import com.vaadin.flow.component.Component;

public abstract class CDragBaseEvent extends CEvent {

	private static final long serialVersionUID = 1L;

	public CDragBaseEvent(Component source, boolean fromClient) {
		super(source, fromClient);
	}

	/** Gets the component from which items were dragged.
	 * @return the drag source component */
	public Component getDragSource() { return getSource(); }
}
