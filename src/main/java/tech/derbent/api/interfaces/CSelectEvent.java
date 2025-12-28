package tech.derbent.api.interfaces;

import com.vaadin.flow.component.Component;
import tech.derbent.api.interfaces.drag.CEvent;

public class CSelectEvent extends CEvent {

	private static final long serialVersionUID = 1L;

	public CSelectEvent(Component source, boolean fromClient) {
		super(source, fromClient);
	}
}
