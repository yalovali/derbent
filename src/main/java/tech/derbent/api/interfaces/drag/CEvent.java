package tech.derbent.api.interfaces.drag;

import java.util.ArrayList;
import java.util.List;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import tech.derbent.api.interfaces.IHasDragControl;

public abstract class CEvent extends ComponentEvent<Component> {

	private static final long serialVersionUID = 1L;
	List<IHasDragControl> sourceList = new ArrayList<>();
	private boolean valid = true;

	public CEvent(Component source, boolean fromClient) {
		super(source, fromClient);
		if (source instanceof final IHasDragControl sourceControl) {
			addSource(sourceControl);
		}
	}

	public void addSource(IHasDragControl iHasDragControl) {
		if (sourceList.contains(iHasDragControl)) {
			return;
		}
		sourceList.add(iHasDragControl);
	}

	public List<IHasDragControl> getSourceList() { return sourceList; }

	public boolean isValid() { return valid; }

	public void setValid(boolean valid) { this.valid = valid; }
}
