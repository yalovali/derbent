package tech.derbent.api.reporting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.utils.Check;

/** CDialogReportConfiguration - Dialog for selecting fields to include in CSV export.
 * <p>
 * Follows the workflow status dialog pattern with grouped field selection. Fields are organized by entity relationships (Base, Status, Assigned To,
 * etc.).
 * </p>
 * <p>
 * <b>Features:</b>
 * <ul>
 * <li>Grouped field selection with checkboxes</li>
 * <li>Select All / Deselect All per group</li>
 * <li>Two-column layout for better space usage</li>
 * <li>Minimum one field validation</li>
 * <li>Max-width 800px for readability</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage:</b>
 *
 * <pre>
 * List&lt;CReportFieldDescriptor&gt; allFields = CReportFieldDescriptor.discoverFields(CActivity.class);
 * CDialogReportConfiguration dialog = new CDialogReportConfiguration(allFields, selectedFields -> {
 * 	// Generate CSV with selected fields
 * 	StreamResource csv = CCSVExporter.exportToCSV(data, selectedFields, "report");
 * 	// Trigger download
 * });
 * dialog.open();
 * </pre>
 * </p>
 * Layer: Reporting UI (API) */
public class CDialogReportConfiguration extends CDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogReportConfiguration.class);
	private static final long serialVersionUID = 1L;
	private final List<CReportFieldDescriptor> allFields;
	private final Map<String, List<Checkbox>> groupCheckboxes;
	private final Consumer<List<CReportFieldDescriptor>> onGenerate;

	public CDialogReportConfiguration(final List<CReportFieldDescriptor> allFields, final Consumer<List<CReportFieldDescriptor>> onGenerate) {
		Check.notNull(allFields, "Fields list cannot be null");
		Check.notNull(onGenerate, "Generate callback cannot be null");
		Check.isTrue(!allFields.isEmpty(), "At least one field must be available");
		this.allFields = allFields;
		this.onGenerate = onGenerate;
		groupCheckboxes = new LinkedHashMap<>();
		try {
			setupDialog();
			setupContent();
			setupButtons();
		} catch (final Exception e) {
			LOGGER.error("Error creating report configuration dialog", e);
			throw new RuntimeException("Failed to create report dialog", e);
		}
	}

	private Checkbox createFieldCheckbox(final CReportFieldDescriptor field) {
		final Checkbox checkbox = new Checkbox(field.getDisplayName());
		checkbox.setValue(true);
		checkbox.getElement().setAttribute("field-path", field.getFieldPath());
		if (field.isCollection()) {
			checkbox.setLabel(field.getDisplayName() + " (List)");
			checkbox.getElement().setAttribute("title", "Collection field - values separated by semicolons");
		}
		return checkbox;
	}

	private CVerticalLayout createFieldGroup(final String groupName, final List<CReportFieldDescriptor> fields) {
		final CVerticalLayout groupLayout = new CVerticalLayout();
		groupLayout.setPadding(false);
		groupLayout.setSpacing(true);
		groupLayout.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)").set("border-radius", "var(--lumo-border-radius-m)")
				.set("padding", "12px").set("background", "var(--lumo-contrast-5pct)");
		final HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidthFull();
		headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		final H4 groupTitle = new H4(groupName);
		groupTitle.getStyle().set("margin", "0");
		final HorizontalLayout groupButtons = new HorizontalLayout();
		groupButtons.setSpacing(true);
		groupButtons.setPadding(false);
		final CButton selectAllBtn = CButton.createTertiary("Select All", null, e -> selectAllInGroup(groupName, true));
		selectAllBtn.getStyle().set("font-size", "0.875rem");
		final CButton deselectAllBtn = CButton.createTertiary("Deselect All", null, e -> selectAllInGroup(groupName, false));
		deselectAllBtn.getStyle().set("font-size", "0.875rem");
		groupButtons.add(selectAllBtn, deselectAllBtn);
		headerLayout.add(groupTitle, groupButtons);
		groupLayout.add(headerLayout);
		final List<Checkbox> checkboxes = new ArrayList<>();
		if (fields.size() > 6) {
			final HorizontalLayout columnsLayout = new HorizontalLayout();
			columnsLayout.setWidthFull();
			columnsLayout.getStyle().set("gap", "24px");
			final CVerticalLayout col1 = new CVerticalLayout();
			col1.setPadding(false);
			col1.setSpacing(true);
			col1.setWidthFull();
			col1.getStyle().set("gap", "8px");
			final CVerticalLayout col2 = new CVerticalLayout();
			col2.setPadding(false);
			col2.setSpacing(true);
			col2.setWidthFull();
			col2.getStyle().set("gap", "8px");
			for (int i = 0; i < fields.size(); i++) {
				final Checkbox checkbox = createFieldCheckbox(fields.get(i));
				checkboxes.add(checkbox);
				if (i < (fields.size() + 1) / 2) {
					col1.add(checkbox);
				} else {
					col2.add(checkbox);
				}
			}
			columnsLayout.add(col1, col2);
			groupLayout.add(columnsLayout);
		} else {
			final CVerticalLayout fieldsLayout = new CVerticalLayout();
			fieldsLayout.setPadding(false);
			fieldsLayout.setSpacing(true);
			fieldsLayout.getStyle().set("gap", "8px");
			for (final CReportFieldDescriptor field : fields) {
				final Checkbox checkbox = createFieldCheckbox(field);
				checkboxes.add(checkbox);
				fieldsLayout.add(checkbox);
			}
			groupLayout.add(fieldsLayout);
		}
		groupCheckboxes.put(groupName, checkboxes);
		return groupLayout;
	}

	private void createFieldSelectionForm() {
		final CVerticalLayout mainLayout1 = new CVerticalLayout();
		mainLayout1.setPadding(false);
		mainLayout1.setSpacing(true);
		mainLayout1.getStyle().set("gap", "16px");
		final Map<String, List<CReportFieldDescriptor>> groupedFields = allFields.stream().collect(Collectors
				.groupingBy(field -> field.getGroupName() != null ? field.getGroupName() : "Other", LinkedHashMap::new, Collectors.toList()));
		for (final Map.Entry<String, List<CReportFieldDescriptor>> entry : groupedFields.entrySet()) {
			final String groupName = entry.getKey();
			final List<CReportFieldDescriptor> fields = entry.getValue();
			final CVerticalLayout groupLayout = createFieldGroup(groupName, fields);
			mainLayout1.add(groupLayout);
		}
		add(mainLayout1);
	}

	@Override
	public String getDialogTitleString() { return "Configure CSV Export"; }

	@Override
	protected Icon getFormIcon() throws Exception { return VaadinIcon.FILE_TABLE.create(); }

	@Override
	protected String getFormTitleString() { return "Select Fields to Export"; }

	private boolean isFieldSelected(final CReportFieldDescriptor field) {
		for (final List<Checkbox> checkboxes : groupCheckboxes.values()) {
			for (final Checkbox checkbox : checkboxes) {
				if (field.getFieldPath().equals(checkbox.getElement().getAttribute("field-path"))) {
					return checkbox.getValue();
				}
			}
		}
		return false;
	}

	private void onGenerateClicked() {
		try {
			final List<CReportFieldDescriptor> selectedFields = new ArrayList<>();
			for (final CReportFieldDescriptor field : allFields) {
				if (isFieldSelected(field)) {
					selectedFields.add(field);
				}
			}
			if (selectedFields.isEmpty()) {
				throw new IllegalStateException("Please select at least one field to export");
			}
			close();
			onGenerate.accept(selectedFields);
			LOGGER.info("CSV export configured with {} fields", selectedFields.size());
		} catch (final Exception e) {
			LOGGER.error("Error during CSV generation", e);
			throw e;
		}
	}

	private void selectAllInGroup(final String groupName, final boolean selected) {
		final List<Checkbox> checkboxes = groupCheckboxes.get(groupName);
		if (checkboxes != null) {
			for (final Checkbox checkbox : checkboxes) {
				checkbox.setValue(selected);
			}
		}
	}

	@Override
	protected void setupButtons() {
		final CButton cancelButton = CButton.createCancelButton("Cancel", e -> close());
		final CButton generateButton = CButton.createPrimary("Generate CSV", VaadinIcon.DOWNLOAD.create(), e -> onGenerateClicked());
		buttonLayout.removeAll();
		buttonLayout.add(cancelButton, generateButton);
	}

	@Override
	protected void setupContent() throws Exception {
		createFieldSelectionForm();
	}

	@Override
	protected void setupDialog() throws Exception {
		setHeaderTitle("Configure CSV Export");
		setWidth("800px");
		setMaxHeight("80vh");
		setCloseOnEsc(true);
		setCloseOnOutsideClick(false);
	}
}
