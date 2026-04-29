package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CComponentId;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
import tech.derbent.api.ui.component.enhanced.CContextMenuSupport;
import tech.derbent.api.ui.component.enhanced.CQuickAccessPanel;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;

/** Shared base for Gnnt board grids, including the timeline resize and scroll state bridge used by the client-side helpers. */
public abstract class CAbstractGnntGridBase extends CVerticalLayout {

	protected static final int NAME_COLUMN_WIDTH_PX = 300;
	private static final long serialVersionUID = 1L;
	private static final int TIMELINE_COLUMN_MIN_WIDTH_PX = 400;
	private static final int TIMELINE_RESERVED_PADDING_PX = 48;
	protected static final String WIDTH_DATE_COMPACT = "110px";
	protected static final String WIDTH_RESPONSIBLE_COMPACT = "135px";
	protected static final String WIDTH_STATUS_COMPACT = "140px";
	private CGanttTimelineRange currentRange;
	protected final Grid<CGnntItem> grid;
	private List<CContextActionDefinition<CGnntItem>> itemContextActions = List.of();
	private GridContextMenu<CGnntItem> itemContextMenu;
	private final Map<String, GridMenuItem<CGnntItem>> itemContextMenuItemsByKey = new LinkedHashMap<>();
	// Optional row double-click handler (feature-specific; e.g. sprint planning opens edit dialogs).
	private Consumer<CGnntItem> itemDoubleClickHandler;
	private CGnntItem lastContextMenuItem;
	private double lastKnownScrollLeft;
	private double lastKnownScrollTop;
	private Component leftHeaderComponent;
	// Optional toolbar hosted in the joined header row (used by Gnnt/Sprint planning views for quick actions + summary).
	private CQuickAccessPanel quickAccessPanel;
	protected final Consumer<CGnntItem> selectionListener;
	private CGnntTimelineHeader timelineHeader;
	private HeaderRow timelineHeaderRow;
	private int timelineWidth = 900;

	protected CAbstractGnntGridBase(final Grid<CGnntItem> grid, final String gridId, final Consumer<CGnntItem> selectionListener) {
		this.grid = grid;
		this.selectionListener = selectionListener;
		setPadding(false);
		setSpacing(false);
		setWidthFull();
		CGrid.setupGrid(grid);
		grid.setId(gridId);
		grid.setWidthFull();
		grid.setHeightFull();
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.asSingleSelect().addValueChangeListener(event -> {
			refreshHeaderActionStates();
			this.selectionListener.accept(event.getValue());
		});
		grid.addItemDoubleClickListener(event -> {
			// Feature-specific hook: selection stays unchanged, but double-click can open a details/edit dialog.
			if (itemDoubleClickHandler != null) {
				itemDoubleClickHandler.accept(event.getItem());
			}
		});
		configureColumns();
		configureTimelineHeaderRow();
		add(grid);
		setFlexGrow(1, grid);
		registerResizeTracking();
		registerScrollTracking();
	}

	protected void addIdColumn() {
		CGrid.styleColumnHeader(grid.addComponentColumn(item -> {
			try {
				return new CComponentId(item.getEntity(), item.getEntityId());
			} catch (final Exception e) {
				return new Span(item.getEntityId() != null ? String.valueOf(item.getEntityId()) : "-");
			}
		}).setWidth(CGrid.WIDTH_ID).setFlexGrow(0).setResizable(true).setKey("id").setComparator(CGnntItem::getEntityId), "ID");
	}

	protected void addSharedColumns() {
		addIdColumn();
		grid.addComponentColumn(this::createIconComponent).setWidth(CGrid.WIDTH_IMAGE).setFlexGrow(0).setResizable(true).setKey("icon").setHeader("");
		configureNameColumn();
		addTrailingSharedColumns();
	}

