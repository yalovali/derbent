package tech.derbent.app.kanban.kanbanline.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** CComponentKanbanBoard - Displays a kanban line as a board with vertical columns and post-it style project items. */
public class CComponentKanbanBoard extends CVerticalLayout implements HasValue<HasValue.ValueChangeEvent<CEntity>, CEntity> {

	private static final long serialVersionUID = 1L;
	private final CComponentKanbanBoardFilterToolbar filterToolbar;
	private final CHorizontalLayout layoutColumns;
	private CKanbanLine kanbanLine;
	private List<CProjectItem<?>> allProjectItems;
	private List<CProjectItem<?>> projectItems;
	private final ISessionService sessionService;

	public CComponentKanbanBoard() {
		Check.notNull(kanbanLine, "Kanban line cannot be null for board component");
		sessionService = CSpringContext.getBean(ISessionService.class);
		Check.notNull(sessionService, "Session service cannot be null for Kanban board");
		allProjectItems = new ArrayList<>();
		projectItems = new ArrayList<>();
		layoutColumns = new CHorizontalLayout();
		layoutColumns.setWidthFull();
		layoutColumns.setSpacing(true);
		filterToolbar = new CComponentKanbanBoardFilterToolbar();
		filterToolbar.addKanbanFilterChangeListener(criteria -> applyFilters());
		setWidthFull();
		setPadding(true);
		setSpacing(true);
		add(filterToolbar, layoutColumns);
		applyFilters();
	}

	private void applyFilters() {
		final CComponentKanbanBoardFilterToolbar.FilterCriteria criteria = filterToolbar.getCurrentCriteria();
		final List<CProjectItem<?>> filtered = new ArrayList<>();
		for (final CProjectItem<?> item : allProjectItems) {
			if (item == null) {
				continue;
			}
			if (!matchesTypeFilter(item, criteria.getEntityType())) {
				continue;
			}
			if (!matchesResponsibleFilter(item, criteria)) {
				continue;
			}
			filtered.add(item);
		}
		projectItems = filtered;
		refreshComponent();
	}

	private CHorizontalLayout buildColumnHeader(final CKanbanColumn column) {
		final CHorizontalLayout headerLayout = new CHorizontalLayout();
		headerLayout.setWidthFull();
		headerLayout.setSpacing(true);
		final CH3 title = new CH3(column.getName());
		title.getStyle().set("margin", "0");
		headerLayout.add(title);
		if (column.getDefaultColumn()) {
			final Span defaultBadge = new Span("Default");
			defaultBadge.getStyle().set("background-color", "#E3F2FD").set("color", "#0D47A1").set("padding", "2px 6px").set("border-radius", "6px")
					.set("font-size", "10px").set("font-weight", "600");
			headerLayout.add(defaultBadge);
		}
		return headerLayout;
	}

	private Component buildColumnLayout(final CKanbanColumn column, final List<CProjectItem<?>> items) {
		final CVerticalLayout columnLayout = new CVerticalLayout();
		columnLayout.setPadding(true);
		columnLayout.setSpacing(true);
		columnLayout.setWidth("280px");
		columnLayout.getStyle().set("background-color", "#F5F5F5").set("border-radius", "10px").set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");
		columnLayout.add(buildColumnHeader(column));
		if (column.getIncludedStatuses() != null && !column.getIncludedStatuses().isEmpty()) {
			final String statuses = column.getIncludedStatuses().stream().filter(Objects::nonNull).map(status -> status.getName())
					.filter(name -> name != null && !name.isBlank()).sorted(String::compareToIgnoreCase).collect(Collectors.joining(", "));
			if (!statuses.isBlank()) {
				final CLabelEntity statusesLabel = new CLabelEntity();
				statusesLabel.setText(statuses);
				statusesLabel.getStyle().set("font-size", "11px").set("color", "#666");
				columnLayout.add(statusesLabel);
			}
		}
		if (items != null) {
			for (final CProjectItem<?> item : items) {
				columnLayout.add(new CComponentPostit(item));
			}
		}
		return columnLayout;
	}

