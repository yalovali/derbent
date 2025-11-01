package tech.derbent.api.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.annotations.CDataProviderResolver;
import tech.derbent.api.components.CColorAwareComboBox;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CProjectItem;
import tech.derbent.api.domains.CProjectItemStatus;
import tech.derbent.api.domains.IHasStatusAndWorkflow;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;

/** Generic CRUD toolbar component that provides comprehensive Create, Read, Update, Delete, and Refresh functionality for any entity type.
 * <p>
 * <b>NEW USAGE PATTERN (Recommended):</b> Create with minimal constructor and configure after:
 *
 * <pre>
 * // Create toolbar with just a binder
 * CCrudToolbar toolbar = CCrudToolbar.create(binder);
 * 
 * // Configure as needed
 * toolbar.setEntityService(entityService);
 * toolbar.setNewEntitySupplier(() -&gt; createNewEntity());
 * toolbar.setRefreshCallback(entity -&gt; refreshForm(entity));
 * toolbar.setNotificationService(notificationService);
 * 
 * // Toolbar is visible with disabled buttons until configured
 * // For dynamic entity types (e.g., Gantt charts):
 * toolbar.reconfigureForEntityType(newEntityClass, newEntityService);
 * </pre>
 * <p>
 * <b>Legacy usage patterns still supported:</b>
 * - IContentOwner-based constructor (deprecated)
 * - Functional interface constructor (deprecated)
 * <p>
 * The toolbar can now be reconfigured at runtime to support different entity types,
 * which is essential for pages like Gantt charts where the selected item type changes.
 * @param <EntityClass> the entity type this toolbar operates on */
public class CCrudToolbar extends HorizontalLayout {

