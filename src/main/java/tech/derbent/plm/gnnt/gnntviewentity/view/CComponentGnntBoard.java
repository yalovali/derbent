package tech.derbent.plm.gnnt.gnntviewentity.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.ui.component.enhanced.CComponentItemDetails;
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

	public static final String ID_BOARD = "custom-gnnt-board";
	public static final String ID_RENDERER_CONTAINER = "custom-gnnt-renderer-container";
	public static final String ID_SUMMARY = "custom-gnnt-summary";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentGnntBoard.class);
	private static final long serialVersionUID = 1L;
	private CAbstractGnntGridBase activeGridComponent;
	private final CComponentItemDetails componentItemDetails;
	private final CGnntBoardFilterToolbar filterToolbar;
	private final CGnntHierarchyMoveService hierarchyMoveService;
	private final CVerticalLayout layoutRendererContainer;
	private final ISessionService sessionService;
	private final Span summaryLabel;
	private final CGnntTimelineService timelineService;

	public CComponentGnntBoard(final ISessionService sessionService) {
		this.sessionService = sessionService;
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
		summaryLabel = new Span("Gnnt board");
		initializeLayout();
	}

	private void initializeLayout() {
		setId(ID_BOARD);
		setPadding(false);
		setSpacing(false);
		setWidthFull();
		setHeightFull();
		summaryLabel.setId(ID_SUMMARY);
		final CVerticalLayout topLayout = new CVerticalLayout();
		topLayout.setPadding(false);
		topLayout.setSpacing(false);
		topLayout.setWidthFull();
		topLayout.setHeightFull();
		topLayout.add(summaryLabel, filterToolbar, layoutRendererContainer);
		topLayout.setFlexGrow(0, summaryLabel);
		topLayout.setFlexGrow(0, filterToolbar);
		topLayout.setFlexGrow(1, layoutRendererContainer);
		final SplitLayout splitLayout = new SplitLayout(topLayout, componentItemDetails);
		splitLayout.setOrientation(SplitLayout.Orientation.VERTICAL);
		splitLayout.setSplitterPosition(58.0);
		splitLayout.setWidthFull();
		splitLayout.setHeightFull();
		add(splitLayout);
	}

	private void onTimelineItemSelected(final CGnntItem selectedItem) {
		if (selectedItem == null) {
			componentItemDetails.clear();
			return;
		}
		componentItemDetails.setValue(selectedItem.getEntity());
	}

	private void onTimelineItemMoved(final CGnntItem draggedItem, final CGnntItem targetItem) {
		try {
			Check.notNull(getValue(), "Select a Gnnt view before reorganizing hierarchy");
			Check.notNull(draggedItem, "Dragged Gnnt item cannot be null");
			Check.notNull(targetItem, "Target Gnnt item cannot be null");
			if (filterToolbar.getCurrentCriteria().hasAnyFilter()) {
				CNotificationService.showWarning("Clear Gnnt filters before dragging items to a new parent.");
				return;
			}
			hierarchyMoveService.reparentItem(draggedItem.getEntity(), targetItem.getEntity());
			refreshComponent();
			CNotificationService.showSuccess("Moved '%s' under '%s'".formatted(draggedItem.getName(), targetItem.getName()));
		} catch (final Exception e) {
			LOGGER.error("Failed to move Gnnt item: {}", e.getMessage());
			CNotificationService.showException("Unable to reposition Gnnt item", e);
		}
	}

	@Override
	protected void onValueChanged(final CGnntViewEntity oldValue, final CGnntViewEntity newValue, final boolean fromClient) {
		LOGGER.debug("Gnnt board changed from {} to {}", oldValue != null ? oldValue.getName() : "null",
				newValue != null ? newValue.getName() : "null");
		refreshComponent();
	}

	@Override
	protected void refreshComponent() {
		try {
			final CGnntViewEntity currentView = getValue();
			if (currentView == null) {
				summaryLabel.setText("Select a Gnnt view to load the board.");
				filterToolbar.setAvailableEntityTypes(List.of());
				ensureActiveGrid(EGnntGridType.FLAT);
				activeGridComponent.setHierarchy(new CGnntHierarchyResult(List.of(), null, List.of()), timelineService.resolveRange(List.of()));
				componentItemDetails.clear();
				return;
			}
			filterToolbar.setProject(currentView.getProject());
			ensureActiveGrid(currentView.getGridType());
			final CGnntHierarchyResult allItemsHierarchy = timelineService.buildHierarchy(currentView, null);
			filterToolbar.setAvailableEntityTypes(allItemsHierarchy.getFlatItems());
			final CGnntHierarchyResult hierarchyResult = timelineService.buildHierarchy(currentView, filterToolbar.getCurrentCriteria());
			final List<CGnntItem> flatItems = hierarchyResult.getFlatItems();
			summaryLabel.setText("Gnnt board '" + currentView.getName() + "' [" + currentView.getGridType().name() + "] - " + flatItems.size()
					+ " agile timeline items");
			activeGridComponent.setHierarchy(hierarchyResult, timelineService.resolveRange(flatItems));
		} catch (final Exception e) {
			LOGGER.error("Failed to refresh Gnnt board: {}", e.getMessage());
			throw e;
		}
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
		activeGridComponent = safeGridType == EGnntGridType.TREE ? new CGnntTreeGrid(this::onTimelineItemSelected, this::onTimelineItemMoved) : new CGnntGrid(
				this::onTimelineItemSelected);
		layoutRendererContainer.add(activeGridComponent);
		layoutRendererContainer.setFlexGrow(1, activeGridComponent);
	}
}
