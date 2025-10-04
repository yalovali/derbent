package tech.derbent.api.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import tech.derbent.api.domains.CEntity;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.domains.CStatus;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.views.CAbstractNamedEntityPage;
import tech.derbent.users.domain.CUser;

public final class CColorUtils {

	private static final String DEFAULT_ICON_MARGIN = "6px";
	private static final String DEFAULT_ICON_SIZE = "16px";
	/** Color for Cancel buttons */
	public static final String CRUD_CANCEL_COLOR = "#6c757d";
	/** Icon for Cancel buttons */
	public static final String CRUD_CANCEL_ICON = "vaadin:close";
	/** Icon for Clone/Copy buttons */
	public static final String CRUD_CLONE_ICON = "vaadin:copy";
	// CRUD Button Color Constants
	/** Color for Create/New buttons */
	public static final String CRUD_CREATE_COLOR = "#28a745";
	// CRUD Button Icon Constants
	/** Icon for Create/New buttons */
	public static final String CRUD_CREATE_ICON = "vaadin:plus";
	/** Color for Delete/Remove buttons */
	public static final String CRUD_DELETE_COLOR = "#dc3545";
	/** Icon for Delete/Remove buttons */
	public static final String CRUD_DELETE_ICON = "vaadin:trash";
	/** Icon for Edit/Update buttons */
	public static final String CRUD_EDIT_ICON = "vaadin:edit";
	/** Icon for Home/Dashboard buttons */
	public static final String CRUD_HOME_ICON = "vaadin:home";
	/** Icon for Layout toggle buttons */
	public static final String CRUD_LAYOUT_HORIZONTAL_ICON = "vaadin:grid-h";
	/** Icon for Layout toggle buttons */
	public static final String CRUD_LAYOUT_VERTICAL_ICON = "vaadin:grid-v";
	/** Color for Read/View buttons */
	public static final String CRUD_READ_COLOR = "#17a2b8";
	/** Color for Save buttons */
	public static final String CRUD_SAVE_COLOR = "#007bff";
	/** Icon for Save buttons */
	public static final String CRUD_SAVE_ICON = "vaadin:check";
	/** Color for Update/Edit buttons */
	public static final String CRUD_UPDATE_COLOR = "#ffc107";
	/** Icon for View/Read buttons */
	public static final String CRUD_VIEW_ICON = "vaadin:eye";
	/** Default color for status entities without color */
	public static final String DEFAULT_COLOR = "#95a5a6";
	/** Default text color for light backgrounds */
	public static final String DEFAULT_DARK_TEXT = "black";
	/** Default text color for dark backgrounds */
	public static final String DEFAULT_LIGHT_TEXT = "white";
	private static final Logger LOGGER = LoggerFactory.getLogger(CColorUtils.class);

	public static Icon createStyledIcon(final String iconString) {
		Check.notBlank(iconString, "Icon string cannot be null or blank");
		final Icon icon = styleIcon(new Icon(iconString));
		icon.addClassNames(IconSize.MEDIUM);
		return icon;
	}

	public static Icon createStyledIcon(final String iconString, final String color) {
		final Icon icon = createStyledIcon(iconString);
		Check.notNull(icon, "Icon cannot be null");
		Check.notBlank(color, "Color cannot be null or blank");
		icon.getStyle().set("color", color);
		return icon;
	}

	public static Icon setIconClassSize(Icon icon, String iconSizeClass) {
		if (icon == null) {
			return null;
		}
		// clear old styles
		icon.getStyle().remove("width");
		icon.getStyle().remove("height");
		icon.getStyle().remove("min-width");
		icon.getStyle().remove("min-height");
		switch (iconSizeClass) {
		case IconSize.MEDIUM:
			icon.getStyle().set("width", "24px").set("height", "24px");
			break;
		case IconSize.LARGE:
			icon.getStyle().set("width", "32px").set("height", "32px");
			break;
		case IconSize.SMALL:
			icon.getStyle().set("width", "16px").set("height", "16px");
			break;
		}
		icon.addClassNames(iconSizeClass);
		return icon;
	}

	public static String getColorFromEntity(final CEntity<?> entity) throws Exception {
		Check.notNull(entity, "entity cannot be null");
		if (entity instanceof CTypeEntity) {
			final CTypeEntity<?> typeEntity = (CTypeEntity<?>) entity;
			return typeEntity.getColor();
		}
		if (entity instanceof CEntityDB) {
			return getStaticIconColorCode(entity.getClass());
		}
		final String errorMsg = String.format("Entity of type %s does not support color extraction", entity.getClass().getSimpleName());
		LOGGER.debug(errorMsg);
		throw new RuntimeException(errorMsg);
	}

