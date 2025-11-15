package tech.derbent.api.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.IconSize;
import tech.derbent.api.domains.CStatus;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entity.domain.CEntity;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.view.CAbstractNamedEntityPage;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.base.users.domain.CUser;

public final class CColorUtils {

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
	private static final String DEFAULT_ICON_MARGIN = "6px";
	private static final String DEFAULT_ICON_SIZE = "16px";
	/** Default text color for dark backgrounds */
	public static final String DEFAULT_LIGHT_TEXT = "white";
	private static final Logger LOGGER = LoggerFactory.getLogger(CColorUtils.class);
	public static String Symbol_BoxChecked = "☒";
	public static String Symbol_BoxUnchecked = "☐";

	public static Span createStyledHeader(final String text, final String color) {
		final Span header = new Span(text);
		header.getStyle().set("color", color);
		header.getStyle().set("font-weight", "bold");
		header.getStyle().set("font-size", "14px");
		header.getStyle().set("text-transform", "uppercase");
		return header;
	}

	public static Icon createStyledIcon(final String iconString) {
		Check.notBlank(iconString, "Icon string cannot be null or blank");
		final Icon icon = styleIcon(new Icon(iconString));
		icon.addClassNames(IconSize.SMALL);
		return icon;
	}

	public static Icon createStyledIcon(final String iconString, final String color) {
		final Icon icon = createStyledIcon(iconString);
		Check.notNull(icon, "Icon cannot be null");
		Check.notBlank(color, "Color cannot be null or blank");
		icon.getStyle().set("color", color);
		return icon;
	}

	public static void debugStyleOfComponent(final Component component) {
		if (component == null) {
			LOGGER.debug("Component is null, cannot debug style");
			return;
		}
		LOGGER.debug("Debugging styles for component of type {}:", component.getClass().getSimpleName());
		component.getElement().getAttributeNames().forEach(attr -> {
			final String value = component.getElement().getAttribute(attr);
			LOGGER.debug("  Attribute: {} = {}", attr, value);
		});
		component.getElement().getStyle().getNames().forEach(style -> {
			final String value = component.getElement().getStyle().get(style);
			LOGGER.debug("  Style: {} = {}", style, value);
		});
	}

