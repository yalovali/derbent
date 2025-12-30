package tech.derbent.api.interfaces.drag;

import java.util.List;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dnd.DropEffect;

public class CDragOverEvent extends CDragBaseEvent {

	private static final long serialVersionUID = 1L;
	private final List<Object> draggedItems;
	private DropEffect dropEffect = DropEffect.MOVE;

	public CDragOverEvent(final Component source, final List<Object> draggedItems, final boolean fromClient) {
		super(source, fromClient);
		this.draggedItems = draggedItems != null ? draggedItems : List.of();
	}

	public Object getDraggedItem() { return !draggedItems.isEmpty() ? draggedItems.get(0) : null; }

	public List<Object> getDraggedItems() { return draggedItems; }

	public DropEffect getDropEffect() { return dropEffect; }

	public void setDropEffect(DropEffect dropEffect) { this.dropEffect = dropEffect != null ? dropEffect : DropEffect.NONE; }

	@Override
	public String toString() {
		return "CDragOverEvent{" + "draggedItems=" + draggedItems + ", dropEffect=" + dropEffect + ", source="
				+ getSource().getClass().getSimpleName() + '}';
	}
}
