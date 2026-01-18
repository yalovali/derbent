package tech.derbent.api.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Hr;
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
import tech.derbent.api.interfaces.ICopyable;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

/** Dialog for copying entities with configurable options. Allows users to copy to same or different entity types with flexible field mapping.
 * @param <EntityClass> the entity type being copied */
public class CDialogClone<EntityClass extends CEntityDB<EntityClass>> extends CDialogDBEdit<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogClone.class);
	private static final long serialVersionUID = 1L;
	private static final String SAME_AS_SOURCE_KEY = "__SAME_AS_SOURCE__";
	
	private CComboBox<String> comboBoxTargetType;
	private Button buttonSelectAll;
	private Checkbox checkboxIncludeRelations;
	private Checkbox checkboxIncludeAttachments;
	private Checkbox checkboxIncludeComments;
	private Checkbox checkboxIncludeAllCollections;
	private Checkbox checkboxCopyStatus;
	private Checkbox checkboxCopyWorkflow;
	private Checkbox checkboxResetDates;
	private Checkbox checkboxResetAssignments;
	private TextField textFieldNewName;
	private boolean allSelected = false;

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
			final CCloneOptions options = new CCloneOptions.Builder()
					.targetClass(targetClass)
					.includeRelations(checkboxIncludeRelations.getValue())
					.includeAttachments(checkboxIncludeAttachments.getValue())
					.includeComments(checkboxIncludeComments.getValue())
					.includeAllCollections(checkboxIncludeAllCollections.getValue())
					.cloneStatus(checkboxCopyStatus.getValue())
					.cloneWorkflow(checkboxCopyWorkflow.getValue())
					.resetDates(checkboxResetDates.getValue())
					.resetAssignments(checkboxResetAssignments.getValue())
					.build();
			
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
			
			// Close dialog
			close();
			
			// Navigate to the new entity using the standard pattern
			navigateToEntity(copy);
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
		if (textFieldNewName != null && getEntity().toString() != null) {
			textFieldNewName.setValue(getEntity().toString() + " (Copy)");
		}
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setPadding(false);
		mainLayout.setWidth("100%");
		
		// === SECTION 1: Basic Information ===
		final H4 basicInfoHeader = new H4("Basic Information");
		basicInfoHeader.getStyle().set("margin-top", "0");
		basicInfoHeader.getStyle().set("margin-bottom", "10px");
		basicInfoHeader.getStyle().set("color", "var(--lumo-primary-text-color)");
		mainLayout.add(basicInfoHeader);
		
		// Name field
		textFieldNewName = new TextField("New Name");
		textFieldNewName.setWidth("100%");
		textFieldNewName.setPlaceholder("Enter name for copied entity");
		textFieldNewName.setRequired(true);
		textFieldNewName.getStyle().set("margin-bottom", "10px");
		mainLayout.add(textFieldNewName);
		
		// Target type selector with improved UI
		final Div targetTypeSection = new Div();
		targetTypeSection.setWidth("100%");
		targetTypeSection.getStyle()
			.set("padding", "12px")
			.set("background", "var(--lumo-contrast-5pct)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("margin-bottom", "15px");
		
		comboBoxTargetType = new CComboBox<>("Copy To Entity Type");
		comboBoxTargetType.setWidth("100%");
		comboBoxTargetType.setRequired(true);
		
		// Get all registered entity classes (not just CProjectItem)
		final List<String> compatibleTypes = getCompatibleTargetTypes();
		comboBoxTargetType.setItems(compatibleTypes);
		
		// Custom item label generator to show entity titles
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
		
		comboBoxTargetType.setValue(SAME_AS_SOURCE_KEY);
		targetTypeSection.add(comboBoxTargetType);
		mainLayout.add(targetTypeSection);
		
		mainLayout.add(new Hr());
		
		// === SECTION 2: Copy Options ===
		final HorizontalLayout optionsHeaderLayout = new HorizontalLayout();
		optionsHeaderLayout.setWidthFull();
		optionsHeaderLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
		optionsHeaderLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		
		final H4 optionsHeader = new H4("Copy Options");
		optionsHeader.getStyle().set("margin", "0");
		optionsHeader.getStyle().set("color", "var(--lumo-primary-text-color)");
		
		buttonSelectAll = new Button("Select All", VaadinIcon.CHECK_SQUARE_O.create());
		buttonSelectAll.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
		buttonSelectAll.addClickListener(event -> toggleSelectAll());
		
		optionsHeaderLayout.add(optionsHeader, buttonSelectAll);
		mainLayout.add(optionsHeaderLayout);
		
		// Options container with styled background
		final VerticalLayout optionsContainer = new VerticalLayout();
		optionsContainer.setSpacing(true);
		optionsContainer.setPadding(true);
		optionsContainer.getStyle()
			.set("background", "var(--lumo-contrast-5pct)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("margin-top", "10px");
		
		// Include options group
		final Div includeGroup = new Div();
		includeGroup.getStyle().set("margin-bottom", "10px");
		
		checkboxIncludeRelations = new Checkbox("Include Relations");
		checkboxIncludeRelations.setTooltipText("Copy parent/child relationships and linked entities");
		
		checkboxIncludeAttachments = new Checkbox("Include Attachments");
		checkboxIncludeAttachments.setTooltipText("Copy file attachments to the new entity");
		
		checkboxIncludeComments = new Checkbox("Include Comments");
		checkboxIncludeComments.setTooltipText("Copy comment history to the new entity");
		
		checkboxIncludeAllCollections = new Checkbox("Include All Collections");
		checkboxIncludeAllCollections.setTooltipText("Copy all related collections (tags, links, etc.)");
		
		includeGroup.add(checkboxIncludeRelations, checkboxIncludeAttachments, 
				checkboxIncludeComments, checkboxIncludeAllCollections);
		optionsContainer.add(includeGroup);
		
		// Divider
		optionsContainer.add(new Hr());
		
		// Status and workflow group
		final Div statusGroup = new Div();
		statusGroup.getStyle().set("margin-bottom", "10px");
		
		checkboxCopyStatus = new Checkbox("Copy Status");
		checkboxCopyStatus.setTooltipText("Keep the same status as the original (otherwise will use initial status)");
		
		checkboxCopyWorkflow = new Checkbox("Copy Workflow");
		checkboxCopyWorkflow.setTooltipText("Keep the same workflow as the original");
		
		statusGroup.add(checkboxCopyStatus, checkboxCopyWorkflow);
		optionsContainer.add(statusGroup);
		
		// Divider
		optionsContainer.add(new Hr());
		
		// Reset options group
		final Div resetGroup = new Div();
		
		checkboxResetDates = new Checkbox("Reset Dates");
		checkboxResetDates.setTooltipText("Clear all date fields (they will be set to current date on save)");
		
		checkboxResetAssignments = new Checkbox("Reset Assignments");
		checkboxResetAssignments.setTooltipText("Clear assigned users (you can reassign after copying)");
		
		resetGroup.add(checkboxResetDates, checkboxResetAssignments);
		optionsContainer.add(resetGroup);
		
		mainLayout.add(optionsContainer);
		
		getDialogLayout().add(mainLayout);
	}

	/** Toggles between Select All and Deselect All. */
	private void toggleSelectAll() {
		allSelected = !allSelected;
		
		if (allSelected) {
			// Select all
			buttonSelectAll.setText("Deselect All");
			buttonSelectAll.setIcon(VaadinIcon.CLOSE_SMALL.create());
			if (checkboxIncludeRelations != null) checkboxIncludeRelations.setValue(true);
			if (checkboxIncludeAttachments != null) checkboxIncludeAttachments.setValue(true);
			if (checkboxIncludeComments != null) checkboxIncludeComments.setValue(true);
			if (checkboxIncludeAllCollections != null) checkboxIncludeAllCollections.setValue(true);
			if (checkboxCopyStatus != null) checkboxCopyStatus.setValue(true);
			if (checkboxCopyWorkflow != null) checkboxCopyWorkflow.setValue(true);
			if (checkboxResetDates != null) checkboxResetDates.setValue(false);
			if (checkboxResetAssignments != null) checkboxResetAssignments.setValue(false);
		} else {
			// Deselect all
			buttonSelectAll.setText("Select All");
			buttonSelectAll.setIcon(VaadinIcon.CHECK_SQUARE_O.create());
			if (checkboxIncludeRelations != null) checkboxIncludeRelations.setValue(false);
			if (checkboxIncludeAttachments != null) checkboxIncludeAttachments.setValue(false);
			if (checkboxIncludeComments != null) checkboxIncludeComments.setValue(false);
			if (checkboxIncludeAllCollections != null) checkboxIncludeAllCollections.setValue(false);
			if (checkboxCopyStatus != null) checkboxCopyStatus.setValue(false);
			if (checkboxCopyWorkflow != null) checkboxCopyWorkflow.setValue(false);
			if (checkboxResetDates != null) checkboxResetDates.setValue(true);
			if (checkboxResetAssignments != null) checkboxResetAssignments.setValue(true);
		}
	}
	
	/** Returns list of compatible target types for cross-type copying.
	 * Returns ALL registered entity classes (not just CProjectItem).
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
					"CActivity", "CMeeting", "CDecision", "CRisk", "CIssue",
					"CTicket", "COrder", "CMilestone", "CValidationCase",
					// Financial
					"CBudget", "CProjectExpense", "CProjectIncome", "CInvoice",
					// Products and deliverables
					"CProduct", "CProductVersion", "CDeliverable",
					"CProjectComponent", "CProjectComponentVersion",
					// Resources
					"CAsset", "CProvider", "CCustomer", "CResource",
					// Workflows and statuses
					"CWorkflowEntity", "CProjectItemStatus", "CWorkflowStatusRelation",
					// Other
					"CSprint", "CSprintItem", "CRiskLevel", "CAttachment", "CComment",
					"CKanbanLine", "CTag", "CTestCase", "CTestRun", "CValidationSession"
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
			final List<String> sorted = typeKeys.subList(1, typeKeys.size()).stream()
				.sorted((a, b) -> {
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
				})
				.collect(Collectors.toList());
			
			// Rebuild list with "Same as Source" first, then sorted items
			typeKeys.clear();
			typeKeys.add(SAME_AS_SOURCE_KEY);
			typeKeys.addAll(sorted);
		}
		
		return typeKeys;
	}

	/** Navigates to the copied entity using the standard navigation pattern.
	 * Follows the same pattern as CNavigableComboBox.createNavigationButton().
	 * @param copiedEntity the entity that was copied and saved */
	private void navigateToEntity(final CEntityDB<?> copiedEntity) {
		try {
			Check.notNull(copiedEntity, "Copied entity cannot be null");
			Check.notNull(copiedEntity.getId(), "Copied entity must have an ID to navigate");
			
			// Get the VIEW_NAME from the entity class
			final Class<?> entityClass = copiedEntity.getClass();
			final String baseViewName = (String) entityClass.getField("VIEW_NAME").get(null);
			Check.notNull(baseViewName, "Base view name cannot be null for class: " + entityClass.getName());
			
			// Get the page entity for this view
			final CPageEntityService pageService = CSpringContext.getBean(CPageEntityService.class);
			final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);
			final CPageEntity pageEntity = pageService.findByNameAndProject(
					baseViewName, 
					sessionService.getActiveProject().orElse(null)
			).orElse(null);
			
			if (pageEntity == null) {
				LOGGER.debug("No page entity found for view name: {}", baseViewName);
				return;
			}
			
			// Navigate to the entity page with item parameter
			final String route = pageEntity.getRoute() + "&item:" + copiedEntity.getId();
			LOGGER.info("Navigating to copied entity: {} at route: {}", copiedEntity, route);
			UI.getCurrent().navigate(route);
		} catch (final Exception e) {
			LOGGER.error("Error navigating to copied entity", e);
			// Don't show error to user - navigation failure shouldn't block the copy operation
		}
	}

	@Override
	protected void validateForm() {
		if (textFieldNewName == null || textFieldNewName.getValue() == null || textFieldNewName.getValue().isBlank()) {
			throw new IllegalArgumentException("Name cannot be empty");
		}
	}
}
