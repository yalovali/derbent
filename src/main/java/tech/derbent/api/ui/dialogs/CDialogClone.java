package tech.derbent.api.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.ICopyable;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;

/** Dialog for copying entities with configurable options. Allows users to copy to same or different entity types with flexible field mapping.
 * @param <EntityClass> the entity type being copied */
public class CDialogClone<EntityClass extends CEntityDB<EntityClass>> extends CDialogDBEdit<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogClone.class);
	private static final long serialVersionUID = 1L;
	private ComboBox<Class<? extends CEntityDB<?>>> comboBoxTargetClass;
	private Checkbox checkboxIncludeRelations;
	private Checkbox checkboxIncludeAttachments;
	private Checkbox checkboxIncludeComments;
	private Checkbox checkboxIncludeAllCollections;
	private Checkbox checkboxCopyStatus;
	private Checkbox checkboxCopyWorkflow;
	private Checkbox checkboxResetDates;
	private Checkbox checkboxResetAssignments;
	private TextField textFieldNewName;

	/** Creates a clone dialog for the specified entity.
	 * @param entity the entity to clone
	 * @param onSave callback invoked when the clone is saved
	 * @throws Exception if dialog setup fails */
	public CDialogClone(final EntityClass entity, final Consumer<EntityClass> onSave) throws Exception {
		super(entity, onSave, true);
		setupDialog();
	}

	@Override
	public String getDialogTitleString() { return "Copy " + getEntity().toString(); }

	@Override
	protected Icon getFormIcon() throws Exception { return CColorUtils.getIconForEntity(getEntity()); }

	@Override
	protected String getFormTitleString() { return "Copy Configuration"; }

	@Override
	protected void save() {
		performCopy();
	}

	/** Performs the copy operation when save is triggered. Creates a copy with the selected options and invokes the save callback. */
	private void performCopy() {
		try {
			validateForm();
			// Determine target class
			@SuppressWarnings ("unchecked")
			final Class<? extends CEntityDB<?>> targetClass = comboBoxTargetClass.getValue() != null ? (Class<? extends CEntityDB<?>>) comboBoxTargetClass.getValue()
					: (Class<? extends CEntityDB<?>>) getEntity().getClass();
			// Build clone options from dialog selections
			final CCloneOptions options = new CCloneOptions.Builder().targetClass(targetClass).includeRelations(checkboxIncludeRelations.getValue())
					.includeAttachments(checkboxIncludeAttachments.getValue()).includeComments(checkboxIncludeComments.getValue())
					.includeAllCollections(checkboxIncludeAllCollections.getValue()).cloneStatus(checkboxCopyStatus.getValue())
					.cloneWorkflow(checkboxCopyWorkflow.getValue()).resetDates(checkboxResetDates.getValue())
					.resetAssignments(checkboxResetAssignments.getValue()).build();
			LOGGER.debug("Copying entity with options: {} to target class: {}", options, targetClass.getSimpleName());
			// Check if entity implements ICopyable
			final EntityClass original = getEntity();
			if (!(original instanceof ICopyable)) {
				CNotificationService.showError("This entity does not support copying");
				return;
			}
			// Perform the copy operation using copyTo pattern
			@SuppressWarnings ("unchecked")
			final CEntityDB<?> copy = ((CEntityDB<EntityClass>) original).copyTo((Class<? extends CEntityDB<?>>) targetClass, options);
			// Update the name from the dialog
			if (copy instanceof CEntityNamed) {
				((CEntityNamed<?>) copy).setName(textFieldNewName.getValue());
			}
			LOGGER.info("Successfully copied entity: {} -> {} (type: {})", original, copy, targetClass.getSimpleName());
			// Invoke the save callback with the copied entity
			if (onSave != null) {
				@SuppressWarnings ("unchecked")
				final EntityClass typedCopy = (EntityClass) copy;
				onSave.accept(typedCopy);
			}
			close();
		} catch (final Exception e) {
			LOGGER.error("Error during copy operation", e);
			CNotificationService.showException("Error during copy", e);
		}
	}

	@Override
	protected void populateForm() {
		// Pre-populate with default values
		if (comboBoxTargetClass != null) {
			comboBoxTargetClass.setValue(null); // null = same type
		}
		if (checkboxIncludeRelations != null) {
			checkboxIncludeRelations.setValue(false);
		}
		if (checkboxIncludeAttachments != null) {
			checkboxIncludeAttachments.setValue(false);
		}
		if (checkboxIncludeComments != null) {
			checkboxIncludeComments.setValue(false);
		}
		if (checkboxIncludeAllCollections != null) {
			checkboxIncludeAllCollections.setValue(false);
		}
		if (checkboxResetDates != null) {
			checkboxResetDates.setValue(true);
		}
		if (checkboxResetAssignments != null) {
			checkboxResetAssignments.setValue(true);
		}
		if (textFieldNewName != null && getEntity().toString() != null) {
			textFieldNewName.setValue(getEntity().toString() + " (Copy)");
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
		textFieldNewName.setPlaceholder("Enter name for copied entity");
		layout.add(textFieldNewName);
		// Target class selector (for cross-type copying)
		comboBoxTargetClass = new ComboBox<>("Copy To Type (Optional)");
		comboBoxTargetClass.setItems(getCompatibleTargetClasses());
		comboBoxTargetClass.setItemLabelGenerator(clazz -> clazz == null ? "Same Type" : clazz.getSimpleName());
		comboBoxTargetClass.setPlaceholder("Same type as source");
		comboBoxTargetClass.setTooltipText("Select a different entity type to copy to (experimental)");
		comboBoxTargetClass.setWidth("100%");
		comboBoxTargetClass.setClearButtonVisible(true);
		layout.add(comboBoxTargetClass);
		// Copy options checkboxes - What to include
		checkboxIncludeRelations = new Checkbox("Include Relations");
		checkboxIncludeRelations.setTooltipText("Copy parent/child relationships and linked entities");
		layout.add(checkboxIncludeRelations);
		checkboxIncludeAttachments = new Checkbox("Include Attachments");
		checkboxIncludeAttachments.setTooltipText("Copy file attachments to the new entity");
		layout.add(checkboxIncludeAttachments);
		checkboxIncludeComments = new Checkbox("Include Comments");
		checkboxIncludeComments.setTooltipText("Copy comment history to the new entity");
		layout.add(checkboxIncludeComments);
		checkboxIncludeAllCollections = new Checkbox("Include All Collections");
		checkboxIncludeAllCollections.setTooltipText("Copy all related collections (tags, links, etc.)");
		layout.add(checkboxIncludeAllCollections);
		// Status and workflow options
		checkboxCopyStatus = new Checkbox("Copy Status");
		checkboxCopyStatus.setTooltipText("Keep the same status as the original (otherwise will use initial status)");
		layout.add(checkboxCopyStatus);
		checkboxCopyWorkflow = new Checkbox("Copy Workflow");
		checkboxCopyWorkflow.setTooltipText("Keep the same workflow as the original");
		layout.add(checkboxCopyWorkflow);
		// Reset options
		checkboxResetDates = new Checkbox("Reset Dates");
		checkboxResetDates.setTooltipText("Clear all date fields (they will be set to current date on save)");
		layout.add(checkboxResetDates);
		checkboxResetAssignments = new Checkbox("Reset Assignments");
		checkboxResetAssignments.setTooltipText("Clear assigned users (you can reassign after copying)");
		layout.add(checkboxResetAssignments);
		getDialogLayout().add(layout);
	}

	/** Returns list of compatible target classes for cross-type copying. Currently returns same type only. Can be extended to support cross-type
	 * copying based on interface implementation.
	 * @return list of compatible entity classes */
	@SuppressWarnings ("unchecked")
	private List<Class<? extends CEntityDB<?>>> getCompatibleTargetClasses() {
		final List<Class<? extends CEntityDB<?>>> compatibleClasses = new ArrayList<>();
		// For now, only allow same type
		// TODO: Add logic to discover compatible types via ICopyable interface
		compatibleClasses.add((Class<? extends CEntityDB<?>>) getEntity().getClass());
		return compatibleClasses;
	}

	@Override
	protected void validateForm() {
		if (textFieldNewName == null || textFieldNewName.getValue() == null || textFieldNewName.getValue().isBlank()) {
			throw new IllegalArgumentException("Name cannot be empty");
		}
	}
}
