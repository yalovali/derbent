package tech.derbent.api.services.pageservice;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.ui.component.ICrudToolbarOwnerPage;
import tech.derbent.api.ui.component.basic.CNavigableComboBox;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

public abstract class CPageService<EntityClass extends CEntityDB<EntityClass>> {

	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CPageService.class);
	protected CDetailsBuilder detailsBuilder = null;
	protected CFormBuilder<?> formBuilder = null;
	private EntityClass previousEntity;
	private final IPageServiceImplementer<EntityClass> view;

	public CPageService(final IPageServiceImplementer<EntityClass> view) {
		this.view = view;
		setPreviousEntity(null);
	}

	/** Handle status change action triggered from the CRUD toolbar.
	 * <p>
	 * Default implementation for entities without workflow support. Simply sets the new status on the current entity. Subclasses should override this
	 * method to implement workflow-aware status validation.
	 * @param newStatus the new status selected by the user
	 * @throws Exception if the status change fails */
	public void actionChangeStatus(final CProjectItemStatus newStatus) {
		LOGGER.debug("Base actionChangeStatus called - entity type does not support workflow status changes");
	}

	public void actionCreate() throws Exception {
		try {
			setPreviousEntity(getCurrentEntity());
			final EntityClass newEntity = getEntityService().newEntity();
			getEntityService().initializeNewEntity(newEntity);
			getView().onEntityCreated(newEntity);
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity instance for type: {} - {}", getEntityClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	public void actionDelete() throws Exception {
		try {
			final EntityClass entity = getCurrentEntity();
			if (entity == null || entity.getId() == null) {
				CNotificationService.showWarning("Please select an item to delete.");
				return;
			}
			// Show confirmation dialog
			CNotificationService.showConfirmationDialog("Delete selected item?", () -> {
				try {
					getEntityService().delete(entity.getId());
					LOGGER.info("Entity deleted successfully with ID: {}", entity.getId());
					getView().onEntityDeleted(entity);
				} catch (final Exception ex) {
					CNotificationService.showException("Error deleting entity with ID:" + entity.getId(), ex);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Error during delete action: {}", e.getMessage());
			throw e;
		}
	}

	@SuppressWarnings ("unchecked")
	public void actionRefresh() throws Exception {
		try {
			final EntityClass entity = getCurrentEntity();
			LOGGER.debug("Refresh action triggered for entity: {}", entity != null ? entity.getId() : "null");
			// Check if current entity is a new unsaved entity (no ID)
			if (entity != null && entity.getId() == null) {
				// Discard the new entity and restore previous selection
				if (previousEntity != null) {
					final CEntityDB<?> reloaded = getEntityService().getById(previousEntity.getId()).orElse(null);
					if (reloaded != null) {
						setCurrentEntity((EntityClass) reloaded);
						getView().onEntityRefreshed((EntityClass) reloaded);
					} else {
						// previous entity no longer exists, clear selection
						getView().selectFirstInGrid();
					}
				} else {
					getView().selectFirstInGrid();
				}
				CNotificationService.showInfo("Entity reloaded.");
				return;
			}
			// Normal refresh for existing entities
			if (entity == null) {
				getView().selectFirstInGrid();
				return;
			}
			final CEntityDB<?> reloaded = getEntityService().getById(entity.getId()).orElse(null);
			if (reloaded != null) {
				getView().onEntityRefreshed((EntityClass) reloaded);
			} else {
				getView().selectFirstInGrid();
			}
			CNotificationService.showInfo("Entity reloaded.");
		} catch (final Exception e) {
			LOGGER.error("Error refreshing entity: {}", e.getMessage());
			throw e;
		}
	}

	public void actionSave() throws Exception {
		try {
			final EntityClass entity = getCurrentEntity();
			LOGGER.debug("Save action triggered for entity: {}", entity != null ? entity.getId() : "null");
			if (entity == null) {
				LOGGER.warn("No current entity for save operation");
				CNotificationService.showWarning("No entity selected for save");
				return;
			}
			if (getView().getBinder() != null) {
				getView().getBinder().writeBean(entity);
			}
			final EntityClass savedEntity = getEntityService().save(entity);
			LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
			setCurrentEntity(savedEntity);
			getView().onEntitySaved(savedEntity);
			getView().populateForm();
			CNotificationService.showSaveSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error saving entity: {}", e.getMessage());
			throw e;
		}
	}

	/** Update the save button state based on validation results. This method is called automatically when the name field changes. */
	protected void baselistener_on_name_change() {
		try {
			LOGGER.debug("Validating entity to update save button state.");
			final boolean isSaveValid = validateEntitySave();
			// Try to access the CRUD toolbar from the view
			if (getView() instanceof ICrudToolbarOwnerPage) {
				final ICrudToolbarOwnerPage crudView = (ICrudToolbarOwnerPage) getView();
				final CCrudToolbar toolbar = crudView.getCrudToolbar();
				Check.notNull(toolbar, "CRUD Toolbar must not be null to update save button state.");
				toolbar.setSaveButtonEnabled(isSaveValid);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not update save button state: {}", e.getMessage());
		}
	}

	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CActivity.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			detailsBuilder = getView().getDetailsBuilder();
			if (detailsBuilder != null) {
				formBuilder = detailsBuilder.getFormBuilder();
			}
			bindMethods(this);
			setup_base_listeners();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CActivity.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	private void bindComponent(final Method method, final Component component, final String methodName, final String componentName,
			final String action) {
		LOGGER.debug("Binding method {} to component {} for action {}.", methodName, componentName, action);
		// check method parameters
		final var parameters = method.getParameterTypes();
		Check.isTrue(parameters.length == 2, "Method {" + methodName + "} has invalid number of parameters. Expected 2 (Component, Object).");
		Check.instanceOf(parameters[0], Component.class, "Method {" + methodName + "} has invalid first parameter. Expected Component");
		Check.instanceOf(parameters[1], Object.class, "Method {" + methodName + "} has invalid second parameter. Expected Object");
		if (!method.canAccess(this)) {
			LOGGER.warn("Method {} is not accessible; setting accessible to true.", methodName);
			method.setAccessible(true);
		}
		// bind method to component based on action
		switch (action) {
		case "click" -> {
			if (component instanceof final Button button) {
				button.addClickListener(event -> {
					try {
						method.invoke(this, component, null); // click events don't have values
					} catch (final Exception e) {
						LOGGER.error("Error invoking method {}: {}", methodName, e.getMessage());
					}
				});
			}
		}
		case "change" -> {
			// implement change listener binding if needed
			if (component instanceof final HasValue<?, ?> hasValue) {
				hasValue.addValueChangeListener(event -> {
					try {
						final Object newValue = event.getValue();
						method.invoke(this, component, newValue);
					} catch (final Exception e) {
						LOGGER.error("Error invoking method {}: {}", methodName, e.getMessage());
					}
				});
			}
		}
		case "focus" -> {
			if (component instanceof Focusable) {
				component.getElement().addEventListener("focus", e -> {
					try {
						method.invoke(this, component, null);
					} catch (final Exception ex) {
						LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
					}
				});
			}
		}
		case "blur" -> {
			if (component instanceof Focusable) {
				component.getElement().addEventListener("blur", e -> {
					try {
						method.invoke(this, component, null);
					} catch (final Exception ex) {
						LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
					}
				});
			}
		}
		// add more actions as needed
		default -> Check.fail("Action {" + action + "} not recognized for binding.");
		}
	}

	protected void bindMethods(final CPageService<?> page) {
		Check.notNull(page, "PageService instance must not be null to bind methods.");
		Check.notNull(formBuilder, "FormBuilder must not be null to bind methods.");
		final var components = formBuilder.getComponentMap();
		final var methods = page.getClass().getDeclaredMethods();
		for (final var method : methods) {
			final var methodName = method.getName();
			// use regex to match method names in format on_[componentName]_[action]
			if (!methodName.matches("on_[a-zA-Z0-9]+_[a" + "-zA-Z0-9]+")) {
				continue;
			}
			final var parts = methodName.split("_");
			if (parts.length != 3) {
				continue;
			}
			final var componentName = parts[1];
			final var action = parts[2];
			final var component = components.get(componentName);
			if (component == null) {
				// skip if the component is not found
				// code remains, fields are dynamic
				LOGGER.warn("Component '{}' not found in FormBuilder for binding method '{}'", componentName, methodName);
				continue;
			}
			bindComponent(method, component, methodName, componentName, action);
		}
	}

	protected Checkbox getCheckbox(final String fieldName) {
		return getComponent(fieldName, Checkbox.class);
	}

	@SuppressWarnings ("unchecked")
	protected <T> ComboBox<T> getComboBox(final String fieldName) {
		final Component component = getComponentByName(fieldName);
		if (component == null) {
			LOGGER.warn("Component '{}' not found", fieldName);
			return null;
		}
		// Check if it's a CNavigableComboBox (which is a CustomField containing a ComboBox)
		if (component instanceof CNavigableComboBox<?>) {
			return (ComboBox<T>) ((CNavigableComboBox<?>) component).getComboBox();
		}
		// Otherwise check if it's a direct ComboBox
		if (component instanceof ComboBox) {
			return (ComboBox<T>) component;
		}
		LOGGER.warn("Component '{}' is not a ComboBox (found: {})", fieldName, component.getClass().getSimpleName());
		return null;
	}

	@SuppressWarnings ("unchecked")
	protected <T extends Component> T getComponent(final String fieldName, final Class<T> componentType) {
		final Component component = getComponentByName(fieldName);
		if (component == null) {
			LOGGER.warn("Component '{}' not found", fieldName);
			return null;
		}
		if (!componentType.isInstance(component)) {
			LOGGER.warn("Component '{}' is of type {} but expected {}", fieldName, component.getClass().getSimpleName(),
					componentType.getSimpleName());
			return null;
		}
		return (T) component;
	}

	/** Get a component from the form by its field name.
	 * @param fieldName the name of the field/component
	 * @return the component, or null if not found */
	protected Component getComponentByName(final String fieldName) {
		if (formBuilder == null) {
			LOGGER.warn("FormBuilder is null; cannot retrieve component '{}'", fieldName);
			return null;
		}
		return formBuilder.getComponentMap().get(fieldName);
	}

	@SuppressWarnings ({
			"rawtypes"
	})
	protected Object getComponentValue(final String fieldName) {
		final Component component = getComponentByName(fieldName);
		if (component == null) {
			LOGGER.warn("Cannot get value: Component '{}' not found", fieldName);
			return null;
		}
		if (component instanceof HasValue) {
			return ((HasValue) component).getValue();
		}
		LOGGER.warn("Component '{}' does not have a value (not a HasValue)", fieldName);
		return null;
	}

	protected EntityClass getCurrentEntity() { return getView().getCurrentEntity(); }

	public CProject getCurrentProject() { return getSessionService().getActiveProject().orElse(null); }

	protected DatePicker getDatePicker(final String fieldName) {
		return getComponent(fieldName, DatePicker.class);
	}

	protected Class<?> getEntityClass() { return getView().getEntityClass(); }

	public CAbstractService<EntityClass> getEntityService() {
		Check.notNull(getView(), "View is not set in page service");
		return getView().getChildService();
	}

	public EntityClass getPreviousEntity() { return previousEntity; }

	protected ISessionService getSessionService() { return getView().getSessionService(); }

	protected TextArea getTextArea(final String fieldName) {
		return getComponent(fieldName, TextArea.class);
	}

	protected TextField getTextField(final String fieldName) {
		return getComponent(fieldName, TextField.class);
	}

	public IPageServiceImplementer<EntityClass> getView() { return view; }

	public void populateForm() {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected void setComponentValue(final String fieldName, final Object value) {
		final Component component = getComponentByName(fieldName);
		if (component == null) {
			LOGGER.warn("Cannot set value: Component '{}' not found", fieldName);
			return;
		}
		if (component instanceof HasValue) {
			try {
				((HasValue) component).setValue(value);
			} catch (final Exception e) {
				LOGGER.error("Error setting value for component '{}': {}", fieldName, e.getMessage());
			}
		} else {
			LOGGER.warn("Component '{}' does not support setting value (not a HasValue)", fieldName);
		}
	}

	public void setCurrentEntity(final EntityClass entity) {
		getView().setCurrentEntity(entity);
	}

	public void setPreviousEntity(final EntityClass previousEntity) { this.previousEntity = previousEntity; }

	/** Setup validation for the name field. Automatically disables the save button when the name field is empty. This method is called automatically
	 * during bind(). Subclasses can override validateEntity() to add additional validation logic. */
	protected void setup_base_listeners() {
		try {
			final TextField nameField = getTextField("name");
			if (nameField != null) {
				nameField.addValueChangeListener(event -> baselistener_on_name_change());
				// Also validate on initial load
				baselistener_on_name_change();
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not setup name field validation: {}", e.getMessage());
		}
	}

	/** Validate the current entity. Default implementation checks that the name field is not empty. Subclasses can override this method to add
	 * additional validation logic.
	 * @return true if the entity is valid and can be saved, false otherwise */
	protected boolean validateEntitySave() {
		try {
			final TextField nameField = getTextField("name");
			if (nameField != null) {
				final String name = nameField.getValue();
				return (name != null) && !name.trim().isEmpty();
			}
			// If there's no name field, consider it valid
			return true;
		} catch (final Exception e) {
			LOGGER.debug("Error during entity validation: {}", e.getMessage());
			return true; // Default to valid on error to avoid blocking saves
		}
	}
}
