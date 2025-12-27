package tech.derbent.app.kanban.kanbanline.view;

import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CComponentPostit - A compact post-it style widget for displaying project items inside kanban columns. */
public class CComponentKanbanPostit extends CComponentWidgetEntity<CSprintItem> {

	private static final long serialVersionUID = 1L;

	/** Creates a post-it card for the given sprint item. */
	public CComponentKanbanPostit(final CSprintItem item) {
		super(item);
		Check.notNull(item, "Sprint item cannot be null for postit");
		addClassName("kanban-postit");
		getStyle().set("width", "100%");
		getElement().setAttribute("tabindex", "0");
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

	/** Resolves the sprintable item for display. */
	private ISprintableItem resolveSprintableItem() {
		final CSprintItem sprintItem = entity;
		return sprintItem != null ? sprintItem.getItem() : null;
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
