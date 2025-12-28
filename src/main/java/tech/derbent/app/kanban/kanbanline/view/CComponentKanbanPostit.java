package tech.derbent.app.kanban.kanbanline.view;

import java.util.HashSet;
import java.util.Set;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.IHasSelectionNotification;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CComponentPostit - A compact post-it style widget for displaying project items inside kanban columns. */
public class CComponentKanbanPostit extends CComponentWidgetEntity<CSprintItem> implements IHasSelectionNotification {

	private static final long serialVersionUID = 1L;
	Set<ComponentEventListener<CSelectEvent>> select_listeners = new HashSet<>();

	/** Creates a post-it card for the given sprint item. */
	public CComponentKanbanPostit(final CSprintItem item) {
		super(item);
		Check.notNull(item, "Sprint item cannot be null for postit");
		addClassName("kanban-postit");
		getStyle().set("width", "100%");
		getElement().setAttribute("tabindex", "0");
		addClickListener(on_component_click());
	}

	/** Builds the primary title line. */
	@Override
	protected void createFirstLine() throws Exception {
		final ISprintableItem item = resolveSprintableItem();
		layoutLineOne.setWidthFull();
		if (item instanceof CEntityDB<?>) {
			layoutLineOne.add(CLabelEntity.createH3Label((CEntityDB<?>) item));
		} else {
			layoutLineOne.add(CLabelEntity.createH3Label(item != null ? item.getName() : ""));
		}
	}

	/** Builds the status and responsible line. */
	@Override
	protected void createSecondLine() throws Exception {
		final ISprintableItem item = resolveSprintableItem();
		if (item == null) {
			return;
		}
		if (item.getStatus() != null) {
			layoutLineTwo.add(new CLabelEntity(item.getStatus()));
		}
		if (item.getResponsible() != null) {
			layoutLineTwo.add(CLabelEntity.createUserLabel(item.getResponsible()));
		}
	}

	/** Builds the date range line when available. */
	@Override
	protected void createThirdLine() {
		final ISprintableItem item = resolveSprintableItem();
		if (item != null && (item.getStartDate() != null || item.getEndDate() != null)) {
			layoutLineThree.add(CLabelEntity.createDateRangeLabel(item.getStartDate(), item.getEndDate()));
		}
	}

	private ComponentEventListener<ClickEvent<HorizontalLayout>> on_component_click() {
		return event -> {
			final CSelectEvent selectEvent = new CSelectEvent(this, true);
			// on_select(selectEvent);
			select_notifyEvents(selectEvent);
		};
	}

	/** Resolves the sprintable item for display. */
	private ISprintableItem resolveSprintableItem() {
		final CSprintItem sprintItem = entity;
		return sprintItem != null ? sprintItem.getItem() : null;
	}

	@Override
	public Set<ComponentEventListener<CSelectEvent>> select_getSelectListeners() {
		// TODO Auto-generated method stub
		return select_listeners;
	}

	/** Sets selected styles for the post-it. */
	@Override
	public void setSelected(final boolean selected) {
		this.selected = selected;
		if (selected) {
			addClassName("kanban-postit-selected");
		} else {
			removeClassName("kanban-postit-selected");
		}
	}
}
