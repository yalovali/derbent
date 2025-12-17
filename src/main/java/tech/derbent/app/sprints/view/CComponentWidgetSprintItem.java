package tech.derbent.app.sprints.view;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CComponentWidgetSprintItem - Widget component for displaying SprintItem entities in grids.
 * <p>
 * This widget displays sprint item information in a three-row layout:
 * <ul>
 * <li><b>Row 1:</b> Item order number badge and project item name with icon</li>
 * <li><b>Row 2:</b> Project item type badge and short description</li>
 * <li><b>Row 3:</b> Status badge, responsible user, and date range</li>
 * </ul>
 * </p>
 * <p>
 * Displays the underlying project item (Activity, Meeting, etc.) with ordering information. Uses CLabelEntity for colorful, visually appealing badges
 * and labels.
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntity */
public class CComponentWidgetSprintItem extends CComponentWidgetEntity<CSprintItem> {

	private static final long serialVersionUID = 1L;

	/** Creates a new sprint item widget for the specified sprint item.
	 * @param sprintItem the sprint item to display in the widget */
	public CComponentWidgetSprintItem(final CSprintItem sprintItem) {
		super(sprintItem);
	}

	/** Creates the first line with order badge and project item name. This line shows the item order number and the underlying project item name.
	 * @throws Exception */
	@Override
	protected void createFirstLine() throws Exception {
		// Show order number badge
		final Integer order = getEntity().getItemOrder();
		final CLabelEntity orderLabel = new CLabelEntity();
		orderLabel.getStyle().set("display", "flex").set("align-items", "center").set("justify-content", "center").set("background-color", "#FFF3E0") // Light
																																						// orange
																																						// background
				.set("color", "#E65100") // Deep orange text
				.set("padding", "4px 8px").set("border-radius", "4px").set("font-size", "10pt").set("font-weight", "600").set("min-width", "32px")
				.set("margin-right", "8px");
		orderLabel.setText("#" + (order != null ? order : 0));
		layoutLineOne.add(orderLabel);
		// Show project item name with icon if item is loaded
		final CProjectItem<?> projectItem = getEntity().getItem();
		if (projectItem != null) {
			layoutLineOne.add(CLabelEntity.createH3Label(projectItem));
		} else {
			// Fallback: show item type and ID if item not loaded
			final CLabelEntity itemLabel = new CLabelEntity();
			itemLabel.setText(getEntity().getItemType() + " #" + getEntity().getItemId());
			itemLabel.getStyle().set("font-weight", "600").set("font-size", "12pt");
			layoutLineOne.add(itemLabel);
		}
	}

	/** Creates the second line with project item type and description. This line shows a colorful badge for the item type and truncated
	 * description. */
	@Override
	protected void createSecondLine() {
		final CProjectItem<?> projectItem = getEntity().getItem();
		// Show item type badge
		final String itemType = getEntity().getItemType();
		if (itemType != null) {
			final CLabelEntity typeLabel = new CLabelEntity();
			// Color coding for different item types
			String bgColor = "#E8EAF6"; // Default: Indigo 50
			String textColor = "#3F51B5"; // Indigo 700
			Icon typeIcon = VaadinIcon.FILE_O.create();
			if ("CActivity".equals(itemType)) {
				bgColor = "#E3F2FD"; // Blue 50
				textColor = "#1976D2"; // Blue 700
				typeIcon = VaadinIcon.TASKS.create();
			} else if ("CMeeting".equals(itemType)) {
				bgColor = "#F3E5F5"; // Purple 50
				textColor = "#7B1FA2"; // Purple 700
				typeIcon = VaadinIcon.CALENDAR.create();
			}
			typeLabel.getStyle().set("display", "flex").set("align-items", "center").set("gap", "4px").set("background-color", bgColor)
					.set("color", textColor).set("padding", "4px 8px").set("border-radius", "4px").set("font-size", "10pt").set("font-weight", "500")
					.set("margin-right", "8px");
			typeIcon.getStyle().set("width", "14px").set("height", "14px").set("color", textColor);
			typeLabel.add(typeIcon);
			// Display friendly name
			final String displayName = itemType.substring(1); // Remove 'C' prefix
			typeLabel.setText(displayName);
			layoutLineTwo.add(typeLabel);
		}
		// Show description if project item is loaded
		if (projectItem != null && projectItem.getDescription() != null && !projectItem.getDescription().isEmpty()) {
			final CLabelEntity descLabel = new CLabelEntity();
			final String truncatedDesc =
					projectItem.getDescription().length() > 80 ? projectItem.getDescription().substring(0, 77) + "..." : projectItem.getDescription();
			descLabel.setText(truncatedDesc);
			descLabel.getStyle().set("font-size", "10pt").set("color", "#666");
			layoutLineTwo.add(descLabel);
		}
	}

	/** Creates the third line with status, user, and dates from the project item. This line shows information from the underlying project item.
	 * @throws Exception */
	@Override
	protected void createThirdLine() throws Exception {
		final CProjectItem<?> projectItem = getEntity().getItem();
		if (projectItem != null) {
			// Show status
			if (projectItem.getStatus() != null) {
				layoutLineThree.add(new CLabelEntity(projectItem.getStatus()));
			}
			// Show responsible user
			if (projectItem.getResponsible() != null) {
				layoutLineThree.add(CLabelEntity.createUserLabel(projectItem.getResponsible()));
			}
			// Show date range
			if (projectItem.getStartDate() != null || projectItem.getEndDate() != null) {
				layoutLineThree.add(CLabelEntity.createDateRangeLabel(projectItem.getStartDate(), projectItem.getEndDate()));
			}
		}
	}

	@Override
	public void drag_checkEventBeforePass(CEvent event) {
		LOGGER.debug("Drag event check before pass: {} comp id:{} event type:{}", event, getId(), event.getClass().getSimpleName());
	}
}
