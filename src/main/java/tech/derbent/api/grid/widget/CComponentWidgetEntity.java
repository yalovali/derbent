package tech.derbent.api.grid.widget;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CEvent;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentWidgetEntity.class);
	private static final long serialVersionUID = 1L;

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

	public static <E extends CEntityDB<?>> ValueProvider<E, Component> widgetValueProvider() {
		return entity -> createWidget(entity);
	}

	// Drag control state
	private final Set<ComponentEventListener<CDragEndEvent>> dragEndListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragStartEvent>> dragStartListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragDropEvent>> dropListeners = new HashSet<>();
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

	/** Adds an edit action button to the widget. */
	protected void addEditAction() {
		addActionButton(VaadinIcon.EDIT, "Edit", "EDIT");
	}

	/** Adds a view action button to the widget. */
	protected void addViewAction() {
		addActionButton(VaadinIcon.EYE, "View Details", "VIEW");
	}

	protected void createFirstLine() throws Exception { /***/
	}

	protected void createSecondLine() throws Exception {/***/
	}

	protected void createThirdLine() throws Exception {/***/
	}
	// ==================== IHasDragStart, IHasDragEnd, IHasDrop Implementation ====================

	@Override
	public void drag_checkEventAfterPass(CEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void drag_checkEventBeforePass(CEvent event) {
		// LOGGER.debug("Drag event check before pass: {} comp id:{} event type:{}", event, getId(), event.getClass().getSimpleName());
	}

	@Override
	public Set<ComponentEventListener<CDragEndEvent>> drag_getDragEndListeners() {
		return dragEndListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragStartEvent>> drag_getDragStartListeners() {
		return dragStartListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragDropEvent>> drag_getDropListeners() {
		return dropListeners;
	}

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

	@Override
	public void setDragEnabled(boolean enabled) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setDropEnabled(boolean enabled) {
		// TODO Auto-generated method stub
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
