package tech.derbent.api.grid.widget;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;

public class CComponentWidgetEntity<EntityClass extends CEntityDB<?>> extends CHorizontalLayout implements IHasDragControl {

	/** Event fired when a widget action is triggered (e.g., delete button clicked). */
	public static class CEntityWidgetEvent<T extends CEntityDB<?>> extends ComponentEvent<CComponentWidgetEntity<T>> {

		/** Widget action types. */
		public enum ActionType {
			CUSTOM, DELETE, EDIT, VIEW
		}

		private static final long serialVersionUID = 1L;
		private final ActionType actionType;
		private final String customAction;
		private final T entity;

		public CEntityWidgetEvent(final CComponentWidgetEntity<T> source, final T entity, final ActionType actionType) {
			super(source, false);
			this.entity = entity;
			this.actionType = actionType;
			customAction = null;
		}

		public CEntityWidgetEvent(final CComponentWidgetEntity<T> source, final T entity, final String customAction) {
			super(source, false);
			this.entity = entity;
			actionType = ActionType.CUSTOM;
			this.customAction = customAction;
		}

		public ActionType getActionType() { return actionType; }

		public String getCustomAction() { return customAction; }

		public T getEntity() { return entity; }
	}

	protected static final Logger LOGGER = LoggerFactory.getLogger(CComponentWidgetEntity.class);
	private static final long serialVersionUID = 1L;
	/** Static map to store widget UI state by entity ID across widget recreation.
	 * <p>
	 * This map is used to preserve UI state (expanded/collapsed sections, selected tabs, etc.) when widgets are recreated during grid refresh
	 * operations. Subclasses should use saveWidgetState() before widget is destroyed and restoreWidgetState() after recreation.
	 * </p>
	 * <p>
	 * Key format: entityClass.getSimpleName() + "_" + entityId Example: "CSprint_123"
	 * </p>
	 */
	private static final Map<String, Map<String, Object>> widgetStateStore = new ConcurrentHashMap<>();

	/** Clears all stored widget state. Should be called when navigating away from a view or when state should be reset. */
	public static void clearAllWidgetState() {
		widgetStateStore.clear();
	}

	/** Clears widget state for a specific entity.
	 * @param entityClass the entity class
	 * @param entityId    the entity ID */
	protected static void clearWidgetState(final Class<?> entityClass, final Long entityId) {
		if (entityClass == null || entityId == null) {
			return;
		}
		final String key = entityClass.getSimpleName() + "_" + entityId;
		widgetStateStore.remove(key);
	}

	@SuppressWarnings ("unchecked")
	public static <E extends CEntityDB<?>> Component createWidget(final E entity) {
		Check.notNull(entity, "Entity cannot be null when creating widget");
		// Try to find a registered widget provider via CEntityRegistry
		final Class<?> entityClass = entity.getClass();
		final Class<?> pageServiceClass = CEntityRegistry.getPageServiceClass(entityClass);
		if (pageServiceClass != null) {
			try {
				// Try to get the widget from the page service
				final Object pageService = CSpringContext.getBean(pageServiceClass);
				if (pageService instanceof IComponentWidgetEntityProvider) {
					final IComponentWidgetEntityProvider<E> provider = (IComponentWidgetEntityProvider<E>) pageService;
					final Component widget = provider.getComponentWidget(entity);
					if (widget != null) {
						return widget;
					}
				}
			} catch (final Exception e) {
				LOGGER.debug("Could not create widget via page service for {}: {}", entityClass.getSimpleName(), e.getMessage());
			}
		}
		// Fall back to generic widget
		return new CComponentWidgetEntity<>(entity);
	}

	public static <E extends CEntityDB<?>> ValueProvider<E, String> dateValueProvider(final Function<E, LocalDate> dateExtractor) {
		Check.notNull(dateExtractor, "Date extractor cannot be null");
		return entity -> {
			if (entity == null) {
				return "";
			}
			final LocalDate date = dateExtractor.apply(entity);
			return date != null ? date.format(CLabelEntity.DATE_FORMATTER) : "";
		};
	}

