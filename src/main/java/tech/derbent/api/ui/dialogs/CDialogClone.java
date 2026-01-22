package tech.derbent.api.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;

/** Dialog for copying entities with configurable options. Allows users to copy to same or different entity types with flexible field mapping.
 * @param <EntityClass> the entity type being copied */
public class CDialogClone<EntityClass extends CEntityDB<EntityClass>> extends CDialogDBEdit<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogClone.class);
	private static final String SAME_AS_SOURCE_KEY = "__SAME_AS_SOURCE__";
	private static final long serialVersionUID = 1L;
	private boolean allSelected = false;
	private Button buttonSelectAll;
	private Checkbox checkboxCopyStatus;
	private Checkbox checkboxCopyWorkflow;
	private Checkbox checkboxIncludeAllCollections;
	private Checkbox checkboxIncludeAttachments;
	private Checkbox checkboxIncludeComments;
	private Checkbox checkboxIncludeRelations;
	private Checkbox checkboxResetAssignments;
	private Checkbox checkboxResetDates;
	private CComboBox<String> comboBoxTargetType;
	private TextField textFieldNewName;

	/** Creates a clone dialog for the specified entity.
	 * @param entity the entity to clone
	 * @param onSave callback invoked when the clone is saved
	 * @throws Exception if dialog setup fails */
	public CDialogClone(final EntityClass entity, final Consumer<EntityClass> onSave) throws Exception {
		super(entity, onSave, true);
		setupDialog();
	}

	/** Generates a fallback name when service is not available. */
	private void generateFallbackName(final Class<?> targetClass) {
		String baseName = null;
		// Try to get name from source entity
		if (getEntity() instanceof CEntityNamed) {
			baseName = ((CEntityNamed<?>) getEntity()).getName();
		}
		// If no name or copying to different type, use target class name
		if (baseName == null || baseName.isBlank() || !targetClass.equals(getEntity().getClass())) {
			baseName = targetClass.getSimpleName() + " (Copy)";
		} else {
			baseName = baseName + " (Copy)";
		}
		textFieldNewName.setValue(baseName);
	}

	/** Returns list of compatible target types for cross-type copying. Returns ALL registered entity classes (not just CProjectItem).
	 * @return list of entity type keys (simple class names) */
	private List<String> getCompatibleTargetTypes() {
		final List<String> typeKeys = new ArrayList<>();
		try {
			// Always add "Same as Source" as first option
			typeKeys.add(SAME_AS_SOURCE_KEY);
			// Get ALL registered entity classes from the registry
			// The registry stores them as simpleName -> Class mappings
			final String[] allEntityTypes = {
					// Core entities
					"CUser", "CCompany", "CProject", "CRole", "CPermission",
					// Project items
					"CActivity", "CMeeting", "CDecision", "CRisk", "CIssue", "CTicket", "COrder", "CMilestone", "CValidationCase",
					// Financial
					"CBudget", "CProjectExpense", "CProjectIncome", "CInvoice",
					// Products and deliverables
					"CProduct", "CProductVersion", "CDeliverable", "CProjectComponent", "CProjectComponentVersion",
					// Resources
					"CAsset", "CProvider", "CCustomer", "CResource",
					// Workflows and statuses
					"CWorkflowEntity", "CProjectItemStatus", "CWorkflowStatusRelation",
					// Other
					"CSprint", "CSprintItem", "CRiskLevel", "CAttachment", "CComment", "CKanbanLine", "CTag", "CTestCase", "CTestRun",
					"CValidationSession"
			};
			final String sourceSimpleName = getEntity().getClass().getSimpleName();
			for (final String typeName : allEntityTypes) {
				try {
					if (CEntityRegistry.isRegistered(typeName)) {
						// Don't add the source type again (we have "Same as Source" already)
						if (!typeName.equals(sourceSimpleName)) {
							typeKeys.add(typeName);
						}
					}
				} catch (final Exception e) {
					LOGGER.debug("Could not check type: {}", typeName);
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Error discovering compatible target types", e);
			// Fallback to just "Same as Source"
			if (typeKeys.isEmpty()) {
				typeKeys.add(SAME_AS_SOURCE_KEY);
			}
		}
		// Sort alphabetically by display name (except first item which is "Same as Source")
		if (typeKeys.size() > 1) {
			final List<String> sorted = typeKeys.subList(1, typeKeys.size()).stream().sorted((a, b) -> {
				try {
					final Class<?> clazzA = CEntityRegistry.getEntityClass(a);
					final Class<?> clazzB = CEntityRegistry.getEntityClass(b);
					final String titleA = CEntityRegistry.getEntityTitleSingular(clazzA);
					final String titleB = CEntityRegistry.getEntityTitleSingular(clazzB);
					final String nameA = titleA != null ? titleA : clazzA.getSimpleName();
					final String nameB = titleB != null ? titleB : clazzB.getSimpleName();
					return nameA.compareToIgnoreCase(nameB);
				} catch (final Exception e) {
					return a.compareToIgnoreCase(b);
				}
			}).collect(Collectors.toList());
			// Rebuild list with "Same as Source" first, then sorted items
			typeKeys.clear();
			typeKeys.add(SAME_AS_SOURCE_KEY);
			typeKeys.addAll(sorted);
		}
		return typeKeys;
	}

	@Override
	public String getDialogTitleString() { return "Copy " + getEntity().toString(); }

	@Override
	protected Icon getFormIcon() throws Exception { return CColorUtils.getIconForEntity(getEntity()); }

	@Override
	protected String getFormTitleString() { return "Copy Configuration"; }

	@SuppressWarnings ("unchecked")
	private void performCopy() {
		try {
			validateForm();
			// Determine target class from combobox selection
			final String selectedKey = comboBoxTargetType.getValue();
			final Class<?> targetClass;
			if (SAME_AS_SOURCE_KEY.equals(selectedKey) || selectedKey == null) {
				// Copy to same type
				targetClass = getEntity().getClass();
			} else {
				// Copy to different type - selectedKey is the simple class name
				try {
					targetClass = CEntityRegistry.getEntityClass(selectedKey);
				} catch (final Exception e) {
					CNotificationService.showError("Invalid target entity type selected");
					return;
				}
			}
			// Build clone options from dialog selections
			final CCloneOptions options = new CCloneOptions.Builder().targetClass(targetClass).includeRelations(checkboxIncludeRelations.getValue())
					.includeAttachments(checkboxIncludeAttachments.getValue()).includeComments(checkboxIncludeComments.getValue())
					.includeAllCollections(checkboxIncludeAllCollections.getValue()).cloneStatus(checkboxCopyStatus.getValue())
					.cloneWorkflow(checkboxCopyWorkflow.getValue()).resetDates(checkboxResetDates.getValue())
					.resetAssignments(checkboxResetAssignments.getValue()).build();
			LOGGER.debug("Copying entity with options: {} to target class: {}", options, targetClass.getSimpleName());
			// Check if entity implements ICopyable
			final EntityClass original = getEntity();
			// Perform the copy operation using copyTo pattern
			final CEntityDB<?> copy = original.copyTo((Class<? extends CEntityDB<?>>) targetClass, options);
			// Update the name from the dialog
			if (copy instanceof CEntityNamed) {
				((CEntityNamed<?>) copy).setName(textFieldNewName.getValue());
			}
			LOGGER.info("Successfully copied entity: {} -> {} (type: {})", original, copy, targetClass.getSimpleName());
			// Invoke the save callback with the copied entity
			if (onSave != null) {
				final EntityClass typedCopy = (EntityClass) copy;
				onSave.accept(typedCopy);
			}
			close();
		} catch (final Exception e) {
			LOGGER.error("Error during copy operation: {}", e.getMessage(), e);
			CNotificationService.showException("Error during copy", e);
		}
	}

	@Override
	protected void populateForm() {
		// Pre-populate with default values
		if (comboBoxTargetType != null) {
			comboBoxTargetType.setValue(SAME_AS_SOURCE_KEY);
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
		if (buttonSelectAll != null) {
			allSelected = false;
			buttonSelectAll.setText("Select All");
			buttonSelectAll.setIcon(VaadinIcon.CHECK_SQUARE_O.create());
		}
		// Generate unique name for the initial entity type (same as source)
		if (textFieldNewName != null && comboBoxTargetType != null) {
			updateGeneratedName(SAME_AS_SOURCE_KEY);
		}
	}

	@Override
	protected void save() {
		performCopy();
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		final VerticalLayout mainLayout1 = new VerticalLayout();
		mainLayout1.setSpacing(false);
		mainLayout1.setPadding(false);
		mainLayout1.setMaxWidth("600px");
		mainLayout1.setWidthFull();
		mainLayout1.getStyle().set("gap", "12px");
		// === SECTION 1: Name Field ===
		textFieldNewName = new TextField("New Name");
		textFieldNewName.setWidthFull();
		textFieldNewName.setPlaceholder("Enter name for copied entity");
		textFieldNewName.setRequired(true);
		mainLayout1.add(textFieldNewName);
		// === SECTION 2: Target Type ===
		comboBoxTargetType = new CComboBox<>("Copy To Entity Type");
		comboBoxTargetType.setWidthFull();
		comboBoxTargetType.setRequired(true);
		// Get all registered entity classes
		final List<String> compatibleTypes = getCompatibleTargetTypes();
		comboBoxTargetType.setItems(compatibleTypes);
		// Custom item label generator
		comboBoxTargetType.setItemLabelGenerator(key -> {
			if (SAME_AS_SOURCE_KEY.equals(key)) {
				final String sourceTitle = CEntityRegistry.getEntityTitleSingular(getEntity().getClass());
				return "⭐ Same as Source (" + (sourceTitle != null ? sourceTitle : getEntity().getClass().getSimpleName()) + ")";
			}
			try {
				final Class<?> clazz = CEntityRegistry.getEntityClass(key);
				final String title = CEntityRegistry.getEntityTitleSingular(clazz);
				return title != null ? title : clazz.getSimpleName();
			} catch (final Exception e) {
				return key;
			}
		});
		// Add listener to update name when target type changes
		comboBoxTargetType.addValueChangeListener(event -> {
			if (event.getValue() != null && textFieldNewName != null) {
				updateGeneratedName(event.getValue());
			}
		});
		comboBoxTargetType.setValue(SAME_AS_SOURCE_KEY);
		mainLayout1.add(comboBoxTargetType);
		// === SECTION 3: Copy Options ===
		final HorizontalLayout optionsHeaderLayout = new HorizontalLayout();
		optionsHeaderLayout.setWidthFull();
		optionsHeaderLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		optionsHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		optionsHeaderLayout.setPadding(false);
		optionsHeaderLayout.getStyle().set("margin-top", "8px");
		final H4 optionsHeader = new H4("Copy Options");
		optionsHeader.getStyle().set("margin", "0").set("font-size", "var(--lumo-font-size-m)").set("font-weight", "600").set("color",
				"var(--lumo-contrast-70pct)");
		buttonSelectAll = new Button("Select All", VaadinIcon.CHECK_SQUARE_O.create());
		buttonSelectAll.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
		buttonSelectAll.addClickListener(event -> toggleSelectAll());
		optionsHeaderLayout.add(optionsHeader, buttonSelectAll);
		mainLayout1.add(optionsHeaderLayout);
		// Options container - 2 columns for better space utilization
		final HorizontalLayout optionsGrid = new HorizontalLayout();
		optionsGrid.setWidthFull();
		optionsGrid.setSpacing(true);
		optionsGrid.getStyle().set("gap", "16px").set("padding", "12px").set("background", "var(--lumo-contrast-5pct)").set("border-radius",
				"var(--lumo-border-radius-m)");
		// Left column
		final VerticalLayout leftColumn = new VerticalLayout();
		leftColumn.setSpacing(false);
		leftColumn.setPadding(false);
		leftColumn.getStyle().set("gap", "8px");
		leftColumn.setWidth("50%");
		checkboxIncludeRelations = new Checkbox("Include Relations");
		checkboxIncludeRelations.setTooltipText("Copy parent/child relationships and linked entities");
		checkboxIncludeAttachments = new Checkbox("Include Attachments");
		checkboxIncludeAttachments.setTooltipText("Copy file attachments to the new entity");
		checkboxIncludeComments = new Checkbox("Include Comments");
		checkboxIncludeComments.setTooltipText("Copy comment history to the new entity");
		checkboxIncludeAllCollections = new Checkbox("Include All Collections");
		checkboxIncludeAllCollections.setTooltipText("Copy all related collections (tags, links, etc.)");
		leftColumn.add(checkboxIncludeRelations, checkboxIncludeAttachments, checkboxIncludeComments, checkboxIncludeAllCollections);
		// Right column
		final VerticalLayout rightColumn = new VerticalLayout();
		rightColumn.setSpacing(false);
		rightColumn.setPadding(false);
		rightColumn.getStyle().set("gap", "8px");
		rightColumn.setWidth("50%");
		checkboxCopyStatus = new Checkbox("Copy Status");
		checkboxCopyStatus.setTooltipText("Keep the same status as the original (otherwise will use initial status)");
		checkboxCopyWorkflow = new Checkbox("Copy Workflow");
		checkboxCopyWorkflow.setTooltipText("Keep the same workflow as the original");
		checkboxResetDates = new Checkbox("Reset Dates");
		checkboxResetDates.setTooltipText("Clear all date fields (they will be set to current date on save)");
		checkboxResetAssignments = new Checkbox("Reset Assignments");
		checkboxResetAssignments.setTooltipText("Clear assigned users (you can reassign after copying)");
		rightColumn.add(checkboxCopyStatus, checkboxCopyWorkflow, checkboxResetDates, checkboxResetAssignments);
		optionsGrid.add(leftColumn, rightColumn);
		mainLayout1.add(optionsGrid);
		getDialogLayout().add(mainLayout1);
	}

	/** Toggles between Select All and Deselect All. */
	private void toggleSelectAll() {
		allSelected = !allSelected;
		// Simple logic: when "Select All", check ALL checkboxes
		// When "Deselect All", uncheck ALL checkboxes
		final boolean checkValue = allSelected;
		if (allSelected) {
			buttonSelectAll.setText("Deselect All");
			buttonSelectAll.setIcon(VaadinIcon.CLOSE_SMALL.create());
		} else {
			buttonSelectAll.setText("Select All");
			buttonSelectAll.setIcon(VaadinIcon.CHECK_SQUARE_O.create());
		}
		// Set all checkboxes to the same value
		if (checkboxIncludeRelations != null) {
			checkboxIncludeRelations.setValue(checkValue);
		}
		if (checkboxIncludeAttachments != null) {
			checkboxIncludeAttachments.setValue(checkValue);
		}
		if (checkboxIncludeComments != null) {
			checkboxIncludeComments.setValue(checkValue);
		}
		if (checkboxIncludeAllCollections != null) {
			checkboxIncludeAllCollections.setValue(checkValue);
		}
		if (checkboxCopyStatus != null) {
			checkboxCopyStatus.setValue(checkValue);
		}
		if (checkboxCopyWorkflow != null) {
			checkboxCopyWorkflow.setValue(checkValue);
		}
		if (checkboxResetDates != null) {
			checkboxResetDates.setValue(checkValue);
		}
		if (checkboxResetAssignments != null) {
			checkboxResetAssignments.setValue(checkValue);
		}
	}

	/** Updates the generated name based on the selected target entity type. */
	@SuppressWarnings ({
			"rawtypes"
	})
	private void updateGeneratedName(final String selectedKey) {
		try {
			final Class<?> targetClass;
			if (SAME_AS_SOURCE_KEY.equals(selectedKey)) {
				targetClass = getEntity().getClass();
			} else {
				targetClass = CEntityRegistry.getEntityClass(selectedKey);
			}
			// Get the service for the target class
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(targetClass);
			if (serviceClass != null) {
				final Object serviceObj = CSpringContext.getBean(serviceClass);
				if (serviceObj instanceof tech.derbent.api.entity.service.CAbstractService) {
					final tech.derbent.api.entity.service.CAbstractService service = (tech.derbent.api.entity.service.CAbstractService) serviceObj;
					// Create a temporary entity and let service initialize it (which generates the name)
					final CEntityDB tempEntity = service.newEntity();
					if (tempEntity instanceof CEntityNamed) {
						final String generatedName = ((CEntityNamed<?>) tempEntity).getName();
						if (generatedName != null && !generatedName.isBlank()) {
							textFieldNewName.setValue(generatedName);
							LOGGER.debug("Generated unique name for {}: {}", targetClass.getSimpleName(), generatedName);
							return;
						}
					}
				}
			}
			// Fallback: use entity name + " (Copy)"
			generateFallbackName(targetClass);
		} catch (final Exception e) {
			LOGGER.error("Error generating name for target type: {}", e.getMessage());
			// Fallback to simple copy name
			if (getEntity() instanceof CEntityNamed) {
				textFieldNewName.setValue(((CEntityNamed<?>) getEntity()).getName() + " (Copy)");
			}
		}
	}

	@Override
	protected void validateForm() {
		if (textFieldNewName == null || textFieldNewName.getValue() == null || textFieldNewName.getValue().isBlank()) {
			throw new IllegalArgumentException("Name cannot be empty");
		}
	}
}
