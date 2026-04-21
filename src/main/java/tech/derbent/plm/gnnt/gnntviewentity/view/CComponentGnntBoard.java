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
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntViewEntity;
import tech.derbent.plm.gnnt.gnntviewentity.service.CGnntTimelineService;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntGrid;

public class CComponentGnntBoard extends CComponentBase<CGnntViewEntity> {

	public static final String ID_BOARD = "custom-gnnt-board";
	public static final String ID_SUMMARY = "custom-gnnt-summary";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentGnntBoard.class);
	private static final long serialVersionUID = 1L;
	private final CComponentItemDetails componentItemDetails;
	private final CGnntGrid componentTimelineGrid;
	private final ISessionService sessionService;
	private final Span summaryLabel;
	private final CGnntTimelineService timelineService;

	public CComponentGnntBoard(final ISessionService sessionService) {
		this.sessionService = sessionService;
		timelineService = CSpringContext.getBean(CGnntTimelineService.class);
		try {
			componentItemDetails = new CComponentItemDetails(sessionService);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to initialize Gnnt details component", e);
		}
		componentTimelineGrid = new CGnntGrid(this::onTimelineItemSelected);
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
		topLayout.add(summaryLabel, componentTimelineGrid);
		topLayout.setFlexGrow(0, summaryLabel);
		topLayout.setFlexGrow(1, componentTimelineGrid);
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
				componentTimelineGrid.setItems(List.of(), timelineService.resolveRange(List.of()));
				componentItemDetails.clear();
				return;
			}
			final List<CGnntItem> items = timelineService.listTimelineItems(currentView);
			summaryLabel.setText("Gnnt board '" + currentView.getName() + "' - " + items.size() + " agile timeline items");
			componentTimelineGrid.setItems(items, timelineService.resolveRange(items));
		} catch (final Exception e) {
			LOGGER.error("Failed to refresh Gnnt board: {}", e.getMessage());
			throw e;
		}
	}
}
