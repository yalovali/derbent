package tech.derbent.api.ui.theme;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinSession;

/** CFontSizeService - Service for managing font size scaling across the application. Provides methods to apply different font size scales (small,
 * medium, large) by dynamically injecting CSS variables. */
@Service
public class CFontSizeService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CFontSizeService.class);

	/** Applies the font size scale to the current UI.
	 * @param scale the font size scale ("small", "medium", or "large") */
	public static void applyFontSizeScale(final String scale) {
		final UI ui = UI.getCurrent();
		if (ui == null) {
			LOGGER.warn("Cannot apply font size scale: No UI available");
			return;
		}
		final String cssVariables = getCssVariablesForScale(scale);
		// Inject CSS variables into the page
		ui.getPage()
				.executeJs("const style = document.getElementById('font-size-override') || document.createElement('style');"
						+ "style.id = 'font-size-override';" + "style.textContent = ':root { ' + $0 + ' }';"
						+ "if (!document.getElementById('font-size-override')) { document.head.appendChild(style); }", cssVariables);
		// LOGGER.info("Applied font size scale: {}", scale);
	}

	/** Returns the list of available font size scales for use in UI components. This method is used as a data provider for the font size scale
	 * combobox.
	 * @return List of available font size scale values */
	public static List<String> getAvailableFontSizeScales() {
		return List.of("small", "medium", "large");
	}

	/** Gets CSS variable overrides for the specified font size scale.
	 * @param scale the font size scale ("small", "medium", or "large")
	 * @return CSS variable declarations as a string */
	private static String getCssVariablesForScale(final String scale) {
		return switch (scale) {
		case "small" -> // Extra small fonts - reduce by ~25% from medium
			"--lumo-font-size-xxxl: 2.25rem; " + "--lumo-font-size-xxl: 1.6875rem; " + "--lumo-font-size-xl: 1rem; "
					+ "--lumo-font-size-l: 0.8125rem; " + "--lumo-font-size-m: 0.75rem; " + "--lumo-font-size-s: 0.6875rem; "
					+ "--lumo-font-size-xs: 0.625rem; " + "--lumo-font-size-xxs: 0.5625rem;";
		case "large" -> // Larger fonts - increase by ~20% from medium
			"--lumo-font-size-xxxl: 3rem; " + "--lumo-font-size-xxl: 2.25rem; " + "--lumo-font-size-xl: 1.375rem; " + "--lumo-font-size-l: 1.125rem; "
					+ "--lumo-font-size-m: 1rem; " + "--lumo-font-size-s: 0.875rem; " + "--lumo-font-size-xs: 0.8125rem; "
					+ "--lumo-font-size-xxs: 0.75rem;";
		default -> // Medium (current default in styles.css)
			"--lumo-font-size-xxxl: 2.5rem; " + "--lumo-font-size-xxl: 1.875rem; " + "--lumo-font-size-xl: 1.125rem; "
					+ "--lumo-font-size-l: 0.9375rem; " + "--lumo-font-size-m: 0.875rem; " + "--lumo-font-size-s: 0.75rem; "
					+ "--lumo-font-size-xs: 0.6875rem; " + "--lumo-font-size-xxs: 0.625rem;";
		};
	}

	/** Retrieves the font size scale preference from the session.
	 * @return the stored font size scale, or "medium" as default */
	public static String getStoredFontSizeScale() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			final String scale = (String) session.getAttribute("fontSizeScale");
			if (scale != null) {
				return scale;
			}
		}
		return "medium";
	}

	/** Stores the font size scale preference in the session.
	 * @param scale the font size scale to store */
	public static void storeFontSizeScale(final String scale) {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setAttribute("fontSizeScale", scale);
		}
	}
}