	/** Gets the state key for this widget.
	 * @param entityClass the entity class
	 * @param entityId    the entity ID
	 * @return the state key */
	protected static String getStateKey(final Class<?> entityClass, final Long entityId) {
		if (entityClass == null || entityId == null) {
			return null;
		}
		return entityClass.getSimpleName() + "_" + entityId;
	}

	/** Gets a state value from storage.
	 * @param entityClass the entity class
	 * @param entityId    the entity ID
	 * @param key         the state key
	 * @return the state value, or null if not found */
	protected static Object getStateValue(final Class<?> entityClass, final Long entityId, final String key) {
		final String stateKey = getStateKey(entityClass, entityId);
		if (stateKey == null) {
			return null;
		}
		final Map<String, Object> state = widgetStateStore.get(stateKey);
		return state != null ? state.get(key) : null;
	}

	@SuppressWarnings ("unchecked")
	public static <E extends CEntityDB<?>, V> ValueProvider<E, V> propertyValueProvider(final String propertyName) {
		Check.notBlank(propertyName, "Property name cannot be blank");
		final String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + (propertyName.length() > 1 ? propertyName.substring(1) : "");
		return entity -> {
			if (entity == null) {
				return null;
			}
			try {
				final java.lang.reflect.Method getter = entity.getClass().getMethod(getterName);
				return (V) getter.invoke(entity);
			} catch (final Exception e) {
				LOGGER.debug("Could not get property {}: {}", propertyName, e.getMessage());
				return null;
			}
		};
	}
	// =============== WIDGET STATE PRESERVATION ===============

	/** Saves a state value to storage.
	 * @param entityClass the entity class
	 * @param entityId    the entity ID
	 * @param key         the state key
	 * @param value       the state value */
	protected static void saveStateValue(final Class<?> entityClass, final Long entityId, final String key, final Object value) {
		final String stateKey = getStateKey(entityClass, entityId);
		if (stateKey == null) {
			return;
		}
		widgetStateStore.computeIfAbsent(stateKey, k -> new ConcurrentHashMap<>()).put(key, value);
	}

	public static <E extends CEntityDB<?>> ValueProvider<E, Component> widgetValueProvider() {
		return entity -> createWidget(entity);
	}

	// Drag control state
	private final boolean dragEnabled = false;
	private final List<ComponentEventListener<GridDragEndEvent<?>>> dragEndListeners = new ArrayList<>();
	private final List<ComponentEventListener<GridDragStartEvent<?>>> dragStartListeners = new ArrayList<>();
	private final boolean dropEnabled = false;
	private final List<ComponentEventListener<GridDropEvent<?>>> dropListeners = new ArrayList<>();
	// =============== INSTANCE MEMBERS ===============
	protected final EntityClass entity;
	protected CVerticalLayout layoutLeft = new CVerticalLayout();
	protected CHorizontalLayout layoutLineOne = new CHorizontalLayout();
	protected CHorizontalLayout layoutLineThree = new CHorizontalLayout();
	protected CHorizontalLayout layoutLineTwo = new CHorizontalLayout();
	protected CVerticalLayout layoutRight = new CVerticalLayout();
	protected boolean selected;

	/** Creates a new entity widget for the specified entity.
	 * @param entity the entity to display in the widget */
	public CComponentWidgetEntity(final EntityClass entity) {
		super();
		Check.notNull(entity, "Entity cannot be null when creating widget");
		this.entity = entity;
		selected = false;
		initializeWidget();
	}

