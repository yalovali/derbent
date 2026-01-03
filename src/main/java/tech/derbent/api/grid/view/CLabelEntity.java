package tech.derbent.api.grid.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IHasColor;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.ui.component.basic.CH2;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.base.users.domain.CUser;

/** CLabelEntity - Unified label component for displaying entities or text values.
 * <p>
 * This component provides a standardized way to display entities across the application with appropriate icons, colors, and formatting based on
 * entity type. It automatically detects entity capabilities (IHasIcon, CStatus, CTypeEntity, CUser) and renders accordingly.
 * </p>
 * <p>
 * <strong>Factory Methods:</strong>
 * </p>
 * <ul>
 * <li>{@link #createLabel(CEntityDB)} - Creates a decorated label for an entity</li>
 * <li>{@link #createLabel(String)} - Creates a simple label for text</li>
 * <li>{@link #createPlainLabel(CEntityDB)} - Creates a plain label without decoration</li>
 * <li>{@link #createH2Label(CEntityDB)} - Creates an H2 header label for entity</li>
 * <li>{@link #createH2Label(String)} - Creates an H2 header label for text</li>
 * <li>{@link #createH3Label(CEntityDB)} - Creates an H3 header label for entity</li>
 * <li>{@link #createH3Label(String)} - Creates an H3 header label for text</li>
 * <li>{@link #createDateLabel(LocalDate)} - Creates a date label with calendar icon</li>
 * <li>{@link #createDateRangeLabel(LocalDate, LocalDate)} - Creates a date range label</li>
 * <li>{@link #createUserLabel(CUser)} - Creates a user label with icon</li>
 * </ul>
 * @author Derbent Framework
 * @since 1.0 */
public class CLabelEntity extends Div {

	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
	private static final String DEFAULT_BORDER_RADIUS = "4px";
	private static final String DEFAULT_PADDING = "4px 8px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CLabelEntity.class);
	private static final long serialVersionUID = 1L;

