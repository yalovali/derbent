package tech.derbent.api.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

/** Generic CRUD toolbar component that provides comprehensive Create, Read, Update, Delete, and Refresh functionality for any entity type. Includes
 * proper binding integration, validation, error handling, and update notifications.
 * @param <EntityClass> the entity type this toolbar operates on */
public class CCrudToolbar<EntityClass extends CEntityDB<EntityClass>> extends HorizontalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCrudToolbar.class);
	private static final long serialVersionUID = 1L;
	private final CEnhancedBinder<EntityClass> binder;
	private CButton createButton;
	private EntityClass currentEntity;
	private CButton deleteButton;
	private Function<EntityClass, String> dependencyChecker;
	private final Class<EntityClass> entityClass;
	private final CAbstractService<EntityClass> entityService;
	private Supplier<EntityClass> newEntitySupplier;
	private CNotificationService notificationService; // Optional injection
	private CButton refreshButton;
	private Consumer<EntityClass> refreshCallback;
	private CButton saveButton;
	private final List<IEntityUpdateListener> updateListeners = new ArrayList<>();

	/** Creates a comprehensive CRUD toolbar.
	 * @param binder        the binder for form validation and data binding
	 * @param entityService the service for CRUD operations
	 * @param entityClass   the entity class type */
	public CCrudToolbar(final CEnhancedBinder<EntityClass> binder, final CAbstractService<EntityClass> entityService,
			final Class<EntityClass> entityClass) {
		this.binder = binder;
		this.entityService = entityService;
		this.entityClass = entityClass;
		setSpacing(true);
		setPadding(true);
		addClassName("crud-toolbar");
		setWidthFull(); // Make toolbar take full width
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
		createButton = CButton.createPrimary("New", VaadinIcon.PLUS.create(), e -> handleCreate());
		createButton.getElement().setAttribute("title", "Create new " + entityClass.getSimpleName());
		// Save (Update) Button
		saveButton = CButton.createPrimary("Save", VaadinIcon.CHECK.create(), e -> {
			try {
				handleSave();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		saveButton.getElement().setAttribute("title", "Save current " + entityClass.getSimpleName());
		// Delete Button
		deleteButton = CButton.createError("Delete", VaadinIcon.TRASH.create(), e -> handleDelete());
		deleteButton.getElement().setAttribute("title", "Delete current " + entityClass.getSimpleName());
		// Refresh Button
		refreshButton = CButton.createTertiary("Refresh", VaadinIcon.REFRESH.create(), e -> handleRefresh());
		refreshButton.getElement().setAttribute("title", "Refresh data");
		add(createButton, saveButton, deleteButton, refreshButton);
		updateButtonStates();
	}

	/** Gets the current entity.
	 * @return the current entity */
	public EntityClass getCurrentEntity() { return currentEntity; }

	/** Handles the create (new entity) operation. */
	private void handleCreate() {
		Check.notNull(newEntitySupplier, "New entity supplier is not set");
		try {
			// Create new entity
			EntityClass newEntity = newEntitySupplier.get();
			Check.notNull(newEntity, "New entity supplier returned null");
			// Set as current entity and bind to form
			setCurrentEntity(newEntity);
			showSuccessNotification("New " + entityClass.getSimpleName() + " created. Fill in the details and click Save.");
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
		Check.notNull(refreshCallback, "Refresh callback is not set");
		try {
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
			if (currentEntity == null) {
				showErrorNotification("Cannot save: No entity selected.");
				return;
			}
			binder.writeBean(currentEntity);
			// Save entity
			final EntityClass savedEntity = entityService.save(currentEntity);
			currentEntity = savedEntity;
			// Re-bind the saved entity to refresh the form with updated data (like generated IDs, timestamps)
			binder.setBean(savedEntity);
			updateButtonStates();
			showSuccessNotification("Data saved successfully");
			// Notify listeners
			notifyListenersSaved(savedEntity);
		} catch (Exception e) {
			LOGGER.error("Error during save operation for entity: {}", entityClass.getSimpleName(), e);
			if (notificationService != null) {
				notificationService.showDeleteError();
			} else {
				showErrorNotification("An error occurred while attempting to delete the entity. Please try again.");
			}
		}
	}

	/** Notifies all listeners that an entity was deleted. */
	private void notifyListenersDeleted(final EntityClass entity) {
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
			EntityClass entityToDelete = currentEntity;
			entityService.delete(currentEntity);
			LOGGER.info("Entity deleted successfully: {} with ID: {}", entityClass.getSimpleName(), entityToDelete.getId());
			// Clear current entity
			currentEntity = null;
			if (binder != null) {
				binder.setBean(null);
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
		currentEntity = (EntityClass) entity;
		updateButtonStates();
		// Bind the entity to the form if available
		Check.notNull(entityClass, "Entity class is not set");
		try {
			binder.setBean((EntityClass) entity);
		} catch (Exception e) {
			LOGGER.error("Error binding entity to form: {}", e.getMessage());
			throw e;
		}
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

	/** Sets the callback for refresh operations.
	 * @param refreshCallback callback to execute when refresh is triggered */
	public void setRefreshCallback(final Consumer<EntityClass> refreshCallback) {
		this.refreshCallback = refreshCallback;
		updateButtonStates();
	}

	/** Sets the notification service. This is typically called via dependency injection or manually after construction. */
	public void setNotificationService(final CNotificationService notificationService) {
		this.notificationService = notificationService;
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
		boolean hasEntity = (currentEntity != null);
		boolean hasEntityId = hasEntity && (currentEntity.getId() != null);
		boolean canCreate = (newEntitySupplier != null);
		boolean canRefresh = (refreshCallback != null);
		if (createButton != null) {
			createButton.setEnabled(canCreate);
		}
		if (saveButton != null) {
			saveButton.setEnabled(hasEntity);
		}
		if (deleteButton != null) {
			deleteButton.setEnabled(hasEntityId);
		}
		if (refreshButton != null) {
			refreshButton.setEnabled(canRefresh);
		}
	}
}
