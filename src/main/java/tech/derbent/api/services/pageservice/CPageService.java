package tech.derbent.api.services.pageservice;

import java.lang.reflect.Method;
import org.slf4j.Logger;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.base.session.service.ISessionService;

public abstract class CPageService<EntityClass extends CEntityDB<EntityClass>> {
	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CPageService.class);
	private EntityClass previousEntity;
	final protected IPageServiceImplementer<EntityClass> view;
	protected CDetailsBuilder detailsBuilder = null;
	protected CFormBuilder<?> formBuilder = null;

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
	public void actionChangeStatus(final CProjectItemStatus newStatus) throws Exception {
		LOGGER.debug("Base actionChangeStatus called - entity type does not support workflow status changes");
	}

	public void actionCreate() throws Exception {
		try {
			setPreviousEntity(getCurrentEntity());
			final EntityClass newEntity = getEntityService().newEntity();
			getEntityService().initializeNewEntity(newEntity);
			view.onEntityCreated(newEntity);
		} catch (final Exception e) {
			LOGGER.error("Error creating new entity instance for type: {} - {}", getEntityClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	public void actionDelete() throws Exception {
		try {
			final EntityClass entity = getCurrentEntity();
			if ((entity == null) || (entity.getId() == null)) {
				CNotificationService.showWarning("Please select an item to delete.");
				return;
			}
			// Show confirmation dialog
			CNotificationService.showConfirmationDialog("Delete selected item?", () -> {
				try {
					getEntityService().delete(entity.getId());
					LOGGER.info("Entity deleted successfully with ID: {}", entity.getId());
					view.onEntityDeleted(entity);
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
			if ((entity != null) && (entity.getId() == null)) {
				// Discard the new entity and restore previous selection
				if (previousEntity != null) {
					final CEntityDB<?> reloaded = getEntityService().getById(previousEntity.getId()).orElse(null);
					if (reloaded != null) {
						setCurrentEntity((EntityClass) reloaded);
						view.onEntityRefreshed((EntityClass) reloaded);
					} else {
						// previous entity no longer exists, clear selection
						view.selectFirstInGrid();
					}
				} else {
					view.selectFirstInGrid();
				}
				CNotificationService.showInfo("Entity reloaded.");
				return;
			}
			// Normal refresh for existing entities
			if (entity == null) {
				view.selectFirstInGrid();
				return;
			}
			final CEntityDB<?> reloaded = getEntityService().getById(entity.getId()).orElse(null);
			if (reloaded != null) {
				view.onEntityRefreshed((EntityClass) reloaded);
			} else {
				view.selectFirstInGrid();
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
			if (view.getBinder() != null) {
				view.getBinder().writeBean(entity);
			}
			final EntityClass savedEntity = getEntityService().save(entity);
			LOGGER.info("Entity saved successfully with ID: {}", savedEntity.getId());
			setCurrentEntity(savedEntity);
			view.onEntitySaved(savedEntity);
			view.populateForm();
			CNotificationService.showSaveSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error saving entity: {}", e.getMessage());
			throw e;
		}
	}

	public void bind() {}

	private void bindComponent(final Method method, final Component component, final String methodName, final String componentName,
			final String action) {
		LOGGER.debug("Binding method {} to component {} for action {}.", methodName, componentName, action);
		// Check method signature to determine if it accepts parameters
		final int paramCount = method.getParameterCount();
		final boolean acceptsParams = paramCount >= 1;
		
		// bind method to component based on action
		switch (action) {
		case "click" -> {
			if (component instanceof final Button button) {
				button.addClickListener(event -> {
					try {
						if (acceptsParams && paramCount == 1) {
							method.invoke(this, component);
						} else if (acceptsParams && paramCount >= 2) {
							method.invoke(this, component, null); // click events don't have values
						} else {
							method.invoke(this);
						}
					} catch (final Exception e) {
						LOGGER.error("Error invoking method {}: {}", methodName, e.getMessage());
					}
				});
			}
		}
		case "change", "changed" -> {
			// implement change listener binding if needed
			if (component instanceof final HasValue<?, ?> hasValue) {
				hasValue.addValueChangeListener(event -> {
					try {
						final Object newValue = event.getValue();
						if (acceptsParams && paramCount == 1) {
							method.invoke(this, component);
						} else if (acceptsParams && paramCount >= 2) {
							method.invoke(this, component, newValue);
						} else {
							method.invoke(this);
						}
					} catch (final Exception e) {
						LOGGER.error("Error invoking method {}: {}", methodName, e.getMessage());
					}
				});
			}
		}
		case "focus" -> {
			if (component instanceof com.vaadin.flow.component.Focusable) {
				component.getElement().addEventListener("focus", e -> {
					try {
						if (acceptsParams && paramCount == 1) {
							method.invoke(this, component);
						} else if (acceptsParams && paramCount >= 2) {
							method.invoke(this, component, null);
						} else {
							method.invoke(this);
						}
					} catch (final Exception ex) {
						LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
					}
				});
			}
		}
		case "blur" -> {
			if (component instanceof com.vaadin.flow.component.Focusable) {
				component.getElement().addEventListener("blur", e -> {
					try {
						if (acceptsParams && paramCount == 1) {
							method.invoke(this, component);
						} else if (acceptsParams && paramCount >= 2) {
							method.invoke(this, component, null);
						} else {
							method.invoke(this);
						}
					} catch (final Exception ex) {
						LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
					}
				});
			}
		}
		// add more actions as needed
		default -> LOGGER.warn("Action {} not recognized for binding.", action);
		}
	}

	protected void bindMethods(final CPageService<?> page) {
		if (formBuilder == null) {
			LOGGER.warn("FormBuilder is null; cannot bind methods.");
			return;
		}
		// get the list of components in the formbuilder
		final var components = formBuilder.getComponentMap();
		// print component names
		for (final var entry : components.entrySet()) {
			LOGGER.debug("Component name: {}", entry.getKey());
		}
		// print methods of this class which are in format on_[componentName]_[action]
		// actions are like click, change etc
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
			Check.notNull(component, "Component " + componentName + " not found in FormBuilder.");
			bindComponent(method, component, methodName, componentName, action);
		}
	}

	protected EntityClass getCurrentEntity() { return view.getCurrentEntity(); }

	protected Class<?> getEntityClass() { return view.getEntityClass(); }

	protected CAbstractService<EntityClass> getEntityService() {
		Check.notNull(view, "View is not set in page service");
		return view.getEntityService();
	}

	public EntityClass getPreviousEntity() { return previousEntity; }

	protected ISessionService getSessionService() { return view.getSessionService(); }
	
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
	
	/** Get a typed component from the form by its field name.
	 * @param <T> the expected component type
	 * @param fieldName the name of the field/component
	 * @param componentType the expected class of the component
	 * @return the typed component, or null if not found or wrong type */
	@SuppressWarnings("unchecked")
	protected <T extends Component> T getComponent(final String fieldName, final Class<T> componentType) {
		final Component component = getComponentByName(fieldName);
		if (component == null) {
			LOGGER.warn("Component '{}' not found", fieldName);
			return null;
		}
		if (!componentType.isInstance(component)) {
			LOGGER.warn("Component '{}' is of type {} but expected {}", 
					fieldName, component.getClass().getSimpleName(), componentType.getSimpleName());
			return null;
		}
		return (T) component;
	}
	
	/** Get a TextField component by field name.
	 * @param fieldName the name of the field
	 * @return the TextField component, or null if not found or wrong type */
	protected com.vaadin.flow.component.textfield.TextField getTextField(final String fieldName) {
		return getComponent(fieldName, com.vaadin.flow.component.textfield.TextField.class);
	}
	
	/** Get a TextArea component by field name.
	 * @param fieldName the name of the field
	 * @return the TextArea component, or null if not found or wrong type */
	protected com.vaadin.flow.component.textfield.TextArea getTextArea(final String fieldName) {
		return getComponent(fieldName, com.vaadin.flow.component.textfield.TextArea.class);
	}
	
	/** Get a ComboBox component by field name.
	 * @param <T> the type of items in the ComboBox
	 * @param fieldName the name of the field
	 * @return the ComboBox component, or null if not found or wrong type */
	@SuppressWarnings("unchecked")
	protected <T> com.vaadin.flow.component.combobox.ComboBox<T> getComboBox(final String fieldName) {
		final Component component = getComponentByName(fieldName);
		if (component == null) {
			LOGGER.warn("Component '{}' not found", fieldName);
			return null;
		}
		// Check if it's a CNavigableComboBox (which is a CustomField containing a ComboBox)
		if (component instanceof tech.derbent.api.components.CNavigableComboBox<?>) {
			return (com.vaadin.flow.component.combobox.ComboBox<T>) ((tech.derbent.api.components.CNavigableComboBox<?>) component).getComboBox();
		}
		// Otherwise check if it's a direct ComboBox
		if (component instanceof com.vaadin.flow.component.combobox.ComboBox) {
			return (com.vaadin.flow.component.combobox.ComboBox<T>) component;
		}
		LOGGER.warn("Component '{}' is not a ComboBox (found: {})", fieldName, component.getClass().getSimpleName());
		return null;
	}
	
	/** Get a Checkbox component by field name.
	 * @param fieldName the name of the field
	 * @return the Checkbox component, or null if not found or wrong type */
	protected com.vaadin.flow.component.checkbox.Checkbox getCheckbox(final String fieldName) {
		return getComponent(fieldName, com.vaadin.flow.component.checkbox.Checkbox.class);
	}
	
	/** Get a DatePicker component by field name.
	 * @param fieldName the name of the field
	 * @return the DatePicker component, or null if not found or wrong type */
	protected com.vaadin.flow.component.datepicker.DatePicker getDatePicker(final String fieldName) {
		return getComponent(fieldName, com.vaadin.flow.component.datepicker.DatePicker.class);
	}
	
	/** Get the current value of a component by field name.
	 * @param fieldName the name of the field
	 * @return the current value, or null if component not found or has no value */
	@SuppressWarnings({"unchecked", "rawtypes"})
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
	
	/** Set the value of a component by field name.
	 * @param fieldName the name of the field
	 * @param value the value to set */
	@SuppressWarnings({"unchecked", "rawtypes"})
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

	protected void setCurrentEntity(final EntityClass entity) {
		view.setCurrentEntity(entity);
	}

	public void setPreviousEntity(final EntityClass previousEntity) { this.previousEntity = previousEntity; }
}