	private Map<Long, CKanbanColumn> buildStatusToColumnMap(final List<CKanbanColumn> columns) {
		final Map<Long, CKanbanColumn> statusToColumn = new HashMap<>();
		for (final CKanbanColumn column : columns) {
			if (column.getIncludedStatuses() == null) {
				continue;
			}
			column.getIncludedStatuses().stream().filter(Objects::nonNull).filter(status -> status.getId() != null)
					.forEach(status -> statusToColumn.putIfAbsent(status.getId(), column));
		}
		return statusToColumn;
	}

	private Map<CKanbanColumn, List<CProjectItem<?>>> initializeColumnMap(final List<CKanbanColumn> columns) {
		final Map<CKanbanColumn, List<CProjectItem<?>>> itemsByColumn = new LinkedHashMap<>();
		for (final CKanbanColumn column : columns) {
			itemsByColumn.put(column, new ArrayList<>());
		}
		return itemsByColumn;
	}

	private boolean matchesResponsibleFilter(final CProjectItem<?> item, final CComponentKanbanBoardFilterToolbar.FilterCriteria criteria) {
		final CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode mode = criteria.getResponsibleMode();
		if (mode == null || mode == CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode.ALL) {
			return true;
		}
		if (mode == CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode.CURRENT_USER) {
			final CUser activeUser = sessionService.getActiveUser().orElse(null);
			Check.notNull(activeUser, "Active user not available for Kanban board filtering");
			return matchesResponsibleUser(item, activeUser);
		}
		if (mode == CComponentKanbanBoardFilterToolbar.ResponsibleFilterMode.SPECIFIC_USER) {
			Check.notNull(criteria.getResponsibleUser(), "Responsible user must be selected for specific filter");
			return matchesResponsibleUser(item, criteria.getResponsibleUser());
		}
		return true;
	}

	private boolean matchesResponsibleUser(final CProjectItem<?> item, final CUser targetUser) {
		if (item.getResponsible() == null || item.getResponsible().getId() == null || targetUser.getId() == null) {
			return false;
		}
		return item.getResponsible().getId().equals(targetUser.getId());
	}

	private boolean matchesTypeFilter(final CProjectItem<?> item, final Class<?> entityClass) {
		if (entityClass == null) {
			return true;
		}
		return entityClass.isAssignableFrom(item.getClass());
	}

	public void refreshComponent() {
		Check.notNull(kanbanLine, "Kanban line cannot be null when refreshing board");
		layoutColumns.removeAll();
		final List<CKanbanColumn> columns = new ArrayList<>(kanbanLine.getKanbanColumns());
		columns.sort(Comparator.comparing(CKanbanColumn::getItemOrder, Comparator.nullsLast(Integer::compareTo)));
		final Map<CKanbanColumn, List<CProjectItem<?>>> itemsByColumn = initializeColumnMap(columns);
		final CKanbanColumn defaultColumn = columns.stream().filter(CKanbanColumn::getDefaultColumn).findFirst().orElse(null);
		final Map<Long, CKanbanColumn> statusToColumn = buildStatusToColumnMap(columns);
		for (final CProjectItem<?> item : projectItems) {
			if (item == null) {
				continue;
			}
			final CKanbanColumn targetColumn = resolveTargetColumn(item, statusToColumn, defaultColumn);
			if (targetColumn != null) {
				itemsByColumn.get(targetColumn).add(item);
			}
		}
		for (final CKanbanColumn column : columns) {
			layoutColumns.add(buildColumnLayout(column, itemsByColumn.get(column)));
		}
	}

	private CKanbanColumn resolveTargetColumn(final CProjectItem<?> item, final Map<Long, CKanbanColumn> statusToColumn,
			final CKanbanColumn defaultColumn) {
		if (item.getStatus() != null && item.getStatus().getId() != null) {
			final CKanbanColumn matched = statusToColumn.get(item.getStatus().getId());
			if (matched != null) {
				return matched;
			}
		}
		return defaultColumn;
	}

	public void setKanbanLine(final CKanbanLine kanbanLine) {
		Check.notNull(kanbanLine, "Kanban line cannot be null");
		this.kanbanLine = kanbanLine;
		refreshComponent();
	}

	public void setProjectItems(final List<CProjectItem<?>> projectItems) {
		Check.notNull(projectItems, "Project items cannot be null for kanban board");
		allProjectItems = new ArrayList<>(projectItems);
		filterToolbar.setAvailableItems(allProjectItems);
		applyFilters();
	}
}
