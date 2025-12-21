package tech.derbent.app.kanban.kanbanline.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** CComponentKanbanBoard - Displays a kanban line as a board with vertical columns and post-it style project items. */
public class CComponentKanbanBoard extends CVerticalLayout
		implements HasValue<HasValue.ValueChangeEvent<CKanbanLine>, CKanbanLine>, IContentOwner {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanBoard.class);
	private final CComponentKanbanBoardFilterToolbar filterToolbar;
	private final CHorizontalLayout layoutColumns;
	private CKanbanLine kanbanLine;
	private List<CProjectItem<?>> allProjectItems;
	private List<CProjectItem<?>> projectItems;
	private final ISessionService sessionService;
	private final List<HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<CKanbanLine>>> valueChangeListeners = new ArrayList<>();
	private boolean readOnly;
	private boolean requiredIndicatorVisible;

	public CComponentKanbanBoard() {
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
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		return null;
	}

	@Override
	public Registration addValueChangeListener(final HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<CKanbanLine>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		valueChangeListeners.add(listener);
		return () -> valueChangeListeners.remove(listener);
	}

	private void applyFilters() {
		Check.notNull(kanbanLine, "Kanban line must be set before applying filters");
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

	@Override
	public CKanbanLine getValue() { return kanbanLine; }

	@Override
	public CEntityDB<?> getCurrentEntity() { return kanbanLine; }

	@Override
	public String getCurrentEntityIdString() {
		if (kanbanLine == null || kanbanLine.getId() == null) {
			return null;
		}
		return kanbanLine.getId().toString();
	}

	@Override
	public CAbstractService<?> getEntityService() { return null; }

	private Map<CKanbanColumn, List<CProjectItem<?>>> initializeColumnMap(final List<CKanbanColumn> columns) {
		final Map<CKanbanColumn, List<CProjectItem<?>>> itemsByColumn = new LinkedHashMap<>();
		for (final CKanbanColumn column : columns) {
			itemsByColumn.put(column, new ArrayList<>());
		}
		return itemsByColumn;
	}

	@Override
	public boolean isEmpty() { return kanbanLine == null; }

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public boolean isRequiredIndicatorVisible() { return requiredIndicatorVisible; }

	@Override
	public void populateForm() {
		setValue(kanbanLine);
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
		layoutColumns.removeAll();
		if (kanbanLine == null) {
			// TODO create an empty loading div
			final CDiv div = new CDiv("Loading columns ...");
			layoutColumns.add(div);
			return;
		}
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
			layoutColumns.add(new CComponentKanbanColumn(column, itemsByColumn.get(column)));
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

	public void setProjectItems(final List<CProjectItem<?>> projectItems) {
		Check.notNull(kanbanLine, "Kanban line must be set before setting project items");
		Check.notNull(projectItems, "Project items cannot be null for kanban board");
		allProjectItems = new ArrayList<>(projectItems);
		filterToolbar.setAvailableItems(allProjectItems);
		applyFilters();
	}

	@Override
	public void setReadOnly(final boolean readOnly) { this.readOnly = readOnly; }

	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) { this.requiredIndicatorVisible = requiredIndicatorVisible; }

	@Override
	public void setCurrentEntity(final CEntityDB<?> entity) {
		if (entity == null) {
			kanbanLine = null;
			return;
		}
		Check.instanceOf(entity, CKanbanLine.class, "Kanban board expects CKanbanLine as current entity");
		kanbanLine = (CKanbanLine) entity;
	}

	@Override
	public void setValue(final CKanbanLine value) {
		LOGGER.debug("Setting Kanban line value: {}", value != null ? value.getName() : "null");
		final CKanbanLine oldValue = kanbanLine;
		kanbanLine = value;
		refreshComponent();
		if (value == null) {
			return;
		}
		if (!Objects.equals(oldValue, value)) {
			final HasValue.ValueChangeEvent<CKanbanLine> event = new HasValue.ValueChangeEvent<CKanbanLine>() {

				private static final long serialVersionUID = 1L;

				@Override
				public HasValue<?, CKanbanLine> getHasValue() { return CComponentKanbanBoard.this; }

				@Override
				public CKanbanLine getOldValue() { return oldValue; }

				@Override
				public CKanbanLine getValue() { return value; }

				@Override
				public boolean isFromClient() { return false; }
			};
			for (final HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<CKanbanLine>> listener : valueChangeListeners) {
				listener.valueChanged(event);
			}
		}
	}
}
