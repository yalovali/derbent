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
import tech.derbent.api.interfaces.IContentOwner;
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

/** Generic CRUD toolbar component that provides comprehensive Create, Read, Update, Delete, and Refresh functionality for any entity type. Includes
 * proper binding integration, validation, error handling, and update notifications.
 * @param <EntityClass> the entity type this toolbar operates on */
public class CCrudToolbar<EntityClass extends CEntityDB<EntityClass>> extends HorizontalLayout {

	private static CDataProviderResolver dataProviderResolver;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCrudToolbar.class);
	private static final long serialVersionUID = 1L;
	final CEnhancedBinder<?> binder;
	private CButton createButton;
	private EntityClass currentEntity;
	private CButton deleteButton;
	private Function<EntityClass, String> dependencyChecker;
	private final Class<EntityClass> entityClass;
	private final CAbstractService<EntityClass> entityService;
	private Supplier<EntityClass> newEntitySupplier;
	private CNotificationService notificationService; // Optional injection
	private IContentOwner parentPage;
	private CProjectItemStatusService projectItemStatusService; // Optional injection for status
	private CButton refreshButton;
	private Consumer<EntityClass> refreshCallback;
	private CButton saveButton;
	private Consumer<EntityClass> saveCallback;
	private ComboBox<CProjectItemStatus> statusComboBox; // Workflow status selector
	private final List<IEntityUpdateListener> updateListeners = new ArrayList<>();
	private CWorkflowStatusRelationService workflowStatusRelationService; // Optional injection

	public CCrudToolbar(IContentOwner parentPage, final CAbstractService<EntityClass> entityService, final Class<EntityClass> entityClass,
			final CEnhancedBinder<EntityClass> binder) {
		this.entityService = entityService;
		this.entityClass = entityClass;
		this.parentPage = parentPage;
		dataProviderResolver = CSpringContext.getBean(CDataProviderResolver.class);
		this.binder = binder;
		setSpacing(true);
		setPadding(true);
		addClassName("crud-toolbar");
		setWidthFull(); // Make toolbar take full width
		// Automatically set dependency checker from service if it implements IDependencyChecker
		dependencyChecker = entityService::checkDeleteAllowed;
		createToolbarButtons();
		LOGGER.debug("Created CCrudToolbar for entity: {}", entityClass.getSimpleName());
	}

	/** Adds an update listener to be notified of CRUD operations.
	 * @param listener the listener to add */
	public void addUpdateListener(final IEntityUpdateListener listener) {
		if (listener != null && !updateListeners.contains(listener)) {
			updateListeners.add(listener);
		}
	}

	/** Creates all the CRUD toolbar buttons. */
	private void createToolbarButtons() {
		// Create (New) Button
		createButton = CButton.createNewButton("New", e -> handleCreate());
		createButton.getElement().setAttribute("title", "Create new " + entityClass.getSimpleName());
		// Save (Update) Button
		saveButton = CButton.createSaveButton("Save", e -> {
			try {
				handleSave();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		saveButton.getElement().setAttribute("title", "Save current " + entityClass.getSimpleName());
		// Delete Button
		deleteButton = CButton.createDeleteButton("Delete", e -> handleDelete());
		deleteButton.getElement().setAttribute("title", "Delete current " + entityClass.getSimpleName());
		// Refresh Button
		refreshButton = CButton.createTertiary("Refresh", VaadinIcon.REFRESH.create(), e -> handleRefresh());
		refreshButton.getElement().setAttribute("title", "Refresh data");
		// Add basic buttons first
		add(createButton, saveButton, deleteButton, refreshButton);
		// Create workflow status combobox if entity is a CProjectItem
		if (CProjectItem.class.isAssignableFrom(entityClass)) {
			createWorkflowStatusComboBox();
		}
		updateButtonStates();
	}

	/** Creates the workflow status combobox for CProjectItem entities. */
	private void createWorkflowStatusComboBox() {
		try {
			statusComboBox = new CColorAwareComboBox<CProjectItemStatus>(parentPage,
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
	public EntityClass getCurrentEntity() { return currentEntity; }

	/** Gets the list of valid next statuses for the current entity based on its workflow.
	 * @param projectItem the project item entity
	 * @return list of valid next statuses */
	private List<CProjectItemStatus> getValidNextStatuses(final CProjectItem<?> projectItem) {
		final List<CProjectItemStatus> validStatuses = new ArrayList<>();
		if (projectItem == null) {
			return validStatuses;
		}
		validStatuses.add(projectItem.getStatus()); // Always include current status
		final CWorkflowEntity workflow = projectItem.getWorkflow();
		final CProjectItemStatus currentStatus = projectItem.getStatus();
		if (workflow == null || workflowStatusRelationService == null) {
			// No workflow assigned - return all statuses if service available
			if (projectItemStatusService != null) {
				try {
					validStatuses.addAll(projectItemStatusService.findAll());
				} catch (Exception e) {
					LOGGER.error("Error loading all statuses", e);
				}
			}
			return validStatuses;
		}
		try {
			// Get workflow relations to find valid next statuses
			final List<CWorkflowStatusRelation> relations = workflowStatusRelationService.findByWorkflow(workflow);
			if (currentStatus != null) {
				// Find relations where fromStatus matches current status
				relations.stream().filter(r -> r.getFromStatus().getId().equals(currentStatus.getId())).map(CWorkflowStatusRelation::getToStatus)
						.distinct().forEach(validStatuses::add);
			}
			// Always include current status
			if (currentStatus != null && !validStatuses.contains(currentStatus)) {
				validStatuses.add(0, currentStatus);
			}
			// If no valid transitions found, show current status only
			if (validStatuses.isEmpty() && currentStatus != null) {
				validStatuses.add(currentStatus);
			}
		} catch (Exception e) {
			LOGGER.error("Error loading workflow status relations", e);
			// Fallback to showing all statuses
			if (projectItemStatusService != null) {
				try {
					validStatuses.addAll(projectItemStatusService.findAll());
				} catch (Exception ex) {
					LOGGER.error("Error loading all statuses as fallback", ex);
				}
			}
		}
		return validStatuses;
	}

	/** Gets the current entity value. This is an alias for getCurrentEntity() to match standard Vaadin component patterns.
	 * @return the current entity */
	public EntityClass getValue() { return currentEntity; }

	/** Handles the create (new entity) operation. */
	private void handleCreate() {
		try {
			LOGGER.debug("Handling create operation for entity: {}", entityClass.getSimpleName());
			Check.notNull(newEntitySupplier, "New entity supplier is not set");
			// Create new entity
			EntityClass newEntity = newEntitySupplier.get();
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
			showSuccessNotification("New " + entityClass.getSimpleName() + " created. Fill in the details and click Save.");
			// Notify listeners that a new entity was created
			notifyListenersCreated(newEntity);
		} catch (Exception exception) {
			LOGGER.error("Error during create operation for entity: {}", entityClass.getSimpleName(), exception);
			if (notificationService != null) {
				notificationService.showCreateError();
			} else {
				showErrorNotification("An error occurred while creating new entity. Please try again.");
			}
		}
	}

	/** Handles the delete operation with confirmation dialog and proper error handling. */
	private void handleDelete() {
		try {
			LOGGER.debug("Handling delete operation for entity: {}", entityClass.getSimpleName());
			if (currentEntity == null || currentEntity.getId() == null) {
				showErrorNotification("Cannot delete: No entity selected or entity not saved yet.");
				return;
			}
			Check.notNull(dependencyChecker, "Dependency checker function is not set");
			String dependencyError = dependencyChecker.apply(currentEntity);
			if (dependencyError != null) {
				showErrorNotification(dependencyError);
				return;
			}
			// Show confirmation dialog
			CConfirmationDialog confirmDialog =
					new CConfirmationDialog("Are you sure you want to delete this " + entityClass.getSimpleName() + "?", this::performDelete);
			confirmDialog.open();
		} catch (Exception exception) {
			LOGGER.error("Error during delete operation for entity: {}", entityClass.getSimpleName(), exception);
			if (notificationService != null) {
				notificationService.showDeleteError();
			} else {
				showErrorNotification("An error occurred while attempting to delete the entity. Please try again.");
			}
		}
	}

	/** Handles the refresh operation. */
	private void handleRefresh() {
		try {
			LOGGER.debug("Handling refresh operation for entity: {}", entityClass.getSimpleName());
			Check.notNull(refreshCallback, "Refresh callback is not set");
			refreshCallback.accept(currentEntity);
			showSuccessNotification("Data refreshed successfully");
		} catch (Exception exception) {
			LOGGER.error("Error during delete operation for entity: {}", entityClass.getSimpleName(), exception);
			if (notificationService != null) {
				notificationService.showDeleteError();
			} else {
				showErrorNotification("An error occurred while attempting to delete the entity. Please try again.");
			}
		}
	}

	/** Handles the save (update) operation with proper validation, error handling, and notifications.
	 * @throws Exception */
	private void handleSave() throws Exception {
		try {
			LOGGER.debug("Attempting to save entity: {}", entityClass.getSimpleName());
			if (currentEntity == null) {
				showErrorNotification("Cannot save: No entity selected.");
				return;
			}
			// Check if save is allowed (validation)
			final String saveError = entityService.checkSaveAllowed(currentEntity);
			if (saveError != null) {
				showErrorNotification(saveError);
				return;
			}
			// Use custom save callback if provided (for integration with binder validation)
			if (saveCallback != null) {
				saveCallback.accept(currentEntity);
			} else {
				// Default save behavior
				final EntityClass savedEntity = entityService.save(currentEntity);
				currentEntity = savedEntity;
				updateButtonStates();
				showSuccessNotification("Data saved successfully");
				// Notify listeners
				notifyListenersSaved(savedEntity);
			}
		} catch (Exception e) {
			LOGGER.error("Error during save operation for entity: {}", entityClass.getSimpleName(), e);
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
			// Validate workflow transition
			final CWorkflowEntity workflow = projectItem.getWorkflow();
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
				entityService.save(currentEntity);
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
	private void notifyListenersCreated(final EntityClass entity) {
		LOGGER.debug("Notifying listeners of entity creation: {}", entityClass.getSimpleName());
		updateListeners.forEach(listener -> {
			try {
				listener.onEntityCreated(entity);
			} catch (final Exception e) {
				LOGGER.error("Error notifying listener of entity creation", e);
				e.printStackTrace();
			}
		});
	}

	/** Notifies all listeners that an entity was deleted. */
	private void notifyListenersDeleted(final EntityClass entity) {
		LOGGER.debug("Notifying listeners of entity deletion: {}", entityClass.getSimpleName());
		updateListeners.forEach(listener -> {
			try {
				listener.onEntityDeleted(entity);
			} catch (final Exception e) {
				LOGGER.error("Error notifying listener of entity deletion", e);
				e.printStackTrace();
			}
		});
	}

	/** Notifies all listeners that an entity was saved. */
	private void notifyListenersSaved(final EntityClass entity) {
		LOGGER.debug("Notifying listeners of entity save: {}", entityClass.getSimpleName());
		updateListeners.forEach(listener -> {
			try {
				listener.onEntitySaved(entity);
			} catch (final Exception e) {
				LOGGER.error("Error notifying listener of entity save", e);
				e.printStackTrace();
			}
		});
	}

	/** Performs the actual delete operation after confirmation. */
	private void performDelete() {
		try {
			LOGGER.debug("Performing delete operation for entity: {}", entityClass.getSimpleName());
			EntityClass entityToDelete = currentEntity;
			entityService.delete(currentEntity);
			LOGGER.info("Entity deleted successfully: {} with ID: {}", entityClass.getSimpleName(), entityToDelete.getId());
			// Clear current entity
			currentEntity = null;
			// Clear the binder to reset the form
			if (binder != null) {
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<EntityClass> typedBinder = (CEnhancedBinder<EntityClass>) binder;
				typedBinder.setBean(null);
			}
			updateButtonStates();
			showSuccessNotification("Entity deleted successfully");
			// Notify listeners
			notifyListenersDeleted(entityToDelete);
		} catch (final Exception e) {
			LOGGER.error("Error during delete operation for entity: {}", entityClass.getSimpleName(), e);
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
		currentEntity = (EntityClass) entity;
		// Automatically set dependency checker from service when entity changes
		if (entityService != null) {
			dependencyChecker = entityService::checkDeleteAllowed;
		}
		updateButtonStates();
	}

	/** Sets the dependency checker function that returns error message if entity cannot be deleted.
	 * @param dependencyChecker function that returns null if entity can be deleted, or error message if it cannot */
	public void setDependencyChecker(final Function<EntityClass, String> dependencyChecker) {
		this.dependencyChecker = dependencyChecker;
	}

	/** Sets the supplier for creating new entity instances.
	 * @param newEntitySupplier supplier that creates new entity instances */
	public void setNewEntitySupplier(final Supplier<EntityClass> newEntitySupplier) {
		this.newEntitySupplier = newEntitySupplier;
		updateButtonStates();
	}

	/** Sets the notification service. This is typically called via dependency injection or manually after construction. */
	public void setNotificationService(final CNotificationService notificationService) {
		this.notificationService = notificationService;
	}

	/** Sets the project item status service for workflow status management. */
	public void setProjectItemStatusService(final CProjectItemStatusService projectItemStatusService) {
		this.projectItemStatusService = projectItemStatusService;
	}

	/** Sets the callback for refresh operations.
	 * @param refreshCallback callback to execute when refresh is triggered */
	public void setRefreshCallback(final Consumer<EntityClass> refreshCallback) {
		this.refreshCallback = refreshCallback;
		updateButtonStates();
	}

	/** Sets the callback for save operations. This allows custom save logic with binder validation.
	 * @param saveCallback callback to execute when save is triggered */
	public void setSaveCallback(final Consumer<EntityClass> saveCallback) {
		this.saveCallback = saveCallback;
	}

	/** Sets the current entity value. This is an alias for setCurrentEntity() to match standard Vaadin component patterns.
	 * @param entity the entity to set as current */
	public void setValue(final EntityClass entity) {
		setCurrentEntity(entity);
	}

	/** Sets the workflow status relation service for workflow validation. */
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
			if (currentEntity instanceof CProjectItem) {
				List<CProjectItemStatus> validStatuses = new ArrayList<>();
				validStatuses = getValidNextStatuses((CProjectItem<?>) currentEntity);
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
