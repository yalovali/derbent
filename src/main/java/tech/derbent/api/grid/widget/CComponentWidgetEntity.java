package tech.derbent.api.grid.widget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/** CEntityWidget - Base widget component for displaying entities in a rich, visual format within grids.
 * <p>
 * This component provides a standardized widget layout for entity display that includes: - Selection indicator (first column icon) - Primary content
 * (name, description) - Secondary content (dates, status, assigned user) - Action buttons (delete, edit, etc.)
 * </p>
 * <p>
 * The widget follows the project's coding guidelines and notification standards. All user actions are validated with Check.XXX functions and
 * exceptions are handled with CNotificationService.
 * </p>
 * <p>
 * The widget is designed to be extendable, allowing entity-specific implementations to add custom content and actions while maintaining consistent
 * styling and behavior.
 * </p>
 * @param <T> the entity type
 * @author Derbent Framework
 * @since 1.0
 * @see IComponentWidgetEntityProvider
 * @see CEntityWidgetEvent */
public class CComponentWidgetEntity<T extends CEntityDB<?>> extends CDiv {

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

		/** Creates a widget event with the specified action type.
		 * @param source     the source widget
		 * @param entity     the entity associated with the event
		 * @param actionType the type of action triggered */
		public CEntityWidgetEvent(final CComponentWidgetEntity<T> source, final T entity, final ActionType actionType) {
			super(source, false);
			this.entity = entity;
			this.actionType = actionType;
			customAction = null;
		}

		/** Creates a widget event with a custom action.
		 * @param source       the source widget
		 * @param entity       the entity associated with the event
		 * @param customAction the custom action name */
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

	protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
	protected static final Logger LOGGER = LoggerFactory.getLogger(CComponentWidgetEntity.class);
	private static final long serialVersionUID = 1L;

	// =============== STATIC VALUE PROVIDERS ===============
	// These static methods provide value providers for grid columns and external use

	/** Creates a generic widget for any entity type using the entity registry.
	 * @param entity the entity to create a widget for
	 * @param <E>    the entity type
	 * @return a widget component for the entity */
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

	/** Returns a static value provider that creates widgets for entities.
	 * Use this when you need to display widgets in grid columns.
	 * @param <E> the entity type
	 * @return a value provider that creates widgets */
	public static <E extends CEntityDB<?>> ValueProvider<E, Component> widgetValueProvider() {
		return entity -> createWidget(entity);
	}

	/** Returns a value provider that extracts the entity name.
	 * @param <E> the entity type
	 * @return a value provider for entity names */
	public static <E extends CEntityNamed<?>> ValueProvider<E, String> nameValueProvider() {
		return entity -> entity != null ? entity.getName() : "";
	}

	/** Returns a value provider that extracts description using reflection.
	 * @param <E> the entity type
	 * @return a value provider for entity descriptions */
	public static <E extends CEntityDB<?>> ValueProvider<E, String> descriptionValueProvider() {
		return entity -> {
			if (entity == null) {
				return "";
			}
			try {
				final java.lang.reflect.Method descMethod = entity.getClass().getMethod("getDescription");
				final Object result = descMethod.invoke(entity);
				return result != null ? result.toString() : "";
			} catch (final Exception e) {
				return "";
			}
		};
	}

	/** Returns a value provider for a specific property using reflection.
	 * @param propertyName the name of the property
	 * @param <E>          the entity type
	 * @param <V>          the value type
	 * @return a value provider for the property */
	@SuppressWarnings ("unchecked")
	public static <E extends CEntityDB<?>, V> ValueProvider<E, V> propertyValueProvider(final String propertyName) {
		Check.notBlank(propertyName, "Property name cannot be blank");
		return entity -> {
			if (entity == null) {
				return null;
			}
			try {
				final String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
				final java.lang.reflect.Method getter = entity.getClass().getMethod(getterName);
				return (V) getter.invoke(entity);
			} catch (final Exception e) {
				LOGGER.debug("Could not get property {}: {}", propertyName, e.getMessage());
				return null;
			}
		};
	}

	/** Returns a value provider for dates.
	 * @param dateExtractor function to extract the date from the entity
	 * @param <E>           the entity type
	 * @return a value provider that formats dates */
	public static <E extends CEntityDB<?>> ValueProvider<E, String> dateValueProvider(final Function<E, LocalDate> dateExtractor) {
		Check.notNull(dateExtractor, "Date extractor cannot be null");
		return entity -> {
			if (entity == null) {
				return "";
			}
			final LocalDate date = dateExtractor.apply(entity);
			return date != null ? date.format(DATE_FORMATTER) : "";
		};
	}

