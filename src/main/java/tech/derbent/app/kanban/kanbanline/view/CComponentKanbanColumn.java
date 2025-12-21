package tech.derbent.app.kanban.kanbanline.view;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import com.vaadin.flow.component.html.Span;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;

/** CComponentKanbanColumn - Renders a single kanban column with its header and post-it items. */
public class CComponentKanbanColumn extends CVerticalLayout {

	private static final long serialVersionUID = 1L;

	public CComponentKanbanColumn(final CKanbanColumn column, final List<CProjectItem<?>> items) {
		Check.notNull(column, "Kanban column cannot be null");
		setPadding(true);
		setSpacing(true);
		setWidth("280px");
		getStyle().set("background-color", "#F5F5F5").set("border-radius", "10px").set("box-shadow", "0 1px 3px rgba(0, 0, 0, 0.1)");
		add(buildHeader(column));
		addStatuses(column);
		if (items != null) {
			for (final CProjectItem<?> item : items) {
				add(new CComponentPostit(item));
			}
		}
	}

	private void addStatuses(final CKanbanColumn column) {
		if (column.getIncludedStatuses() == null || column.getIncludedStatuses().isEmpty()) {
			return;
		}
		final String statuses = column.getIncludedStatuses().stream().filter(Objects::nonNull).map(status -> status.getName())
				.filter(name -> name != null && !name.isBlank()).sorted(String::compareToIgnoreCase).collect(Collectors.joining(", "));
		if (!statuses.isBlank()) {
			final CLabelEntity statusesLabel = new CLabelEntity();
			statusesLabel.setText(statuses);
			statusesLabel.getStyle().set("font-size", "11px").set("color", "#666");
			add(statusesLabel);
		}
	}

	private CHorizontalLayout buildHeader(final CKanbanColumn column) {
		final CHorizontalLayout headerLayout = new CHorizontalLayout();
		headerLayout.setWidthFull();
		headerLayout.setSpacing(true);
		final CH3 title = new CH3(column.getName());
		title.getStyle().set("margin", "0");
		headerLayout.add(title);
		if (column.getDefaultColumn()) {
			final Span defaultBadge = new Span("Default");
			defaultBadge.getStyle().set("background-color", "#E3F2FD").set("color", "#0D47A1").set("padding", "2px 6px").set("border-radius", "6px")
					.set("font-size", "10px").set("font-weight", "600");
			headerLayout.add(defaultBadge);
		}
		return headerLayout;
	}
}
