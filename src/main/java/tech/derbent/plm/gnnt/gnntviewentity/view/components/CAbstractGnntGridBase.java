package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.util.function.Consumer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CComponentId;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;

public abstract class CAbstractGnntGridBase extends CVerticalLayout {

	protected static final String WIDTH_DATE_COMPACT = "110px";
	protected static final String WIDTH_RESPONSIBLE_COMPACT = "135px";
	protected static final String WIDTH_STATUS_COMPACT = "140px";
	private static final long serialVersionUID = 1L;

	protected final Grid<CGnntItem> grid;
	protected final Consumer<CGnntItem> selectionListener;
	private CGanttTimelineRange currentRange;
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
		grid.asSingleSelect().addValueChangeListener(event -> this.selectionListener.accept(event.getValue()));
		configureColumns();
		configureTimelineHeaderRow();
		add(grid);
		setFlexGrow(1, grid);
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
		for (final Grid.Column<CGnntItem> column : grid.getColumns()) {
			timelineHeaderRow.getCell(column).setText("");
		}
	}

	protected Component createIconComponent(final CGnntItem item) {
		try {
			final Icon icon = item.getEntity() != null ? CColorUtils.getIconForEntity(item.getEntity())
					: CColorUtils.getIconFromString(item.getIconString());
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

	protected Grid<CGnntItem> getGrid() {
		return grid;
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

	protected void updateTimelineRange(final CGanttTimelineRange range) {
		currentRange = range;
		rebuildTimelineHeader();
	}

	public abstract void setHierarchy(CGnntHierarchyResult hierarchyResult, CGanttTimelineRange range);
}
