package tech.derbent.api.grid.widget;

import java.time.LocalDate;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.utils.Check;
import tech.derbent.base.users.domain.CUser;

/** CComponentWidgetEntityOfProject - Base widget component for project items.
 * <p>
 * This class provides a standardized widget layout for entities that belong to a project (extend CProjectItem). It implements a three-row layout:
 * </p>
 * <ul>
 * <li><b>Row 1 (Top):</b> Entity name (12pt font)</li>
 * <li><b>Row 2 (Middle):</b> Short description (100 chars) and status with color/icon (10pt font)</li>
 * <li><b>Row 3 (Bottom):</b> Responsible user, due date, start date, etc. (10pt font)</li>
 * </ul>
 * <p>
 * Subclasses should override the following template methods to provide entity-specific content:
 * </p>
 * <ul>
 * <li>{@link #getEntityDueDate()} - Return the entity's due date</li>
 * <li>{@link #getEntityStartDate()} - Return the entity's start date</li>
 * <li>{@link #getEntityResponsible()} - Return the entity's responsible user</li>
 * <li>{@link #getEntityStatus()} - Return the entity's status</li>
 * <li>{@link #getEntityDescription()} - Return the entity's description (override from parent)</li>
 * </ul>
 *
 * @param <T> the entity type, must extend CProjectItem
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntity */
public abstract class CComponentWidgetEntityOfProject<T extends CProjectItem<?>> extends CComponentWidgetEntity<T> {

	private static final long serialVersionUID = 1L;

	/** Max description length to display in the widget. */
	protected static final int MAX_DESCRIPTION_LENGTH = 100;

	/** Creates a new project item widget for the specified entity.
	 * @param item the project item to display in the widget */
	public CComponentWidgetEntityOfProject(final T item) {
		super(item);
	}

	/** Template method to get the due date from the entity. Default implementation delegates to CProjectItem.getEndDate().
	 * @return the entity's due date, or null if not available */
	protected LocalDate getEntityDueDate() { return getEntity().getEndDate(); }

	/** Template method to get the responsible user from the entity. Default implementation delegates to CProjectItem.getResponsible().
	 * @return the entity's responsible user, or null if not assigned */
	protected CUser getEntityResponsible() { return getEntity().getResponsible(); }

	/** Template method to get the start date from the entity. Default implementation delegates to CProjectItem.getStartDate().
	 * @return the entity's start date, or null if not available */
	protected LocalDate getEntityStartDate() { return getEntity().getStartDate(); }

	/** Template method to get the status from the entity. Default implementation delegates to CProjectItem.getStatus().
	 * @return the entity's status, or null if not available */
	protected CProjectItemStatus getEntityStatus() { return getEntity().getStatus(); }

	/** Get the name from the entity using direct getter.
	 * @return the entity's name */
	protected String getEntityName() { return getEntity().getName(); }

