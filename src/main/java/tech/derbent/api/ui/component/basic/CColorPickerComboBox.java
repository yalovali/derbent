package tech.derbent.api.ui.component.basic;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.base.session.service.ISessionService;

/** Custom color picker component that properly implements HasValueAndElement */
public class CColorPickerComboBox extends Composite<CHorizontalLayout>
		implements HasValueAndElement<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CColorPickerComboBox.class);
	private static final long serialVersionUID = 1L;

	/** Helper method to validate and format color values
	 * @param color input color string
	 * @return formatted color string or null if invalid */
	private static String validateAndFormatColor(String color) {
		if (color == null || color.trim().isEmpty()) {
			return null;
		}
		String trimmedColor = color.trim();
		// Add # if missing
		if (!trimmedColor.startsWith("#")) {
			trimmedColor = "#" + trimmedColor;
		}
		// Validate hex format
		if (trimmedColor.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
			// Convert 3-digit to 6-digit if needed
			if (trimmedColor.length() == 4) {
				final String hex = trimmedColor.substring(1);
				trimmedColor = "#" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
			}
			return trimmedColor.toLowerCase();
		}
		return null; // Invalid format
	}

	private final ComboBox<String> colorField;
	private boolean persistenceEnabled = false;
	private String persistenceKey;
	private final CDiv previewDiv = new CDiv();
	private ISessionService sessionService;

	public CColorPickerComboBox(final EntityFieldInfo fieldInfo) {
		colorField = new ComboBox<>();
		CAuxillaries.setId(colorField);
		// Allow custom values for text input
		colorField.setAllowCustomValue(true);
		colorField.setPlaceholder(fieldInfo.getPlaceholder().isEmpty() ? "#000000" : fieldInfo.getPlaceholder());
		colorField.setReadOnly(fieldInfo.isReadOnly());
		// Set up color items before setting initial value
		final List<String> colors = CColorUtils.getWebColors();
		colorField.setItems(colors);
		// Configure custom renderer for color display
		colorField.setRenderer(new ComponentRenderer<>(this::createColorItemRenderer));
		// Configure layout
		getContent().setAlignItems(CHorizontalLayout.Alignment.CENTER);
		getContent().setSpacing(true);
		getContent().add(colorField);
		getContent().add(previewDiv);
		// Initialize with default value
		final String defaultValue = fieldInfo.getDefaultValue();
		if (defaultValue == null || defaultValue.trim().isEmpty()) {
			setValue("#000000");
		} else {
			setValue(defaultValue);
		}
		colorField.addValueChangeListener(event -> {
			final String colorValue = event.getValue();
			if (colorValue != null && !colorValue.trim().isEmpty()) {
				// Validate and format color value
				final String formattedColor = validateAndFormatColor(colorValue);
				if (formattedColor != null) {
					updatePreview(formattedColor);
				}
			} else {
				setValue("#cccccc");
			}
		});
		// Handle custom value entry
		colorField.addCustomValueSetListener(event -> {
			final String customValue = event.getDetail();
			final String formattedColor = validateAndFormatColor(customValue);
			if (formattedColor != null) {
				colorField.setValue(formattedColor);
				updatePreview(formattedColor);
			}
		});
	}

	@Override
	public Registration
			addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>> listener) {
		return colorField.addValueChangeListener(listener);
	}

	/** Creates a custom renderer for color items in the dropdown */
	private Span createColorItemRenderer(String colorValue) {
		final Span colorItem = new Span();
		if (colorValue != null && !colorValue.trim().isEmpty()) {
			// Create a colored square preview
			final Span colorSquare = new Span();
			colorSquare.getStyle().set("display", "inline-block").set("width", "20px").set("height", "20px").set("background-color", colorValue)
					.set("border", "1px solid #ccc").set("border-radius", "3px").set("margin-right", "8px").set("vertical-align", "middle");
			final Span colorText = new Span(colorValue.toUpperCase());
			colorText.getStyle().set("vertical-align", "middle");
			colorItem.add(colorSquare, colorText);
		} else {
			colorItem.setText("Select Color");
		}
		return colorItem;
	}

	/** Disables automatic persistence for this ColorPickerComboBox.
	 * <p>
	 * After calling this method, the component will no longer automatically save or restore its value.
	 * </p>
	 * @see #enablePersistence(String) */
	public void disablePersistence() {
		persistenceEnabled = false;
		LOGGER.info("[CColorPickerComboBox] Persistence disabled for key: {}", persistenceKey);
	}

	/** Enables automatic persistence for this ColorPickerComboBox.
	 * <p>
	 * Once enabled, the component will automatically:
	 * <ul>
	 * <li>Save its color value to session storage whenever the user changes it</li>
	 * <li>Restore its color value from session storage when the component attaches to the UI</li>
	 * </ul>
	 * </p>
	 * @param storageKey The unique key to use for storing the value in session storage
	 * @throws IllegalArgumentException if storageKey is null or blank
	 * @see #disablePersistence() */
	
	public void enablePersistence(final String storageKey) {
		if (storageKey == null || storageKey.isBlank()) {
			throw new IllegalArgumentException("Storage key cannot be null or blank");
		}
		persistenceKey = storageKey;
		persistenceEnabled = true;
		// Get session service
		if (sessionService == null) {
			sessionService = CSpringContext.getBean(ISessionService.class);
		}
		LOGGER.info("[CColorPickerComboBox] Persistence enabled for key: {}", storageKey);
		// Add value change listener to save on every change
		colorField.addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				LOGGER.debug("[CColorPickerComboBox] Value change not from client, skipping save for key: {}", persistenceKey);
				return;
			}
			if (persistenceEnabled) {
				saveValue();
			}
		});
		// Add attach listener to restore when component is added to UI
		addAttachListener(event -> {
			if (persistenceEnabled) {
				restoreValue();
			}
		});
		// If already attached, restore immediately
		if (isAttached()) {
			restoreValue();
		}
	}

	@Override
	public String getEmptyValue() { return ""; }

	@Override
	public String getValue() { return colorField.getValue(); }

	/** Checks if persistence is enabled for this ColorPickerComboBox.
	 * @return true if persistence is enabled, false otherwise */
	public boolean isPersistenceEnabled() { return persistenceEnabled; }

	@Override
	public boolean isReadOnly() { return colorField.isReadOnly(); }

	@Override
	public boolean isRequiredIndicatorVisible() { return colorField.isRequiredIndicatorVisible(); }

	/** Restores the value from session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the component attaches.
	 * </p>
	 */
	private void restoreValue() {
		if (!persistenceEnabled || sessionService == null) {
			return;
		}
		try {
			final Optional<String> storedValue = sessionService.getSessionValue(persistenceKey);
			if (storedValue.isPresent()) {
				final String color = storedValue.get();
				LOGGER.debug("[CColorPickerComboBox] Restoring value '{}' for key: {}", color, persistenceKey);
				final String validated = validateAndFormatColor(color);
				if (validated != null) {
					setValue(validated);
					LOGGER.info("[CColorPickerComboBox] Restored value for key: {}", persistenceKey);
				} else {
					LOGGER.warn("[CColorPickerComboBox] Stored value '{}' is not a valid color for key: {}", color, persistenceKey);
				}
			}
		} catch (final Exception e) {
			LOGGER.error("[CColorPickerComboBox] Error restoring value for key: {}", persistenceKey, e);
		}
	}

	/** Saves the current value to session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the value changes.
	 * </p>
	 */
	private void saveValue() {
		LOGGER.debug("[CColorPickerComboBox] Saving value for key: {}", persistenceKey);
		if (!persistenceEnabled || sessionService == null) {
			return;
		}
		try {
			final String value = getValue();
			if (value != null && !value.isBlank()) {
				sessionService.setSessionValue(persistenceKey, value);
				LOGGER.debug("[CColorPickerComboBox] Saved value '{}' for key: {}", value, persistenceKey);
			} else {
				sessionService.removeSessionValue(persistenceKey);
				LOGGER.debug("[CColorPickerComboBox] Cleared value for key: {}", persistenceKey);
			}
		} catch (final Exception e) {
			LOGGER.error("[CColorPickerComboBox] Error saving value for key: {}", persistenceKey, e);
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		colorField.setReadOnly(readOnly);
	}

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		colorField.setRequiredIndicatorVisible(requiredIndicatorVisible);
	}

	@Override
	public void setValue(String value) {
		if (value == null || value.trim().isEmpty()) {
			// Set default black if value is null/empty
			colorField.setValue("#000000");
			updatePreview("#000000");
		} else {
			final String formattedValue = validateAndFormatColor(value);
			if (formattedValue != null) {
				colorField.setValue(formattedValue);
				updatePreview(formattedValue);
			} else {
				// If validation fails, set to black
				colorField.setValue("#000000");
				updatePreview("#000000");
			}
		}
	}

	/** Updates the preview div with the selected color and sets the combobox background color */
	private void updatePreview(String colorValue) {
		if (colorValue != null && !colorValue.trim().isEmpty()) {
			// Update preview div
			previewDiv.getStyle().set("background-color", colorValue).set("width", "40px").set("height", "36px").set("border", "2px solid #ddd")
					.set("border-radius", "6px").set("margin-left", "8px").set("display", "inline-block").set("vertical-align", "top")
					.set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)").set("flex-shrink", "0");
			previewDiv.setTitle("Current color: " + colorValue.toUpperCase());
			// Set the combobox input field background color
			colorField.getElement().getStyle().set("--vaadin-input-field-background", colorValue);
			// Calculate and apply contrasting text color for readability
			final String textColor = CColorUtils.getContrastTextColor(colorValue);
			colorField.getElement().getStyle().set("color", textColor);
		} else {
			// Clear styles when no color is selected
			colorField.getElement().getStyle().remove("--vaadin-input-field-background");
			colorField.getElement().getStyle().remove("color");
		}
	}
}