	/** Returns a value provider for progress percentage.
	 * @param progressExtractor function to extract progress from the entity
	 * @param <E>               the entity type
	 * @return a value provider that formats progress as percentage string */
	public static <E extends CEntityDB<?>> ValueProvider<E, String> progressValueProvider(final Function<E, Integer> progressExtractor) {
		Check.notNull(progressExtractor, "Progress extractor cannot be null");
		return entity -> {
			if (entity == null) {
				return "";
			}
			final Integer progress = progressExtractor.apply(entity);
			return progress != null ? progress + "%" : "";
		};
	}

	/** Returns a value provider for hours/duration.
	 * @param hoursExtractor function to extract hours from the entity
	 * @param <E>            the entity type
	 * @return a value provider that formats hours */
	public static <E extends CEntityDB<?>> ValueProvider<E, String> hoursValueProvider(final Function<E, BigDecimal> hoursExtractor) {
		Check.notNull(hoursExtractor, "Hours extractor cannot be null");
		return entity -> {
			if (entity == null) {
				return "";
			}
			final BigDecimal hours = hoursExtractor.apply(entity);
			if ((hours == null) || (hours.compareTo(BigDecimal.ZERO) == 0)) {
				return "";
			}
			return hours + "h";
		};
	}

	// =============== INSTANCE MEMBERS ===============

	protected final T entity;
	protected CHorizontalLayout layoutActions;
	protected CDiv layoutContent;
	protected CDiv layoutPrimary;
	protected CDiv layoutSecondary;
	protected CDiv layoutSelection;
	protected boolean selected;

	/** Creates a new entity widget for the specified entity.
	 * @param entity the entity to display in the widget */
	public CComponentWidgetEntity(final T entity) {
		super();
		Check.notNull(entity, "Entity cannot be null when creating widget");
		this.entity = entity;
		selected = false;
		initializeWidget();
	}

	/** Adds a custom action button to the widget.
	 * @param icon       the icon for the button
	 * @param tooltip    the tooltip text
	 * @param actionName the action name to fire in events */
	protected void addActionButton(final VaadinIcon icon, final String tooltip, final String actionName) {
		Check.notNull(icon, "Icon cannot be null for action button");
		Check.notBlank(actionName, "Action name cannot be blank for action button");
		final CButton button = new CButton(new Icon(icon));
		button.setTooltipText(tooltip != null ? tooltip : actionName);
		button.addClassName("widget-action-button");
		button.getStyle().set("min-width", "var(--lumo-size-s)");
		button.getStyle().set("padding", "0");
		button.addClickListener(e -> on_actionButton_clicked(actionName));
		layoutActions.add(button);
	}

	/** Adds an action listener that will be notified when widget actions are triggered.
	 * @param listener the listener to add
	 * @return a registration that can be used to remove the listener */
	@SuppressWarnings ("unchecked")
	public Registration addActionListener(final com.vaadin.flow.component.ComponentEventListener<CEntityWidgetEvent<T>> listener) {
		return addListener((Class<CEntityWidgetEvent<T>>) (Class<?>) CEntityWidgetEvent.class, listener);
	}

	/** Adds a date row to the secondary content.
	 * @param label the date label
	 * @param date  the date value
	 * @param icon  optional icon to show before the date */
	protected void addDateRow(final String label, final LocalDate date, final VaadinIcon icon) {
		if (date == null) {
			return;
		}
		final CDiv row = createInfoRow();
		if (icon != null) {
			final Icon dateIcon = new Icon(icon);
			dateIcon.setSize("14px");
			dateIcon.addClassName(TextColor.SECONDARY);
			row.add(dateIcon);
		}
		final Span labelSpan = new Span(label + ": ");
		labelSpan.addClassNames(FontSize.XSMALL, TextColor.SECONDARY);
		final Span dateSpan = new Span(date.format(DATE_FORMATTER));
		dateSpan.addClassNames(FontSize.XSMALL);
		row.add(labelSpan, dateSpan);
		layoutSecondary.add(row);
	}

	/** Adds a delete action button to the widget. */
	protected void addDeleteAction() {
		addActionButton(VaadinIcon.TRASH, "Delete", "DELETE");
	}

	/** Adds an edit action button to the widget. */
	protected void addEditAction() {
		addActionButton(VaadinIcon.EDIT, "Edit", "EDIT");
	}

