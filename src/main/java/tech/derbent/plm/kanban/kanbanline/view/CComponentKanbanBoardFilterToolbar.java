package tech.derbent.plm.kanban.kanbanline.view;

import java.util.List;
import java.util.Objects;
import tech.derbent.api.ui.component.filter.CEntityTypeFilter;
import tech.derbent.api.ui.component.filter.CKanbanBoardModeFilter;
import tech.derbent.api.ui.component.filter.CKanbanSearchFilter;
import tech.derbent.api.ui.component.filter.CKanbanSprintMembershipFilter;
import tech.derbent.api.ui.component.filter.CResponsibleUserFilter;
import tech.derbent.api.ui.component.filter.CSprintFilter;
import tech.derbent.api.ui.component.filter.CUniversalFilterToolbar;
import tech.derbent.plm.kanban.kanbanline.domain.EKanbanViewMode;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintItem;

/** CComponentKanbanBoardFilterToolbar - Filtering toolbar for Kanban board items.
 * <p>
 * Supports two board modes:
 * <ul>
 * <li><strong>Sprint Board</strong> (default): shows items for the selected sprint. Sprint selector visible, membership filter hidden.</li>
 * <li><strong>Status Board</strong>: shows all project items grouped by status. Membership filter visible, sprint selector hidden.</li>
 * </ul>
 * </p>
 * <p>
 * Filters available in all modes: View mode toggle, entity type, responsible user, free-text search.<br>
 * Sprint Board only: Sprint selector.<br>
 * Status Board only: Sprint membership (All / In sprint / Backlog only).
 * </p> */
public class CComponentKanbanBoardFilterToolbar extends CUniversalFilterToolbar<CSprintItem> {

	private static final long serialVersionUID = 1L;
	private final CKanbanBoardModeFilter boardModeFilter;
	private final CSprintFilter sprintFilter;
	private final CKanbanSprintMembershipFilter membershipFilter;
	private final CEntityTypeFilter entityTypeFilter;
	private final CResponsibleUserFilter responsibleUserFilter;
	private final CKanbanSearchFilter searchFilter;

	/** Builds the filter toolbar and its components using composition. */
	public CComponentKanbanBoardFilterToolbar() {
		super();
		setId("kanbanBoardFilterToolbar");
		boardModeFilter = new CKanbanBoardModeFilter();
		sprintFilter = new CSprintFilter();
		membershipFilter = new CKanbanSprintMembershipFilter();
		entityTypeFilter = new CEntityTypeFilter();
		responsibleUserFilter = new CResponsibleUserFilter();
		searchFilter = new CKanbanSearchFilter();
		// Order: view mode toggle → sprint/membership → type → responsible → search
		addFilterComponent(boardModeFilter);
		addFilterComponent(sprintFilter);
		addFilterComponent(membershipFilter);
		addFilterComponent(entityTypeFilter);
		addFilterComponent(responsibleUserFilter);
		addFilterComponent(searchFilter);
		// Dynamic visibility: sprint selector ↔ membership filter based on board mode
		boardModeFilter.addChangeListener(mode -> {
			final boolean isSprintBoard = mode == null || mode == EKanbanViewMode.SPRINT_BOARD;
			sprintFilter.setVisible(isSprintBoard);
			membershipFilter.setVisible(!isSprintBoard);
		});
		// Default: Sprint Board mode — show sprint selector, hide membership filter
		membershipFilter.setVisible(false);
		build();
	}

	/** Returns the current board view mode. */
	public EKanbanViewMode getCurrentMode() {
		final EKanbanViewMode mode = getCurrentCriteria().getValue(CKanbanBoardModeFilter.FILTER_KEY);
		return mode != null ? mode : EKanbanViewMode.SPRINT_BOARD;
	}

	public CKanbanBoardModeFilter getBoardModeFilter() { return boardModeFilter; }

	public CEntityTypeFilter getEntityTypeFilter() { return entityTypeFilter; }

	public CKanbanSprintMembershipFilter getMembershipFilter() { return membershipFilter; }

	public CResponsibleUserFilter getResponsibleUserFilter() { return responsibleUserFilter; }

	public CKanbanSearchFilter getSearchFilter() { return searchFilter; }

	public CSprintFilter getSprintFilter() { return sprintFilter; }

	/** Updates available entity type options based on sprint item list. */
	public void setAvailableItems(final List<CSprintItem> items) {
		Objects.requireNonNull(items, "Sprint items list cannot be null");
		final List<Object> entities =
				items.stream().filter(Objects::nonNull).map(CSprintItem::getParentItem).filter(Objects::nonNull).map(item -> (Object) item).toList();
		entityTypeFilter.setAvailableEntityTypes(entities);
	}

	/** Updates available sprint options and selects a default. */
	public void setAvailableSprints(final List<CSprint> sprints, final CSprint defaultSprint) {
		sprintFilter.setAvailableSprints(sprints, defaultSprint);
	}
}
