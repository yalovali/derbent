package tech.derbent.abstracts.components;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.ValidationException;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CEntityUpdateListener;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.base.ui.dialogs.CWarningDialog;

/** Generic save toolbar component that provides save and delete functionality for any entity. Includes proper binding integration, validation, error
 * handling, and update notifications.
 * @param <EntityClass> the entity type this toolbar operates on */
public class CSaveToolbar<EntityClass extends CEntityDB<EntityClass>> extends HorizontalLayout {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSaveToolbar.class);
	private final CEnhancedBinder<EntityClass> binder;
	private final CAbstractService<EntityClass> entityService;
	private final List<CEntityUpdateListener> updateListeners = new ArrayList<>();
	private EntityClass currentEntity;
	private final Class<EntityClass> entityClass;

	/** Creates a generic save toolbar.
	 * @param binder        the binder for form validation and data binding
	 * @param entityService the service for save/delete operations
	 * @param entityClass   the entity class type
	 * @param currentEntity the current entity being edited */
	public CSaveToolbar(final CEnhancedBinder<EntityClass> binder, final CAbstractService<EntityClass> entityService,
			final Class<EntityClass> entityClass, final EntityClass currentEntity) {
		this.binder = binder;
		this.entityService = entityService;
		this.entityClass = entityClass;
		this.currentEntity = currentEntity;
		setSpacing(true);
		setPadding(true);
		createToolbarButtons();
		LOGGER.debug("Created CSaveToolbar for entity type: {}", entityClass.getSimpleName());
	}

	/** Adds an update listener to be notified of save/delete operations.
	 * @param listener the listener to add */
	public void addUpdateListener(final CEntityUpdateListener listener) {
		if (listener != null && !updateListeners.contains(listener)) {
			updateListeners.add(listener);
		}
	}

	/** Creates the save and delete buttons for the toolbar. */
	private void createToolbarButtons() {
		final CButton saveButton = CButton.createPrimary("Save", null, e -> handleSave());
		final CButton deleteButton = CButton.createError("Delete", null, e -> handleDelete());
		// Only show delete button if entity has an ID (exists in database)
		if (currentEntity != null && currentEntity.getId() != null) {
			add(saveButton, deleteButton);
		} else {
			add(saveButton);
		}
	}

	/** Handles the delete operation with proper error handling and notifications. */
	private void handleDelete() {
		if (currentEntity == null || currentEntity.getId() == null) {
			showErrorNotification("Cannot delete: No entity selected or entity not saved yet.");
			return;
		}
		try {
			LOGGER.debug("Delete button clicked for entity: {} with ID: {}", entityClass.getSimpleName(), currentEntity.getId());
			// Confirm deletion (you could add a confirmation dialog here if needed)
			entityService.delete(currentEntity);
			LOGGER.info("Entity deleted successfully: {} with ID: {}", entityClass.getSimpleName(), currentEntity.getId());
			showSuccessNotification("Entity deleted successfully");
			// Notify listeners
			notifyListenersDeleted(currentEntity);
		} catch (final Exception exception) {
			LOGGER.error("Error during delete operation for entity: {}", entityClass.getSimpleName(), exception);
			showErrorNotification("An error occurred while deleting. Please try again.");
		}
	}

	/** Handles the save operation with proper validation, error handling, and notifications. */
	private void handleSave() {
		try {
			LOGGER.debug("Save button clicked for entity: {}", entityClass.getSimpleName());
			// Ensure we have an entity to save
			if (currentEntity == null) {
				LOGGER.debug("No current entity, creating new one");
				currentEntity = createNewEntity();
			}
			// Write form data to entity (this will validate)
			binder.writeBean(currentEntity);
			// Save entity
			final EntityClass savedEntity = entityService.save(currentEntity);
			LOGGER.info("Entity saved successfully: {} with ID: {}", entityClass.getSimpleName(), savedEntity.getId());
			// Update current entity reference
			currentEntity = savedEntity;
			showSuccessNotification("Data saved successfully");
			// Notify listeners
			notifyListenersSaved(savedEntity);
		} catch (final ObjectOptimisticLockingFailureException exception) {
			LOGGER.error("Optimistic locking failure during save", exception);
			showErrorNotification("Error updating the data. Somebody else has updated the record while you were making changes.");
		} catch (final ValidationException validationException) {
			LOGGER.error("Validation error during save", validationException);
			handleValidationError(validationException);
		} catch (final Exception exception) {
			LOGGER.error("Unexpected error during save operation for entity: {}", entityClass.getSimpleName(), exception);
			showErrorNotification("An unexpected error occurred while saving. Please try again.");
		}
	}

	/** Handles validation errors with enhanced error reporting if available. */
	private void handleValidationError(final ValidationException validationException) {
		if (binder.hasValidationErrors()) {
			LOGGER.error("Detailed validation errors:");
			LOGGER.error(binder.getFormattedErrorSummary());
			// Show detailed error information
			final StringBuilder errorMessage = new StringBuilder("Failed to save the data. Please check:\n");
			binder.getFieldsWithErrors()
					.forEach(field -> errorMessage.append("• ").append(field).append(": ").append(binder.getFieldError(field)).append("\n"));
			new CWarningDialog(errorMessage.toString()).open();
		} else {
			new CWarningDialog("Failed to save the data. Please check that all required fields are filled and values are valid.").open();
		}
	}

	/** Creates a new entity instance using reflection. */
	private EntityClass createNewEntity() {
		try {
			return entityClass.getDeclaredConstructor().newInstance();
		} catch (final Exception e) {
			LOGGER.error("Failed to create new entity instance for: {}", entityClass.getSimpleName(), e);
			throw new RuntimeException("Failed to create new entity instance", e);
		}
	}

	/** Notifies all listeners that an entity was deleted. */
	private void notifyListenersDeleted(final EntityClass entity) {
		updateListeners.forEach(listener -> {
			try {
				listener.onEntityDeleted(entity);
			} catch (final Exception e) {
				LOGGER.warn("Error notifying listener of entity deletion", e);
			}
		});
	}

	/** Notifies all listeners that an entity was saved. */
	private void notifyListenersSaved(final EntityClass entity) {
		updateListeners.forEach(listener -> {
			try {
				listener.onEntitySaved(entity);
			} catch (final Exception e) {
				LOGGER.warn("Error notifying listener of entity save", e);
			}
		});
	}

	/** Removes an update listener.
	 * @param listener the listener to remove */
	public void removeUpdateListener(final CEntityUpdateListener listener) {
		updateListeners.remove(listener);
	}

	/** Updates the current entity reference.
	 * @param entity the new current entity */
	public void setCurrentEntity(final EntityClass entity) {
		this.currentEntity = entity;
		// Recreate buttons to show/hide delete button based on entity state
		removeAll();
		createToolbarButtons();
	}

	/** Shows an error notification. */
	private void showErrorNotification(final String message) {
		try {
			final Notification notification = Notification.show(message);
			notification.setPosition(Notification.Position.MIDDLE);
			notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		} catch (final Exception e) {
			LOGGER.warn("Error showing error notification '{}': {}", message, e.getMessage());
		}
	}

	/** Shows a success notification. */
	private void showSuccessNotification(final String message) {
		try {
			final Notification notification = Notification.show(message);
			notification.setPosition(Notification.Position.BOTTOM_START);
		} catch (final Exception e) {
			LOGGER.warn("Error showing notification '{}': {}", message, e.getMessage());
		}
	}
}
