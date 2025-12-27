package tech.derbent.app.kanban.kanbanline.view;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.binder.Binder;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CComponentKanbanColumn - Renders a single kanban column with its header and post-it items. */
public class CComponentKanbanColumn extends CComponentBase<CKanbanColumn> {

	private static final long serialVersionUID = 1L;
	private final Binder<CKanbanColumn> binder;
	private final Span defaultBadge;
	private final CHorizontalLayout headerLayout;
	private final CVerticalLayout itemsLayout;
	Logger LOGGER = LoggerFactory.getLogger(CComponentKanbanColumn.class);
	private Consumer<CComponentKanbanPostit> postitSelectionListener;
	private List<CSprintItem> sprintItems = List.of();
	private final CLabelEntity statusesLabel;
	private final CH3 title;

	/** Creates the kanban column component and its layout. */
	public CComponentKanbanColumn() {
		setPadding(true);
		setSpacing(true);
		setWidth("280px");
		setMinHeight("500px");
		setHeight(null);
		setDefaultHorizontalComponentAlignment(Alignment.STRETCH);
		addClassName("kanban-column");
		getStyle().set("border-radius", "10px").set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");
		headerLayout = new CHorizontalLayout();
		headerLayout.setWidthFull();
		headerLayout.setSpacing(true);
		title = new CH3("");
		title.getStyle().set("margin", "0");
		defaultBadge = new Span("Default");
		defaultBadge.getStyle().set("background-color", "#E3F2FD").set("color", "#0D47A1").set("padding", "2px 6px").set("border-radius", "6px")
				.set("font-size", "10px").set("font-weight", "600");
		headerLayout.add(title);
		add(headerLayout);
		statusesLabel = new CLabelEntity();
		statusesLabel.getStyle().set("font-size", "11px").set("color", "#666");
		add(statusesLabel);
		itemsLayout = new CVerticalLayout(false, true, false);
		itemsLayout.setPadding(false);
		itemsLayout.setSpacing(true);
		itemsLayout.setWidthFull();
		itemsLayout.setHeight(null);
		itemsLayout.addClassName("kanban-column-items");
		add(itemsLayout);
		binder = new Binder<>(CKanbanColumn.class);
		binder.forField(this).bind(value -> value, (bean, value) -> {/**/});
	}

	/** Applies the configured background color for the column. */
	private void applyBackgroundColor() {
		final CKanbanColumn column = getValue();
		final String backgroundColor =
				column != null && column.getColor() != null && !column.getColor().isBlank() ? column.getColor() : CKanbanColumn.DEFAULT_COLOR;
		getStyle().set("background-color", backgroundColor);
	}

	/** Filters items that should appear in this column. */
	private List<CSprintItem> filterItems(final List<CSprintItem> items) {
		LOGGER.debug("Filtering items for kanban column {}", getValue() != null ? getValue().getName() : "null");
		if (items == null || items.isEmpty()) {
			return List.of();
		}
		final CKanbanColumn column = getValue();
		if (column == null || column.getId() == null) {
			return List.of();
		}
		final Long columnId = column.getId();
		return items.stream().filter(Objects::nonNull).filter(item -> {
			final Long itemColumnId = item.getKanbanColumnId();
			return itemColumnId != null && itemColumnId.equals(columnId);
		}).toList();
	}

	/** Propagates a post-it click to the selection listener. */
	private void on_postit_clicked(final CComponentKanbanPostit postit) {
		if (postitSelectionListener != null) {
			postitSelectionListener.accept(postit);
		}
	}

	/** Updates the column UI when its value changes. */
	@Override
	protected void onValueChanged(final CKanbanColumn oldValue, final CKanbanColumn newValue, final boolean fromClient) {
		LOGGER.debug("Kanban column value changed from {} to {}", oldValue != null ? oldValue.getName() : "null",
				newValue != null ? newValue.getName() : "null");
		if (binder.getBean() == newValue) {
			return;
		}
		binder.setBean(newValue);
		applyBackgroundColor();
		refreshHeader();
		refreshStatuses();
		refreshItems();
	}

	/** Refreshes the column UI components. */
	@Override
	protected void refreshComponent() {
		applyBackgroundColor();
		refreshHeader();
		refreshStatuses();
		refreshItems();
	}

	/** Refreshes the header title and default badge. */
	private void refreshHeader() {
		final CKanbanColumn column = getValue();
		title.setText(column != null ? column.getName() : "");
		final boolean isDefault = column != null && Boolean.TRUE.equals(column.getDefaultColumn());
		if (isDefault) {
			if (!headerLayout.getChildren().anyMatch(component -> component == defaultBadge)) {
				headerLayout.add(defaultBadge);
			}
		} else {
			headerLayout.remove(defaultBadge);
		}
	}

	/** Refreshes the item cards inside the column. */
	private void refreshItems() {
		LOGGER.debug("Refreshing items for kanban column {}", getValue() != null ? getValue().getName() : "null");
		itemsLayout.removeAll();
		for (final CSprintItem item : filterItems(sprintItems)) {
			final CComponentKanbanPostit postit = new CComponentKanbanPostit(item);
			postit.addClickListener(event -> on_postit_clicked(postit));
			itemsLayout.add(postit);
		}
	}

	/** Refreshes the status summary label. */
	private void refreshStatuses() {
		LOGGER.debug("Refreshing statuses label for kanban column {}", getValue() != null ? getValue().getName() : "null");
		final CKanbanColumn column = getValue();
		if (column == null || column.getIncludedStatuses() == null || column.getIncludedStatuses().isEmpty()) {
			statusesLabel.setText("");
			return;
		}
		final String statuses = column.getIncludedStatuses().stream().filter(Objects::nonNull).map(status -> status.getName())
				.filter(name -> name != null && !name.isBlank()).sorted(String::compareToIgnoreCase).collect(Collectors.joining(", "));
		statusesLabel.setText(statuses);
	}

	/** Sets the items displayed in this column. */
	public void setItems(final List<CSprintItem> items) {
		LOGGER.debug("Setting items for kanban column {}", getValue() != null ? getValue().getName() : "null");
		sprintItems = items == null ? List.of() : List.copyOf(items);
		refreshItems();
	}

	/** Sets the selection listener for post-it clicks. */
	public void setPostitSelectionListener(final Consumer<CComponentKanbanPostit> listener) {
		postitSelectionListener = listener;
	}
}
