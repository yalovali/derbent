package tech.derbent.api.ui.dialogs;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.CCloneOptions.CloneDepth;
import tech.derbent.api.interfaces.ICloneable;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;

/**
 * Dialog for cloning entities with configurable options.
 * Allows users to select clone depth and configure various clone behaviors.
 * 
 * @param <EntityClass> the entity type being cloned
 */
public class CDialogClone<EntityClass extends CEntityDB<EntityClass>> extends CDialogDBEdit<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogClone.class);
	private static final long serialVersionUID = 1L;

	private ComboBox<CloneDepth> comboBoxCloneDepth;
	private Checkbox checkboxCloneStatus;
	private Checkbox checkboxCloneWorkflow;
	private Checkbox checkboxResetDates;
	private Checkbox checkboxResetAssignments;
	private TextField textFieldNewName;

	/**
	 * Creates a clone dialog for the specified entity.
	 * 
	 * @param entity the entity to clone
	 * @param onSave callback invoked when the clone is saved
	 * @throws Exception if dialog setup fails
	 */
	public CDialogClone(final EntityClass entity, final Consumer<EntityClass> onSave) throws Exception {
		super(entity, onSave, true);
		setupDialog();
	}

	@Override
	public String getDialogTitleString() {
		return "Clone " + getEntity().toString();
	}

	@Override
	protected Icon getFormIcon() throws Exception {
		return CColorUtils.getIconForEntity(getEntity());
	}

	@Override
	protected String getFormTitleString() {
		return "Clone Configuration";
	}

	@Override
	protected void populateForm() {
		// Pre-populate with default values
		if (comboBoxCloneDepth != null) {
			comboBoxCloneDepth.setValue(CloneDepth.BASIC_ONLY);
		}
		if (checkboxResetDates != null) {
			checkboxResetDates.setValue(true);
		}
		if (checkboxResetAssignments != null) {
			checkboxResetAssignments.setValue(true);
		}
		if (textFieldNewName != null && getEntity().toString() != null) {
			textFieldNewName.setValue(getEntity().toString() + " (Clone)");
		}
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();

		final VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);
		layout.setPadding(false);

		// Name field
		textFieldNewName = new TextField("New Name");
		textFieldNewName.setWidth("100%");
		textFieldNewName.setPlaceholder("Enter name for cloned entity");
		layout.add(textFieldNewName);

		// Clone depth selector
		comboBoxCloneDepth = new ComboBox<>("Clone Depth");
		comboBoxCloneDepth.setItems(CloneDepth.values());
		comboBoxCloneDepth.setItemLabelGenerator(depth -> {
			switch (depth) {
				case BASIC_ONLY:
					return "Basic Fields Only (name, description, numbers)";
				case WITH_RELATIONS:
					return "With Relations (+ parent/child links)";
				case WITH_ATTACHMENTS:
					return "With Attachments (+ files)";
				case WITH_COMMENTS:
					return "With Comments (+ comment history)";
				case FULL_DEEP_CLONE:
					return "Full Deep Clone (everything)";
				default:
					return depth.name();
			}
		});
		comboBoxCloneDepth.setWidth("100%");
		layout.add(comboBoxCloneDepth);

		// Clone options checkboxes
		checkboxCloneStatus = new Checkbox("Clone Status");
		checkboxCloneStatus.setTooltipText("Keep the same status as the original (otherwise will use initial status)");
		layout.add(checkboxCloneStatus);

		checkboxCloneWorkflow = new Checkbox("Clone Workflow");
		checkboxCloneWorkflow.setTooltipText("Keep the same workflow as the original");
		layout.add(checkboxCloneWorkflow);

		checkboxResetDates = new Checkbox("Reset Dates");
		checkboxResetDates.setTooltipText("Clear all date fields (they will be set to current date on save)");
		layout.add(checkboxResetDates);

		checkboxResetAssignments = new Checkbox("Reset Assignments");
		checkboxResetAssignments.setTooltipText("Clear assigned users (you can reassign after cloning)");
		layout.add(checkboxResetAssignments);

		getDialogLayout().add(layout);
	}

	@Override
	protected void validateForm() {
		if (textFieldNewName == null || textFieldNewName.getValue() == null || textFieldNewName.getValue().isBlank()) {
			throw new IllegalArgumentException("Name cannot be empty");
		}
	}

	/**
	 * Performs the clone operation when save is triggered.
	 * Creates a clone with the selected options and invokes the save callback.
	 */
	@Override
	protected void on_save_clicked() {
		try {
			validateForm();

			// Build clone options from dialog selections
			final CCloneOptions options = new CCloneOptions.Builder()
					.depth(comboBoxCloneDepth.getValue())
					.cloneStatus(checkboxCloneStatus.getValue())
					.cloneWorkflow(checkboxCloneWorkflow.getValue())
					.resetDates(checkboxResetDates.getValue())
					.resetAssignments(checkboxResetAssignments.getValue())
					.build();

			LOGGER.debug("Cloning entity with options: {}", options);

			// Check if entity implements ICloneable
			final EntityClass original = getEntity();
			if (!(original instanceof ICloneable)) {
				CNotificationService.showError("This entity does not support cloning");
				return;
			}

			// Perform the clone operation
			@SuppressWarnings("unchecked")
			final ICloneable<EntityClass> cloneable = (ICloneable<EntityClass>) original;
			final EntityClass clone = cloneable.createClone(options);

			// Update the name from the dialog
			if (clone instanceof tech.derbent.api.entity.domain.CEntityNamed) {
				((tech.derbent.api.entity.domain.CEntityNamed<?>) clone).setName(textFieldNewName.getValue());
			}

			LOGGER.info("Successfully cloned entity: {} -> {}", original, clone);

			// Invoke the save callback with the cloned entity
			if (getOnSave() != null) {
				getOnSave().accept(clone);
			}

			close();
		} catch (final CloneNotSupportedException e) {
			LOGGER.error("Clone operation failed", e);
			CNotificationService.showException("Failed to clone entity: " + e.getMessage(), e);
		} catch (final Exception e) {
			LOGGER.error("Error during clone operation", e);
			CNotificationService.showException("Error during clone", e);
		}
	}
}