	/** Helper to calculate brightness (0 = dark, 1 = bright) */
	private static double getBrightness(final String hex) {
		final int r = Integer.parseInt(hex.substring(1, 3), 16);
		final int g = Integer.parseInt(hex.substring(3, 5), 16);
		final int b = Integer.parseInt(hex.substring(5, 7), 16);
		// relative luminance formula (per W3C)
		return ((0.2126 * r) + (0.7152 * g) + (0.0722 * b)) / 255.0;
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

	public static Icon getIconForEntity(final CEntityDB<?> entity) throws Exception {
		final Icon icon = new Icon(getStaticIconFilename(entity.getClass().getName()));
		return styleIcon(icon);
	}

	public static Icon getIconForViewClass(final CAbstractNamedEntityPage<?> view) throws Exception {
		final Icon icon = new Icon(getStaticIconFilename(view.getClass().getName()));
		return styleIcon(icon);
	}

	public static Icon getIconForViewClass(final Class<? extends CAbstractNamedEntityPage<?>> clazz) throws Exception {
		final Icon icon = new Icon(getStaticIconFilename(clazz));
		return styleIcon(icon);
	}

	public static String getRandomColor(final boolean dark) {
		String color;
		double brightness;
		do {
			color = String.format("#%06x", (int) (Math.random() * 0xFFFFFF));
			brightness = getBrightness(color);
		} while (dark && (dark ? brightness > 0.3 : brightness <= 1)); // dark < 0.5, light >= 0.5
		return color;
	}

	public static String getRandomFromWebColors(final boolean dark) {
		final List<String> colors = getWebColors();
		// Filter colors based on brightness
		List<String> filtered = new ArrayList<>();
		if (dark) {
			for (final String hex : colors) {
				final double brightness = getBrightness(hex);
				if (dark && (brightness < 0.7)) { // dark colors
					filtered.add(hex);
				}
			}
		}
		// fallback: if no filtered colors, use full list
		if (filtered.isEmpty()) {
			filtered = colors;
		}
		final int index = (int) (Math.random() * filtered.size());
		return filtered.get(index);
	}

	public static String getRouteForView(final Class<? extends CAbstractNamedEntityPage<?>> clazz) {
		return Optional.ofNullable(clazz.getAnnotation(Route.class)).map(Route::value).filter(s -> !s.isBlank())
				.orElseThrow(() -> new IllegalArgumentException("Missing @Route on " + clazz.getSimpleName()));
	}

	public static String getStaticIconColorCode(final Class<?> clazz) throws Exception {
		try {
			// Try registry first for fast lookup
			final String color = CEntityRegistry.getDefaultColor(clazz);
			if (color != null) {
				return color;
			}
			// Fall back to reflection for backward compatibility
			return getStaticStringValue(clazz, "DEFAULT_COLOR");
		} catch (final Exception e) {
			LOGGER.error("Error getting static icon color code for class {}: {}", clazz.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	public static String getStaticIconColorCode(final String className) throws Exception {
		Check.notBlank(className, "className is blank");
		// Try registry first for fast lookup
		final String color = CEntityRegistry.getDefaultColorByName(className);
		if (color != null) {
			return color;
		}
		// Fall back to reflection for backward compatibility
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Check.notNull(cl, "ClassLoader is null");
		final Class<?> clazz = Class.forName(className, true, cl);
		return getStaticIconColorCode(clazz);
	}

	public static String getStaticIconFilename(final Class<?> clazz) throws Exception {
		try {
			// Try registry first for fast lookup
			final String icon = CEntityRegistry.getDefaultIcon(clazz);
			if (icon != null) {
				return icon;
			}
			// Fall back to reflection for backward compatibility
			return getStaticStringValue(clazz, "DEFAULT_ICON");
		} catch (final Exception e) {
			LOGGER.error("Error getting static icon filename for class {}: {}", clazz.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	public static String getStaticIconFilename(final String className) throws Exception {
		Check.notBlank(className, "className is blank");
		// Try registry first for fast lookup
		final String icon = CEntityRegistry.getDefaultIconByName(className);
		if (icon != null) {
			return icon;
		}
		// Fall back to reflection for backward compatibility
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Check.notNull(cl, "ClassLoader is null");
		final Class<?> clazz = Class.forName(className, true, cl);
		return getStaticIconFilename(clazz);
	}

	public static String getStaticStringValue(final Class<?> clazz, final String fieldName) throws Exception {
		try {
			final Field field = clazz.getDeclaredField(fieldName);
			return field.get(null).toString();
		} catch (final Exception e) {
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

	public static List<String> getWebColors() {
		// Common hex color constants for color picker
		final List<String> colors = new ArrayList<>();
		colors.add("#F0F8FF"); // AliceBlue
		colors.add("#FAEBD7"); // AntiqueWhite
		colors.add("#00FFFF"); // Aqua
		colors.add("#7FFFD4"); // Aquamarine
		colors.add("#F0FFFF"); // Azure
		colors.add("#F5F5DC"); // Beige
		colors.add("#FFE4C4"); // Bisque
		colors.add("#000000"); // Black
		colors.add("#FFEBCD"); // BlanchedAlmond
		colors.add("#0000FF"); // Blue
		colors.add("#8A2BE2"); // BlueViolet
		colors.add("#A52A2A"); // Brown
		colors.add("#DEB887"); // BurlyWood
		colors.add("#5F9EA0"); // CadetBlue
		colors.add("#7FFF00"); // Chartreuse
		colors.add("#D2691E"); // Chocolate
		colors.add("#FF7F50"); // Coral
		colors.add("#6495ED"); // CornflowerBlue
		colors.add("#FFF8DC"); // Cornsilk
		colors.add("#DC143C"); // Crimson
		colors.add("#00FFFF"); // Cyan
		colors.add("#00008B"); // DarkBlue
		colors.add("#008B8B"); // DarkCyan
		colors.add("#B8860B"); // DarkGoldenRod
		colors.add("#A9A9A9"); // DarkGray
		colors.add("#006400"); // DarkGreen
		colors.add("#BDB76B"); // DarkKhaki
		colors.add("#8B008B"); // DarkMagenta
		colors.add("#556B2F"); // DarkOliveGreen
		colors.add("#f39c12");
		colors.add("#d012d7"); // DarkOrchid
		colors.add("#e74c3c"); // DarkRed
		colors.add("#27ae60"); // DarkSalmon
		colors.add("#95a5a6"); // DarkSeaGreen
		return colors;
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

	public static Icon setIconClassSize(final Icon icon, final String iconSizeClass) {
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
			icon.getStyle().set("min-width", "24px").set("min-height", "24px");
			break;
		case IconSize.LARGE:
			icon.getStyle().set("min-width", "32px").set("min-height", "32px");
			break;
		case IconSize.SMALL:
			icon.getStyle().set("min-width", "16px").set("min-height", "16px");
			break;
		default:
			throw new IllegalArgumentException("Invalid icon size class: " + iconSizeClass);
		}
		icon.addClassNames(iconSizeClass);
		return icon;
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

	/** Private constructor to prevent instantiation. */
	private CColorUtils() {
		// Utility class - no instantiation
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
}
