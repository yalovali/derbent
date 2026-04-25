package tech.derbent.plm.sprints.planning.view.components;

import java.util.List;
import java.util.function.Consumer;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.treegrid.TreeGrid;

import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
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

	protected CAbstractSprintPlanningTreeGridBase(final String gridId, final Consumer<CGnntItem> selectionListener,
			final String headerControlsIdPrefix) {
		super(new TreeGrid<>(), gridId, selectionListener);

		// Place expand/collapse controls into the left-side timeline header row so they stay aligned with the timeline range.
		installHeaderExpandCollapseControls(headerControlsIdPrefix);
	}

	protected final TreeGrid<CGnntItem> getTreeGrid() {
		return (TreeGrid<CGnntItem>) getGrid();
	}

	protected final void setRootItems(final List<CGnntItem> rootItems) {
		lastRootItems = rootItems != null ? rootItems : List.of();
	}

	public final void expandAll() {
		getTreeGrid().expand(lastRootItems);
	}

	public final void collapseAll() {
		getTreeGrid().collapse(lastRootItems);
	}

	private void installHeaderExpandCollapseControls(final String idPrefix) {
		if (idPrefix == null || idPrefix.isBlank()) {
			return;
		}

		final CButton buttonExpandAll = CButton.createTertiary("", VaadinIcon.PLUS_SQUARE_O.create(), event -> expandAll());
		buttonExpandAll.setId(idPrefix + "-expand-all");
		buttonExpandAll.addThemeVariants(ButtonVariant.LUMO_SMALL);

		final CButton buttonCollapseAll = CButton.createTertiary("", VaadinIcon.MINUS_SQUARE_O.create(), event -> collapseAll());
		buttonCollapseAll.setId(idPrefix + "-collapse-all");
		buttonCollapseAll.addThemeVariants(ButtonVariant.LUMO_SMALL);

		final CHorizontalLayout headerControls = new CHorizontalLayout(buttonExpandAll, buttonCollapseAll);
		headerControls.setPadding(false);
		headerControls.setSpacing(true);
		headerControls.setAlignItems(Alignment.CENTER);
		headerControls.setId(idPrefix + "-header-controls");

		setLeftHeaderComponent(headerControls);
	}
}
