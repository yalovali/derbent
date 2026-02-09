package tech.derbent.api.ui.component.basic;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.CAuxillaries;

/** CFilePathSelector - Custom component for selecting file paths with a text field and browse button.
 * <p>
 * This component provides a user-friendly interface for file path selection, consisting of:
 * <ul>
 * <li><b>TextField:</b> Displays the selected file path (can be manually edited)</li>
 * <li><b>Button ("..."):</b> Opens a file selection dialog</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage in Entity:</b>
 *
 * <pre>
 * &#64;Column (name = "config_file_path")
 * &#64;AMetaData (displayName = "Configuration File", required = false, isFilePath = true)
 * private String configFilePath;
 * </pre>
 * </p>
 * <p>
 * <b>Component Binding:</b> This component implements {@link HasValueAndElement} and can be bound directly to entity String fields via Vaadin Binder.
 * </p>
 * <p>
 * <b>File Selection:</b> Uses browser's native file input for file selection. The selected file path is displayed in the text field.
 * </p>
 */
public class CFilePathSelector extends Composite<CHorizontalLayout>
		implements HasValueAndElement<AbstractField.ComponentValueChangeEvent<CFilePathSelector, String>, String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CFilePathSelector.class);
	private static final long serialVersionUID = 1L;
	private final CButton buttonBrowse;
	private String currentValue;
	private boolean readOnly = false;
	private boolean required = false;
	private final CTextField textFieldPath;
	private final List<ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CFilePathSelector, String>>> valueChangeListeners =
			new ArrayList<>();

	/** Constructor for CFilePathSelector component.
	 * @param fieldInfo Field information containing display settings and metadata */
	public CFilePathSelector(final EntityFieldInfo fieldInfo) {
		// Create text field for path display
		textFieldPath = new CTextField();
		CAuxillaries.setId(textFieldPath);
		textFieldPath.setPlaceholder("Enter file path or click ... to browse");
		textFieldPath.setWidthFull();
		// Add value change listener to text field
		textFieldPath.addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				return; // Skip programmatic changes
			}
			final String oldValue = currentValue;
			currentValue = event.getValue();
			fireValueChangeEvent(oldValue, currentValue, true);
		});
		// Create browse button
		buttonBrowse = new CButton("...", VaadinIcon.FOLDER_OPEN.create());
		CAuxillaries.setId(buttonBrowse);
		buttonBrowse.addClickListener(event -> on_buttonBrowse_clicked());
		// Setup layout
		final CHorizontalLayout layout = getContent();
		layout.setSpacing(false);
		layout.getStyle().set("gap", "8px");
		layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.BASELINE);
		layout.add(textFieldPath, buttonBrowse);
		// Initialize with empty value
		setValue(null);
		// Set read-only state from field info
		if (fieldInfo != null) {
			setReadOnly(fieldInfo.isReadOnly());
			setRequiredIndicatorVisible(fieldInfo.isRequired());
		}
		LOGGER.debug("Created CFilePathSelector for field: {}", fieldInfo != null ? fieldInfo.getFieldName() : "unknown");
	}

	@Override
	public Registration
			addValueChangeListener(final ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CFilePathSelector, String>> listener) {
		valueChangeListeners.add(listener);
		return () -> valueChangeListeners.remove(listener);
	}

	/** Fires value change event to all registered listeners.
	 * @param oldValue     the old value
	 * @param newValue     the new value
	 * @param isFromClient whether the change originated from the client */
	private void fireValueChangeEvent(final String oldValue, final String newValue, final boolean isFromClient) {
		final AbstractField.ComponentValueChangeEvent<CFilePathSelector, String> event =
				new AbstractField.ComponentValueChangeEvent<>(this, this, oldValue, isFromClient);
		for (final ValueChangeListener<? super AbstractField.ComponentValueChangeEvent<CFilePathSelector, String>> listener : valueChangeListeners) {
			listener.valueChanged(event);
		}
	}

	@Override
	public String getValue() { return currentValue; }

	@Override
	public boolean isReadOnly() { return readOnly; }

	@Override
	public boolean isRequiredIndicatorVisible() { return required; }

	/** Handles browse button click - opens file selection dialog. */
	private void on_buttonBrowse_clicked() {
		LOGGER.debug("Browse button clicked for file path selection");
		// Create a hidden file input element using JavaScript
		// This provides a native file selection dialog
		getElement().executeJs("const input = document.createElement('input');" + "input.type = 'file';" + "input.onchange = (e) => {"
				+ "  const file = e.target.files[0];" + "  if (file) {" + "    $0.$server.updatePath(file.name);" + "  }" + "};" + "input.click();",
				getElement()).then(result -> {
					LOGGER.debug("File selection dialog opened");
				});
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
		textFieldPath.setReadOnly(readOnly);
		buttonBrowse.setEnabled(!readOnly);
	}

	@Override
	public void setRequiredIndicatorVisible(final boolean required) {
		this.required = required;
		textFieldPath.setRequiredIndicatorVisible(required);
	}

	@Override
	public void setValue(final String value) {
		final String oldValue = currentValue;
		currentValue = value != null ? value : "";
		textFieldPath.setValue(currentValue);
		// Fire change event (programmatic change, not from client)
		fireValueChangeEvent(oldValue, currentValue, false);
	}

	/** Server-side method called from JavaScript when file is selected. Note: Due to browser security restrictions, we can only get the file name,
	 * not the full path.
	 * @param fileName the selected file name */
	@com.vaadin.flow.component.ClientCallable
	public void updatePath(final String fileName) {
		LOGGER.debug("File selected: {}", fileName);
		final String oldValue = currentValue;
		currentValue = fileName;
		textFieldPath.setValue(fileName);
		fireValueChangeEvent(oldValue, fileName, true);
	}
}