	protected void addTrailingSharedColumns() {
		grid.addColumn(CGnntItem::getStartDate).setWidth(WIDTH_DATE_COMPACT).setFlexGrow(0).setResizable(true).setKey("startDate").setHeader("Start");
		grid.addColumn(CGnntItem::getEndDate).setWidth(WIDTH_DATE_COMPACT).setFlexGrow(0).setResizable(true).setKey("endDate").setHeader("End");
		grid.addComponentColumn(item -> {
			if (item.getAssignedTo() == null) {
				return new Span("-");
			}
			try {
				return new CLabelEntity(item.getAssignedTo());
			} catch (final Exception e) {
				return new Span(item.getResponsibleName());
			}
		}).setWidth(WIDTH_RESPONSIBLE_COMPACT).setFlexGrow(0).setResizable(true).setKey("assignedTo").setHeader("Responsible");
		grid.addComponentColumn(item -> {
			if (item.getEntity() instanceof IHasStatusAndWorkflow<?>) {
				try {
					return new CLabelEntity(((IHasStatusAndWorkflow<?>) item.getEntity()).getStatus());
				} catch (final Exception e) {
					return new Span("-");
				}
			}
			return new Span("-");
		}).setWidth(WIDTH_STATUS_COMPACT).setFlexGrow(0).setResizable(true).setKey("status").setHeader("Status");
		grid.addComponentColumn(this::createTimelineComponent).setWidth(timelineWidth + "px").setFlexGrow(0).setKey("timeline").setHeader("Timeline");
	}

	protected abstract void configureColumns();
	protected abstract void configureNameColumn();

	private void configureTimelineHeaderRow() {
		timelineHeaderRow = grid.prependHeaderRow();
		grid.getColumns().forEach((final Grid.Column<CGnntItem> column) -> timelineHeaderRow.getCell(column).setText(""));
	}

	protected Component createIconComponent(final CGnntItem item) {
		try {
			final Icon icon =
					item.getEntity() != null ? CColorUtils.getIconForEntity(item.getEntity()) : CColorUtils.getIconFromString(item.getIconString());
			icon.getStyle().set("color", item.getColorCode());
			return icon;
		} catch (final Exception e) {
			try {
				return CColorUtils.createStyledIcon(item.getIconString(), item.getColorCode());
			} catch (final Exception innerException) {
				try {
					return CColorUtils.createStyledIcon(CGnntItem.DEFAULT_ICON, CGnntItem.DEFAULT_COLOR);
				} catch (final Exception defaultIconException) {
					return new Span("-");
				}
			}
		}
	}

	private Component createTimelineComponent(final CGnntItem item) {
		if (currentRange == null) {
			return new Span("-");
		}
		return new CGnntTimelineRow(item, currentRange.startDate(), currentRange.endDate(), timelineWidth);
	}

	private void ensureItemContextMenu() {
		if (itemContextMenu != null) {
			return;
		}
		itemContextMenu = grid.addContextMenu();
		// Bind context-menu state to the row under the mouse so right-click actions stay in sync even when selection has not changed yet.
		itemContextMenu.setDynamicContentHandler(item -> {
			lastContextMenuItem = item;
			if (item != null) {
				grid.select(item);
			}
			refreshItemContextMenuState(item);
			return item != null;
		});
		itemContextMenu.addGridContextMenuOpenedListener(event -> {
			final CGnntItem contextItem = event.getItem().orElse(null);
			if (!event.isOpened()) {
				lastContextMenuItem = null;
				return;
			}
			lastContextMenuItem = contextItem;
			if (contextItem != null) {
				grid.select(contextItem);
			}
			refreshItemContextMenuState(contextItem);
		});
	}

	protected Grid<CGnntItem> getGrid() { return grid; }

	/** Places a component into the left-side timeline header row (the same row that hosts the timeline range header).
	 * <p>
	 * This is intentionally a small hook so feature-specific UIs (for example sprint planning) can place expand/collapse controls aligned with the
	 * timeline header without changing the shared Gnnt project views.
	 * </p>
	 */
	protected final Component getLeftHeaderComponent() { return leftHeaderComponent; }

	protected abstract int getNonTimelineColumnWidthPx();

