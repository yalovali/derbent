package tech.derbent.plm.kanban.kanbanline.view;

import java.util.List;
import java.util.Objects;
import tech.derbent.api.ui.component.filter.CKanbanSearchFilter;
import tech.derbent.api.ui.component.filter.CKanbanSprintScopeFilter;
import tech.derbent.api.ui.component.filter.CResponsibleUserFilter;
import tech.derbent.api.ui.component.filter.CUniversalFilterToolbar;
import tech.derbent.plm.kanban.kanbanline.domain.EKanbanViewMode;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;

/** CComponentKanbanBoardFilterToolbar - Filtering toolbar for Kanban board items.
 * <p>
 * Sprint selection supports an empty value (clear) which is treated as <strong>All sprints</strong>.
 * </p> */
public class CComponentKanbanBoardFilterToolbar extends CUniversalFilterToolbar<CSprintItem> {

	private static final long serialVersionUID = 1L;
	private final CKanbanSprintScopeFilter sprintFilter;
	private final CResponsibleUserFilter responsibleUserFilter;
	private final CKanbanSearchFilter searchFilter;

	/** Builds the filter toolbar and its components using composition. */
	public CComponentKanbanBoardFilterToolbar() {
		super();
		setId("kanbanBoardFilterToolbar");
		sprintFilter = new CKanbanSprintScopeFilter();
		responsibleUserFilter = new CResponsibleUserFilter();
		searchFilter = new CKanbanSearchFilter();
		// Order: sprint → responsible → search
		addFilterComponent(sprintFilter);
		addFilterComponent(responsibleUserFilter);
		addFilterComponent(searchFilter);
		build();
	}

	/** Returns the current board view mode. */
	public EKanbanViewMode getCurrentMode() {
		return EKanbanViewMode.SPRINT_BOARD;
	}

	public CResponsibleUserFilter getResponsibleUserFilter() { return responsibleUserFilter; }

	public CKanbanSearchFilter getSearchFilter() { return searchFilter; }

	public CKanbanSprintScopeFilter getSprintFilter() { return sprintFilter; }

	public void setAvailableItems(final List<CSprintItem> items) {
		Objects.requireNonNull(items, "Sprint items list cannot be null");
	}

	/** Updates available sprint options and selects a default. */
	public void setAvailableSprints(final List<CSprint> sprints, final CSprint defaultSprint) {
		sprintFilter.setAvailableSprints(sprints, defaultSprint);
	}
}
