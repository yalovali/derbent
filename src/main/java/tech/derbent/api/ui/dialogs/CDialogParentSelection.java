package tech.derbent.api.ui.dialogs;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CParentChildRelationService;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/** CDialogParentSelection - Dialog for selecting a hierarchical parent for a project item.
 * <p>
 * Supports up to 4 levels of hierarchy (Epic → Feature → User Story → Task) based on entity type configuration. Each level's combobox is filtered by
 * the previous level's selection.
 * <p>
 * Features:
 * <ul>
 * <li>Hierarchical combobox filtering</li>
 * <li>Type-based parent level configuration</li>
 * <li>Circular dependency prevention</li>
 * <li>Clear parent option</li>
 * <li>Parent type validation</li>
 * </ul>
 */
public class CDialogParentSelection extends CDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogParentSelection.class);
	private static final long serialVersionUID = 1L;
	private CButton buttonCancel;
	private CButton buttonClear;
	private CButton buttonSelect;
	// Configuration
	private final CProjectItem<?> childItem;
	private final CTypeEntity<?> childType;
	// UI Components
	private ComboBox<CProjectItem<?>> comboBoxLevel1;
	private ComboBox<CProjectItem<?>> comboBoxLevel2;
	private ComboBox<CProjectItem<?>> comboBoxLevel3;
	private ComboBox<CProjectItem<?>> comboBoxLevel4;
	private final Consumer<CProjectItem<?>> onSelection;
	// Services
	private final CParentChildRelationService parentChildService;
	private final CProject<?> project;

	/** Creates a parent selection dialog.
	 * @param childItem   the item that needs a parent assigned
	 * @param onSelection callback when parent is selected (receives selected parent or null for clear) */
	public CDialogParentSelection(final CProjectItem<?> childItem, final Consumer<CProjectItem<?>> onSelection) {
		Objects.requireNonNull(childItem, "Child item cannot be null");
		Objects.requireNonNull(childItem.getId(), "Child item must be persisted");
		Objects.requireNonNull(onSelection, "Selection callback cannot be null");
		this.childItem = childItem;
		project = childItem.getProject();
		Check.notNull(project, "Child item must have a project assigned");
		childType = getEntityType(childItem);
		this.onSelection = onSelection;
		parentChildService = CSpringContext.getBean(CParentChildRelationService.class);
		try {
			setupDialog();
			// Width handled by CDialog base class (responsive pattern)
			setHeight("auto");
			setResizable(false);
		} catch (final Exception e) {
			LOGGER.error("Error setting up parent selection dialog", e);
			CNotificationService.showException("Error creating dialog", e);
		}
	}

	/** Factory method for cancel button. */
	protected CButton create_buttonCancel() {
		return CButton.createTertiary("Cancel", VaadinIcon.CLOSE.create(), event -> on_buttonCancel_clicked());
	}

	/** Factory method for clear parent button. */
	protected CButton create_buttonClear() {
		return CButton.createError("Clear Parent", VaadinIcon.TRASH.create(), event -> on_buttonClear_clicked());
	}

	/** Factory method for select button. */
	protected CButton create_buttonSelect() {
		final CButton button = CButton.createPrimary("Select", VaadinIcon.CHECK.create(), event -> on_buttonSelect_clicked());
		button.setEnabled(false);
		return button;
	}

	/** Create a combobox for a specific parent level.
	 * @param entityClassName the class name of entities to show (e.g., "CActivity")
	 * @param parentFilter    parent item to filter by (for hierarchical filtering)
	 * @return combobox or null if entity class not found */
	private ComboBox<CProjectItem<?>> createParentComboBox(final String entityClassName, final CProjectItem<?> parentFilter) {
		Check.notBlank(entityClassName, "Entity class name cannot be blank");
		try {
			final Class<?> entityClass = CEntityRegistry.getEntityClassByTitle(entityClassName);
			if (entityClass == null) {
				LOGGER.warn("Could not find entity class for: {}", entityClassName);
				return null;
			}
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
			if (serviceClass == null) {
				LOGGER.warn("Could not find service class for entity: {}", entityClassName);
				return null;
			}
			final CProjectItemService<?> service = (CProjectItemService<?>) CSpringContext.getBean(serviceClass);
			final ComboBox<CProjectItem<?>> comboBox = new ComboBox<>();
			comboBox.setWidthFull();
			comboBox.setItemLabelGenerator(CProjectItem::getName);
			comboBox.setPlaceholder("Select " + entityClassName);
			// Load items
			if (parentFilter != null) {
				// Filter by parent - get only children of the specified entity type
				Objects.requireNonNull(parentFilter.getId(), "Parent filter must be persisted");
				final List<CProjectItem<?>> children = parentChildService.getChildrenByType(parentFilter, entityClassName);
				comboBox.setItems(children);
			} else {
				// Show all items of this type in the current project (service uses active project from session)
				final List<?> items = service.findAll();
				final List<CProjectItem<?>> projectItems = new ArrayList<>();
				items.forEach((final Object item) -> {
					if (item instanceof CProjectItem) {
						final CProjectItem<?> projItem = (CProjectItem<?>) item;
						// Double-check project matches (in case session project changed)
						if (projItem.getProject() != null && projItem.getProject().getId().equals(project.getId())) {
							projectItems.add(projItem);
						}
					}
				});
				comboBox.setItems(projectItems);
			}
			return comboBox;
		} catch (final Exception e) {
			LOGGER.error("Error creating parent combobox for {}", entityClassName, e);
			return null;
		}
	}

	@Override
	public String getDialogTitleString() { return "Select Parent for " + childItem.getName(); }

	private CTypeEntity<?> getEntityType(final CProjectItem<?> item) {
		try {
			final Method getEntityTypeMethod = item.getClass().getMethod("getEntityType");
			final Object entityType = getEntityTypeMethod.invoke(item);
			if (entityType instanceof CTypeEntity) {
				return (CTypeEntity<?>) entityType;
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not get entity type for {}", item.getClass().getSimpleName());
		}
		return null;
	}

	@Override
	protected Icon getFormIcon() { return VaadinIcon.SITEMAP.create(); }

	@Override
	protected String getFormTitleString() { return "Hierarchical Parent Selection"; }

	/** Handle cancel button click. */
	protected void on_buttonCancel_clicked() {
		close();
	}

	/** Handle clear parent button click. */
	protected void on_buttonClear_clicked() {
		try {
			LOGGER.debug("Clearing parent for {}#{}", childItem.getClass().getSimpleName(), childItem.getId());
			onSelection.accept(null);
			close();
		} catch (final Exception e) {
			LOGGER.error("Error clearing parent", e);
			CNotificationService.showException("Error clearing parent", e);
		}
	}

	/** Handle select button click. */
	protected void on_buttonSelect_clicked() {
		try {
			// Find the last non-null combobox value (deepest level selected)
			CProjectItem<?> selectedParent = null;
			if (comboBoxLevel4 != null && comboBoxLevel4.getValue() != null) {
				selectedParent = comboBoxLevel4.getValue();
			} else if (comboBoxLevel3 != null && comboBoxLevel3.getValue() != null) {
				selectedParent = comboBoxLevel3.getValue();
			} else if (comboBoxLevel2 != null && comboBoxLevel2.getValue() != null) {
				selectedParent = comboBoxLevel2.getValue();
			} else if (comboBoxLevel1 != null && comboBoxLevel1.getValue() != null) {
				selectedParent = comboBoxLevel1.getValue();
			}
			if (selectedParent == null) {
				CNotificationService.showWarning("Please select a parent item");
				return;
			}
			// Validate that this doesn't create a circular dependency
			if (parentChildService.wouldCreateCircularDependency(selectedParent.getId(), selectedParent.getClass().getSimpleName(), childItem.getId(),
					childItem.getClass().getSimpleName())) {
				CNotificationService.showError("Cannot set this parent: would create a circular dependency");
				return;
			}
			// Validate that parent can have children
			if (!CParentChildRelationService.canHaveChildren(selectedParent)) {
				CNotificationService.showError("The selected item type cannot have children");
				return;
			}
			LOGGER.debug("Selected parent: {}#{} for child {}#{}", selectedParent.getClass().getSimpleName(), selectedParent.getId(),
					childItem.getClass().getSimpleName(), childItem.getId());
			onSelection.accept(selectedParent);
			close();
		} catch (final Exception e) {
			LOGGER.error("Error selecting parent", e);
			CNotificationService.showException("Error selecting parent", e);
		}
	}

	protected void on_comboBoxLevel1_changed(final CProjectItem<?> selectedItem) {
		// Enable level 2 if available and item selected
		if (comboBoxLevel2 != null) {
			comboBoxLevel2.setEnabled(selectedItem != null);
			if (selectedItem != null) {
				// Reload level 2 items filtered by level 1 selection
				Objects.requireNonNull(childType, "Child type must not be null");
				final String level2Class = childType.getParentLevel2EntityClass();
				if (level2Class != null && !level2Class.isBlank()) {
					final ComboBox<CProjectItem<?>> newCombo = createParentComboBox(level2Class, selectedItem);
					if (newCombo != null) {
						final List<CProjectItem<?>> items = new ArrayList<>();
						newCombo.getListDataView().getItems().forEach(items::add);
						comboBoxLevel2.setItems(items);
					}
				}
			} else {
				comboBoxLevel2.clear();
			}
		}
		// Clear and disable subsequent levels
		if (comboBoxLevel3 != null) {
			comboBoxLevel3.clear();
			comboBoxLevel3.setEnabled(false);
		}
		if (comboBoxLevel4 != null) {
			comboBoxLevel4.clear();
			comboBoxLevel4.setEnabled(false);
		}
		// Enable select button if any level has a value
		updateSelectButtonState();
	}

	protected void on_comboBoxLevel2_changed(final CProjectItem<?> selectedItem) {
		// Enable level 3 if available and item selected
		if (comboBoxLevel3 != null) {
			comboBoxLevel3.setEnabled(selectedItem != null);
			if (selectedItem != null) {
				// Reload level 3 items filtered by level 2 selection
				Objects.requireNonNull(childType, "Child type must not be null");
				final String level3Class = childType.getParentLevel3EntityClass();
				if (level3Class != null && !level3Class.isBlank()) {
					final ComboBox<CProjectItem<?>> newCombo = createParentComboBox(level3Class, selectedItem);
					if (newCombo != null) {
						final List<CProjectItem<?>> items = new ArrayList<>();
						newCombo.getListDataView().getItems().forEach(items::add);
						comboBoxLevel3.setItems(items);
					}
				}
			} else {
				comboBoxLevel3.clear();
			}
		}
		// Clear and disable level 4
		if (comboBoxLevel4 != null) {
			comboBoxLevel4.clear();
			comboBoxLevel4.setEnabled(false);
		}
		updateSelectButtonState();
	}

	protected void on_comboBoxLevel3_changed(final CProjectItem<?> selectedItem) {
		// Enable level 4 if available and item selected
		if (comboBoxLevel4 != null) {
			comboBoxLevel4.setEnabled(selectedItem != null);
			if (selectedItem != null) {
				// Reload level 4 items filtered by level 3 selection
				Objects.requireNonNull(childType, "Child type must not be null");
				final String level4Class = childType.getParentLevel4EntityClass();
				if (level4Class != null && !level4Class.isBlank()) {
					final ComboBox<CProjectItem<?>> newCombo = createParentComboBox(level4Class, selectedItem);
					if (newCombo != null) {
						final List<CProjectItem<?>> items = new ArrayList<>();
						newCombo.getListDataView().getItems().forEach(items::add);
						comboBoxLevel4.setItems(items);
					}
				}
			} else {
				comboBoxLevel4.clear();
			}
		}
		updateSelectButtonState();
	}

	/** @param selectedItem */
	protected void on_comboBoxLevel4_changed(final CProjectItem<?> selectedItem) {
		updateSelectButtonState();
	}

	@Override
	protected void setupButtons() {
		buttonSelect = create_buttonSelect();
		buttonClear = create_buttonClear();
		buttonCancel = create_buttonCancel();
		// Enable clear button only if item currently has a parent
		buttonClear.setEnabled(childItem.hasParent());
		buttonLayout.add(buttonSelect, buttonClear, buttonCancel);
	}

	@Override
	protected void setupContent() {
		final CVerticalLayout layout = new CVerticalLayout();
		layout.setSpacing(true);
		layout.setPadding(false);
		// Add description
		final CSpan description =
				new CSpan("Select a parent item from up to 4 hierarchical levels. " + "Each level filters based on the previous selection.");
		description.getStyle().set("color", "var(--lumo-secondary-text-color)");
		layout.add(description);
		// Get parent level configuration from child type
		if (childType != null) {
			// Level 1
			final String level1Class = childType.getParentLevel1EntityClass();
			if (level1Class != null && !level1Class.isBlank()) {
				layout.add(new CH3("Level 1: " + level1Class));
				comboBoxLevel1 = createParentComboBox(level1Class, null);
				if (comboBoxLevel1 != null) {
					comboBoxLevel1.addValueChangeListener(e -> on_comboBoxLevel1_changed(e.getValue()));
					layout.add(comboBoxLevel1);
				}
			}
			// Level 2
			final String level2Class = childType.getParentLevel2EntityClass();
			if (level2Class != null && !level2Class.isBlank()) {
				layout.add(new CH3("Level 2: " + level2Class));
				comboBoxLevel2 = createParentComboBox(level2Class, null);
				if (comboBoxLevel2 != null) {
					comboBoxLevel2.setEnabled(false);
					comboBoxLevel2.addValueChangeListener(e -> on_comboBoxLevel2_changed(e.getValue()));
					layout.add(comboBoxLevel2);
				}
			}
			// Level 3
			final String level3Class = childType.getParentLevel3EntityClass();
			if (level3Class != null && !level3Class.isBlank()) {
				layout.add(new CH3("Level 3: " + level3Class));
				comboBoxLevel3 = createParentComboBox(level3Class, null);
				if (comboBoxLevel3 != null) {
					comboBoxLevel3.setEnabled(false);
					comboBoxLevel3.addValueChangeListener(e -> on_comboBoxLevel3_changed(e.getValue()));
					layout.add(comboBoxLevel3);
				}
			}
			// Level 4
			final String level4Class = childType.getParentLevel4EntityClass();
			if (level4Class != null && !level4Class.isBlank()) {
				layout.add(new CH3("Level 4: " + level4Class));
				comboBoxLevel4 = createParentComboBox(level4Class, null);
				if (comboBoxLevel4 != null) {
					comboBoxLevel4.setEnabled(false);
					comboBoxLevel4.addValueChangeListener(e -> on_comboBoxLevel4_changed(e.getValue()));
					layout.add(comboBoxLevel4);
				}
			}
		}
		// If no levels configured, show all items of the same project
		if (comboBoxLevel1 == null) {
			final CSpan warningSpan =
					new CSpan("No hierarchical levels configured for this item type. " + "Please configure parent levels in the type settings.");
			warningSpan.getStyle().set("color", "var(--lumo-error-text-color)");
			layout.add(warningSpan);
		}
		mainLayout.add(layout);
	}

	private void updateSelectButtonState() {
		if (buttonSelect != null) {
			boolean hasSelection = false;
			if (comboBoxLevel1 != null && comboBoxLevel1.getValue() != null) {
				hasSelection = true;
			}
			if (comboBoxLevel2 != null && comboBoxLevel2.getValue() != null) {
				hasSelection = true;
			}
			if (comboBoxLevel3 != null && comboBoxLevel3.getValue() != null) {
				hasSelection = true;
			}
			if (comboBoxLevel4 != null && comboBoxLevel4.getValue() != null) {
				hasSelection = true;
			}
			buttonSelect.setEnabled(hasSelection);
		}
	}
}