	/** Determines appropriate text color based on background color. Uses a simple brightness calculation to determine if white or black text would be
	 * more readable.
	 * @param backgroundColor the background color in hex format (e.g., "#FF0000")
	 * @return "white" for dark backgrounds, "black" for light backgrounds */
	public static String getContrastTextColor(final String backgroundColor) {
		Check.notBlank(backgroundColor, "Background color cannot be null or blank");
		try {
			String color = backgroundColor.trim();
			// Remove # if present
			if (color.startsWith("#")) {
				color = color.substring(1);
			}
			// Parse RGB values
			if (color.length() == 6) {
				final int r = Integer.parseInt(color.substring(0, 2), 16);
				final int g = Integer.parseInt(color.substring(2, 4), 16);
				final int b = Integer.parseInt(color.substring(4, 6), 16);
				// Calculate brightness using relative luminance formula
				final double brightness = ((0.299 * r) + (0.587 * g) + (0.114 * b)) / 255;
				// Return white text for dark backgrounds, black text for light backgrounds
				return brightness < 0.5 ? DEFAULT_LIGHT_TEXT : DEFAULT_DARK_TEXT;
			}
		} catch (final Exception e) {
			LOGGER.debug("Error calculating contrast color for background {}: {}", backgroundColor, e.getMessage());
			throw e;
		}
		return DEFAULT_DARK_TEXT; // Default fallback
	}

	public static String getDisplayTextFromEntity(final Object item) {
		if (item == null) {
			return "N/A";
		}
		// Try to get the name property if the object has one
		if (item instanceof CEntityDB) {
			try {
				final Method m = item.getClass().getMethod("getName");
				final Object v = m.invoke(item);
				return v == null ? "" : v.toString();
			} catch (final ReflectiveOperationException ignore) {
				// Fallback to toString if getName() is not available
			}
		}
		return item.toString();
	}

	public static Icon getIconForEntity(final CEntityDB<?> entity) throws Exception {
		Icon icon = new Icon(getStaticIconFilename(entity.getClass().getName()));
		return styleIcon(icon);
	}

	public static Icon getIconForViewClass(final CAbstractNamedEntityPage<?> view) throws Exception {
		Icon icon = new Icon(getStaticIconFilename(view.getClass().getName()));
		return styleIcon(icon);
	}

	public static Icon getIconForViewClass(final Class<? extends CAbstractNamedEntityPage<?>> clazz) throws Exception {
		Icon icon = new Icon(getStaticIconFilename(clazz));
		return styleIcon(icon);
	}

	public static String getRandomColor(boolean dark) { // TODO Auto-generated method stub
		// Generate a random color in hex format
		if (dark) {
			String color;
			do {
				color = String.format("#%06x", (int) (Math.random() * 0xFFFFFF));
			} while (getContrastTextColor(color).equals(DEFAULT_DARK_TEXT)); // Ensure it's a dark color
			return color;
		}
		String color = String.format("#%06x", (int) (Math.random() * 0xFFFFFF));
		return color;
	}

	public static String getRouteForView(final Class<? extends CAbstractNamedEntityPage<?>> clazz) {
		return Optional.ofNullable(clazz.getAnnotation(Route.class)).map(Route::value).filter(s -> !s.isBlank()) // boş string durumunu da kontrol et
				.orElseThrow(() -> new IllegalArgumentException("Missing @Route on " + clazz.getSimpleName()));
	}

