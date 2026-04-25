package tech.derbent.plm.sprints.planning.view.components;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.function.Consumer;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.treegrid.TreeGrid;

import tech.derbent.api.ui.component.basic.CButton;
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
		getQuickAccessPanel().addTertiaryButton("expand-all", "", VaadinIcon.PLUS_SQUARE_O, this::expandAll).setId(idPrefix + "-expand-all");
		getQuickAccessPanel().addTertiaryButton("collapse-all", "", VaadinIcon.MINUS_SQUARE_O, this::collapseAll).setId(idPrefix + "-collapse-all");
		((CButton) getQuickAccessPanel().getControl("expand-all").orElseThrow()).addThemeVariants(ButtonVariant.LUMO_SMALL);
		((CButton) getQuickAccessPanel().getControl("collapse-all").orElseThrow()).addThemeVariants(ButtonVariant.LUMO_SMALL);
	}
}
