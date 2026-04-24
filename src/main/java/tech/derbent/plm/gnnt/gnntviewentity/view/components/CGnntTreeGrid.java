package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;

public class CGnntTreeGrid extends CAbstractGnntGridBase {

	public static final String ID_TREE_GRID = "custom-gnnt-tree-grid";
	private static final long serialVersionUID = 1L;
	private CGnntItem draggedItem;
	private final BiConsumer<CGnntItem, CGnntItem> moveListener;

	public CGnntTreeGrid(final Consumer<CGnntItem> selectionListener, final BiConsumer<CGnntItem, CGnntItem> moveListener) {
		super(new TreeGrid<>(), ID_TREE_GRID, selectionListener);
		this.moveListener = moveListener;
		configureDragAndDrop();
	}

	@Override
	protected void configureColumns() {
		configureNameColumn();
		addIdColumn();
		addTrailingSharedColumns();
	}

	@Override
	protected void configureNameColumn() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final var nameColumn = treeGrid.addComponentHierarchyColumn(this::createHierarchyComponent)
				.setAutoWidth(false)
				.setResizable(true)
				.setKey("name")
				.setHeader("Name")
				.setFlexGrow(0)
				.setWidth(NAME_COLUMN_WIDTH_PX + "px");
		CGrid.styleColumnHeader(nameColumn, "Name");
	}

	@Override
	protected int getNonTimelineColumnWidthPx() {
		return NAME_COLUMN_WIDTH_PX + 80 + 110 + 110 + 135 + 140;
	}

	@Override
	public void setHierarchy(final CGnntHierarchyResult hierarchyResult, final CGanttTimelineRange range) {
		updateTimelineRange(range);
		final CGnntHierarchyResult safeHierarchyResult = hierarchyResult != null ? hierarchyResult : new CGnntHierarchyResult(null, null, null);
		getTreeGrid().setItems(safeHierarchyResult.getRootItems(), safeHierarchyResult::getChildren);
		getTreeGrid().expandRecursively(safeHierarchyResult.getRootItems(), 25);
		if (!safeHierarchyResult.getFlatItems().isEmpty()) {
			getTreeGrid().select(safeHierarchyResult.getFlatItems().get(0));
		} else {
			selectionListener.accept(null);
		}
	}

	private void configureDragAndDrop() {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		treeGrid.setRowsDraggable(true);
		treeGrid.setDropMode(GridDropMode.ON_TOP);
		treeGrid.addDragStartListener(event -> draggedItem = event.getDraggedItems().stream().findFirst().orElse(null));
		treeGrid.addDragEndListener(event -> draggedItem = null);
		treeGrid.addDropListener(event -> {
			if (draggedItem == null || moveListener == null || event.getDropTargetItem().isEmpty()) {
				return;
			}
			moveListener.accept(draggedItem, event.getDropTargetItem().get());
			draggedItem = null;
		});
	}

	private TreeGrid<CGnntItem> getTreeGrid() {
		return (TreeGrid<CGnntItem>) getGrid();
	}

	private Component createHierarchyComponent(final CGnntItem item) {
		final CHorizontalLayout layout = new CHorizontalLayout();
		layout.setSpacing(true);
		layout.setPadding(false);
		layout.setAlignItems(Alignment.CENTER);
		final Component iconComponent = createIconComponent(item);
		iconComponent.getElement().getStyle().set("color", item.getColorCode());
		final Span name = new Span(item.getName());
		name.getStyle().set("font-weight", item.isParentItem() ? "700" : "400")
				.set("color", item.getColorCode());
		layout.add(iconComponent, name);
		return layout;
	}
}
