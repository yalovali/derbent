package tech.derbent.plm.kanban.kanbanline.view;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.treegrid.TreeGrid;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;

/**
 * Compact tree grid for the Kanban backlog column.
 *
 * <p>Kanban backlog wants the same hierarchy visibility as the Gnnt tree grid, but only needs the name
 * column (no timeline, no extra metadata) to keep the column narrow.</p>
 */
public class CKanbanBacklogTreeGrid extends CVerticalLayout {

	public static final String ID_GRID = "custom-kanban-backlog-tree";
	private static final long serialVersionUID = 1L;

	private final TreeGrid<CGnntItem> treeGrid;
	private CGnntItem selectedItem;
	private final Set<String> expandedEntityKeys = new HashSet<>();
	private boolean hierarchyInitialized;
	private Consumer<CProjectItem<?>> dragStartListener;
	private Runnable dragEndListener;
	private Consumer<CProjectItem<?>> selectionListener;

	public CKanbanBacklogTreeGrid() {
		setPadding(false);
		setSpacing(false);
		setWidthFull();
		setHeightFull();

		treeGrid = new TreeGrid<>();
		CGrid.setupGrid(treeGrid);
		treeGrid.setId(ID_GRID);
		treeGrid.setWidthFull();
		treeGrid.setHeightFull();
		treeGrid.setSelectionMode(TreeGrid.SelectionMode.SINGLE);
		treeGrid.addComponentHierarchyColumn(this::createHierarchyComponent)
				.setHeader("Name")
				.setKey("name")
				.setAutoWidth(false)
				.setFlexGrow(1);
		treeGrid.asSingleSelect().addValueChangeListener(event -> {
			selectedItem = event.getValue();
			final CProjectItem<?> selectedEntity = selectedItem != null ? selectedItem.getEntity() : null;
			if (selectionListener != null) {
				selectionListener.accept(selectedEntity);
			}
		});

		// Backlog drag is cross-component (TreeGrid → Kanban column). We only emit drag start/end hooks.
		treeGrid.setRowsDraggable(true);
		treeGrid.addDragStartListener(event -> {
			final CGnntItem item = event.getDraggedItems().stream().findFirst().orElse(null);
			if (item == null || item.getEntity() == null) {
				return;
			}
			if (dragStartListener != null) {
				dragStartListener.accept(item.getEntity());
			}
		});
		treeGrid.addDragEndListener(event -> {
			if (dragEndListener != null) {
				dragEndListener.run();
			}
		});

		add(treeGrid);
		setFlexGrow(1, treeGrid);
	}

	public void setDragStartListener(final Consumer<CProjectItem<?>> dragStartListener) {
		this.dragStartListener = dragStartListener;
	}

	public void setDragEndListener(final Runnable dragEndListener) {
		this.dragEndListener = dragEndListener;
	}

	public void setSelectionListener(final Consumer<CProjectItem<?>> selectionListener) {
		this.selectionListener = selectionListener;
	}

	public CProjectItem<?> getSelectedEntity() {
		return selectedItem != null ? selectedItem.getEntity() : null;
	}

	public void setHierarchy(final CGnntHierarchyResult hierarchyResult) {
		final CGnntItem selected = treeGrid.asSingleSelect().getValue();
		final String selectedKey = selected != null ? selected.getEntityKey() : null;
		final Set<String> expandedKeysSnapshot = hierarchyInitialized ? new HashSet<>(expandedEntityKeys) : Set.of();

		final CGnntHierarchyResult safeResult = hierarchyResult != null ? hierarchyResult : new CGnntHierarchyResult(List.of(), Map.of(), List.of());
		final List<CGnntItem> flatItems = safeResult.getFlatItems();
		final Map<String, CGnntItem> itemByKey = buildItemKeyMap(flatItems);

		treeGrid.setItems(safeResult.getRootItems(), safeResult::getChildren);
		expandedEntityKeys.clear();

		if (!hierarchyInitialized) {
			treeGrid.expandRecursively(safeResult.getRootItems(), 25);
			for (final CGnntItem item : flatItems) {
				final String itemKey = item != null ? item.getEntityKey() : null;
				if (itemKey != null && item.isParentItem()) {
					expandedEntityKeys.add(itemKey);
				}
			}
			hierarchyInitialized = true;
		} else {
			expandedEntityKeys.addAll(restoreExpandedState(itemByKey, expandedKeysSnapshot));
		}

		final CGnntItem restoredSelection = selectedKey != null ? itemByKey.get(selectedKey) : null;
		if (restoredSelection != null) {
			treeGrid.select(restoredSelection);
		} else if (!flatItems.isEmpty()) {
			treeGrid.select(flatItems.get(0));
		}
	}

	private Map<String, CGnntItem> buildItemKeyMap(final List<CGnntItem> flatItems) {
		final Map<String, CGnntItem> itemByKey = new HashMap<>();
		for (final CGnntItem item : flatItems) {
			final String key = item != null ? item.getEntityKey() : null;
			if (key != null) {
				itemByKey.put(key, item);
			}
		}
		return itemByKey;
	}

	private Set<String> restoreExpandedState(final Map<String, CGnntItem> itemByKey, final Set<String> expandedKeys) {
		if (expandedKeys == null || expandedKeys.isEmpty() || itemByKey.isEmpty()) {
			return Set.of();
		}
		final Set<String> keysExpanded = new HashSet<>();
		for (final String key : expandedKeys) {
			final CGnntItem item = itemByKey.get(key);
			if (item != null && item.isParentItem()) {
				treeGrid.expand(item);
				keysExpanded.add(key);
			}
		}
		return keysExpanded;
	}

	private Component createHierarchyComponent(final CGnntItem item) {
		Check.notNull(item, "Item cannot be null");
		final CHorizontalLayout layout = new CHorizontalLayout();
		layout.setSpacing(true);
		layout.setPadding(false);
		layout.getStyle().set("gap", "var(--lumo-space-xs)");
		final Span name = new Span(item.getName());
		name.getStyle().set("font-weight", item.isParentItem() ? "700" : "400");
		layout.add(name);
		return layout;
	}
}
