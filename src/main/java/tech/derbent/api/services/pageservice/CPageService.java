package tech.derbent.api.services.pageservice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.interfaces.IHasDragEnd;
import tech.derbent.api.interfaces.IHasDragStart;
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

	private static final Pattern HANDLER_PATTERN = Pattern.compile("on_([A-Za-z0-9]+)_([A-Za-z0-9]+)");
	private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(CPageService.class);
	// Custom components registered for method binding (outside of FormBuilder)
	private final Map<String, Component> customComponents = new HashMap<>();
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
		case "dragStart" -> {
			// Check if component implements IHasDragStart interface first
			if (component instanceof IHasDragStart<?>) {
				bindDragStart((IHasDragStart<?>) component, method, methodName);
			} else if (component instanceof Grid<?>) {
				// Fallback to direct Grid binding for backward compatibility
				bindGridDragStart((Grid<?>) component, method, methodName);
			} else {
				// Fallback to generic DOM event listener
				component.getElement().addEventListener("dragstart", e -> {
					try {
						method.invoke(this, component, null);
					} catch (final Exception ex) {
						LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
					}
				});
			}
		}
		case "dragEnd" -> {
			// Check if component implements IHasDragEnd interface first
			if (component instanceof IHasDragEnd<?>) {
				bindDragEnd((IHasDragEnd<?>) component, method, methodName);
			} else if (component instanceof Grid<?>) {
				// Fallback to direct Grid binding for backward compatibility
				bindGridDragEnd((Grid<?>) component, method, methodName);
			} else {
				// Fallback to generic DOM event listener
				component.getElement().addEventListener("dragend", e -> {
					try {
						method.invoke(this, component, null);
					} catch (final Exception ex) {
						LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
					}
				});
			}
		}
		case "drop" -> {
			// Drop event is only supported for Grid components
			if (component instanceof Grid<?>) {
				bindGridDrop((Grid<?>) component, method, methodName);
			} else {
				LOGGER.warn("Drop action is only supported for Grid components, component {} is {}", componentName,
						component.getClass().getSimpleName());
			}
		}
		// add more actions as needed
		default -> Check.warn("Action {" + action + "} not recognized for binding.");
		}
	}

	/** Binds a component's drag end event to a page service handler method. This method supports any component implementing IHasDragEnd interface.
	 * Note: GridDragEndEvent doesn't provide dragged items, so we pass an empty event.
	 * @param component  the component implementing IHasDragEnd
	 * @param method     the handler method to invoke
	 * @param methodName the name of the handler method */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void bindDragEnd(final IHasDragEnd<?> component, final Method method, final String methodName) {
		// Verify that the component is also a Vaadin Component
		if (!(component instanceof Component)) {
			LOGGER.error("Component implementing IHasDragEnd must also extend Component: {}", component.getClass().getSimpleName());
			return;
		}
		final Component vaadinComponent = (Component) component;
		component.addDragEndListener(event -> {
			try {
				LOGGER.debug("[DragDebug] CPageService.bindDragEnd: Invoking {} on component {}", methodName,
						component.getClass().getSimpleName());
				// GridDragEndEvent doesn't provide dragged items
				// Handler methods should track items from dragStart event if needed
				final CDragDropEvent<?> dragEvent = new CDragDropEvent(null, component);
				method.invoke(this, vaadinComponent, dragEvent);
			} catch (final Exception ex) {
				LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
			}
		});
		LOGGER.debug("Bound IHasDragEnd component drag end event to method {}", methodName);
	}

	/** Binds a component's drag start event to a page service handler method. This method supports any component implementing IHasDragStart
	 * interface.
	 * @param component  the component implementing IHasDragStart
	 * @param method     the handler method to invoke
	 * @param methodName the name of the handler method */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void bindDragStart(final IHasDragStart<?> component, final Method method, final String methodName) {
		// Verify that the component is also a Vaadin Component
		if (!(component instanceof Component)) {
			LOGGER.error("Component implementing IHasDragStart must also extend Component: {}", component.getClass().getSimpleName());
			return;
		}
		final Component vaadinComponent = (Component) component;
		component.addDragStartListener(event -> {
			try {
				final List<?> draggedItems = new ArrayList<>(event.getDraggedItems());
				final int itemCount = draggedItems != null ? draggedItems.size() : 0;
				LOGGER.debug("[DragDebug] CPageService.bindDragStart: Invoking {} on component {}, items={}", methodName,
						component.getClass().getSimpleName(), itemCount);
				final CDragDropEvent<?> dragEvent = new CDragDropEvent(draggedItems, component);
				method.invoke(this, vaadinComponent, dragEvent);
			} catch (final Exception ex) {
				LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
			}
		});
		LOGGER.debug("Bound IHasDragStart component drag start event to method {}", methodName);
	}

	/** Binds a Grid's drag end event to a page service handler method. Note: GridDragEndEvent doesn't provide dragged items, so we pass an empty
	 * event.
	 * @param grid       the Grid component
	 * @param method     the handler method to invoke
	 * @param methodName the name of the handler method */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void bindGridDragEnd(final Grid<?> grid, final Method method, final String methodName) {
		grid.addDragEndListener(event -> {
			try {
				// GridDragEndEvent doesn't provide dragged items
				// Handler methods should track items from dragStart event if needed
				final CDragDropEvent<?> dragEvent = new CDragDropEvent(null, grid);
				method.invoke(this, grid, dragEvent);
			} catch (final Exception ex) {
				LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
			}
		});
		LOGGER.debug("Bound Grid drag end event to method {}", methodName);
	}

	/** Binds a Grid's drag start event to a page service handler method.
	 * @param grid       the Grid component
	 * @param method     the handler method to invoke
	 * @param methodName the name of the handler method */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void bindGridDragStart(final Grid<?> grid, final Method method, final String methodName) {
		grid.addDragStartListener(event -> {
			try {
				final List<?> draggedItems = new ArrayList<>(event.getDraggedItems());
				final CDragDropEvent<?> dragEvent = new CDragDropEvent(draggedItems, grid);
				method.invoke(this, grid, dragEvent);
			} catch (final Exception ex) {
				LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
			}
		});
		LOGGER.debug("Bound Grid drag start event to method {}", methodName);
	}

	/** Binds a Grid's drop event to a page service handler method.
	 * @param grid       the Grid component
	 * @param method     the handler method to invoke
	 * @param methodName the name of the handler method */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private void bindGridDrop(final Grid<?> grid, final Method method, final String methodName) {
		grid.addDropListener(event -> {
			try {
				// Get drop information
				final Object targetItem = event.getDropTargetItem().orElse(null);
				final var dropLocation = event.getDropLocation();
				// Note: The dragged items are tracked externally and passed via the drag source
				// We create an event with null dragged items as they're tracked in the drag start
				final CDragDropEvent<?> dropEvent = new CDragDropEvent(null, null, targetItem, dropLocation, grid);
				method.invoke(this, grid, dropEvent);
			} catch (final Exception ex) {
				LOGGER.error("Error invoking method {}: {}", methodName, ex.getMessage());
			}
		});
		LOGGER.debug("Bound Grid drop event to method {}", methodName);
	}

	public void bindMethods(final CPageService<?> page) {
		Check.notNull(page, "PageService instance must not be null to bind methods.");
		// Combine form components and custom components
		final Map<String, Component> allComponents = new HashMap<>();
		
		// Get components from detailsBuilder's centralized map if available
		if (detailsBuilder != null && detailsBuilder.getComponentMap() != null) {
			allComponents.putAll(detailsBuilder.getComponentMap());
			LOGGER.debug("Added {} components from detailsBuilder's centralized map", detailsBuilder.getComponentMap().size());
		}
		
		// Also include formBuilder components for backward compatibility
		if (formBuilder != null) {
			allComponents.putAll(formBuilder.getComponentMap());
		}
		
		// Add custom registered components (these take precedence)
		allComponents.putAll(customComponents);
		
		// print the component names for debugging
		LOGGER.debug("Binding methods for components: {}", allComponents.keySet());
		// filter methods with name matching regex:("on_[a-zA-Z0-9]+_[a" + "-zA-Z0-9]+")
		// final var methods = Arrays.stream(page.getClass().getDeclaredMethods()).filter(m ->
		// m.getName().matches("on_[a-zA-Z0-9]+_[a-zA-Z0-9]+")).toList();
		final var methods = page.getClass().getDeclaredMethods();
		for (final var method : methods) {
			final var matcher = HANDLER_PATTERN.matcher(method.getName());
			if (!matcher.matches()) {
				continue;
			}
			// LOGGER.debug("Found handler method: {}", method.getName());
			final var componentName = matcher.group(1);
			final var action = matcher.group(2);
			final var component = allComponents.get(componentName);
			if (component == null) {
				LOGGER.warn("Component '{}' not found in FormBuilder or custom components for binding method '{}'", componentName, method.getName());
				continue;
			}
			bindComponent(method, component, method.getName(), componentName, action);
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
		// First check detailsBuilder's centralized map (most comprehensive)
		if (detailsBuilder != null && detailsBuilder.getComponentMap() != null) {
			final Component component = detailsBuilder.getComponentMap().get(fieldName);
			if (component != null) {
				return component;
			}
		}
		
		// Fall back to formBuilder for backward compatibility
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
		return getView().getEntityService();
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