	public final CQuickAccessPanel getQuickAccessPanel() { return quickAccessPanel; }

	public CGnntItem getSelectedItem() { return grid.asSingleSelect().getValue(); }

	@ClientCallable
	private void onGridScroll(final double scrollTop, final double scrollLeft) {
		// Keep the last scroll offsets server-side so refreshes can re-apply them after new data is set.
		lastKnownScrollTop = scrollTop;
		lastKnownScrollLeft = scrollLeft;
	}

	@ClientCallable
	private void onTimelineHostResize(final double hostWidth) {
		// Keep the timeline scaled to the free space in the grid instead of letting the name column absorb resize changes.
		final int availableWidth =
				Math.max(TIMELINE_COLUMN_MIN_WIDTH_PX, (int) Math.round(hostWidth) - getNonTimelineColumnWidthPx() - TIMELINE_RESERVED_PADDING_PX);
		if (availableWidth == timelineWidth) {
			return;
		}
		timelineWidth = availableWidth;
		final Grid.Column<CGnntItem> timelineColumn = grid.getColumnByKey("timeline");
		if (timelineColumn != null) {
			timelineColumn.setWidth(timelineWidth + "px");
		}
		rebuildTimelineHeader();
		grid.getDataProvider().refreshAll();
	}

	private void rebuildTimelineHeader() {
		final Grid.Column<CGnntItem> timelineColumn = grid.getColumnByKey("timeline");
		if (timelineHeaderRow == null || timelineColumn == null) {
			return;
		}
		if (currentRange == null) {
			timelineHeaderRow.getCell(timelineColumn).setText("");
			return;
		}
		timelineHeader = new CGnntTimelineHeader(currentRange.startDate(), currentRange.endDate(), timelineWidth, range -> {
			currentRange = range;
			grid.getDataProvider().refreshAll();
		}, newWidth -> {
			timelineWidth = newWidth;
			currentRange = new CGanttTimelineRange(currentRange.startDate(), currentRange.endDate());
			timelineColumn.setWidth(timelineWidth + "px");
			rebuildTimelineHeader();
			grid.getDataProvider().refreshAll();
		});
		timelineHeaderRow.getCell(timelineColumn).setComponent(timelineHeader);
	}

	protected final void refreshHeaderActionStates() {
		if (quickAccessPanel != null) {
			quickAccessPanel.refreshContextActionStates();
		}
		refreshItemContextMenuState(grid.asSingleSelect().getValue());
	}

	private void refreshItemContextMenuState(final CGnntItem contextItem) {
		if (itemContextMenuItemsByKey.isEmpty()) {
			return;
		}
		for (final CContextActionDefinition<CGnntItem> action : itemContextActions) {
			final GridMenuItem<CGnntItem> menuItem = itemContextMenuItemsByKey.get(action.getKey());
			if (menuItem == null) {
				continue;
			}
			CContextMenuSupport.refreshGridActionState(menuItem, action, contextItem);
		}
	}

	private void registerResizeTracking() {
		addAttachListener(event -> getElement().executeJs("""
					const host = this;
					if (host.__ganttResizeObserver) {
					  return;
					}
					const notify = () => {
					  const width = Math.round(host.getBoundingClientRect().width || 0);
					  if (host.$server && width > 0) {
					    host.$server.onTimelineHostResize(width);
					  }
					};
					host.__ganttResizeObserver = new ResizeObserver(() => notify());
					host.__ganttResizeObserver.observe(host);
					window.addEventListener('resize', notify);
					notify();
				"""));
	}

	private void registerScrollTracking() {
		addAttachListener(event -> grid.getElement().executeJs("""
					const grid = this;
					if (grid.__gnntScrollObserverInstalled) {
					  return;
					}
					const scroller = grid.shadowRoot && grid.shadowRoot.querySelector('[part="items"]');
					if (!scroller) {
					  return;
					}
					let timeoutHandle = null;
					const notify = () => {
					  if (timeoutHandle) {
					    return;
					  }
					  timeoutHandle = window.setTimeout(() => {
					    timeoutHandle = null;
					    if (grid.$server) {
					      grid.$server.onGridScroll(scroller.scrollTop, scroller.scrollLeft);
					    }
					  }, 100);
					};
					scroller.addEventListener('scroll', notify, { passive: true });
					notify();
					grid.__gnntScrollObserverInstalled = true;
				"""));
	}