	protected void addActionButton(final VaadinIcon icon, final String tooltip, final String actionName) {
		Check.notNull(icon, "Icon cannot be null for action button");
		Check.notBlank(actionName, "Action name cannot be blank for action button");
		final CButton button = new CButton(new Icon(icon));
		button.setTooltipText(tooltip != null ? tooltip : actionName);
		button.addClassName("widget-action-button");
		button.getStyle().set("min-width", "var(--lumo-size-s)");
		button.getStyle().set("padding", "0");
		button.addClickListener(e -> on_actionButton_clicked(actionName));
		layoutRight.add(button);
	}

	@SuppressWarnings ("unchecked")
	public Registration addActionListener(final ComponentEventListener<CEntityWidgetEvent<EntityClass>> listener) {
		return addListener((Class<CEntityWidgetEvent<EntityClass>>) (Class<?>) CEntityWidgetEvent.class, listener);
	}

	/** Adds a delete action button to the widget. */
	protected void addDeleteAction() {
		addActionButton(VaadinIcon.TRASH, "Delete", "DELETE");
	}

	/** Adds a listener for drag end events. Implements IHasDragEnd interface.
	 * @param listener the listener to be notified when drag ends
	 * @return a registration object that can be used to remove the listener */
	@Override
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public Registration addDragEndListener(final ComponentEventListener listener) {
		Check.notNull(listener, "Drag end listener cannot be null");
		dragEndListeners.add(listener);
		LOGGER.debug("[DragDebug] CComponentWidgetEntity: Added drag end listener, total: {}", dragEndListeners.size());
		return () -> dragEndListeners.remove(listener);
	}

	/** Adds a listener for drag start events. Implements IHasDragStart interface.
	 * @param listener the listener to be notified when drag starts
	 * @return a registration object that can be used to remove the listener */
	@Override
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public Registration addDragStartListener(final ComponentEventListener listener) {
		Check.notNull(listener, "Drag start listener cannot be null");
		dragStartListeners.add(listener);
		LOGGER.debug("[DragDebug] CComponentWidgetEntity: Added drag start listener, total: {}", dragStartListeners.size());
		return () -> dragStartListeners.remove(listener);
	}

	/** Adds a listener for drop events. Implements IHasDrop interface.
	 * @param listener the listener to be notified when items are dropped
	 * @return a registration object that can be used to remove the listener */
	@Override
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public Registration addDropListener(final ComponentEventListener listener) {
		Check.notNull(listener, "Drop listener cannot be null");
		dropListeners.add(listener);
		LOGGER.debug("[DragDebug] CComponentWidgetEntity: Added drop listener, total: {}", dropListeners.size());
		return () -> dropListeners.remove(listener);
	}

	/** Adds an edit action button to the widget. */
	protected void addEditAction() {
		addActionButton(VaadinIcon.EDIT, "Edit", "EDIT");
	}

	/** Adds a view action button to the widget. */
	protected void addViewAction() {
		addActionButton(VaadinIcon.EYE, "View Details", "VIEW");
	}

	protected void createFirstLine() throws Exception {}

	protected void createSecondLine() throws Exception {}

	protected void createThirdLine() throws Exception {}
	// ==================== IHasDragStart, IHasDragEnd, IHasDrop Implementation ====================

	public List<ComponentEventListener<GridDragEndEvent<?>>> getDragEndListeners() { return dragEndListeners; }

	public List<ComponentEventListener<GridDragStartEvent<?>>> getDragStartListeners() { return dragStartListeners; }

	public List<ComponentEventListener<GridDropEvent<?>>> getDropListeners() { return dropListeners; }

	/** Gets the entity displayed in this widget.
	 * @return the entity */
	public EntityClass getEntity() { return entity; }

	/** Initializes the widget structure and content. */
	protected void initializeWidget() {
		try {
			CAuxillaries.setId(this);
			// Ensure the horizontal layout (this) uses full width so children can expand
			setWidthFull();
			// Make the left vertical layout take available space
			layoutLeft.setWidthFull();
			// Ensure left expands and right does not take flex space
			expand(layoutLeft);
			// Right layout should not expand and must have no width set
			layoutRight.setSizeUndefined();
			setFlexGrow(0, layoutRight);
			createFirstLine();
			createSecondLine();
			createThirdLine();
			add(layoutLeft, layoutRight);
			layoutLeft.add(layoutLineOne, layoutLineTwo, layoutLineThree);
			// Restore UI state after widget creation
			restoreWidgetState();
		} catch (final Exception e) {
			LOGGER.error("Error initializing widget for entity {}: {}", entity, e.getMessage());
			layoutLeft.add(new CDiv("Exception occured"));
		}
	}