	public static String getStaticIconColorCode(final Class<?> clazz) throws Exception {
		try {
			return getStaticStringValue(clazz, "DEFAULT_COLOR");
		} catch (Exception e) {
			LOGGER.error("Error getting static icon filename for class {}: {}", clazz.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	public static String getStaticIconColorCode(final String className) throws Exception {
		Check.notBlank(className, "className is blank");
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Check.notNull(cl, "ClassLoader is null");
		final Class<?> clazz = Class.forName(className, true, cl);
		return getStaticIconColorCode(clazz);
	}

	public static String getStaticIconFilename(final Class<?> clazz) throws Exception {
		try {
			return getStaticStringValue(clazz, "DEFAULT_ICON");
		} catch (Exception e) {
			LOGGER.error("Error getting static icon filename for class {}: {}", clazz.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	public static String getStaticIconFilename(final String className) throws Exception {
		Check.notBlank(className, "className is blank");
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Check.notNull(cl, "ClassLoader is null");
		final Class<?> clazz = Class.forName(className, true, cl);
		return getStaticIconFilename(clazz);
	}

	public static String getStaticStringValue(final Class<?> clazz, final String fieldName) throws Exception {
		try {
			final Field field = clazz.getDeclaredField(fieldName);
			return field.get(null).toString();
		} catch (Exception e) {
			LOGGER.error("Error getting static string value for field {} in class {}: {}", fieldName, clazz.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	public static String getStaticStringValue(final String className, final String fieldName) throws Exception {
		Check.notBlank(className, "className is blank");
		Check.notBlank(fieldName, "fieldName is blank");
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Check.notNull(cl, "ClassLoader is null");
		final Class<?> clazz = Class.forName(className, true, cl);
		return getStaticStringValue(clazz, fieldName);
	}

	public static String getTitleForView(final Class<? extends CAbstractNamedEntityPage<?>> clazz) {
		return Optional.ofNullable(clazz.getAnnotation(PageTitle.class)).map(PageTitle::value)
				.orElseThrow(() -> new IllegalArgumentException("Missing @PageTitle on " + clazz.getSimpleName()));
	}

	public static boolean isStatusEntity(final Class<?> entityType) {
		Check.notNull(entityType, "entityType cannot be null");
		try {
			// Check if the class extends CStatus
			if (CStatus.class.isAssignableFrom(entityType)) {
				return true;
			}
			// Check if the class extends CTypeEntity (which status entities inherit from)
			if (CTypeEntity.class.isAssignableFrom(entityType)) {
				return true;
			}
			// Also check by class name pattern for status entities
			final String className = entityType.getSimpleName();
			if (className.endsWith("Status") || className.contains("Status")) {
				return true;
			}
			return false;
		} catch (final Exception e) {
			LOGGER.error("Error checking if entity type {} is a status entity: {}", entityType.getSimpleName(), e.getMessage());
			return false;
		}
	}

	/** Gets user initials for avatar */
	private String getInitials(final CUser user) {
		Check.notNull(user, "User cannot be null when generating initials");
		final StringBuilder initials = new StringBuilder();
		if ((user.getName() != null) && !user.getName().isEmpty()) {
			initials.append(user.getName().charAt(0));
		}
		if ((user.getLastname() != null) && !user.getLastname().isEmpty()) {
			initials.append(user.getLastname().charAt(0));
		}
		return initials.length() > 0 ? initials.toString().toUpperCase() : "?";
	}

	/** Creates a user avatar with profile picture if available */
	protected Avatar createUserAvatar(final CUser user) {
		final Avatar avatar = new Avatar();
		Check.notNull(user, "User cannot be null when creating avatar");
		avatar.setName(user.getName() + " " + (user.getLastname() != null ? user.getLastname() : ""));
		if ((user.getProfilePictureData() != null) && (user.getProfilePictureData().length > 0)) {
			// TODO: Convert byte array to StreamResource for avatar For now, just use
			// initials
		}
		avatar.setAbbreviation(getInitials(user));
		return avatar;
	}

	/** Gets a generic entity display with icon - works for any named entity with icon support. This method creates a horizontal layout containing an
	 * entity's icon and name.
	 * @param entity the named entity to display (must not be null)
	 * @return a HorizontalLayout containing the entity's icon and name
	 * @throws IllegalArgumentException if entity is null */
	public static HorizontalLayout getEntityWithIcon(final CEntityNamed<?> entity) {
		Check.notNull(entity, "Entity cannot be null when creating entity with icon display");
		final HorizontalLayout layout = new HorizontalLayout();
		layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
		layout.setSpacing(true);
		try {
			// Get the entity's icon using the existing infrastructure
			final Icon icon = getIconForEntity(entity);
			if (icon != null) {
				icon.setSize("24px");
				layout.add(icon);
			}
		} catch (final Exception e) {
			LOGGER.warn("Could not create icon for entity {}: {}", entity.getClass().getSimpleName(), e.getMessage());
			// Continue without icon rather than failing completely
		}
		// Add entity name - for Users, include lastname if available
		String displayName = entity.getName();
		if (entity instanceof CUser) {
			final CUser user = (CUser) entity;
			displayName = user.getName() + " " + (user.getLastname() != null ? user.getLastname() : "");
		}
		final Span entityName = new Span(displayName);
		layout.add(entityName);
		return layout;
	}

	/** Private constructor to prevent instantiation. */
	private CColorUtils() {
		// Utility class - no instantiation
	}

	/** Applies icon styling with consistent sizing and spacing.
	 * @param icon the icon to style */
	public static Icon styleIcon(final Icon icon) {
		if (icon == null) {
			return null;
		}
		icon.getStyle().set("margin-right", DEFAULT_ICON_MARGIN);
		icon.getStyle().set("width", DEFAULT_ICON_SIZE);
		icon.getStyle().set("height", DEFAULT_ICON_SIZE);
		icon.getStyle().set("flex-shrink", "0"); // Prevent icon from shrinking
		return icon;
	}
}