	private static CDataProviderResolver dataProviderResolver;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCrudToolbar.class);
	private static final long serialVersionUID = 1L;
	
	// Core components
	private final CEnhancedBinder<?> binder;
	private CButton createButton;
	private CButton saveButton;
	private CButton deleteButton;
	private CButton refreshButton;
	private ComboBox<CProjectItemStatus> statusComboBox; // Workflow status selector
	
	// State
	private CEntityDB<?> currentEntity;
	private Class<?> entityClass; // Now mutable to support entity type changes
	
	// Configuration (all can be set after construction)
	private CAbstractService<?> entityService;
	private Supplier<?> newEntitySupplier;
	private Consumer<?> refreshCallback;
	private Consumer<?> saveCallback;
	private Function<?, String> dependencyChecker;
	private CNotificationService notificationService; // Optional injection
	private CWorkflowStatusRelationService workflowStatusRelationService; // Optional injection
	
	// Listeners
	private final List<IEntityUpdateListener> updateListeners = new ArrayList<>();

	/** Minimal constructor - creates toolbar with just a binder. All other configuration is done via setters.
	 * The toolbar will be visible with disabled buttons until properly configured.
	 * @param binder the data binder for form validation */
	public CCrudToolbar(final CEnhancedBinder<?> binder) {
		Check.notNull(binder, "Binder cannot be null");
		this.binder = binder;
		dataProviderResolver = CSpringContext.getBean(CDataProviderResolver.class);
		
		// Configure toolbar appearance
		setSpacing(true);
		setPadding(true);
		addClassName("crud-toolbar");
		setWidthFull();
		
		// Create UI components (buttons will be disabled until configured)
		createToolbarButtons();
		
		LOGGER.debug("Created CCrudToolbar with minimal configuration");
	}

	/** Static factory method for creating a CCrudToolbar with minimal configuration.
	 * This is the recommended way to create a toolbar.
	 * @param binder the data binder for form validation
	 * @return a new CCrudToolbar instance */
	public static CCrudToolbar create(final CEnhancedBinder<?> binder) {
		return new CCrudToolbar(binder);
	}

	/** Adds an update listener to be notified of CRUD operations.
	 * @param listener the listener to add */
	public void addUpdateListener(final IEntityUpdateListener listener) {
		if (listener != null && !updateListeners.contains(listener)) {
			updateListeners.add(listener);
		}
	}

	/** Reconfigures the toolbar for a different entity type and service.
	 * This is essential for pages like Gantt charts where the entity type changes based on user selection.
	 * @param newEntityClass the new entity class type
	 * @param newEntityService the new entity service */
	@SuppressWarnings("unchecked")
	public void reconfigureForEntityType(Class<?> newEntityClass, CAbstractService<?> newEntityService) {
		LOGGER.debug("Reconfiguring toolbar for entity type: {}", newEntityClass != null ? newEntityClass.getSimpleName() : "null");
		
		this.entityClass = newEntityClass;
		this.entityService = newEntityService;
		
		// Update dependency checker from new service
		if (newEntityService != null) {
			this.dependencyChecker = newEntityService::checkDeleteAllowed;
		} else {
			this.dependencyChecker = null;
		}
		
		// Recreate status combobox if needed
		if (statusComboBox != null) {
			remove(statusComboBox);
			statusComboBox = null;
		}
		
		// Only create status combobox if entity type supports it
		if (newEntityClass != null && IHasStatusAndWorkflow.class.isAssignableFrom(newEntityClass)) {
			createWorkflowStatusComboBox();
		}
		
		updateButtonStates();
	}

	/** Sets the entity service for CRUD operations.
	 * @param entityService the entity service */
	public void setEntityService(CAbstractService<?> entityService) {
		this.entityService = entityService;
		if (entityService != null) {
			this.dependencyChecker = entityService::checkDeleteAllowed;
		}
		updateButtonStates();
	}

	/** Sets the entity class type. Used in conjunction with setEntityService for dynamic entity type changes.
	 * @param entityClass the entity class type */
	public void setEntityClass(Class<?> entityClass) {
		this.entityClass = entityClass;
		updateButtonStates();
	}

	/** Configures button visibility for customized toolbar layouts.
	 * @param showCreate  whether to show the Create button
	 * @param showSave    whether to show the Save button
	 * @param showDelete  whether to show the Delete button
	 * @param showRefresh whether to show the Refresh button */
	public void configureButtonVisibility(boolean showCreate, boolean showSave, boolean showDelete, boolean showRefresh) {
		if (createButton != null) {
			createButton.setVisible(showCreate);
		}
		if (saveButton != null) {
			saveButton.setVisible(showSave);
		}
		if (deleteButton != null) {
			deleteButton.setVisible(showDelete);
		}
		if (refreshButton != null) {
			refreshButton.setVisible(showRefresh);
		}
	}

	/** Creates all the CRUD toolbar buttons. */
	private void createToolbarButtons() {
		createButton = CButton.createNewButton("New", e -> handleCreate());
		createButton.getElement().setAttribute("title", "Create new entity");
		saveButton = CButton.createSaveButton("Save", e -> {
			try {
				handleSave();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		});
		saveButton.getElement().setAttribute("title", "Save current entity");
		deleteButton = CButton.createDeleteButton("Delete", e -> handleDelete());
		deleteButton.getElement().setAttribute("title", "Delete current entity");
		refreshButton = CButton.createTertiary("Refresh", VaadinIcon.REFRESH.create(), e -> handleRefresh());
		refreshButton.getElement().setAttribute("title", "Refresh data");
		add(createButton, saveButton, deleteButton, refreshButton);
		// Create workflow status combobox if entity is a CProjectItem
		// if (IHasStatusAndWorkflow.class.isAssignableFrom(entityClass)) {
		createWorkflowStatusComboBox();
		// }
		updateButtonStates();
	}

	/** Creates the workflow status combobox for CProjectItem entities. */
	private void createWorkflowStatusComboBox() {
		try {
			// Only create if we have a valid entity class that supports workflow
			if (entityClass == null || !IHasStatusAndWorkflow.class.isAssignableFrom(entityClass)) {
				return;
			}
			
			statusComboBox = new CColorAwareComboBox<CProjectItemStatus>(null,
					CEntityFieldService.createFieldInfo(CProjectItem.class.getDeclaredField("status")), binder, dataProviderResolver);
			statusComboBox.addValueChangeListener(event -> {
				if (event.isFromClient() && event.getValue() != null && currentEntity instanceof CProjectItem) {
					handleWorkflowStatusChange((CProjectItem<?>) currentEntity, event.getValue());
				}
			});
			add(statusComboBox);
		} catch (Exception e) {
			LOGGER.error("Error creating workflow status combobox", e);
		}
	}

	/** Gets the binder associated with this toolbar.
	 * @return the binder instance */
	public CEnhancedBinder<?> getBinder() { return binder; }

	/** Gets the current entity.
	 * @return the current entity */
	public CEntityDB<?> getCurrentEntity() { return currentEntity; }

	/** Gets the current entity value. This is an alias for getCurrentEntity() to match standard Vaadin component patterns.
	 * @return the current entity */
	public CEntityDB<?> getValue() { return currentEntity; }

	/** Handles the create (new entity) operation. */
	private void handleCreate() {
		try {
			LOGGER.debug("Handling create operation for entity");
			Check.notNull(newEntitySupplier, "New entity supplier is not set");
			// Create new entity
			Object newEntity = newEntitySupplier.get();
			Check.notNull(newEntity, "New entity supplier returned null");
			// Initialize the new entity with default values from session and available data
			try {
				entityService.initializeNewEntity(newEntity);
				LOGGER.debug("Initialized new entity with default values");
			} catch (IllegalStateException e) {
				// If initialization fails due to missing required data, show specific error
				LOGGER.error("Failed to initialize new entity: {}", e.getMessage());
				showErrorNotification("Cannot create new entity: " + e.getMessage());
				return;
			}
			// Set as current entity and bind to form
			setCurrentEntity(newEntity);
			showSuccessNotification("New entity created. Fill in the details and click Save.");
			// Notify listeners that a new entity was created
			notifyListenersCreated(newEntity);
		} catch (Exception exception) {
			LOGGER.error("Error during create operation for entity", exception);
			if (notificationService != null) {
				notificationService.showCreateError();
			} else {
				showErrorNotification("An error occurred while creating new entity. Please try again.");
			}
		}
	}

	/** Handles the delete operation with confirmation dialog and proper error handling. */
	@SuppressWarnings("unchecked")
	private void handleDelete() {
		try {
			Check.notNull(entityService, "Entity service cannot be null");
			Check.notNull(currentEntity, "Current entity cannot be null");
			LOGGER.debug("Handling delete operation for entity : {}", currentEntity.getClass().getSimpleName());
			if (currentEntity == null || currentEntity.getId() == null) {
				showErrorNotification("Cannot delete: No entity selected or entity not saved yet.");
				return;
			}
			Check.notNull(dependencyChecker, "Dependency checker function is not set");
			String dependencyError = ((Function<Object, String>) dependencyChecker).apply(currentEntity);
			if (dependencyError != null) {
				showErrorNotification(dependencyError);
				return;
			}
			// Show confirmation dialog
			CConfirmationDialog confirmDialog = new CConfirmationDialog(
					"Are you sure you want to delete this " + currentEntity.getClass().getSimpleName() + "?", this::performDelete);
			confirmDialog.open();
		} catch (Exception exception) {
			LOGGER.error("Error during delete operation for entity: {}", currentEntity != null ? currentEntity.getClass().getSimpleName() : "null", exception);
			if (notificationService != null) {
				notificationService.showDeleteError();
			} else {
				showErrorNotification("An error occurred while attempting to delete the entity. Please try again.");
			}
		}
	}

	/** Handles the refresh operation. */
	@SuppressWarnings("unchecked")
	private void handleRefresh() {
		try {
			LOGGER.debug("Handling refresh operation for entity: {}", currentEntity != null ? currentEntity.getClass().getSimpleName() : "null");
			Check.notNull(refreshCallback, "Refresh callback is not set");
			((Consumer<Object>) refreshCallback).accept(currentEntity);
			showSuccessNotification("Data refreshed successfully");
		} catch (Exception exception) {
			LOGGER.error("Error during refresh operation for entity: {}", currentEntity != null ? currentEntity.getClass().getSimpleName() : "null", exception);
			if (notificationService != null) {
				notificationService.showError("Refresh failed");
			} else {
				showErrorNotification("An error occurred while attempting to refresh the entity. Please try again.");
			}
		}
	}

	/** Handles the save (update) operation with proper validation, error handling, and notifications.
	 * @throws Exception */
	@SuppressWarnings("unchecked")
	private void handleSave() throws Exception {
		try {
			Check.notNull(entityService, "Entity service cannot be null");
			Check.notNull(currentEntity, "Current entity cannot be null");
			LOGGER.debug("Attempting to save entity: {}", currentEntity.getClass().getSimpleName());
			if (currentEntity == null) {
				showErrorNotification("Cannot save: No entity selected.");
				return;
			}
			// Check if save is allowed (validation)
			final String saveError = ((CAbstractService<CEntityDB<?>>) entityService).checkSaveAllowed(currentEntity);
			if (saveError != null) {
				showErrorNotification(saveError);
				return;
			}
			// Use custom save callback if provided (for integration with binder validation)
			if (saveCallback != null) {
				((Consumer<Object>) saveCallback).accept(currentEntity);
			} else {
				// Default save behavior
				final Object savedEntity = ((CAbstractService<CEntityDB<?>>) entityService).save(currentEntity);
				currentEntity = (CEntityDB<?>) savedEntity;
				updateButtonStates();
				showSuccessNotification("Data saved successfully");
				// Notify listeners
				notifyListenersSaved(savedEntity);
			}
		} catch (Exception e) {
			LOGGER.error("Error during save operation for entity: {}", currentEntity != null ? currentEntity.getClass().getSimpleName() : "null", e);
			if (notificationService != null) {
				notificationService.showSaveError();
			} else {
				showErrorNotification("An error occurred while attempting to save the entity. Please try again.");
			}
			throw e;
		}
	}

	/** Handles workflow status change, validates transition and saves the entity.
	 * @param projectItem the project item to update
	 * @param newStatus   the new status to set */
	@SuppressWarnings("unchecked")
	private void handleWorkflowStatusChange(final CProjectItem<?> projectItem, final CProjectItemStatus newStatus) {
		try {
			if (projectItem == null || newStatus == null) {
				return;
			}
			final CProjectItemStatus currentStatus = projectItem.getStatus();
			// If status hasn't changed, do nothing
			if (currentStatus != null && currentStatus.getId().equals(newStatus.getId())) {
				return;
			}
			Check.instanceOf(projectItem, IHasStatusAndWorkflow.class, "Current entity is not a CProjectItem");
			final CWorkflowEntity workflow = ((IHasStatusAndWorkflow<?>) projectItem).getWorkflow();
			if (workflow != null && currentStatus != null && workflowStatusRelationService != null) {
				final List<CWorkflowStatusRelation> relations = workflowStatusRelationService.findByWorkflow(workflow);
				final boolean isValidTransition = relations.stream()
						.anyMatch(r -> r.getFromStatus().getId().equals(currentStatus.getId()) && r.getToStatus().getId().equals(newStatus.getId()));
				if (!isValidTransition) {
					showErrorNotification("Invalid workflow transition from '" + currentStatus.getName() + "' to '" + newStatus.getName() + "'");
					// Reset combobox to current status
					statusComboBox.setValue(currentStatus);
					return;
				}
			}
			// Update and save entity
			projectItem.setStatus(newStatus);
			try {
				((CAbstractService<CEntityDB<?>>) entityService).save(currentEntity);
				showSuccessNotification("Status updated to '" + newStatus.getName() + "'");
				// Notify listeners
				notifyListenersSaved(currentEntity);
			} catch (Exception e) {
				LOGGER.error("Error saving entity after status change", e);
				showErrorNotification("Failed to update status: " + e.getMessage());
				// Reset combobox to current status
				statusComboBox.setValue(currentStatus);
			}
		} catch (Exception e) {
			LOGGER.error("Error handling workflow status change", e);
			showErrorNotification("Error updating status: " + e.getMessage());
		}
	}

	/** Notifies all listeners that an entity was created. */
	@SuppressWarnings("unchecked")
	private void notifyListenersCreated(final Object entity) {
		if (currentEntity != null) {
			LOGGER.debug("Notifying listeners of entity creation: {}", currentEntity.getClass().getSimpleName());
		}
		updateListeners.forEach(listener -> {
			try {
				listener.onEntityCreated((CEntityDB<?>) entity);
			} catch (final Exception e) {
				LOGGER.error("Error notifying listener of entity creation", e);
				e.printStackTrace();
			}
		});
	}

	/** Notifies all listeners that an entity was deleted. */
	@SuppressWarnings("unchecked")
	private void notifyListenersDeleted(final Object entity) {
		if (currentEntity != null) {
			LOGGER.debug("Notifying listeners of entity deletion: {}", currentEntity.getClass().getSimpleName());
		}
		updateListeners.forEach(listener -> {
			try {
				listener.onEntityDeleted((CEntityDB<?>) entity);
			} catch (final Exception e) {
				LOGGER.error("Error notifying listener of entity deletion", e);
				e.printStackTrace();
			}
		});
	}

	/** Notifies all listeners that an entity was saved. */
	@SuppressWarnings("unchecked")
	private void notifyListenersSaved(final Object entity) {
		if (currentEntity != null) {
			LOGGER.debug("Notifying listeners of entity save: {}", currentEntity.getClass().getSimpleName());
		}
		updateListeners.forEach(listener -> {
			try {
				listener.onEntitySaved((CEntityDB<?>) entity);
			} catch (final Exception e) {
				LOGGER.error("Error notifying listener of entity save", e);
				e.printStackTrace();
			}
		});
	}

	/** Performs the actual delete operation after confirmation. */
	@SuppressWarnings("unchecked")
	private void performDelete() {
		try {
			LOGGER.debug("Performing delete operation for entity: {}", currentEntity.getClass().getSimpleName());
			CEntityDB<?> entityToDelete = currentEntity;
			((CAbstractService<CEntityDB<?>>) entityService).delete(currentEntity);
			LOGGER.info("Entity deleted successfully: {} with ID: {}", currentEntity.getClass().getSimpleName(), entityToDelete.getId());
			// Clear current entity
			currentEntity = null;
			// Clear the binder to reset the form
			if (binder != null) {
				((CEnhancedBinder<CEntityDB<?>>) binder).setBean(null);
			}
			updateButtonStates();
			showSuccessNotification("Entity deleted successfully");
			// Notify listeners
			notifyListenersDeleted(entityToDelete);
		} catch (final Exception e) {
			LOGGER.error("Error during delete operation for entity: {}", currentEntity != null ? currentEntity.getClass().getSimpleName() : "null", e);
			if (notificationService != null) {
				notificationService.showDeleteError();
			} else {
				showErrorNotification("An error occurred while deleting the entity. Please try again.");
			}
			throw e;
		}
	}

	/** Removes an update listener.
	 * @param listener the listener to remove */
	public void removeUpdateListener(final IEntityUpdateListener listener) {
		updateListeners.remove(listener);
	}

	/** Updates the current entity and refreshes button states.
	 * @param entity the current entity */
	@SuppressWarnings ("unchecked")
	public void setCurrentEntity(final Object entity) {
		// LOGGER.debug("Setting current entity in toolbar: {}", entity != null ? entityClass.getSimpleName() : "null");
		currentEntity = (CEntityDB<?>) entity;
		// Automatically set dependency checker from service when entity changes
		if (entityService != null) {
			dependencyChecker = entityService::checkDeleteAllowed;
		}
		updateButtonStates();
	}

	/** OPTIONAL CONFIGURATOR: Sets the dependency checker function that returns error message if entity cannot be deleted. By default, uses
	 * entityService::checkDeleteAllowed. Only use this to override the default behavior.
	 * @param dependencyChecker function that returns null if entity can be deleted, or error message if it cannot */
	public void setDependencyChecker(final Function<?, String> dependencyChecker) {
		this.dependencyChecker = dependencyChecker;
	}

	/** OPTIONAL CONFIGURATOR: Sets the supplier for creating new entity instances.
	 * @param newEntitySupplier supplier that creates new entity instances */
	public void setNewEntitySupplier(final Supplier<?> newEntitySupplier) {
		this.newEntitySupplier = newEntitySupplier;
		updateButtonStates();
	}

	/** OPTIONAL CONFIGURATOR: Sets the notification service for user messages.
	 * @param notificationService the notification service to use */
	public void setNotificationService(final CNotificationService notificationService) {
		this.notificationService = notificationService;
	}

	/** OPTIONAL CONFIGURATOR: Sets the callback for refresh operations.
	 * @param refreshCallback callback to execute when refresh is triggered */
	public void setRefreshCallback(final Consumer<?> refreshCallback) {
		this.refreshCallback = refreshCallback;
		updateButtonStates();
	}

	/** OPTIONAL CONFIGURATOR: Sets the callback for save operations. This allows custom save logic with binder validation. By default, uses standard
	 * save logic with entityService. Only use this to override the default behavior.
	 * @param saveCallback callback to execute when save is triggered */
	public void setSaveCallback(final Consumer<?> saveCallback) {
		this.saveCallback = saveCallback;
	}

	/** Sets the current entity value. This is an alias for setCurrentEntity() to match standard Vaadin component patterns.
	 * @param entity the entity to set as current */
	public void setValue(final Object entity) {
		setCurrentEntity(entity);
	}

	/** OPTIONAL CONFIGURATOR: Sets the workflow status relation service for workflow validation.
	 * @param workflowStatusRelationService the workflow status relation service to use */
	public void setWorkflowStatusRelationService(final CWorkflowStatusRelationService workflowStatusRelationService) {
		this.workflowStatusRelationService = workflowStatusRelationService;
	}

	/** Shows an error notification. Uses CNotificationService if available, falls back to direct Vaadin call. */
	private void showErrorNotification(final String message) {
		if (notificationService != null) {
			notificationService.showError(message);
		} else {
			// Fallback to direct Vaadin call if service not injected
			final Notification notification = Notification.show(message);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		}
	}

	/** Shows a success notification. Uses CNotificationService if available, falls back to direct Vaadin call. */
	private void showSuccessNotification(final String message) {
		if (notificationService != null) {
			notificationService.showSuccess(message);
		} else {
			// Fallback to direct Vaadin call if service not injected
			final Notification notification = Notification.show(message);
			notification.setPosition(Notification.Position.BOTTOM_START);
		}
	}

	/** Updates button enabled/disabled states based on current context. */
	private void updateButtonStates() {
		// LOGGER.debug("Updating button states in toolbar for entity.");
		boolean hasEntity = (currentEntity != null);
		boolean hasEntityId = hasEntity && (currentEntity.getId() != null);
		boolean canCreate = (newEntitySupplier != null);
		boolean canRefresh = (refreshCallback != null);
		boolean canSave = hasEntity && (saveCallback != null || entityService != null);
		if (createButton != null) {
			createButton.setEnabled(canCreate);
		}
		if (saveButton != null) {
			saveButton.setEnabled(canSave);
		}
		if (deleteButton != null) {
			deleteButton.setEnabled(hasEntityId);
		}
		if (refreshButton != null) {
			refreshButton.setEnabled(canRefresh);
		}
		// Update workflow status combobox
		if (statusComboBox != null) {
			boolean enabled = false;
			if (currentEntity instanceof IHasStatusAndWorkflow) {
				List<CProjectItemStatus> validStatuses = new ArrayList<>();
				CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
				validStatuses = statusService.getValidNextStatuses((IHasStatusAndWorkflow<?>) currentEntity);
				statusComboBox.setItems(validStatuses);
				statusComboBox.setValue(((CProjectItem<?>) currentEntity).getStatus());
				enabled = true;
			} else {
				statusComboBox.setItems(new ArrayList<>());
				statusComboBox.setValue(null);
			}
			statusComboBox.setEnabled(enabled);
		}
	}
}
