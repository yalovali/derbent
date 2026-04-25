package tech.derbent.plm.sprints.planning.view.components;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.treegrid.TreeGrid;

import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CContextActionDefinition;
import tech.derbent.api.ui.component.enhanced.CContextMenuSupport;
import tech.derbent.api.ui.component.enhanced.CQuickAccessPanel;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CAbstractGnntGridBase;

/**
 * Base class for sprint-planning-specific tree grids.
 *
 * <p>We keep this separate from the generic Gnnt board grids so sprint/backlog planning can add
 * hierarchy controls (expand/collapse) in the same header row as the timeline range header without
 * changing the Gnnt project view UI.</p>
 */
public abstract class CAbstractSprintPlanningTreeGridBase extends CAbstractGnntGridBase {

	private static final long serialVersionUID = 1L;

	private List<CGnntItem> lastRootItems = List.of();
	private final Set<String> expandedItemKeys = new LinkedHashSet<>();
	private List<CContextActionDefinition<CGnntItem>> hierarchyContextActions = List.of();

	protected CAbstractSprintPlanningTreeGridBase(final String gridId, final Consumer<CGnntItem> selectionListener,
			final String headerControlsIdPrefix) {
		super(new TreeGrid<>(), gridId, selectionListener);

		// Sprint planning places quick actions (expand/collapse, refresh, metrics, etc.) into the same header row as the timeline range.
		final String safePrefix = headerControlsIdPrefix != null && !headerControlsIdPrefix.isBlank() ? headerControlsIdPrefix : gridId;
		setQuickAccessPanel(new CQuickAccessPanel(safePrefix + "-quick-access"));

		getTreeGrid().addExpandListener(event -> event.getItems().forEach(item -> trackExpandedItem(item, true)));
		getTreeGrid().addCollapseListener(event -> event.getItems().forEach(item -> trackExpandedItem(item, false)));

		installHeaderExpandCollapseControls(safePrefix);
	}

	protected final TreeGrid<CGnntItem> getTreeGrid() {
		return (TreeGrid<CGnntItem>) getGrid();
	}

	protected final void setHierarchyContextActions(final List<CContextActionDefinition<CGnntItem>> actions) {
		hierarchyContextActions = actions != null ? List.copyOf(actions) : List.of();
	}

	protected final void setRootItems(final List<CGnntItem> rootItems) {
		lastRootItems = rootItems != null ? rootItems : List.of();
	}

	public final void expandAll() {
		getTreeGrid().expand(lastRootItems);
		lastRootItems.forEach(item -> trackExpandedItem(item, true));
	}

	public final void collapseAll() {
		getTreeGrid().collapse(lastRootItems);
		lastRootItems.forEach(item -> trackExpandedItem(item, false));
	}

	protected final void restoreExpandedState(final Map<String, CGnntItem> itemByKey) {
		final TreeGrid<CGnntItem> treeGrid = getTreeGrid();
		final List<CGnntItem> itemsToExpand = expandedItemKeys.stream()
				.map(itemByKey::get)
				.filter(item -> item != null)
				.toList();
		if (!itemsToExpand.isEmpty()) {
			treeGrid.expand(itemsToExpand);
			return;
		}
		treeGrid.expand(lastRootItems);
	}

	private void trackExpandedItem(final CGnntItem item, final boolean expanded) {
		final String entityKey = item != null ? item.getEntityKey() : null;
		if (entityKey == null) {
			return;
		}
		if (expanded) {
			expandedItemKeys.add(entityKey);
			return;
		}
		expandedItemKeys.remove(entityKey);
	}

	private void installHeaderExpandCollapseControls(final String idPrefix) {
		// Keep hierarchy controls in the shared quick-access toolbar so all sprint planning grids feel the same.
		getQuickAccessPanel().addIconTertiaryButton("expand-all", "Expand all", VaadinIcon.PLUS_SQUARE_O, this::expandAll)
				.setId(idPrefix + "-expand-all");
		getQuickAccessPanel().addIconTertiaryButton("collapse-all", "Collapse all", VaadinIcon.MINUS_SQUARE_O, this::collapseAll)
				.setId(idPrefix + "-collapse-all");
		((CButton) getQuickAccessPanel().getControl("expand-all").orElseThrow()).addThemeVariants(ButtonVariant.LUMO_SMALL);
		((CButton) getQuickAccessPanel().getControl("collapse-all").orElseThrow()).addThemeVariants(ButtonVariant.LUMO_SMALL);
	}

	protected final <T extends Component> T decorateHierarchyComponent(final T component, final CGnntItem item) {
		if (component == null || item == null || hierarchyContextActions.isEmpty()) {
			return component;
		}
		// TreeGrid hierarchy cells render custom components, so we attach a mirrored context menu here as well.
		final ContextMenu contextMenu = new ContextMenu(component);
		contextMenu.setOpenOnClick(false);
		final Map<String, MenuItem> menuItemsByKey = new java.util.LinkedHashMap<>();
		for (final CContextActionDefinition<CGnntItem> action : hierarchyContextActions) {
			menuItemsByKey.put(action.getKey(), CContextMenuSupport.registerComponentAction(contextMenu, action, () -> item));
		}
		contextMenu.addOpenedChangeListener(event -> {
			if (!event.isOpened()) {
				return;
			}
			// Component-based hierarchy cells can hold detached proxies, so the mirrored menu acts on the row without forcing a selection refresh.
			for (final CContextActionDefinition<CGnntItem> action : hierarchyContextActions) {
				final MenuItem menuItem = menuItemsByKey.get(action.getKey());
				if (menuItem != null) {
					CContextMenuSupport.refreshComponentActionState(menuItem, action, item);
				}
			}
		});
		return component;
	}
}
