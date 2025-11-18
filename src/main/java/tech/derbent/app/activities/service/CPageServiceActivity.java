package tech.derbent.app.activities.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.services.pageservice.CPageServiceWithWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;

public class CPageServiceActivity extends CPageServiceWithWorkflow<CActivity> {
	public Logger LOGGER = LoggerFactory.getLogger(CPageServiceActivity.class);
	Long serialVersionUID = 1L;

	public CPageServiceActivity(final IPageServiceImplementer<CActivity> view) {
		super(view);
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CActivity.class.getSimpleName());
			Check.notNull(view, "View must not be null to bind page service.");
			super.bind();
			detailsBuilder = view.getDetailsBuilder();
			if (detailsBuilder != null) {
				formBuilder = detailsBuilder.getFormBuilder();
			}
			bindMethods(this);
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CActivity.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	/** Example callback: Called when the 'name' field value changes.
	 * This demonstrates the OLD signature (no parameters) - still supported for backward compatibility. */
	protected void on_name_changed() {
		LOGGER.debug("Activity name changed to: {}", getCurrentEntity().getName());
	}

	/** Example callback: Called when the 'name' field value changes with NEW signature.
	 * This demonstrates accessing the component and value directly.
	 * When both old and new signatures exist, the new one (with parameters) will be called. */
	protected void on_name_change(final Component component, final Object value) {
		LOGGER.info("=== Name field changed ===");
		LOGGER.info("Component type: {}", component.getClass().getSimpleName());
		LOGGER.info("New value: {}", value);
		
		// You can also use the helper methods to get the component
		final TextField nameField = getTextField("name");
		if (nameField != null) {
			LOGGER.info("Name field current value (via helper): {}", nameField.getValue());
		}
		
		// Or use the generic value getter
		final Object nameValue = getComponentValue("name");
		LOGGER.info("Name value (via getComponentValue): {}", nameValue);
	}

	/** Example callback: Called when the 'status' combobox value changes.
	 * This prints the status BEFORE it's saved to demonstrate real-time monitoring. */
	protected void on_status_change(final Component component, final Object value) {
		LOGGER.info("=== Status changed (BEFORE save) ===");
		LOGGER.info("Component type: {}", component.getClass().getSimpleName());
		LOGGER.info("New status value: {}", value);
		
		// Access the combobox using the helper method
		final ComboBox<?> statusCombo = getComboBox("status");
		if (statusCombo != null) {
			LOGGER.info("Status combobox current value: {}", statusCombo.getValue());
			LOGGER.info("Status combobox has {} items", statusCombo.getListDataView().getItemCount());
		}
		
		// You can also get/set values programmatically
		final Object currentStatus = getComponentValue("status");
		LOGGER.info("Current status (via helper): {}", currentStatus);
	}

	/** Example callback: Called when the 'description' field gets focus.
	 * This demonstrates the 'focus' event. */
	protected void on_description_focus(final Component component) {
		LOGGER.info("=== Description field focused ===");
		LOGGER.info("Component: {}", component.getClass().getSimpleName());
		
		// You could use this to show help text, load suggestions, etc.
		final Object descValue = getComponentValue("description");
		LOGGER.info("Current description value: {}", descValue);
	}

	/** Example callback: Called when the 'description' field loses focus.
	 * This demonstrates the 'blur' event. */
	protected void on_description_blur(final Component component) {
		LOGGER.info("=== Description field lost focus ===");
		
		// You could use this to validate, auto-save, etc.
		final Object descValue = getComponentValue("description");
		if (descValue != null && !descValue.toString().trim().isEmpty()) {
			LOGGER.info("Description has content: {}", descValue);
		} else {
			LOGGER.info("Description is empty");
		}
	}
}
