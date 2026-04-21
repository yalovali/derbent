package tech.derbent.plm.gnnt.gnntviewentity.view.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntHierarchyResult;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;

public class CGnntGrid extends CAbstractGnntGridBase {

	public static final String ID_GRID = "custom-gnnt-grid";
	private static final long serialVersionUID = 1L;

	private final List<CGnntItem> timelineItems = new ArrayList<>();

	public CGnntGrid(final Consumer<CGnntItem> selectionListener) {
		super(new CGrid<>(CGnntItem.class), ID_GRID, selectionListener);
	}

	@Override
	protected void configureColumns() {
		addSharedColumns();
	}

	@Override
	protected void configureNameColumn() {
		grid.addComponentColumn(this::createNameComponent).setAutoWidth(true).setResizable(true).setKey("name").setHeader("Name").setFlexGrow(1);
	}

	private Component createNameComponent(final CGnntItem item) {
		final Div container = new Div();
		container.getStyle().set("display", "flex");
		container.getStyle().set("align-items", "center");
		container.getStyle().set("gap", "var(--lumo-space-xs)");
		container.getStyle().set("padding-left", (item.getHierarchyLevel() * 20) + "px");
		container.getElement().setProperty("title", "Hierarchy level " + item.getHierarchyLevel());
		if (item.getHierarchyLevel() > 0) {
			final Span branch = new Span("↳");
			branch.getStyle().set("color", "var(--lumo-secondary-text-color)");
			branch.getStyle().set("font-size", "var(--lumo-font-size-s)");
			container.add(branch);
		}
		final Span name = new Span(item.getIndentedName());
		name.getStyle().set("font-weight", item.isParentItem() ? "700" : "400")
				.set("color", item.getColorCode());
		container.add(name);
		return container;
	}

	@Override
	public void setHierarchy(final CGnntHierarchyResult hierarchyResult, final CGanttTimelineRange range) {
		timelineItems.clear();
		if (hierarchyResult != null) {
			timelineItems.addAll(hierarchyResult.getFlatItems());
		}
		updateTimelineRange(range);
		grid.setItems(timelineItems);
		if (!timelineItems.isEmpty()) {
			grid.select(timelineItems.get(0));
		} else {
			selectionListener.accept(null);
		}
	}
}
