package tech.derbent.abstracts.utils;

import java.lang.reflect.Method;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import tech.derbent.abstracts.domains.CEntity;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CTypeEntity;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.base.domain.CStatus;

/** CColorUtils - Utility class for color operations and status entity color management.
 * <p>
 * This utility class provides common methods for extracting colors from status entities, calculating contrast text colors, and applying color styling
 * to UI components.
 * </p>
 * <p>
 * The class follows the project's coding guidelines by centralizing color-related logic and providing reusable methods for color-aware components.
 * </p>
 * @author Derbent Framework
 * @since 1.0 */
public final class CColorUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(CColorUtils.class);
	/** Default color for status entities without color */
	public static final String DEFAULT_COLOR = "#95a5a6";
	/** Default text color for light backgrounds */
	public static final String DEFAULT_DARK_TEXT = "black";
	/** Default text color for dark backgrounds */
	public static final String DEFAULT_LIGHT_TEXT = "white";

	/** Applies color styling to a component using CSS styles. This is a helper method that can be used with any component that has getStyle() method.
	 * @param component       the component to style (must have getStyle() method)
	 * @param backgroundColor the background color
	 * @param autoContrast    whether to automatically calculate text color
	 * @param padding         the padding to apply
	 * @param borderRadius    the border radius to apply
	 * @param minWidth        the minimum width to apply */
	public static void applyColorStyling(final Object component, final String backgroundColor, final boolean autoContrast, final String padding,
			final String borderRadius, final String minWidth) {
		if ((component == null) || (backgroundColor == null)) {
			return;
		}
		try {
			// Use reflection to get the style property
			final Method getStyleMethod = component.getClass().getMethod("getStyle");
			final Object style = getStyleMethod.invoke(component);
			final Method setMethod = style.getClass().getMethod("set", String.class, String.class);
			setMethod.invoke(style, "background-color", backgroundColor);
			if (autoContrast) {
				setMethod.invoke(style, "color", getContrastTextColor(backgroundColor));
			}
			if ((padding != null) && !padding.trim().isEmpty()) {
				setMethod.invoke(style, "padding", padding);
			}
			if ((borderRadius != null) && !borderRadius.trim().isEmpty()) {
				setMethod.invoke(style, "border-radius", borderRadius);
			}
			if ((minWidth != null) && !minWidth.trim().isEmpty()) {
				setMethod.invoke(style, "min-width", minWidth);
			}
			setMethod.invoke(style, "display", "inline-block");
		} catch (final Exception e) {
			LOGGER.warn("Error applying color styling to component: {}", e.getMessage());
		}
	}

	public static String getColorFromEntity(final CEntity<?> entity) throws Exception {
		if (entity == null) {
			return null;
		}
		if (entity instanceof CTypeEntity) {
			final CTypeEntity<?> typeEntity = (CTypeEntity<?>) entity;
			return typeEntity.getColor();
		}
		if (entity instanceof CEntityDB) {
			return CAuxillaries.invokeStaticMethodOfStr(entity.getClass().getName(), "getEntityColorCode");
		}
		LOGGER.debug("Entity of type {} does not support color extraction", entity.getClass().getSimpleName());
		return null;
	}

	/** Determines appropriate text color based on background color. Uses a simple brightness calculation to determine if white or black text would be
	 * more readable.
	 * @param backgroundColor the background color in hex format (e.g., "#FF0000")
	 * @return "white" for dark backgrounds, "black" for light backgrounds */
	public static String getContrastTextColor(final String backgroundColor) {
		if ((backgroundColor == null) || backgroundColor.trim().isEmpty()) {
			return DEFAULT_DARK_TEXT; // Default to black text
		}
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
		}
		return DEFAULT_DARK_TEXT; // Default fallback
	}

	public static String getDisplayTextFromEntity(final Object item) {
		if (item == null) {
			return "N/A";
		}
		return item.toString();
	}

	public static String getEntityColorCode(final String className) throws Exception {
		return CAuxillaries.invokeStaticMethodOfStr(className, "getEntityColorCode");
	}

	public static String getIconColorCode(final Class<?> clazz) throws Exception {
		return CAuxillaries.invokeStaticMethodOfStr(clazz, "getIconColorCode");
	}

	public static String getIconColorCode(final String className) throws Exception {
		return CAuxillaries.invokeStaticMethodOfStr(className, "getIconColorCode");
	}

	public static String getIconFilename(final Class<?> clazz) throws Exception {
		return CAuxillaries.invokeStaticMethodOfStr(clazz, "getIconFilename");
	}

	public static String getIconFilename(final String className) throws Exception {
		return CAuxillaries.invokeStaticMethodOfStr(className, "getIconFilename");
	}

	public static Icon getIconForEntity(final CEntityDB<?> entity) throws Exception {
		return new Icon(getIconFilename(entity.getClass().getName()));
	}

	public static Icon getIconForViewClass(final CAbstractNamedEntityPage view) throws Exception {
		return new Icon(getIconFilename(view.getClass().getName()));
	}

	public static Icon getIconForViewClass(final Class<? extends CAbstractNamedEntityPage> clazz) throws Exception {
		return new Icon(getIconFilename(clazz));
	}

	public static String getRouteForView(final Class<? extends CAbstractNamedEntityPage<?>> clazz) {
		return Optional.ofNullable(clazz.getAnnotation(Route.class)).map(Route::value).filter(s -> !s.isBlank()) // boş string durumunu da kontrol et
				.orElseThrow(() -> new IllegalArgumentException("Missing @Route on " + clazz.getSimpleName()));
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
			LOGGER.warn("Error checking if entity type {} is a status entity: {}", entityType.getSimpleName(), e.getMessage());
			return false;
		}
	}

	/** Private constructor to prevent instantiation. */
	private CColorUtils() {
		// Utility class - no instantiation
	}
}
