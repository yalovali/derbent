package tech.derbent.api.grid.widget;

import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasColorAndIcon;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.base.users.domain.CUser;

/** CComponentWidgetEntityOfProject - Base widget component for displaying project item entities in grids.
 * <p>
 * This widget provides a three-row layout with common content:
 * <ul>
 * <li><b>Row 1:</b> Entity name with icon and color (if entity implements IHasColorAndIcon)</li>
 * <li><b>Row 2:</b> Description (truncated with ellipsis)</li>
 * <li><b>Row 3:</b> Status badge, responsible user, and date range</li>
 * </ul>
 * </p>
 * <p>
 * Subclasses can override createFirstLine(), createSecondLine(), createThirdLine() to customize content.
 * </p>
 * @author Derbent Framework
 * @since 1.0
 * @param <T> the entity type extending CProjectItem */
public abstract class CComponentWidgetEntityOfProject<T extends CProjectItem<?>> extends CComponentWidgetEntity<T> {

	/** Max description length to display in the widget. */
	protected static final int MAX_DESCRIPTION_LENGTH = 100;
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentWidgetEntityOfProject.class);
	private static final long serialVersionUID = 1L;

	/** Creates a new project item widget for the specified entity.
	 * @param item the project item to display in the widget */
	public CComponentWidgetEntityOfProject(final T item) {
		super(item);
	}

	/** Creates the first line with entity name, icon, and color styling.
	 * If entity implements IHasColorAndIcon, the icon and color will be used. */
	@Override
	protected void createFirstLine() {
		final T item = getEntity();
		if (item == null) {
			layoutLineOne.add(new CH3("(No Entity)"));
			return;
		}
		String name = item.getName();
		if (name == null || name.isEmpty()) {
			name = "(No Name)";
		}
		final CHorizontalLayout nameLayout = new CHorizontalLayout();
		nameLayout.setSpacing(true);
		nameLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		// Add icon if entity has IHasColorAndIcon interface
		if (item instanceof IHasColorAndIcon) {
			try {
				final IHasColorAndIcon colorAndIconEntity = (IHasColorAndIcon) item;
				final String iconName = colorAndIconEntity.getIcon();
				if (iconName != null && !iconName.isEmpty()) {
					final Icon icon = CColorUtils.createStyledIcon(iconName);
					if (icon != null) {
						// Apply color to icon if available
						final String color = colorAndIconEntity.getColor();
						if (color != null && !color.isEmpty()) {
							icon.getStyle().set("color", color);
						}
						nameLayout.add(icon);
					}
				}
			} catch (final Exception e) {
				LOGGER.debug("Could not create icon for entity {}: {}", item.getClass().getSimpleName(), e.getMessage());
			}
		}
		// Add name span with styling
		final CH3 nameSpan = new CH3(name);
		// Apply color styling to name if entity has color
		if (item instanceof IHasColorAndIcon) {
			try {
				final IHasColorAndIcon colorAndIconEntity = (IHasColorAndIcon) item;
				final String color = colorAndIconEntity.getColor();
				if (color != null && !color.isEmpty()) {
					nameSpan.getStyle().set("color", color);
				}
			} catch (final Exception e) {
				LOGGER.debug("Could not apply color to name for entity {}: {}", item.getClass().getSimpleName(), e.getMessage());
			}
		}
		nameLayout.add(nameSpan);
		layoutLineOne.add(nameLayout);
	}

	/** Creates the second line with truncated description. */
	@Override
	protected void createSecondLine() {
		final T item = getEntity();
		if (item == null) {
			layoutLineTwo.add(new CDiv("(No Description)"));
			return;
		}
		String description = item.getDescription();
		if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
			description = description.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
		}
		if (description == null || description.isEmpty()) {
			description = "(No Description)";
		}
		final CDiv descriptionDiv = new CDiv(description);
		descriptionDiv.getStyle().set("font-size", "10pt");
		descriptionDiv.getStyle().set("color", "#666");
		layoutLineTwo.add(descriptionDiv);
	}

	/** Creates the third line with status badge, responsible user, and date range. */
	@Override
	protected void createThirdLine() {
		final T item = getEntity();
		if (item == null) {
			return;
		}
		// Add status display
		final CProjectItemStatus status = item.getStatus();
		if (status != null) {
			layoutLineThree.add(createStatusDisplay(status));
		}
		// Add responsible user display
		final CUser responsible = item.getResponsible();
		if (responsible != null) {
			layoutLineThree.add(createUserDisplay(responsible));
		}
		// Add date range display - CProjectItem has getStartDate() and getEndDate() methods
		final LocalDate startDate = item.getStartDate();
		final LocalDate endDate = item.getEndDate();
		if (startDate != null || endDate != null) {
			layoutLineThree.add(createDateRangeDisplay(startDate, endDate));
		}
	}

	/** Creates a styled status display component with color.
	 * @param status the status entity to display
	 * @return the status display component */
	protected CHorizontalLayout createStatusDisplay(final CProjectItemStatus status) {
		final CHorizontalLayout statusLayout = new CHorizontalLayout();
		statusLayout.setSpacing(true);
		statusLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		statusLayout.getStyle().set("padding", "2px 6px");
		statusLayout.getStyle().set("border-radius", "4px");
		statusLayout.getStyle().set("font-size", "10pt");
		if (status == null) {
			statusLayout.add(new Span("No Status"));
			return statusLayout;
		}
		try {
			// Get status color and apply background
			final String color = status.getColor();
			if (color != null && !color.isEmpty()) {
				statusLayout.getStyle().set("background-color", color);
				statusLayout.getStyle().set("color", CColorUtils.getContrastTextColor(color));
			}
			// Add status icon from DEFAULT_ICON constant if available
			try {
				final String iconName = CColorUtils.getStaticIconFilename(status.getClass());
				if (iconName != null && !iconName.isEmpty()) {
					final Icon icon = CColorUtils.createStyledIcon(iconName);
					if (icon != null) {
						icon.getStyle().set("width", "14px");
						icon.getStyle().set("height", "14px");
						if (color != null && !color.isEmpty()) {
							icon.getStyle().set("color", CColorUtils.getContrastTextColor(color));
						}
						statusLayout.add(icon);
					}
				}
			} catch (final Exception e) {
				LOGGER.debug("Could not get icon for status: {}", e.getMessage());
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not style status display: {}", e.getMessage());
		}
		// Add status name
		final Span statusName = new Span(status.getName() != null ? status.getName() : "Unknown");
		statusLayout.add(statusName);
		return statusLayout;
	}

	/** Creates a styled user display component with icon.
	 * @param user the user entity to display
	 * @return the user display component */
	protected CHorizontalLayout createUserDisplay(final CUser user) {
		final CHorizontalLayout userLayout = new CHorizontalLayout();
		userLayout.setSpacing(true);
		userLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		userLayout.getStyle().set("font-size", "10pt");
		userLayout.getStyle().set("color", "#666");
		if (user == null) {
			userLayout.add(new Span("No Assignee"));
			return userLayout;
		}
		try {
			// Add user icon
			final Icon icon = CColorUtils.createStyledIcon("vaadin:user");
			if (icon != null) {
				icon.getStyle().set("width", "14px");
				icon.getStyle().set("height", "14px");
				icon.getStyle().set("color", "#666");
				userLayout.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create user icon: {}", e.getMessage());
		}
		// Build display name
		String displayName = user.getName();
		if (user.getLastname() != null && !user.getLastname().isEmpty()) {
			displayName = displayName + " " + user.getLastname();
		}
		final Span userName = new Span(displayName != null ? displayName : "Unknown User");
		userLayout.add(userName);
		return userLayout;
	}

	/** Creates a styled date range display component.
	 * @param startDate the start date (can be null)
	 * @param endDate   the end date (can be null)
	 * @return the date range display component */
	protected CHorizontalLayout createDateRangeDisplay(final LocalDate startDate, final LocalDate endDate) {
		final CHorizontalLayout dateLayout = new CHorizontalLayout();
		dateLayout.setSpacing(true);
		dateLayout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		dateLayout.getStyle().set("font-size", "10pt");
		dateLayout.getStyle().set("color", "#666");
		try {
			// Add calendar icon
			final Icon icon = CColorUtils.createStyledIcon("vaadin:calendar");
			if (icon != null) {
				icon.getStyle().set("width", "14px");
				icon.getStyle().set("height", "14px");
				icon.getStyle().set("color", "#666");
				dateLayout.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create calendar icon: {}", e.getMessage());
		}
		// Format date range
		final StringBuilder dateRange = new StringBuilder();
		if (startDate != null) {
			dateRange.append(startDate.format(DATE_FORMATTER));
		}
		if (startDate != null && endDate != null) {
			dateRange.append(" - ");
		}
		if (endDate != null) {
			dateRange.append(endDate.format(DATE_FORMATTER));
		}
		final Span dateSpan = new Span(dateRange.toString());
		dateLayout.add(dateSpan);
		return dateLayout;
	}
}
