package tech.derbent.app.kanban.kanbanline.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** CComponentKanbanBoard - Displays a kanban line as a board with vertical columns and post-it style project items. */
public class CComponentKanbanBoard extends CComponentBase<CKanbanLine> implements IContentOwner {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanBoard.class);
	private static final long serialVersionUID = 1L;

	private static boolean matchesResponsibleUser(final CProjectItem<?> item, final CUser targetUser) {
		if (item.getResponsible() == null || item.getResponsible().getId() == null || targetUser.getId() == null) {
			return false;
		}
		return item.getResponsible().getId().equals(targetUser.getId());
	}

	private static boolean matchesTypeFilter(final CProjectItem<?> item, final Class<?> entityClass) {
		if (entityClass == null) {
			return true;
		}
		return entityClass.isAssignableFrom(item.getClass());
	}

	private List<CProjectItem<?>> allProjectItems;
	private final CComponentKanbanBoardFilterToolbar filterToolbar;
	private final CHorizontalLayout layoutColumns;
	private List<CProjectItem<?>> projectItems;
	private final ISessionService sessionService;

	public CComponentKanbanBoard() {
		LOGGER.debug("Initializing Kanban board component");
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

	private void applyFilters() {
		LOGGER.debug("Applying filters to Kanban board component");
		final CKanbanLine currentLine = getValue();
		Check.notNull(currentLine, "Kanban line must be set before applying filters");
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

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		LOGGER.debug("Creating new entity instance is not supported for Kanban board component");
		return null;
	}

	@Override
	public String getCurrentEntityIdString() {
		final CKanbanLine currentLine = getValue();
		if (currentLine == null || currentLine.getId() == null) {
			return null;
		}
		return currentLine.getId().toString();
	}

	@Override
	public CAbstractService<?> getEntityService() { return null; }

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

	@Override
	protected void onValueChanged(final CKanbanLine oldValue, final CKanbanLine newValue, final boolean fromClient) {
		LOGGER.debug("Kanban board value changed from {} to {}", oldValue, newValue);
		refreshComponent();
	}

	@Override
	public void populateForm() {
		LOGGER.debug("Populating Kanban board component");
		refreshComponent();
	}

	public void refreshComponent() {
		LOGGER.debug("Refreshing Kanban board component");
		layoutColumns.removeAll();
		final CKanbanLine currentLine = getValue();
		if (currentLine == null) {
			// TODO create an empty loading div
			final CDiv div = new CDiv("Loading columns ...");
			layoutColumns.add(div);
			return;
		}
		final List<CKanbanColumn> columns = new ArrayList<>(currentLine.getKanbanColumns());
		columns.sort(Comparator.comparing(CKanbanColumn::getItemOrder, Comparator.nullsLast(Integer::compareTo)));
		for (final CKanbanColumn column : columns) {
			final CComponentKanbanColumn columnComponent = new CComponentKanbanColumn();
			columnComponent.setValue(column);
			columnComponent.setItems(projectItems);
			layoutColumns.add(columnComponent);
		}
	}

	public void setProjectItems(final List<CProjectItem<?>> projectItems) {
		LOGGER.debug("Setting project items for Kanban board component");
		Check.notNull(getValue(), "Kanban line must be set before setting project items");
		Check.notNull(projectItems, "Project items cannot be null for kanban board");
		allProjectItems = new ArrayList<>(projectItems);
		filterToolbar.setAvailableItems(allProjectItems);
		applyFilters();
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		LOGGER.debug("Setting current entity for Kanban board component");
		if (entity == null) {
			setValue(null);
			return;
		}
		Check.instanceOf(entity, CKanbanLine.class, "Kanban board expects CKanbanLine as current entity");
		if (entity.equals(getValue())) {
			LOGGER.debug("New entity is the same as current value; no action taken");
			return;
		}
		setValue((CKanbanLine) entity);
	}
}