	/** Adds an info row with icon and text to the secondary content.
	 * @param icon the icon to show
	 * @param text the text to display */
	protected void addInfoRow(final VaadinIcon icon, final String text) {
		if ((text == null) || text.isBlank()) {
			return;
		}
		final CDiv row = createInfoRow();
		if (icon != null) {
			final Icon infoIcon = new Icon(icon);
			infoIcon.setSize("14px");
			infoIcon.addClassName(TextColor.SECONDARY);
			row.add(infoIcon);
		}
		final Span textSpan = new Span(text);
		textSpan.addClassNames(FontSize.XSMALL, TextColor.SECONDARY);
		row.add(textSpan);
		layoutSecondary.add(row);
	}

	/** Adds a status badge to the secondary content.
	 * @param statusEntity the status entity to display
	 * @param label        optional label to show before the status */
	protected void addStatusBadge(final CEntityDB<?> statusEntity, final String label) {
		if (statusEntity == null) {
			return;
		}
		try {
			final CDiv row = createInfoRow();
			if ((label != null) && !label.isBlank()) {
				final Span labelSpan = new Span(label + ": ");
				labelSpan.addClassNames(FontSize.XSMALL, TextColor.SECONDARY);
				row.add(labelSpan);
			}
			// Create status badge
			final String displayText = CColorUtils.getDisplayTextFromEntity(statusEntity);
			final String color = CColorUtils.getColorFromEntity(statusEntity);
			final Span statusSpan = new Span(displayText);
			statusSpan.addClassNames(FontSize.XSMALL, FontWeight.MEDIUM);
			statusSpan.getStyle().set("padding", "2px 8px");
			statusSpan.getStyle().set("border-radius", "12px");
			if ((color != null) && !color.isBlank()) {
				statusSpan.getStyle().set("background-color", color);
				statusSpan.getStyle().set("color", CColorUtils.getContrastTextColor(color));
			} else {
				statusSpan.getStyle().set("background-color", "#e9ecef");
				statusSpan.getStyle().set("color", "#495057");
			}
			row.add(statusSpan);
			layoutSecondary.add(row);
		} catch (final Exception e) {
			LOGGER.warn("Error adding status badge: {}", e.getMessage());
		}
	}

	/** Adds a user info row to the secondary content.
	 * @param user  the user entity
	 * @param label the label to show */
	protected void addUserRow(final CEntityNamed<?> user, final String label) {
		if (user == null) {
			return;
		}
		final CDiv row = createInfoRow();
		final Icon userIcon = new Icon(VaadinIcon.USER);
		userIcon.setSize("14px");
		userIcon.addClassName(TextColor.SECONDARY);
		final Span labelSpan = new Span(label + ": ");
		labelSpan.addClassNames(FontSize.XSMALL, TextColor.SECONDARY);
		final Span nameSpan = new Span(user.getName());
		nameSpan.addClassNames(FontSize.XSMALL, FontWeight.MEDIUM);
		row.add(userIcon, labelSpan, nameSpan);
		layoutSecondary.add(row);
	}

	/** Adds a view action button to the widget. */
	protected void addViewAction() {
		addActionButton(VaadinIcon.EYE, "View Details", "VIEW");
	}

	/** Builds the primary content section. Override in subclasses to customize. */
	protected void buildPrimaryContent() {
		// Default implementation: show name and description if available
		if (entity instanceof CEntityNamed<?>) {
			final CEntityNamed<?> named = (CEntityNamed<?>) entity;
			// Name
			final Span nameSpan = new Span(named.getName());
			nameSpan.addClassNames(FontSize.MEDIUM, FontWeight.SEMIBOLD);
			layoutPrimary.add(nameSpan);
			// Description (if available)
			final String description = getEntityDescription();
			if ((description != null) && !description.isBlank()) {
				final Span descSpan = new Span(description);
				descSpan.addClassNames(FontSize.SMALL, TextColor.SECONDARY);
				descSpan.getStyle().set("white-space", "nowrap");
				descSpan.getStyle().set("overflow", "hidden");
				descSpan.getStyle().set("text-overflow", "ellipsis");
				descSpan.getStyle().set("max-width", "300px");
				layoutPrimary.add(descSpan);
			}
		}
	}

	/** Builds the secondary content section. Override in subclasses to add custom info rows. */
	protected void buildSecondaryContent() {
		// Default implementation: empty
		// Subclasses should override to add entity-specific secondary content
	}

