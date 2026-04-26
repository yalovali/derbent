package tech.derbent.plm.gnnt.gnntviewentity.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.page.view.CDynamicPageRouter;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentItemDetails;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
import tech.derbent.api.ui.component.enhanced.CQuickAccessPanel;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntViewEntity;
import tech.derbent.plm.gnnt.gnntviewentity.domain.EGnntGridType;
import tech.derbent.plm.gnnt.gnntviewentity.service.CGnntHierarchyMoveService;
import tech.derbent.plm.gnnt.gnntviewentity.service.CGnntTimelineService;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CAbstractGnntGridBase;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntBoardFilterToolbar;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntGrid;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTreeGrid;

public class CComponentGnntBoard extends CComponentBase<CGnntViewEntity> {

	private static final double DEFAULT_SPLITTER_POSITION = 58.0;
	public static final String ID_BOARD = "custom-gnnt-board";
	public static final String ID_DETAILS_TOGGLE_BUTTON = "custom-gnnt-details-toggle-button";
	public static final String ID_RENDERER_CONTAINER = "custom-gnnt-renderer-container";
	public static final String ID_SUMMARY = "custom-gnnt-summary";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentGnntBoard.class);
	private static final long serialVersionUID = 1L;
	private CAbstractGnntGridBase activeGridComponent;
	private final CComponentItemDetails componentItemDetails;
	private boolean detailsVisible = true;
	private final CGnntBoardFilterToolbar filterToolbar;
	private final CGnntHierarchyMoveService hierarchyMoveService;
	private final CVerticalLayout layoutRendererContainer;
	private double previousSplitterPosition = DEFAULT_SPLITTER_POSITION;
	private CEntityNamed<?> selectedDetailsEntity;
	private SplitLayout splitLayout;
	private final CGnntTimelineService timelineService;

	public CComponentGnntBoard(final ISessionService sessionService) {
		timelineService = CSpringContext.getBean(CGnntTimelineService.class);
		hierarchyMoveService = CSpringContext.getBean(CGnntHierarchyMoveService.class);
		try {
			componentItemDetails = new CComponentItemDetails(sessionService);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to initialize Gnnt details component", e);
		}
		filterToolbar = new CGnntBoardFilterToolbar();
		filterToolbar.addFilterChangeListener(criteria -> refreshComponent());
		layoutRendererContainer = new CVerticalLayout();
		layoutRendererContainer.setId(ID_RENDERER_CONTAINER);
		layoutRendererContainer.setPadding(false);
		layoutRendererContainer.setSpacing(false);
		layoutRendererContainer.setWidthFull();
		layoutRendererContainer.setHeightFull();
		initializeLayout();
	}

	private List<CContextActionDefinition<CGnntItem>> buildContextActions() {
		return List.of(
				CContextActionDefinition.of("show-details", "Show details", com.vaadin.flow.component.icon.VaadinIcon.SEARCH,
						context -> context != null && context.getEntity() != null, context -> context != null && context.getEntity() != null,
						this::showItemDetails),
				CContextActionDefinition.of("open-page", "Open page", com.vaadin.flow.component.icon.VaadinIcon.EDIT,
						context -> context != null && context.getEntity() != null, context -> context != null && context.getEntity() != null,
						this::openItemPage),
				CContextActionDefinition.of("refresh", "Refresh", com.vaadin.flow.component.icon.VaadinIcon.REFRESH, context -> true, context -> true,
						context -> refreshComponent()));
	}

	private void configureQuickAccessPanel() {
		final CQuickAccessPanel panel = activeGridComponent != null ? activeGridComponent.getQuickAccessPanel() : null;
		if (panel == null) {
			return;
		}
		panel.setOnToggleDetails(this::toggleDetailsPanel);
		panel.setOnRefresh(this::refreshComponent);
		panel.setDetailsVisible(detailsVisible);
		final List<CContextActionDefinition<CGnntItem>> contextActions = buildContextActions();
		panel.setContextActions(contextActions, () -> activeGridComponent != null ? activeGridComponent.getSelectedItem() : null);
		// Keep filter actions consistent with sprint planning: compact actions belong in the quick-access header.
		panel.addControls(filterToolbar.extractQuickControlsForQuickAccess());
		if (activeGridComponent != null) {
			activeGridComponent.setItemContextActions(contextActions);
		}
		// Always keep a placeholder summary element installed; refresh() fills in the item count.
		updateQuickAccessSummary(panel, 0);
	}

	private void ensureActiveGrid(final EGnntGridType gridType) {
		final EGnntGridType safeGridType = gridType != null ? gridType : EGnntGridType.FLAT;
		if (activeGridComponent != null && safeGridType == EGnntGridType.FLAT && activeGridComponent instanceof CGnntGrid) {
			return;
		}
		if (activeGridComponent != null && safeGridType == EGnntGridType.TREE && activeGridComponent instanceof CGnntTreeGrid) {
			return;
		}
		layoutRendererContainer.removeAll();
		activeGridComponent = safeGridType == EGnntGridType.TREE ? new CGnntTreeGrid(this::onTimelineItemSelected, this::onTimelineItemMoved)
				: new CGnntGrid(this::onTimelineItemSelected);
		// Inline-edit saves should rebuild the hierarchy/timeline immediately.
		activeGridComponent.setRefreshCallback(this::refreshComponent);
		layoutRendererContainer.add(activeGridComponent);
		layoutRendererContainer.setFlexGrow(1, activeGridComponent);
		configureQuickAccessPanel();
	}

	private void initializeLayout() {
		setId(ID_BOARD);
		setPadding(false);
		setSpacing(false);
		setWidthFull();
		setHeightFull();
		final CVerticalLayout topLayout = new CVerticalLayout();
		topLayout.setPadding(false);
		topLayout.setSpacing(false);
		topLayout.setWidthFull();
		topLayout.setHeightFull();
		// Filters are hosted inside the grid-header quick-access panel (more vertical space for the timeline rows).
		topLayout.add(layoutRendererContainer);
		topLayout.setFlexGrow(1, layoutRendererContainer);
		splitLayout = new SplitLayout(topLayout, componentItemDetails);
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.setSplitterPosition(DEFAULT_SPLITTER_POSITION);
		splitLayout.setWidthFull();
		splitLayout.setHeightFull();
		add(splitLayout);
	}

	private void onTimelineItemMoved(final CGnntItem draggedItem, final CGnntItem targetItem) {
		try {
			Check.notNull(getValue(), "Select a Gnnt view before reorganizing hierarchy");
			Check.notNull(draggedItem, "Dragged Gnnt item cannot be null");
			Check.notNull(targetItem, "Target Gnnt item cannot be null");
			final boolean filtersActive = filterToolbar.getCurrentCriteria().hasAnyFilter();
			final int draggedLevel = CHierarchyNavigationService.getEntityLevel(draggedItem.getEntity());
			final int targetLevel = CHierarchyNavigationService.getEntityLevel(targetItem.getEntity());
			final CEntityNamed<?> newParent = hierarchyMoveService.reparentItem(draggedItem.getEntity(), targetItem.getEntity());
			refreshComponent();
			final String parentLabel = newParent != null ? newParent.getName() : "root";
			final String suffix = draggedLevel == targetLevel ? " (dropped on same level → moved as sibling; ordering follows the default sort)" : "";
			if (filtersActive) {
				CNotificationService.showWarning("Moved '%s' under '%s'%s. The item may disappear from the filtered view until filters are cleared."
						.formatted(draggedItem.getName(), parentLabel, suffix));
				return;
			}
			CNotificationService.showSuccess("Moved '%s' under '%s'%s".formatted(draggedItem.getName(), parentLabel, suffix));
		} catch (final IllegalArgumentException e) {
			LOGGER.debug("Invalid Gnnt move: {}", e.getMessage());
			// Use a modal dialog so long hierarchy rules stay readable while the user decides the next drop.
			CNotificationService.showWarningDialog("Invalid Gnnt drop", e.getMessage());
		} catch (final Exception e) {
			LOGGER.error("Failed to move Gnnt item: {}", e.getMessage(), e);
			CNotificationService.showException("Unable to reposition Gnnt item", e);
		}
	}

	private void onTimelineItemSelected(final CGnntItem selectedItem) {
		selectedDetailsEntity = selectedItem != null ? selectedItem.getEntity() : null;
		if (!detailsVisible) {
			// Skip detail page lookups while hidden; selection is still remembered for later.
			return;
		}
		if (selectedDetailsEntity == null) {
			componentItemDetails.clear();
			return;
		}
		componentItemDetails.setValue(selectedDetailsEntity);
	}

	@Override
	protected void onValueChanged(final CGnntViewEntity oldValue, final CGnntViewEntity newValue, final boolean fromClient) {
		LOGGER.debug("Gnnt board changed from {} to {}", oldValue != null ? oldValue.getName() : "null",
				newValue != null ? newValue.getName() : "null");
		refreshComponent();
	}

	private void openItemPage(final CGnntItem item) {
		if (item == null || item.getEntity() == null) {
			return;
		}
		try {
			showItemDetails(item);
			CDynamicPageRouter.navigateToEntity(item.getEntity());
		} catch (final Exception e) {
			CNotificationService.showException("Unable to open Gnnt item page", e);
		}
	}

	@Override
	protected void refreshComponent() {
		try {
			final CGnntViewEntity currentView = getValue();
			if (currentView == null) {
				filterToolbar.setAvailableEntityTypes(List.of());
				ensureActiveGrid(EGnntGridType.FLAT);
				activeGridComponent.setHierarchy(new CGnntHierarchyResult(List.of(), null, List.of()), timelineService.resolveRange(List.of()));
				updateQuickAccessSummary(activeGridComponent.getQuickAccessPanel(), 0);
				selectedDetailsEntity = null;
				if (detailsVisible) {
					componentItemDetails.clear();
				}
				return;
			}
			filterToolbar.setProject(currentView.getProject());
			ensureActiveGrid(currentView.getGridType());
			activeGridComponent.setInlineEditingEnabled(Boolean.TRUE.equals(currentView.getIsInlineEditingEnabled()));
			final CGnntHierarchyResult allItemsHierarchy = timelineService.buildHierarchy(currentView, null);
			filterToolbar.setAvailableEntityTypes(allItemsHierarchy.getFlatItems());
			final CGnntHierarchyResult hierarchyResult = timelineService.buildHierarchy(currentView, filterToolbar.getCurrentCriteria());
			final List<CGnntItem> flatItems = hierarchyResult.getFlatItems();
			activeGridComponent.setHierarchy(hierarchyResult, timelineService.resolveRange(flatItems));
			updateQuickAccessSummary(activeGridComponent.getQuickAccessPanel(), flatItems != null ? flatItems.size() : 0);
		} catch (final Exception e) {
			LOGGER.error("Failed to refresh Gnnt board: {}", e.getMessage(), e);
			throw e;
		}
	}

	private void showItemDetails(final CGnntItem item) {
		if (activeGridComponent != null) {
			// Keep row highlight, details, and quick-access actions in sync for toolbar and right-click flows.
			activeGridComponent.setSelectedItem(item);
			return;
		}
		onTimelineItemSelected(item);
	}

	private void toggleDetailsPanel() {
		if (splitLayout == null) {
			return;
		}
		detailsVisible = !detailsVisible;
		final CQuickAccessPanel panel = activeGridComponent != null ? activeGridComponent.getQuickAccessPanel() : null;
		if (panel != null) {
			panel.setDetailsVisible(detailsVisible);
		}
		if (detailsVisible) {
			componentItemDetails.setVisible(true);
			splitLayout.setSplitterPosition(previousSplitterPosition);
			// Rehydrate the details panel only when it is visible again (avoids work on each selection while hidden).
			if (selectedDetailsEntity == null) {
				componentItemDetails.clear();
			} else {
				componentItemDetails.setValue(selectedDetailsEntity);
			}
		} else {
			previousSplitterPosition = splitLayout.getSplitterPosition();
			splitLayout.setSplitterPosition(100.0);
			componentItemDetails.setVisible(false);
		}
	}

	private void updateQuickAccessSummary(final CQuickAccessPanel panel, final int itemCount) {
		if (panel == null) {
			return;
		}
		final Span summary = panel.getControl("summary").filter(Span.class::isInstance).map(Span.class::cast).orElseGet(() -> {
			final Span created = new Span();
			created.setId(ID_SUMMARY);
			created.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)").set("padding", "0 6px");
			panel.addControl("summary", created);
			return created;
		});
		summary.setText("Items: %d".formatted(itemCount));
	}
}
