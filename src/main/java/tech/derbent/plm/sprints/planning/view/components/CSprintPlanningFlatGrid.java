package tech.derbent.plm.sprints.planning.view.components;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CAbstractGnntGridBase;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;

/**
 * Flat timeline grid for sprint planning backlog.
 *
 * <p>This is intentionally a new component so we can share drag state via
 * {@link CSprintPlanningDragContext} (cross-grid backlog ↔ sprint moves) without
 * touching the existing Gnnt board implementation.</p>
 */
public class CSprintPlanningFlatGrid extends CAbstractGnntGridBase {

	public static final String ID_GRID = "custom-sprint-planning-backlog-grid";
	private static final long serialVersionUID = 1L;

	private final CSprintPlanningDragContext dragContext;
	private final BiConsumer<CGnntItem, CGnntItem> dropListener;
	private final String gridId;

	public CSprintPlanningFlatGrid(final String gridId, final CSprintPlanningDragContext dragContext, final Consumer<CGnntItem> selectionListener,
			final BiConsumer<CGnntItem, CGnntItem> dropListener) {
		super(new CGrid<>(CGnntItem.class), gridId, selectionListener);
		this.gridId = gridId;
		this.dragContext = dragContext;
		this.dropListener = dropListener;
		configureDragAndDrop();
	}

	@Override
	protected void configureColumns() {
		addSharedColumns();
	}

	@Override
	protected void configureNameColumn() {
		grid.addComponentColumn(item -> {
			final CHorizontalLayout layout = new CHorizontalLayout();
			layout.setPadding(false);
			layout.setSpacing(true);
			layout.setAlignItems(Alignment.CENTER);
			layout.add(createIconComponent(item));
			final Span name = new Span(item.getName());
			name.getStyle().set("font-weight", "500");
			layout.add(name);
			return layout;
		}).setKey("name").setHeader("Name").setResizable(true).setWidth(NAME_COLUMN_WIDTH_PX + "px").setFlexGrow(0);
		CGrid.styleColumnHeader(grid.getColumnByKey("name"), "Name");
	}

	@Override
	protected int getNonTimelineColumnWidthPx() {
		return NAME_COLUMN_WIDTH_PX + 80 + 110 + 110 + 135 + 140;
	}

	@Override
	public void setHierarchy(final CGnntHierarchyResult hierarchyResult, final CGanttTimelineRange range) {
		updateTimelineRange(range);
		final List<CGnntItem> items = hierarchyResult != null ? hierarchyResult.getFlatItems() : List.of();
		grid.setItems(items);
		if (!items.isEmpty() && grid.asSingleSelect().getValue() == null) {
			grid.select(items.get(0));
		}
		restoreGridScrollPosition();
	}

	private void configureDragAndDrop() {
		grid.setRowsDraggable(true);
		grid.setDropMode(GridDropMode.BETWEEN);

		grid.addDragStartListener(event -> {
			final CGnntItem dragged = event.getDraggedItems().stream().findFirst().orElse(null);
			if (dragContext != null) {
				dragContext.setDraggedItem(dragged, gridId);
			}
		});
		grid.addDragEndListener(event -> {
			if (dragContext != null) {
				dragContext.clear();
			}
		});
		grid.addDropListener(event -> {
			final CGnntItem dropSource = dragContext != null ? dragContext.getDraggedItem() : null;
			if (dragContext != null) {
				dragContext.clear();
			}
			if (dropSource == null || dropListener == null) {
				return;
			}
			final CGnntItem targetItem = event.getDropTargetItem().orElse(null);
			// Target can be null when dropping on empty space; we still treat it as "drop on backlog".
			dropListener.accept(dropSource, targetItem);
		});
	}
}