	/** Applies color styling to an icon based on entity color.
	 * @param icon   the icon to style
	 * @param entity the entity providing the color */
	private static void applyIconColor(final Icon icon, final CEntityDB<?> entity) {
		if (icon == null) {
			return;
		}
		Check.instanceOf(entity, IHasIcon.class, "Entity must implement IHasIcon to apply icon color");
		try {
			final String color = CColorUtils.getColorFromEntity(entity);
			icon.getStyle().set("width", "24px").set("height", "24px").set("flex-shrink", "0");
			if (color != null && !color.isBlank()) {
				icon.getStyle().set("color", color);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not get color for entity icon: {}", e.getMessage());
		}
	}

	/** Creates a date label with calendar icon.
	 * @param date the date to display (can be null)
	 * @return a CLabelEntity with date display */
	public static CLabelEntity createDateLabel(final LocalDate date) {
		final CLabelEntity label = new CLabelEntity();
		label.getStyle().set("font-size", "10pt").set("color", "#666");
		if (date == null) {
			label.setText("No date");
			return label;
		}
		try {
			final Icon icon = CColorUtils.createStyledIcon("vaadin:calendar");
			if (icon != null) {
				icon.getStyle().set("width", "14px").set("height", "14px").set("color", "#666");
				label.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create calendar icon: {}", e.getMessage());
		}
		final Span dateSpan = new Span(date.format(DATE_FORMATTER));
		label.add(dateSpan);
		return label;
	}

	/** Creates a date range label with calendar icon.
	 * @param startDate the start date (can be null)
	 * @param endDate   the end date (can be null)
	 * @return a CLabelEntity with date range display */
	public static CLabelEntity createDateRangeLabel(final LocalDate startDate, final LocalDate endDate) {
		final CLabelEntity label = new CLabelEntity();
		label.getStyle().set("font-size", "10pt").set("color", "#666");
		if (startDate == null && endDate == null) {
			label.setText("No dates");
			return label;
		}
		try {
			final Icon icon = CColorUtils.createStyledIcon("vaadin:calendar");
			if (icon != null) {
				icon.getStyle().set("width", "14px").set("height", "14px").set("color", "#666");
				label.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not create calendar icon: {}", e.getMessage());
		}
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
		label.add(dateSpan);
		return label;
	}

	/** Creates an H2 header label for an entity with icon and color.
	 * @param entity the entity to display
	 * @return a Div containing an H2 with entity display
	 * @throws Exception */
	public static CLabelEntity createH2Label(final CEntityDB<?> entity) throws Exception {
		final CLabelEntity container = new CLabelEntity();
		final String displayText = CColorUtils.getDisplayTextFromEntity(entity);
		final CH2 header = new CH2(displayText);
		header.getStyle().set("margin", "0");
		if (entity instanceof IHasIcon) {
			final Icon icon = CColorUtils.getIconForEntity(entity);
			if (icon != null) {
				applyIconColor(icon, entity);
				container.add(icon);
			}
		}
		container.add(header);
		return container;
	}

	/** Creates an H2 header label for text.
	 * @param text the text to display
	 * @return a Div containing an H2 */
	public static CLabelEntity createH2Label(final String text) {
		final CLabelEntity container = new CLabelEntity();
		final CH2 header = new CH2(text != null ? text : "");
		header.getStyle().set("margin", "0");
		container.add(header);
		return container;
	}

	/** Creates an H3 header label for an entity with icon and color.
	 * @param entity the entity to display
	 * @return a Div containing an H3 with entity display
	 * @throws Exception */
	public static Div createH3Label(final CEntityDB<?> entity) throws Exception {
		final CLabelEntity container = new CLabelEntity();
		final String displayText = CColorUtils.getDisplayTextFromEntity(entity);
		final CH3 header = new CH3(displayText);
		header.getStyle().set("margin", "0");
		if (entity instanceof IHasIcon) {
			final Icon icon = CColorUtils.getIconForEntity(entity);
			if (icon != null) {
				applyIconColor(icon, entity);
				container.add(icon);
			}
		}
		container.add(header);
		return container;
	}

	/** Creates an H3 header label for text.
	 * @param text the text to display
	 * @return a Div containing an H3 */
	public static Div createH3Label(final String text) {
		final CLabelEntity container = new CLabelEntity();
		container.getStyle().set("display", "flex").set("align-items", "center");
		final CH3 header = new CH3(text != null ? text : "");
		header.getStyle().set("margin", "0");
		container.add(header);
		return container;
	}

	/** Creates a plain label for an entity without any decoration (no icon, no color).
	 * @param entity the entity to display (can be null)
	 * @return a new CLabelEntity instance
	 * @throws Exception */
	public static CLabelEntity createPlainLabel(final CEntityDB<?> entity) throws Exception {
		final CLabelEntity label = new CLabelEntity();
		label.setValue(entity, false);
		return label;
	}

	protected static Avatar createUserAvatar(final CUser user) {
		return createUserAvatar(user, "24px");
	}

	protected static Avatar createUserAvatar(final CUser user, final String size) {
		final Avatar avatar = user.getAvatar();
		avatar.setWidth(size);
		avatar.setHeight(size);
		avatar.getStyle().set("flex-shrink", "0");
		return avatar;
	}

	/** Creates a user label with icon and full name.
	 * @param user the user to display (can be null)
	 * @return a CLabelEntity with user display */
	public static CLabelEntity createUserLabel(final CUser user) {
		final CLabelEntity label = new CLabelEntity();
		if (user == null) {
			label.setText("No user");
			label.getStyle().set("color", "#666").set("font-style", "italic");
			return label;
		}
		label.add(createUserAvatar(user));
		String displayName = user.getName();
		if (user.getLastname() != null && !user.getLastname().isEmpty()) {
			displayName += " " + user.getLastname();
		}
		final Span nameSpan = new Span(displayName);
		label.add(nameSpan);
		return label;
	}

	/** Creates a compact user label with abbreviated name for space-constrained displays.
	 * Displays only first name with avatar, truncating long names to 12 characters.
	 * @param user the user to display (can be null)
	 * @return a CLabelEntity with compact user display */
	public static CLabelEntity createCompactUserLabel(final CUser user) {
		final CLabelEntity label = new CLabelEntity();
		if (user == null) {
			label.setText("No user");
			label.getStyle().set("color", "#666").set("font-style", "italic");
			return label;
		}
		
		label.add(createUserAvatar(user, "16px"));
		
		String displayName = user.getName();
		if (displayName != null && displayName.length() > 15) {
			displayName = displayName.substring(0, 12) + "...";
		}
		
		final Span nameSpan = new Span("👤 " + displayName);
		nameSpan.getStyle()
			.set("font-size", "11px")
			.set("color", "#666")
			.set("display", "inline-flex")
			.set("align-items", "center")
			.set("gap", "2px");
		label.add(nameSpan);
		
		return label;
	}

	/** Default constructor. */
	public CLabelEntity() {
		super();
		initializeComponent();
	}

	/** Constructor with entity value.
	 * @param entity the entity to display
	 * @throws Exception */
	public CLabelEntity(final CEntityDB<?> entity) throws Exception {
		super();
		initializeComponent();
		setValue(entity, true);
	}

	/** Constructor with text content.
	 * @param text the text content */
	public CLabelEntity(final String text) {
		super();
		initializeComponent();
		setTextValue(text);
	}

	/** Initializes the component with default styling. */
	private void initializeComponent() {
		getStyle().set("display", "flex");
		getStyle().set("align-items", "center");
		getStyle().set("padding", DEFAULT_PADDING);
		getStyle().set("box-sizing", "border-box");
		getStyle().set("gap", "6px");
	}

	/** Resets styling to default state. */
	private void resetStyles() {
		getStyle().remove("background-color");
		getStyle().remove("color");
		getStyle().remove("font-style");
		getStyle().remove("border-radius");
	}

	/** Sets a text value.
	 * @param text the text to display */
	public void setTextValue(final String text) {
		removeAll();
		resetStyles();
		setText(text != null ? text : "");
	}

	public void setValue(final CEntityDB<?> entity, final boolean showIconColor) throws Exception {
		setValue(entity, CColorUtils.getDisplayTextFromEntity(entity), showIconColor);
	}

	public void setValue(final CEntityDB<?> entity, String displayText, final boolean showIconColor) throws Exception {
		try {
			removeAll();
			resetStyles();
			if (entity == null) {
				setText("N/A");
				getStyle().set("color", "#666666");
				getStyle().set("font-style", "italic");
				return;
			}
			if (entity instanceof IHasColor && showIconColor) {
				try {
					final String color = CColorUtils.getColorFromEntity(entity);
					getStyle().set("background-color", color);
					getStyle().set("color", CColorUtils.getContrastTextColor(color));
					getStyle().set("border-radius", DEFAULT_BORDER_RADIUS);
				} catch (final Exception e) {
					LOGGER.debug("Could not get color for entity: {}", e.getMessage());
				}
			}
			if (entity instanceof CUser && showIconColor) {
				add(createUserAvatar((CUser) entity));
			} else if (entity instanceof IHasIcon && showIconColor) {
				final Icon icon = CColorUtils.getIconForEntity(entity);
				CColorUtils.styleIcon(icon);
				try {
					final String color = CColorUtils.getColorFromEntity(entity);
					icon.getStyle().set("color", CColorUtils.getContrastTextColor(color));
				} catch (final Exception e) {
					LOGGER.debug("Could not get color for entity icon: {}", e.getMessage());
				}
				add(icon);
			}
			// Add text content
			final Span textSpan = new Span(displayText);
			add(textSpan);
		} catch (final Exception e) {
			LOGGER.error("Error setting value for CLabelEntity: {}", e.getMessage());
			throw e;
		}
	}
}
