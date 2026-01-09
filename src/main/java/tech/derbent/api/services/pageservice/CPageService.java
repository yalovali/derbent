package tech.derbent.api.services.pageservice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.interfaces.CSelectEvent;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.IHasSelectionNotification;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.ui.component.ICrudToolbarOwnerPage;
import tech.derbent.api.ui.component.basic.CNavigableComboBox;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.base.session.service.ISessionService;

public abstract class CPageService<EntityClass extends CEntityDB<EntityClass>> implements IPageServiceHasStatusAndWorkflow<EntityClass> {

	private static final Pattern HANDLER_PATTERN = Pattern.compile("on_([A-Za-z0-9]+)_([A-Za-z0-9]+)");
	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CPageService.class);

	//
	protected static void on_dragStart(@SuppressWarnings ("unused") CDragDropEvent event) {
		LOGGER.debug("Drag start event received in base CPageService.");
	}

	private CDragStartEvent activeDragStartEvent = null;
	// Custom components registered for method binding (outside of FormBuilder)
	private final Map<String, Component> customComponents = new HashMap<>();
	protected CDetailsBuilder detailsBuilder = null;
	private final Map<String, ComponentEventListener<CDragEndEvent>> dragEndListenerRegistry = new HashMap<>();
	private final Map<String, ComponentEventListener<CDragStartEvent>> dragStartListenerRegistry = new HashMap<>();
	private final Map<String, ComponentEventListener<CDragDropEvent>> dropListenerRegistry = new HashMap<>();
	private EntityClass previousEntity;
	private final Map<String, ComponentEventListener<CSelectEvent>> selectListenerRegistry = new HashMap<>();
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
	@Override
	public void actionChangeStatus(final CProjectItemStatus newStatus) throws Exception {
		try {
			final EntityClass entity = getValue();
			LOGGER.debug("actionChangeStatus triggered: newStatus={}, entityId={}, entityType={}", newStatus != null ? newStatus.getName() : "null",
					entity != null ? entity.getId() : "null", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(entity, "No current entity to change status for.");
			Check.instanceOf(entity, IHasStatusAndWorkflow.class, "Entity must have status implementation");
			LOGGER.debug("Setting new status '{}' for entity ID: {}", newStatus != null ? newStatus.getName() : "null", entity.getId());
			((IHasStatusAndWorkflow<?>) entity).setStatus(newStatus);
			getEntityService().save(entity);
			LOGGER.debug("Entity saved successfully after status change. Triggering refresh...");
			// actionRefresh();
			// actionSave();
			getView().onEntitySaved(entity);
			getView().populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error changing status: {}", e.getMessage(), e);
			throw e;
		}
	}

	public void actionCreate() throws Exception {
		try {
			setPreviousEntity(getValue());
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
			final EntityClass entity = getValue();
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
					actionRefresh();
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
			final EntityClass entity = getValue();
			LOGGER.debug("Refresh action triggered for entity: {}", entity != null ? entity.getId() : "null");
			// Check if current entity is a new unsaved entity (no ID)
			if (entity != null && entity.getId() == null) {
				// Discard the new entity and restore previous selection
				if (previousEntity != null) {
					final CEntityDB<?> reloaded = getEntityService().getById(previousEntity.getId()).orElse(null);
					if (reloaded != null) {
						setValue((EntityClass) reloaded);
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
			final EntityClass entity = getValue();
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
			setValue(savedEntity);
			getView().onEntitySaved(savedEntity);
			getView().populateForm();
			CNotificationService.showSaveSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error saving entity: {}", e.getMessage());
			throw e;
		}
	}

	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CActivity.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			detailsBuilder = getView().getDetailsBuilder();
			bindMethods(this);
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CActivity.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	private void bindComponent(final Method method, final Component component, final String methodName, final String componentName,
			// TODO Check that crudtoolbar is not binded for change events such as status?
			// how about new etc function?
			final String action) {
		// LOGGER.debug("[BindDebug] Starting binding for method {} to component {} for
		// action {}.", methodName, componentName, action);
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
		case "dragStart" -> {
			// Check if component implements IHasDragStart interface first
			if (component instanceof IHasDragControl) {
				// LOGGER.debug("[DragDebug] Component {} implements IHasDragStart, binding via
				// interface", componentName);
				bindDragStart((IHasDragControl) component, method, methodName);
			} else {
				// Fail-fast: dragStart handler defined but component doesn't support it
				throw new IllegalArgumentException(String.format(
						"DragStart action requires IHasDragStart interface or Grid. Component '%s' is %s which doesn't support drag start. "
								+ "Handler method '%s' cannot be bound. Implement IHasDragStart or use Grid.",
						componentName, component.getClass().getSimpleName(), methodName));
			}
		}
		case "dragEnd" -> {
			// Check if component implements IHasDragEnd interface first
			if (component instanceof IHasDragControl) {
				LOGGER.debug("[DragDebug] Component {} implements IHasDragEnd, binding via interface", componentName);
				bindDragEnd((IHasDragControl) component, method, methodName);
			} else {
				// Fail-fast: dragEnd handler defined but component doesn't support it
				throw new IllegalArgumentException(String.format(
						"DragEnd action requires IHasDragEnd interface or Grid. Component '%s' is %s which doesn't support drag end. "
								+ "Handler method '%s' cannot be bound. Implement IHasDragEnd or use Grid.",
						componentName, component.getClass().getSimpleName(), methodName));
			}
		}
		case "drop" -> {
			if (component instanceof IHasDragControl) {
				LOGGER.debug("[DragDebug] Component {} implements IHasDrop, binding drop event", componentName);
				bindIHasDropEvent((IHasDragControl) component, method, methodName);
			} else {
				// Fail-fast: Drop handler defined but component doesn't support drops
				throw new IllegalArgumentException(
						String.format("Drop action requires Grid or IHasDrop interface. Component '%s' is %s which doesn't support drops. "
								+ "Handler method '%s' cannot be bound.", componentName, component.getClass().getSimpleName(), methodName));
			}
		}
		case "selected", "clicked" -> {
			if (component instanceof IHasSelectionNotification) {
				bindSelection((IHasSelectionNotification) component, method, methodName);
			} else {
				throw new IllegalArgumentException(String
						.format("Selection action requires IHasSelectionNotification. Component '%s' is %s which doesn't support selection events. "
								+ "Handler method '%s' cannot be bound.", componentName, component.getClass().getSimpleName(), methodName));
			}
		}
		// add more actions as needed
		default -> Check.warn("Action {" + action + "} not recognized for binding.");
		}
		LOGGER.debug("[BindDebug] Successfully bound method {} to component {} for action {}", methodName, componentName, action);
	}

	private void bindDragEnd(final IHasDragControl component, final Method method, final String methodName) {
		Check.instanceOf(component, Component.class, "Component implementing IHasDragEnd must also extend Component");
		final Component vaadinComponent = (Component) component;
		final String key = component.getClass().getName() + "#DRAG_END#" + methodName;
		final ComponentEventListener<CDragEndEvent> listener = dragEndListenerRegistry.computeIfAbsent(key, k -> {
			LOGGER.debug("[BindDebug] Creating drag-end listener for {}", k);
			return event -> {
				try {
					LOGGER.debug("[DragDebug] Invoking {} on component {}", methodName, component.getClass().getSimpleName());
					method.invoke(this, vaadinComponent, event);
				} catch (final Exception ex) {
					LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage(), ex);
				}
			};
		});
		component.addEventListener_dragEnd(listener);
		LOGGER.debug("[BindDebug] Bound IHasDragEnd event to method {} (cached={})", methodName, dragEndListenerRegistry.containsKey(key));
	}

	@SuppressWarnings ({})
	private void bindDragStart(final IHasDragControl component, final Method method, final String methodName) {
		Check.instanceOf(component, Component.class, "Component implementing IHasDragStart must also extend Component");
		Check.notNull(component, "Component for drag start binding cannot be null");
		Check.notNull(method, "Method for drag start binding cannot be null");
		final Component vaadinComponent = (Component) component;
		final String key = component.getClass().getName() + "#DRAG_START#" + methodName;
		final ComponentEventListener<CDragStartEvent> listener = dragStartListenerRegistry.computeIfAbsent(key, k -> {
			LOGGER.debug("[BindDebug] Creating drag-start listener for {}", k);
			return event -> {
				try {
					method.invoke(this, vaadinComponent, event);
				} catch (final Exception ex) {
					LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage(), ex);
				}
			};
		});
		component.addEventListener_dragStart(listener);
		LOGGER.debug("[BindDebug] Bound IHasDragStart event to method {} (cached={})", methodName, dragStartListenerRegistry.containsKey(key));
	}

	/** Binds a component's drop event to a page service handler method. Supports components implementing IHasDrop interface.
	 * @param component  the component implementing IHasDrop
	 * @param method     the handler method to invoke
	 * @param methodName the name of the handler method */
	private void bindIHasDropEvent(final IHasDragControl component, final Method method, final String methodName) {
		// Aynı component + aynı handler → aynı listener
		final String key = component.getClass().getName() + "#" + methodName;
		final ComponentEventListener<CDragDropEvent> listener = dropListenerRegistry.computeIfAbsent(key, k -> {
			// LOGGER.debug("[BindDebug] Creating new drop listener for {}", k);
			return event -> {
				try {
					// LOGGER.info("[DragDebug] Drop event received, invoking {}", methodName);
					method.invoke(this, component, event);
					// LOGGER.info("[DragDebug] Method {} invoked successfully", method.getName());
				} catch (final Exception ex) {
					LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage(), ex);
				}
			};
		});
		component.addEventListener_dragDrop(listener);
		LOGGER.debug("[BindDebug] Bound IHasDrop drop event to method {} (cached={})", methodName, dropListenerRegistry.containsKey(key));
	}

	public void bindMethods(final CPageService<?> page) {
		Check.notNull(page, "PageService instance must not be null to bind methods.");
		final Map<String, Component> allComponents = getAllComponents();
		// LOGGER.debug("[BindDebug] Total components available for binding: {} - {}",
		// allComponents.size(), allComponents.keySet());
		// Scan for handler methods matching on_{componentName}_{action} pattern
		final var methods = page.getClass().getDeclaredMethods();
		for (final var method : methods) {
			final var matcher = HANDLER_PATTERN.matcher(method.getName());
			if (!matcher.matches()) {
				continue;
			}
			final var componentName = matcher.group(1);
			final var action = matcher.group(2);
			final var component = allComponents.get(componentName);
			if (component == null) {
				LOGGER.warn("[BindDebug] Component '{}' not found for binding method '{}' - Available components: {}", componentName,
						method.getName(), allComponents.keySet());
				continue;
			}
			// LOGGER.debug("[BindDebug] Binding method '{}' to component '{}' (type: {})
			// for action '{}'", method.getName(),
			// componentName,component.getClass().getSimpleName(), action);
			bindComponent(method, component, method.getName(), componentName, action);
		}
	}

	private void bindSelection(final IHasSelectionNotification component, final Method method, final String methodName) {
		final String key = component.getClass().getName() + "#SELECT#" + methodName;
		final ComponentEventListener<CSelectEvent> listener = selectListenerRegistry.computeIfAbsent(key, k -> {
			LOGGER.debug("[BindDebug] Creating select listener for {}", k);
			return event -> {
				try {
					method.invoke(this, component, event);
				} catch (final Exception ex) {
					LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage(), ex);
				}
			};
		});
		component.addEventListener_select(listener);
		LOGGER.debug("[BindDebug] Bound selection event to method {} (cached={})", methodName, selectListenerRegistry.containsKey(key));
	}

	protected CDragStartEvent getActiveDragStartEvent() { return activeDragStartEvent; }

	public Map<String, Component> getAllComponents() {
		// TODO why this always generates a new map? !!!
		// Combine components from detailsBuilder and custom registered components//
		final Map<String, Component> allComponents = new HashMap<>();
		// Get components from detailsBuilder's centralized map if available
		if (detailsBuilder != null && detailsBuilder.getComponentMap() != null) {
			allComponents.putAll(detailsBuilder.getComponentMap());
		}
		// Add custom registered components (these take precedence)
		allComponents.putAll(customComponents);
		return allComponents;
	}

	protected Checkbox getCheckbox(final String fieldName) {
		return getComponent(fieldName, Checkbox.class);
	}

	@SuppressWarnings ("unchecked")
	protected <T> ComboBox<T> getComboBox(final String fieldName) {
		final Component component = getComponentByName(fieldName);
		Check.notNull(component, String.format("Component '%s' not found. Check component registration and field name.", fieldName));
		// Check if it's a CNavigableComboBox (which is a CustomField containing a
		// ComboBox)
		if (component instanceof CNavigableComboBox<?>) {
			return (ComboBox<T>) ((CNavigableComboBox<?>) component).getComboBox();
		}
		// Otherwise check if it's a direct ComboBox
		if (component instanceof ComboBox) {
			return (ComboBox<T>) component;
		}
		// Fail-fast: Component exists but wrong type
		throw new IllegalArgumentException(String.format("Component '%s' is not a ComboBox. Found: %s. Expected ComboBox or CNavigableComboBox.",
				fieldName, component.getClass().getSimpleName()));
	}

	@SuppressWarnings ("unchecked")
	protected <T extends Component> T getComponent(final String fieldName, final Class<T> componentType) {
		final Component component = getComponentByName(fieldName);
		Check.notNull(component, String.format("Component '%s' not found. Check component registration and field name.", fieldName));
		if (!componentType.isInstance(component)) {
			throw new IllegalArgumentException(
					String.format("Component '%s' is of type %s but expected %s. Check component type matches handler usage.", fieldName,
							component.getClass().getSimpleName(), componentType.getSimpleName()));
		}
		return (T) component;
	}

	/** Get a component from the form by its field name.
	 * @param fieldName the name of the field/component
	 * @return the component, or null if not found */
	protected Component getComponentByName(final String fieldName) {
		final Component component = getAllComponents().get(fieldName);
		if (component != null) {
			return component;
		}
		LOGGER.warn("Component '{}' not found in detailsBuilder component map.", fieldName);
		return null;
	}

	@SuppressWarnings ({
			"rawtypes"
	})
	protected Object getComponentValue(final String fieldName) {
		final Component component = getComponentByName(fieldName);
		Check.notNull(component, String.format("Cannot get value: Component '%s' not found. Check component registration.", fieldName));
		if (!(component instanceof HasValue)) {
			throw new IllegalArgumentException(
					String.format("Component '%s' does not have a value (not a HasValue). Found type: %s. Use HasValue components for getValue().",
							fieldName, component.getClass().getSimpleName()));
		}
		return ((HasValue) component).getValue();
	}

	public CProject getCurrentProject() { return getSessionService().getActiveProject().orElse(null); }

	protected DatePicker getDatePicker(final String fieldName) {
		return getComponent(fieldName, DatePicker.class);
	}

	protected Class<?> getEntityClass() { return getView().getEntityClass(); }

	@Override
	public CAbstractService<EntityClass> getEntityService() {
		Check.notNull(getView(), "View is not set in page service");
		return getView().getEntityService();
	}

	public EntityClass getPreviousEntity() { return previousEntity; }

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return CSpringContext.getBean(CProjectItemStatusService.class); }

	protected ISessionService getSessionService() { return getView().getSessionService(); }

	protected TextArea getTextArea(final String fieldName) {
		return getComponent(fieldName, TextArea.class);
	}

	protected TextField getTextField(final String fieldName) {
		return getComponent(fieldName, TextField.class);
	}

	protected EntityClass getValue() { return getView().getValue(); }

	@Override
	public IPageServiceImplementer<EntityClass> getView() { return view; }

	protected void on_dragEnd(@SuppressWarnings ("unused") CDragDropEvent event) {
		setActiveDragStartEvent(null);
	}

	/** Update the save button state based on validation results. This method is called automatically when the name field changes. */
	protected void on_name_change() {
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

	public void populateForm() { /*****/
	}

	/** Registers a custom component for method binding. This allows components that are not part of the entity form to be bound to handler methods
	 * using the on_{componentName}_{action} pattern.
	 * <p>
	 * Example:
	 *
	 * <pre>
	 * CComponentListSprintItems sprintItems = new CComponentListSprintItems(...);
	 * registerComponent("sprintItems", sprintItems.getGrid());
	 * // Now you can create handler methods like:
	 * // public void on_sprintItems_dragStart(Component component, Object value) { ... }
	 * // public void on_sprintItems_drop(Component component, Object value) { ... }
	 * </pre>
	 *
	 * @param name      the name to use in handler method names
	 * @param component the component to register */
	public void registerComponent(final String name, final Component component) {
		Check.notBlank(name, "Component name cannot be blank");
		Check.notNull(component, "Component cannot be null");
		customComponents.put(name, component);
		LOGGER.debug("Registered custom component '{}' of type {}", name, component.getClass().getSimpleName());
	}

	protected void setActiveDragStartEvent(final CDragStartEvent activeDragStartEvent) { this.activeDragStartEvent = activeDragStartEvent; }

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected void setComponentValue(final String fieldName, final Object value) {
		final Component component = getComponentByName(fieldName);
		Check.notNull(component, String.format("Cannot set value: Component '%s' not found. Check component registration.", fieldName));
		Check.instanceOf(component, HasValue.class, String.format("Component '%s' does not support setting value (not a HasValue)", fieldName));
		try {
			((HasValue) component).setValue(value);
		} catch (final Exception e) {
			LOGGER.error("Error setting value for component '{}': {}. Value type: {}, Component type: {}", fieldName, e.getMessage(),
					value != null ? value.getClass().getSimpleName() : "null", component.getClass().getSimpleName());
			throw e;
		}
	}

	public void setPreviousEntity(final EntityClass previousEntity) { this.previousEntity = previousEntity; }

	@Override
	public void setValue(final EntityClass entity) {
		getView().setValue(entity);
	}

	/** Removes a previously registered custom component.
	 * @param name the name of the component to unregister */
	public void unregisterComponent(final String name) {
		customComponents.remove(name);
		LOGGER.debug("Unregistered custom component '{}'", name);
	}

	/** Validate the current entity. Default implementation checks that the name field is not empty. Subclasses can override this method to add
	 * additional validation logic.
	 * @return true if the entity is valid and can be saved, false otherwise */
	protected boolean validateEntitySave() {
		try {
			final TextField nameField = getTextField("name");
			if (nameField != null) {
				final String name = nameField.getValue();
				return name != null && !name.trim().isEmpty();
			}
			// If there's no name field, consider it valid
			return true;
		} catch (final Exception e) {
			LOGGER.debug("Error during entity validation: {}", e.getMessage());
			return true; // Default to valid on error to avoid blocking saves
		}
	}
}
