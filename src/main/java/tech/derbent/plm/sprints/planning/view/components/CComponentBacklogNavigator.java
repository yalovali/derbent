package tech.derbent.plm.sprints.planning.view.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dnd.DropEffect;
import com.vaadin.flow.component.dnd.DropEvent;
import com.vaadin.flow.component.dnd.DropTarget;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.IHasSelectionNotification;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.filter.CFilterToolbarSupport;
import tech.derbent.api.utils.CSearchTextFilterSupport;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.sprints.domain.CSprintItem;
import tech.derbent.plm.sprints.planning.domain.ESprintPlanningScope;

/** Reusable parent+leaf backlog navigator based on sprint-planning components.
 * <p>
 * This component intentionally focuses on navigation + selection. Boards that need drag/drop or sprint assignment should wire additional handlers
 * externally.
 * </p>
 */
public class CComponentBacklogNavigator extends CVerticalLayout implements IHasSelectionNotification, IHasDragControl {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentBacklogNavigator.class);
	private static final long serialVersionUID = 1L;
	private final CSprintPlanningBacklogBrowser backlogBrowser;
	private boolean showClosed = false;
	private final CSprintPlanningDragContext dragContext = new CSprintPlanningDragContext();
	private final java.util.Set<ComponentEventListener<CDragEndEvent>> dragEndListeners = new java.util.HashSet<>();
	private final java.util.Set<ComponentEventListener<CDragStartEvent>> dragStartListeners = new java.util.HashSet<>();
	private final java.util.Set<ComponentEventListener<CDragDropEvent>> dropListeners = new java.util.HashSet<>();
	private DropTarget<com.vaadin.flow.component.Component> dropTarget;
	private Map<String, CProjectItem<?, ?>> hierarchyItemsByKey = Map.of();
	private final CTextField leafSearchField;
	private final CTextField parentSearchField;
	private CProject<?> project;
	private ESprintPlanningScope scope = ESprintPlanningScope.BACKLOG;
	private final Set<ComponentEventListener<CSelectEvent>> selectListeners = new java.util.HashSet<>();

	public CComponentBacklogNavigator() {
		setPadding(false);
		setSpacing(false);
		setSizeFull();
		parentSearchField = CFilterToolbarSupport.createSearchField("Search", "Search parents...", null, null,
				ValueChangeMode.EAGER, 200, ignored -> refreshBrowserOnly());
		parentSearchField.setLabel("");
		parentSearchField.getStyle().set("min-width", "0");
		leafSearchField = CFilterToolbarSupport.createSearchField("Search", "Search backlog...", null, null,
				ValueChangeMode.EAGER, 200, ignored -> refreshBrowserOnly());
		leafSearchField.setLabel("");
		leafSearchField.getStyle().set("min-width", "0");
		final Consumer<CSprintPlanningDropRequest> noopBacklogDrop = null;
		final java.util.function.BiConsumer<CGnntItem, CGnntItem> noopParentDrop = (ignored, ignored2) -> {
			// Navigation-only component.
		};
		backlogBrowser = new CSprintPlanningBacklogBrowser(dragContext, this::onLeafSelected, noopBacklogDrop,
				noopParentDrop, List.of(parentSearchField), this::onLeafDragStart);
		backlogBrowser.getLeafQuickAccessPanel().addCustomComponent(leafSearchField);
		add(backlogBrowser);
		expand(backlogBrowser);
		drag_setDropEnabled(true);
	}

	@Override
	public void drag_checkEventAfterPass(final CEvent event) {
		// No-op.
	}

	@Override
	public void drag_checkEventBeforePass(final CEvent event) {
		// No-op.
	}

	@Override
	public java.util.Set<ComponentEventListener<CDragEndEvent>> drag_getDragEndListeners() {
		return dragEndListeners;
	}

	@Override
	public java.util.Set<ComponentEventListener<CDragStartEvent>> drag_getDragStartListeners() {
		return dragStartListeners;
	}

	@Override
	public java.util.Set<ComponentEventListener<CDragDropEvent>> drag_getDropListeners() {
		return dropListeners;
	}

	@Override
	public boolean drag_isDropAllowed(final CDragStartEvent event) {
		return true;
	}

	@Override
	public void drag_setDragEnabled(final boolean enabled) {
		// Grid-level drag is always enabled by the sprint-planning grids.
	}

	@Override
	public void drag_setDropEnabled(final boolean enabled) {
		if (!enabled) {
			dropTarget = null;
			return;
		}
		dropTarget = DropTarget.create(backlogBrowser.getLeavesGridComponent());
		dropTarget.setDropEffect(DropEffect.MOVE);
		dropTarget.addDropListener((final DropEvent<com.vaadin.flow.component.Component> event) -> on_dragDrop(
				new CDragDropEvent(getId().orElse("backlog-navigator"), this, null, GridDropLocation.ON_TOP, true)));
		dropTarget.setActive(true);
	}

	public List<Component> getBacklogLeafFilterComponents() {
		leafSearchField.getElement().removeFromParent();
		return List.of(leafSearchField);
	}

	private String getLeafSearchText() {
		final String value = leafSearchField.getValue();
		return value != null ? value.trim() : "";
	}

	private String getParentSearchText() {
		final String value = parentSearchField.getValue();
		return value != null ? value.trim() : "";
	}

	public CProjectItem<?, ?> getSelectedBacklogItem() {
		final CGnntItem selectedLeaf = backlogBrowser.getSelectedLeafItem();
		final Object entity = selectedLeaf != null ? selectedLeaf.getEntity() : null;
		return entity instanceof CProjectItem<?, ?> ? (CProjectItem<?, ?>) entity : null;
	}

	private void onLeafDragStart(final CGnntItem dragged) {
		final Object entity = dragged != null ? dragged.getEntity() : null;
		if (entity == null) {
			return;
		}
		on_dragStart(new CDragStartEvent(this, java.util.List.of(entity), true));
	}

	/** @param leaf */
	private void onLeafSelected(final CGnntItem leaf) {
		select_notifyEvents(new CSelectEvent(this, true));
	}

	private void rebuildBacklogData() {
		final var built = CBacklogNavigatorHierarchyBuilder.buildBacklogData(hierarchyItemsByKey, scope,
				this::shouldIncludeParent, this::shouldIncludeLeaf, this::resolveItemOrder);
		final CGanttTimelineRange range = CBacklogNavigatorHierarchyBuilder
				.resolveTimelineRange(built.leafHierarchy().getFlatItems(), built.parentHierarchy().getFlatItems());
		final List<CGnntItem> leaves = built.leafHierarchy().getFlatItems();
		backlogBrowser.setBacklogData(built.parentHierarchy(), built.leafHierarchy(), hierarchyItemsByKey, range);
		backlogBrowser.setParentRollupSummaries(
				CBacklogNavigatorHierarchyBuilder.computeParentRollupsAll(hierarchyItemsByKey));
		backlogBrowser.setBacklogMetrics(CBacklogNavigatorHierarchyBuilder.computeBacklogMetrics(leaves));
		backlogBrowser.getParentQuickAccessPanel().refreshContextActionStates();
		backlogBrowser.getLeafQuickAccessPanel().refreshContextActionStates();
	}

	private void refreshBrowserOnly() {
		rebuildBacklogData();
	}

	public void refreshData() {
		reloadHierarchy();
	}

	private void reloadHierarchy() {
		if (project == null) {
			hierarchyItemsByKey = Map.of();
			final CGnntHierarchyResult emptyHierarchy = new CGnntHierarchyResult(List.of(), Map.of(), List.of());
			final CGanttTimelineRange emptyRange =
					CBacklogNavigatorHierarchyBuilder.resolveTimelineRange(List.of());
			backlogBrowser.setBacklogData(emptyHierarchy, emptyHierarchy, Map.of(), emptyRange);
			backlogBrowser.setParentRollupSummaries(Map.of());
			backlogBrowser.setBacklogMetrics(new CSprintPlanningSprintMetrics(0, 0, 0, 0));
			return;
		}
		try {
			final CHierarchyNavigationService hierarchyNavigationService =
					CSpringContext.getBean(CHierarchyNavigationService.class);
			final Map<String, CProjectItem<?, ?>> itemsByKey = new HashMap<>();
			for (final CProjectItem<?, ?> projectItem : hierarchyNavigationService.listHierarchyItems(project)) {
				final String entityKey = CHierarchyNavigationService.buildEntityKey(projectItem);
				if (entityKey != null) {
					itemsByKey.put(entityKey, projectItem);
				}
			}
			hierarchyItemsByKey = Map.copyOf(itemsByKey);
			rebuildBacklogData();
		} catch (final Exception e) {
			LOGGER.error("Failed to load backlog hierarchy reason={}", e.getMessage(), e);
		}
	}

	private int resolveItemOrder(final Object entity) {
		if (!(entity instanceof final ISprintableItem sprintableItem)) {
			return entity instanceof CProjectItem<?, ?>
					? CHierarchyNavigationService.getEntityLevel((CProjectItem<?, ?>) entity) : Integer.MAX_VALUE;
		}
		final CSprintItem sprintItem = sprintableItem.getSprintItem();
		final Integer order = sprintItem != null ? sprintItem.getItemOrder() : null;
		return order != null ? order : Integer.MAX_VALUE;
	}

	@Override
	public Set<ComponentEventListener<CSelectEvent>> select_getSelectListeners() {
		return selectListeners;
	}

	public void setProject(final CProject<?> project) {
		this.project = project;
		reloadHierarchy();
	}

	public void setScope(final ESprintPlanningScope scope) {
		this.scope = scope != null ? scope : ESprintPlanningScope.BACKLOG;
		rebuildBacklogData();
	}

	/** Configures whether the parent browser shows [done/total Tasks] rollup next to each parent name.
	 * @param show true to display rollup counts, false to hide them */
	public void setShowParentTaskRollup(final boolean show) {
		backlogBrowser.setShowParentTaskRollup(show);
	}

	private boolean isClosed(final CProjectItem<?, ?> item) {
		return item != null && item.getStatus() != null && Boolean.TRUE.equals(item.getStatus().getFinalStatus());
	}

	private boolean shouldIncludeLeaf(final CProjectItem<?, ?> item) {
		if (item == null) {
			return false;
		}
		if (!showClosed && isClosed(item)) {
			return false;
		}
		return CSearchTextFilterSupport.matches(getLeafSearchText(), item.getName(), item.getDescription());
	}

	private boolean shouldIncludeParent(final CProjectItem<?, ?> item) {
		if (item == null) {
			return false;
		}
		if (!showClosed && isClosed(item)) {
			return false;
		}
		return CSearchTextFilterSupport.matches(getParentSearchText(), item.getName(), item.getDescription());
	}

	/** Sets whether closed (final-status) items are shown in the backlog. Default is false (closed items hidden).
	 * @param showClosed true to show closed items, false to hide them */
	public void setShowClosed(final boolean showClosed) {
		// Change-guard: rebuildBacklogData() is expensive; skip when value is unchanged.
		if (this.showClosed != showClosed) {
			this.showClosed = showClosed;
			rebuildBacklogData();
		}
	}
}