	/** Checks if the widget is selected.
	 * @return true if selected */
	public boolean isSelected() { return selected; }
	// =============== WIDGET STATE PRESERVATION METHODS ===============

	/** Handles action button clicks.
	 * @param actionName the name of the action */
	protected void on_actionButton_clicked(final String actionName) {
		Check.notBlank(actionName, "Action name cannot be blank");
		LOGGER.debug("Widget action triggered: {} for entity: {}", actionName, entity);
		try {
			final CEntityWidgetEvent.ActionType actionType = switch (actionName.toUpperCase()) {
			case "DELETE" -> CEntityWidgetEvent.ActionType.DELETE;
			case "EDIT" -> CEntityWidgetEvent.ActionType.EDIT;
			case "VIEW" -> CEntityWidgetEvent.ActionType.VIEW;
			default -> CEntityWidgetEvent.ActionType.CUSTOM;
			};
			if (actionType == CEntityWidgetEvent.ActionType.CUSTOM) {
				fireEvent(new CEntityWidgetEvent<>(this, entity, actionName));
			} else {
				fireEvent(new CEntityWidgetEvent<>(this, entity, actionType));
			}
		} catch (final Exception e) {
			LOGGER.error("Error handling widget action {}: {}", actionName, e.getMessage());
		}
	}

	/** Restores widget UI state after reconstruction. Subclasses should override to restore their specific state properties.
	 * <p>
	 * Called automatically after widget initialization to restore state from the previous widget instance. Default implementation does nothing.
	 * Subclasses should call super.restoreWidgetState() first, then restore their own state.
	 * </p>
	 * <p>
	 * Example override:
	 *
	 * <pre>
	 * {@code
	 *
	 * @Override
	 * protected void restoreWidgetState() {
	 * 	super.restoreWidgetState();
	 * 	Boolean visible = (Boolean) getStateValue(entity.getClass(), entity.getId(), "itemsVisible");
	 * 	if (visible != null) {
	 * 		sprintItemsVisible = visible;
	 * 		containerSprintItems.setVisible(visible);
	 * 	}
	 * }
	 * }
	 * </pre>
	 * </p>
	 */
	protected void restoreWidgetState() {
		// Default implementation: no state to restore
		// Subclasses should override to restore their specific UI state
	}

	/** Saves widget UI state before destruction. Subclasses should override to save their specific state properties.
	 * <p>
	 * Called before widget is destroyed during grid refresh to preserve state for the next widget instance. Default implementation does nothing.
	 * Subclasses should call super.saveWidgetState() first, then save their own state.
	 * </p>
	 * <p>
	 * Example override:
	 *
	 * <pre>
	 * {@code
	 *
	 * @Override
	 * public void saveWidgetState() {
	 * 	super.saveWidgetState();
	 * 	saveStateValue(entity.getClass(), entity.getId(), "itemsVisible", sprintItemsVisible);
	 * }
	 * }
	 * </pre>
	 * </p>
	 */
	public void saveWidgetState() {
		// Default implementation: no state to save
		// Subclasses should override to save their specific UI state
	}

	public void setSelected(final boolean selected) {
		this.selected = selected;
		if (selected) {
			getStyle().set("border-color", "var(--lumo-primary-color)");
			getStyle().set("background-color", "var(--lumo-primary-color-10pct)");
		} else {
			getStyle().set("border-color", "#e9ecef");
			getStyle().set("background-color", "#ffffff");
		}
	}
}