	protected void restoreGridScrollPosition() {
		grid.getElement().executeJs("""
					const scroller = this.shadowRoot && this.shadowRoot.querySelector('[part="items"]');
					if (scroller) {
					  scroller.scrollTop = $0;
					  scroller.scrollLeft = $1;
					}
				""", lastKnownScrollTop, lastKnownScrollLeft);
	}

	public abstract void setHierarchy(CGnntHierarchyResult hierarchyResult, CGanttTimelineRange range);

	public final void setItemContextActions(final List<CContextActionDefinition<CGnntItem>> actions) {
		itemContextActions = actions != null ? List.copyOf(actions) : List.of();
		ensureItemContextMenu();
		itemContextMenu.removeAll();
		itemContextMenuItemsByKey.clear();
		for (final CContextActionDefinition<CGnntItem> action : itemContextActions) {
			final GridMenuItem<CGnntItem> menuItem = CContextMenuSupport.registerGridAction(itemContextMenu, action, () -> lastContextMenuItem);
			itemContextMenuItemsByKey.put(action.getKey(), menuItem);
		}
	}

	public final void setItemDoubleClickHandler(final Consumer<CGnntItem> itemDoubleClickHandler) {
		this.itemDoubleClickHandler = itemDoubleClickHandler;
	}

	/** Sets the joined (non-timeline) header content.
	 * <p>
	 * Used by {@link CQuickAccessPanel} hosting so Gnnt/timeline grids can attach quick actions (refresh, details toggle, expand/collapse, metrics)
	 * in a single, consistent slot.
	 * </p>
	 */
	protected final void setLeftHeaderComponent(final Component component) {
		// Keep a reference so subclasses can expose quick-action panels via getters (and tests can assert installed header content).
		leftHeaderComponent = component;
		if (timelineHeaderRow == null) {
			return;
		}
		final Grid.Column<CGnntItem> timelineColumn = grid.getColumnByKey("timeline");
		final List<Grid.Column<CGnntItem>> joinColumns = new ArrayList<>();
		for (final Grid.Column<CGnntItem> column : grid.getColumns()) {
			if (timelineColumn != null && timelineColumn.equals(column)) {
				continue;
			}
			joinColumns.add(column);
		}
		if (joinColumns.isEmpty()) {
			return;
		}
		if (joinColumns.size() == 1) {
			// Vaadin forbids join() with < 2 columns; minimal grids (e.g. parent browsers) still need header quick actions.
			final Grid.Column<CGnntItem> onlyColumn = joinColumns.get(0);
			if (component == null) {
				timelineHeaderRow.getCell(onlyColumn).setText("");
				return;
			}
			timelineHeaderRow.getCell(onlyColumn).setComponent(component);
			return;
		}
		@SuppressWarnings ({
				"rawtypes"
		})
		final Grid.Column[] columnArray = joinColumns.toArray(new Grid.Column[0]);
		if (component == null) {
			timelineHeaderRow.join(columnArray).setText("");
			return;
		}
		timelineHeaderRow.join(columnArray).setComponent(component);
	}

	/** Stores and installs a shared quick access panel into the left header slot. */
	protected final void setQuickAccessPanel(final CQuickAccessPanel panel) {
		quickAccessPanel = panel;
		setLeftHeaderComponent(panel);
	}

	public final void setSelectedItem(final CGnntItem item) {
		if (item == null) {
			grid.deselectAll();
			refreshHeaderActionStates();
			return;
		}
		grid.asSingleSelect().setValue(item);
	}

	protected void updateTimelineRange(final CGanttTimelineRange range) {
		currentRange = range;
		rebuildTimelineHeader();
	}
}
