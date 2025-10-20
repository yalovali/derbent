package tech.derbent.api.components;

import java.util.List;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.components.CHorizontalLayout;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;

/** Custom color picker component that properly implements HasValueAndElement */
public class CColorPickerComboBox extends Composite<CHorizontalLayout>
		implements HasValueAndElement<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>, String> {

	private static final long serialVersionUID = 1L;
	private final ComboBox<String> colorField;
	private final CDiv previewDiv = new CDiv();

	public CColorPickerComboBox(final EntityFieldInfo fieldInfo) {
		this.colorField = new ComboBox<>();
		CAuxillaries.setId(colorField);
		// Allow custom values for text input
		colorField.setAllowCustomValue(true);
		colorField.setPlaceholder(fieldInfo.getPlaceholder().isEmpty() ? "#000000" : fieldInfo.getPlaceholder());
		colorField.setReadOnly(fieldInfo.isReadOnly());
		// Set up color items before setting initial value
		List<String> colors = CColorUtils.getWebColors();
		colorField.setItems(colors);
		// Configure custom renderer for color display
		colorField.setRenderer(new ComponentRenderer<>(this::createColorItemRenderer));
		// Configure layout
		getContent().setAlignItems(CHorizontalLayout.Alignment.CENTER);
		getContent().setSpacing(true);
		getContent().add(colorField);
		getContent().add(previewDiv);
		// Initialize with default value
		String defaultValue = fieldInfo.getDefaultValue();
		if (defaultValue == null || defaultValue.trim().isEmpty()) {
			setValue("#000000");
		} else {
			setValue(defaultValue);
		}
		colorField.addValueChangeListener(event -> {
			String colorValue = event.getValue();
			if (colorValue != null && !colorValue.trim().isEmpty()) {
				// Validate and format color value
				String formattedColor = validateAndFormatColor(colorValue);
				if (formattedColor != null) {
					updatePreview(formattedColor);
				}
			} else {
				setValue("#cccccc");
			}
		});
		// Handle custom value entry
		colorField.addCustomValueSetListener(event -> {
			String customValue = event.getDetail();
			String formattedColor = validateAndFormatColor(customValue);
			if (formattedColor != null) {
				colorField.setValue(formattedColor);
				updatePreview(formattedColor);
			}
		});
	}

	/** Creates a custom renderer for color items in the dropdown */
	private Span createColorItemRenderer(String colorValue) {
		Span colorItem = new Span();
		if (colorValue != null && !colorValue.trim().isEmpty()) {
			// Create a colored square preview
			Span colorSquare = new Span();
			colorSquare.getStyle().set("display", "inline-block").set("width", "20px").set("height", "20px").set("background-color", colorValue)
					.set("border", "1px solid #ccc").set("border-radius", "3px").set("margin-right", "8px").set("vertical-align", "middle");
			Span colorText = new Span(colorValue.toUpperCase());
			colorText.getStyle().set("vertical-align", "middle");
			colorItem.add(colorSquare, colorText);
		} else {
			colorItem.setText("Select Color");
		}
		return colorItem;
	}

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
				String hex = trimmedColor.substring(1);
				trimmedColor = "#" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
			}
			return trimmedColor.toLowerCase();
		}
		return null; // Invalid format
	}

	@Override
	public void setValue(String value) {
		if (value == null || value.trim().isEmpty()) {
			// Set default black if value is null/empty
			colorField.setValue("#000000");
			updatePreview("#000000");
		} else {
			String formattedValue = validateAndFormatColor(value);
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

	@Override
	public String getValue() { return colorField.getValue(); }

	/** Updates the preview div with the selected color */
	private void updatePreview(String colorValue) {
		if (colorValue != null && !colorValue.trim().isEmpty()) {
			previewDiv.getStyle().set("background-color", colorValue).set("width", "40px").set("height", "36px").set("border", "2px solid #ddd")
					.set("border-radius", "6px").set("margin-left", "8px").set("display", "inline-block").set("vertical-align", "top")
					.set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)").set("flex-shrink", "0");
			previewDiv.setTitle("Current color: " + colorValue.toUpperCase());
		}
	}

	@Override
	public Registration
			addValueChangeListener(ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>> listener) {
		return colorField.addValueChangeListener(listener);
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		colorField.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() { return colorField.isReadOnly(); }

	@Override
	public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
		colorField.setRequiredIndicatorVisible(requiredIndicatorVisible);
	}

	@Override
	public boolean isRequiredIndicatorVisible() { return colorField.isRequiredIndicatorVisible(); }

	@Override
	public String getEmptyValue() { return ""; }
}