	/** Creates a styled info row container.
	 * @return a configured CDiv for info rows */
	protected CDiv createInfoRow() {
		final CDiv row = new CDiv();
		row.addClassNames(Display.FLEX, AlignItems.CENTER, Gap.XSMALL);
		row.getStyle().set("flex-shrink", "0");
		return row;
	}

	/** Gets the entity displayed in this widget.
	 * @return the entity */
	public T getEntity() { return entity; }

	/** Gets the entity description if available. Override in subclasses for custom description extraction.
	 * @return the entity description, or null if not available */
	protected String getEntityDescription() {
		try {
			final java.lang.reflect.Method descMethod = entity.getClass().getMethod("getDescription");
			final Object result = descMethod.invoke(entity);
			return result != null ? result.toString() : null;
		} catch (final Exception e) {
			// No description method available
			return null;
		}
	}

	/** Initializes the widget structure and content. */
	protected void initializeWidget() {
		CAuxillaries.setId(this);
		// Remove default CDiv flex classes and set custom widget styling
		removeClassName(Display.FLEX);
		removeClassName(AlignItems.CENTER);
		removeClassName(Gap.SMALL);
		// Widget container styling
		addClassNames(Display.FLEX, FlexDirection.ROW, AlignItems.CENTER, Gap.SMALL, Padding.XSMALL);
		getStyle().set("border", "1px solid #e9ecef");
		getStyle().set("border-radius", "8px");
		getStyle().set("background-color", "#ffffff");
		getStyle().set("min-height", "60px");
		getStyle().set("cursor", "pointer");
		getStyle().set("transition", "all 0.2s ease");
		setWidthFull();
		// Selection indicator
		layoutSelection = new CDiv();
		layoutSelection.removeClassName(Display.FLEX);
		layoutSelection.removeClassName(AlignItems.CENTER);
		layoutSelection.getStyle().set("width", "4px");
		layoutSelection.getStyle().set("height", "100%");
		layoutSelection.getStyle().set("min-height", "50px");
		layoutSelection.getStyle().set("border-radius", "4px 0 0 4px");
		layoutSelection.getStyle().set("background-color", "transparent");
		// Main content container
		layoutContent = new CDiv();
		layoutContent.removeClassName(Display.FLEX);
		layoutContent.removeClassName(AlignItems.CENTER);
		layoutContent.addClassNames(Display.FLEX, FlexDirection.COLUMN, Gap.XSMALL);
		layoutContent.getStyle().set("flex", "1");
		layoutContent.getStyle().set("min-width", "0"); // Allow text truncation
		// Primary content (name, description)
		layoutPrimary = new CDiv();
		layoutPrimary.removeClassName(Display.FLEX);
		layoutPrimary.removeClassName(AlignItems.CENTER);
		layoutPrimary.addClassNames(Display.FLEX, FlexDirection.COLUMN, Gap.XSMALL);
		// Secondary content (dates, status, etc.)
		layoutSecondary = new CDiv();
		layoutSecondary.removeClassName(Display.FLEX);
		layoutSecondary.removeClassName(AlignItems.CENTER);
		layoutSecondary.addClassNames(Display.FLEX, FlexDirection.ROW, Gap.MEDIUM);
		layoutSecondary.getStyle().set("flex-wrap", "wrap");
		// Actions container
		layoutActions = new CHorizontalLayout();
		layoutActions.setPadding(false);
		layoutActions.setSpacing(false);
		layoutActions.addClassName(Gap.XSMALL);
		layoutActions.getStyle().set("flex-shrink", "0");
		// Build content
		buildPrimaryContent();
		buildSecondaryContent();
		// Assemble layout
		layoutContent.add(layoutPrimary, layoutSecondary);
		add(layoutSelection, layoutContent, layoutActions);
	}

	/** Checks if the widget is selected.
	 * @return true if selected */
	public boolean isSelected() { return selected; }

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

	/** Sets the selected state of the widget.
	 * @param selected true to mark as selected */
	public void setSelected(final boolean selected) {
		this.selected = selected;
		if (selected) {
			layoutSelection.getStyle().set("background-color", "var(--lumo-primary-color)");
			getStyle().set("border-color", "var(--lumo-primary-color)");
			getStyle().set("background-color", "var(--lumo-primary-color-10pct)");
		} else {
			layoutSelection.getStyle().set("background-color", "transparent");
			getStyle().set("border-color", "#e9ecef");
			getStyle().set("background-color", "#ffffff");
		}
	}
}