	@Override
	protected void initializeWidget() {
		super.initializeWidget();
		// Apply widget styling
		addClassNames(Padding.SMALL);
		getStyle().set("border", "1px solid #e9ecef");
		getStyle().set("border-radius", "8px");
		getStyle().set("background-color", "#ffffff");
		getStyle().set("cursor", "pointer");
		getStyle().set("transition", "all 0.2s ease");
		setWidthFull();

		// Configure left layout (3 rows)
		layoutLeft.setPadding(false);
		layoutLeft.setSpacing(false);
		layoutLeft.getStyle().set("flex-grow", "1");
		layoutLeft.getStyle().set("overflow", "hidden");

		// Configure right layout (actions)
		layoutRight.setPadding(false);
		layoutRight.setSpacing(false);
		layoutRight.getStyle().set("flex-shrink", "0");

		// Configure row layouts
		layoutLineOne.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.XSMALL);
		layoutLineTwo.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.XSMALL);
		layoutLineThree.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.XSMALL);

		// Build content
		buildLineOne();
		buildLineTwo();
		buildLineThree();
	}

	/** Build the first line content: Entity name (12pt font). */
	protected void buildLineOne() {
		final CSpan span = new CSpan(getEntityName());
		span.addClassNames(FontWeight.SEMIBOLD, FontSize.SMALL);
		span.getStyle().set("overflow", "hidden");
		span.getStyle().set("text-overflow", "ellipsis");
		span.getStyle().set("white-space", "nowrap");
		layoutLineOne.add(span);
	}

	/** Build the second line content: Short description (100 chars) and status with color/icon (10pt font). */
	protected void buildLineTwo() {
		// Description (truncated)
		final String description = getEntityDescription();
		if (description != null && !description.isEmpty()) {
			final String truncated = description.length() > MAX_DESCRIPTION_LENGTH
					? description.substring(0, MAX_DESCRIPTION_LENGTH) + "..."
					: description;
			final CSpan span = new CSpan(truncated);
			span.addClassNames(FontSize.XSMALL, TextColor.SECONDARY);
			span.getStyle().set("overflow", "hidden");
			span.getStyle().set("text-overflow", "ellipsis");
			span.getStyle().set("white-space", "nowrap");
			span.getStyle().set("flex-grow", "1");
			layoutLineTwo.add(span);
		}

		// Status badge
		final CProjectItemStatus status = getEntityStatus();
		if (status != null) {
			final CDiv badge = createStatusBadge(status);
			layoutLineTwo.add(badge);
		}
	}

	/** Build the third line content: Responsible user, due date, start date, etc. (10pt font). */
	protected void buildLineThree() {
		// Responsible user
		final CUser user = getEntityResponsible();
		if (user != null) {
			final CDiv row = createInfoItem(VaadinIcon.USER, user.getName());
			layoutLineThree.add(row);
		}

		// Start date
		final LocalDate startDate = getEntityStartDate();
		if (startDate != null) {
			final CDiv row = createInfoItem(VaadinIcon.CALENDAR, startDate.format(DATE_FORMATTER));
			layoutLineThree.add(row);
		}

		// Due date
		final LocalDate dueDate = getEntityDueDate();
		if (dueDate != null) {
			final CDiv row = createInfoItem(VaadinIcon.CALENDAR_CLOCK, dueDate.format(DATE_FORMATTER));
			layoutLineThree.add(row);
		}
	}

	/** Create a status badge with color and icon.
	 * @param status the status to display
	 * @return a styled CDiv containing the status badge */
	protected CDiv createStatusBadge(final CProjectItemStatus status) {
		Check.notNull(status, "Status cannot be null");
		final CDiv badge = new CDiv();
		badge.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.XSMALL);
		badge.getStyle().set("padding", "2px 6px");
		badge.getStyle().set("border-radius", "4px");
		badge.getStyle().set("font-size", "10px");
		badge.getStyle().set("flex-shrink", "0");

		// Set background color from status
		final String color = status.getColor();
		if (color != null && !color.isEmpty()) {
			badge.getStyle().set("background-color", color);
			badge.getStyle().set("color", "#ffffff");
		} else {
			badge.getStyle().set("background-color", "#e9ecef");
			badge.getStyle().set("color", "#6c757d");
		}

		// Add status name
		final CSpan span = new CSpan(status.getName());
		badge.add(span);

		return badge;
	}

	/** Create an info item with icon and text.
	 * @param icon the icon to display
	 * @param text the text to display
	 * @return a styled CDiv containing the info item */
	protected CDiv createInfoItem(final VaadinIcon icon, final String text) {
		final CDiv row = new CDiv();
		row.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.XSMALL);
		row.getStyle().set("flex-shrink", "0");

		if (icon != null) {
			final Icon infoIcon = icon.create();
			infoIcon.setSize("12px");
			infoIcon.getStyle().set("color", "var(--lumo-secondary-text-color)");
			row.add(infoIcon);
		}

		if (text != null && !text.isEmpty()) {
			final Span span = new Span(text);
			span.getStyle().set("font-size", "10px");
			span.getStyle().set("color", "var(--lumo-secondary-text-color)");
			row.add(span);
		}

		return row;
	}
}
