package tech.derbent.api.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.basic.CColorPickerComboBox;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CVerticalLayout;

/** Demo page to showcase the CColorPickerComboBox component with background color functionality */
@Route ("color-picker-demo")
@PageTitle ("Color Picker Demo")
@AnonymousAllowed
public class CColorPickerDemo extends Main {

	public static final String DEFAULT_COLOR = "#8377C5"; // CDE Active Purple - color picker demo
	public static final String DEFAULT_ICON = "vaadin:paintbrush";
	private static final long serialVersionUID = 1L;

	private static CDiv createDemoSection(String title, String description, String id, String defaultColor) {
		final CDiv section = new CDiv();
		section.getStyle().set("margin", "20px 0").set("padding", "20px").set("border", "2px solid #ddd").set("border-radius", "8px")
				.set("background-color", "#f9f9f9");
		final H2 sectionTitle = new H2(title);
		sectionTitle.getStyle().set("margin-top", "0");
		section.add(sectionTitle);
		final Paragraph sectionDesc = new Paragraph(description);
		section.add(sectionDesc);
		// Create EntityFieldInfo for the color picker
		final EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldName("color");
		fieldInfo.setDisplayName("Color");
		fieldInfo.setPlaceholder("Select a color");
		fieldInfo.setDefaultValue(defaultColor);
		fieldInfo.setReadOnly(false);
		// Create the color picker
		final CColorPickerComboBox colorPicker = new CColorPickerComboBox(fieldInfo);
		colorPicker.setId(id);
		colorPicker.getElement().getStyle().set("width", "300px");
		section.add(colorPicker);
		// Add instructions
		final Paragraph instructions = new Paragraph("Try selecting different colors from the dropdown or entering a hex value (e.g., #FF5733). "
				+ "Notice how the input field background changes to match your selection!");
		instructions.getStyle().set("font-style", "italic").set("color", "#666").set("margin-top", "10px");
		section.add(instructions);
		return section;
	}

	public CColorPickerDemo() {
		super();
		final CVerticalLayout layout = new CVerticalLayout();
		layout.setPadding(true);
		layout.setSpacing(true);
		layout.setWidthFull();
		// Title
		final H2 title = new H2("Color Picker ComboBox Demo");
		layout.add(title);
		// Description
		final Paragraph description = new Paragraph("This demo shows the CColorPickerComboBox component with background color functionality. "
				+ "When you select a color, both the input field and the preview box will display the selected color. "
				+ "The text color automatically adjusts for readability (white on dark colors, black on light colors).");
		layout.add(description);
		// Demo 1: Basic color picker
		layout.add(createDemoSection("Demo 1: Basic Color Picker", "Select a color to see the input field background change", "color-picker-demo-1"));
		// Demo 2: Color picker with different default
		layout.add(createDemoSection("Demo 2: Color Picker with Red Default", "This picker starts with a red color (#e74c3c)", "color-picker-demo-2",
				"#e74c3c"));
		// Demo 3: Color picker with light color
		layout.add(createDemoSection("Demo 3: Color Picker with Light Default",
				"This picker starts with a light color (#FFE4C4) - notice the dark text for readability", "color-picker-demo-3", "#FFE4C4"));
		// Demo 4: Color picker with dark color
		layout.add(createDemoSection("Demo 4: Color Picker with Dark Default",
				"This picker starts with a dark color (#2C3E50) - notice the white text for readability", "color-picker-demo-4", "#2C3E50"));
		add(layout);
	}

	private CDiv createDemoSection(String title, String description, String id) {
		return createDemoSection(title, description, id, "#4A90E2");
	}
}
