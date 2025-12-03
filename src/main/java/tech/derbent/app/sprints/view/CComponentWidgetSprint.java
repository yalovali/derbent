package tech.derbent.app.sprints.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.app.sprints.domain.CSprint;

/** CComponentWidgetSprint - Widget component for displaying Sprint entities in grids.
 * <p>
 * This widget displays sprint information in a three-row layout:
 * <ul>
 * <li><b>Row 1:</b> Sprint name with calendar icon and sprint color</li>
 * <li><b>Row 2:</b> Sprint type badge, item count with colorful display</li>
 * <li><b>Row 3:</b> Status badge, responsible user, and date range with calendar icons</li>
 * </ul>
 * </p>
 * <p>
 * Extends CComponentWidgetEntityOfProject and adds sprint-specific information like item count and sprint type.
 * Uses CLabelEntity for colorful, visually appealing badges and labels.
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntityOfProject */
public class CComponentWidgetSprint extends CComponentWidgetEntityOfProject<CSprint> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentWidgetSprint.class);
	private static final long serialVersionUID = 1L;

	/** Creates a new sprint widget for the specified sprint.
	 * @param sprint the sprint to display in the widget */
	public CComponentWidgetSprint(final CSprint sprint) {
		super(sprint);
	}

	/** Creates the second line with sprint type and item count.
	 * This line shows colorful badges for sprint type and item count. */
	@Override
	protected void createSecondLine() {
		// Show sprint type with color if available
		if (getEntity().getEntityType() != null) {
			final CLabelEntity typeLabel = new CLabelEntity();
			typeLabel.setValue(getEntity().getEntityType(), true);
			typeLabel.getStyle().set("margin-right", "8px");
			layoutLineTwo.add(typeLabel);
		}
		// Show item count with icon and colorful badge
		final Integer itemCount = getEntity().getItemCount();
		final CLabelEntity itemCountLabel = new CLabelEntity();
		itemCountLabel.getStyle().set("display", "flex").set("align-items", "center").set("gap", "4px")
				.set("background-color", "#E3F2FD") // Light blue background
				.set("color", "#1976D2") // Blue text
				.set("padding", "4px 8px").set("border-radius", "4px").set("font-size", "10pt").set("font-weight", "500");
		// Add tasks icon
		final Icon icon = VaadinIcon.TASKS.create();
		icon.getStyle().set("width", "14px").set("height", "14px").set("color", "#1976D2");
		itemCountLabel.add(icon);
		// Add count text
		final String countText = (itemCount != null ? itemCount : 0) + " item" + ((itemCount != null && itemCount != 1) ? "s" : "");
		itemCountLabel.setText(countText);
		layoutLineTwo.add(itemCountLabel);
	}
}
