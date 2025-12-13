package tech.derbent.api.interfaces.drag;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public abstract class CEvent extends ComponentEvent<Component> {

	private static final long serialVersionUID = 1L;

	public CEvent(Component source, boolean fromClient) {
		super(source, fromClient);
	}
}
