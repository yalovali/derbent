package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.gannt.ganntviewentity.view.components.CGanntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;

public class CGnntGrid extends CVerticalLayout {

	public static final String ID_GRID = "custom-gnnt-grid";
	private static final long serialVersionUID = 1L;

	private final CGrid<CGnntItem> grid;
	private final Consumer<CGnntItem> selectionListener;
	private final CVerticalLayout timelineHeaderContainer;
	private final List<CGnntItem> timelineItems = new ArrayList<>();
	private CGanttTimelineRange currentRange;
	private CGnntTimelineHeader timelineHeader;
	private int timelineWidth = 900;

	public CGnntGrid(final Consumer<CGnntItem> selectionListener) {
		this.selectionListener = selectionListener;
		setPadding(false);
		setSpacing(false);
		setWidthFull();

		timelineHeaderContainer = new CVerticalLayout();
		timelineHeaderContainer.setPadding(false);
		timelineHeaderContainer.setSpacing(false);
		timelineHeaderContainer.setWidthFull();

		grid = new CGrid<>(CGnntItem.class);
		CGrid.setupGrid(grid);
		grid.setId(ID_GRID);
		grid.setWidthFull();
		grid.setHeightFull();
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.asSingleSelect().addValueChangeListener(event -> this.selectionListener.accept(event.getValue()));

		configureColumns();
		add(timelineHeaderContainer, grid);
		setFlexGrow(0, timelineHeaderContainer);
		setFlexGrow(1, grid);
	}

	private void configureColumns() {
		grid.addIdColumn(CGnntItem::getEntity, CGnntItem::getEntityId, "ID", "id");
		grid.addColumn(CGnntItem::getEntityTypeTitle).setAutoWidth(true).setKey("entityType").setHeader("Type");
		grid.addComponentColumn(this::createNameComponent).setAutoWidth(true).setKey("name").setHeader("Name").setFlexGrow(1);
		grid.addColumn(CGnntItem::getStartDate).setWidth(CGrid.WIDTH_DATE).setFlexGrow(0).setKey("startDate").setHeader("Start");
		grid.addColumn(CGnntItem::getEndDate).setWidth(CGrid.WIDTH_DATE).setFlexGrow(0).setKey("endDate").setHeader("End");
		grid.addColumnEntityNamed(CGnntItem::getAssignedTo, "Responsible");
		grid.addComponentColumn(item -> {
			if (item.getEntity() instanceof IHasStatusAndWorkflow<?>) {
				try {
					return new CLabelEntity(((IHasStatusAndWorkflow<?>) item.getEntity()).getStatus());
				} catch (final Exception e) {
					return new Span("-");
				}
			}
			return new Span("-");
		}).setWidth(CGrid.WIDTH_REFERENCE).setFlexGrow(0).setKey("status").setHeader("Status");
		grid.addComponentColumn(this::createTimelineComponent).setWidth(timelineWidth + "px").setFlexGrow(0).setKey("timeline").setHeader("Timeline");
	}

	private Component createNameComponent(final CGnntItem item) {
		final Span name = new Span(item.getIndentedName());
		name.getStyle().set("padding-left", (item.getHierarchyLevel() * 16) + "px");
		name.getStyle().set("font-weight", item.getHierarchyLevel() == 0 ? "600" : "400");
		return name;
	}

	private Component createTimelineComponent(final CGnntItem item) {
		if (currentRange == null) {
			return new Span("-");
		}
		return new CGnntTimelineRow(item, currentRange.startDate(), currentRange.endDate(), timelineWidth);
	}

	private void rebuildTimelineHeader() {
		timelineHeaderContainer.removeAll();
		if (currentRange == null) {
			return;
		}
		timelineHeader = new CGnntTimelineHeader(currentRange.startDate(), currentRange.endDate(), timelineWidth, range -> {
			currentRange = range;
			grid.getDataProvider().refreshAll();
		}, newWidth -> {
			timelineWidth = newWidth;
			currentRange = new CGanttTimelineRange(currentRange.startDate(), currentRange.endDate());
			rebuildTimelineHeader();
			grid.getColumnByKey("timeline").setWidth(timelineWidth + "px");
			grid.getDataProvider().refreshAll();
		});
		timelineHeaderContainer.add(timelineHeader);
	}

	public void setItems(final List<CGnntItem> items, final CGanttTimelineRange range) {
		timelineItems.clear();
		if (items != null) {
			timelineItems.addAll(items);
		}
		currentRange = range;
		rebuildTimelineHeader();
		grid.setItems(timelineItems);
		if (!timelineItems.isEmpty()) {
			grid.select(timelineItems.get(0));
		} else {
			selectionListener.accept(null);
		}
	}
}
