package tech.derbent.api.ui.component.enhanced;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.page.view.CDynamicPageRouter;
import tech.derbent.base.session.service.ISessionService;

/** CComponentItemDetails - A standard component implementing HasValue interface for displaying entity details.
 * <p>
 * This component displays the details of a CProjectItem entity in a CDynamicPageRouter. When a value is set via
 * setValue(), the component automatically locates and displays the entity's default detail page.
 * </p>
 * <p>
 * Features:
 * <ul>
 * <li>Implements HasValue interface for standard Vaadin binding</li>
 * <li>Automatically displays entity details when value is set</li>
 * <li>Uses CDynamicPageRouter to display entity-specific detail pages</li>
 * <li>Supports value change listeners</li>
 * <li>Handles null values gracefully (clears display)</li>
 * </ul>
 * </p>
 * <p>
 * Usage:
 * <pre>
 * CComponentItemDetails itemDetails = new CComponentItemDetails(sessionService, pageEntityService, detailSectionService);
 * itemDetails.setValue(activity); // Displays activity details
 * itemDetails.addValueChangeListener(event -> {
 *     CProjectItem<?> item = event.getValue();
 *     // Handle value change
 * });
 * </pre>
 * </p>
 * @author Derbent Framework
 * @since 1.0 */
public class CComponentItemDetails extends CVerticalLayout implements HasValue<HasValue.ValueChangeEvent<CProjectItem<?>>, CProjectItem<?>> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentItemDetails.class);
	private static final long serialVersionUID = 1L;

	private final CDynamicPageRouter currentEntityPageRouter;
	private CProjectItem<?> currentValue = null;
	private final CPageEntityService pageEntityService;
	private CProjectItem<?> previousValue = null;
	private boolean readOnly = false;
	private final ISessionService sessionService;

	// HasValue interface fields
	private final List<ValueChangeListener<? super ValueChangeEvent<CProjectItem<?>>>> valueChangeListeners = new ArrayList<>();

	/** Creates a new CComponentItemDetails component.
	 * @param sessionService        the session service for accessing user session data
	 * @param pageEntityService     the page entity service for locating entity detail pages
	 * @param detailSectionService  the detail section service for page configuration
	 * @throws Exception if there's an error initializing the component */
	public CComponentItemDetails(final ISessionService sessionService, final CPageEntityService pageEntityService,
			final CDetailSectionService detailSectionService) throws Exception {
		Check.notNull(sessionService, "Session service cannot be null");
		Check.notNull(pageEntityService, "Page entity service cannot be null");
		Check.notNull(detailSectionService, "Detail section service cannot be null");

		this.sessionService = sessionService;
		this.pageEntityService = pageEntityService;

		// Create the CDynamicPageRouter for displaying entity details
		this.currentEntityPageRouter = new CDynamicPageRouter(pageEntityService, sessionService, detailSectionService, null);

		// Add the router to this component
		add(currentEntityPageRouter);

		// Set full width and height to expand with container
		setWidthFull();
		setHeightFull();

		LOGGER.debug("CComponentItemDetails initialized");
	}

	/** Registers a value change listener.
	 * @param listener the value change listener to register
	 * @return Registration object to remove the listener */
	@Override
	public Registration addValueChangeListener(final ValueChangeListener<? super ValueChangeEvent<CProjectItem<?>>> listener) {
		Check.notNull(listener, "ValueChangeListener cannot be null");
		valueChangeListeners.add(listener);
		LOGGER.debug("Added value change listener to CComponentItemDetails");
		return () -> valueChangeListeners.remove(listener);
	}

	/** Clears the current value and display. Equivalent to calling setValue(null). */
	@Override
	public void clear() {
		LOGGER.debug("Clearing current value");
		setValue(null);
	}

	/** Fires a value change event to all registered listeners.
	 * @param newValue   the new value
	 * @param fromClient whether the change originated from the client */
	protected void fireValueChangeEvent(final CProjectItem<?> newValue, final boolean fromClient) {
		final CProjectItem<?> oldValue = previousValue;
		previousValue = newValue;

		if (!valueChangeListeners.isEmpty()) {
			LOGGER.debug("Firing value change event: old={}, new={}, fromClient={}", oldValue != null ? oldValue.getName() : "null",
					newValue != null ? newValue.getName() : "null", fromClient);

			final ValueChangeEvent<CProjectItem<?>> event = new ValueChangeEvent<CProjectItem<?>>() {
				private static final long serialVersionUID = 1L;

				@Override
				public HasValue<?, CProjectItem<?>> getHasValue() {
					return CComponentItemDetails.this;
				}

				@Override
				public CProjectItem<?> getOldValue() {
					return oldValue;
				}

				@Override
				public CProjectItem<?> getValue() {
					return newValue;
				}

				@Override
				public boolean isFromClient() {
					return fromClient;
				}
			};

			for (final ValueChangeListener<? super ValueChangeEvent<CProjectItem<?>>> listener : valueChangeListeners) {
				try {
					listener.valueChanged(event);
				} catch (final Exception e) {
					LOGGER.error("Error notifying value change listener", e);
				}
			}
		}
	}

	/** Gets the current value of this component (the displayed entity).
	 * @return the currently displayed entity (can be null) */
	@Override
	public CProjectItem<?> getValue() {
		return currentValue;
	}

	/** Checks if the component is empty (no value set).
	 * @return true if no value is set, false otherwise */
	@Override
	public boolean isEmpty() {
		return currentValue == null;
	}

	/** Checks if the component is read-only.
	 * @return true if read-only, false otherwise */
	@Override
	public boolean isReadOnly() {
		return readOnly;
	}

	/** Checks if the required indicator is visible.
	 * @return false (required indicator not currently implemented) */
	@Override
	public boolean isRequiredIndicatorVisible() {
		return false;
	}

	/** Locates and displays the entity's detail page in the CDynamicPageRouter.
	 * @param entity the entity to display, or null to clear the display */
	private void locateEntityInDynamicPage(final CProjectItem<?> entity) {
		try {
			if (entity == null) {
				currentEntityPageRouter.loadSpecificPage(null, null, true);
				return;
			}

			LOGGER.debug("Creating dynamic page for entity: {}", entity.getName());

			// Get the VIEW_NAME from the entity class using reflection
			final Field viewNameField = entity.getClass().getField("VIEW_NAME");
			final String entityViewName = (String) viewNameField.get(null);

			// Find the page entity for this view name
			final CPageEntity page = pageEntityService.findByNameAndProject(entityViewName, sessionService.getActiveProject().orElse(null))
					.orElseThrow(() -> new IllegalStateException("No page found for view name: " + entityViewName));

			Check.notNull(page, "Page entity cannot be null");

			// Load the specific page with the entity details
			currentEntityPageRouter.loadSpecificPage(page.getId(), entity.getId(), true);

			LOGGER.debug("Successfully loaded detail page for entity: {}", entity.getName());
		} catch (final Exception e) {
			LOGGER.error("Error creating dynamic page for entity", e);
			CNotificationService.showException("Error displaying entity details", e);
		}
	}

	/** Sets the read-only state of this component. Note: Read-only mode prevents value changes but doesn't affect the display.
	 * @param readOnly true to make read-only, false to make editable */
	@Override
	public void setReadOnly(final boolean readOnly) {
		this.readOnly = readOnly;
		LOGGER.debug("Read-only mode set to: {}", readOnly);
	}

	/** Sets whether the required indicator should be visible.
	 * @param requiredIndicatorVisible true to show required indicator, false to hide */
	@Override
	public void setRequiredIndicatorVisible(final boolean requiredIndicatorVisible) {
		// Note: CComponentItemDetails doesn't currently support required indicator
		LOGGER.debug("setRequiredIndicatorVisible({}) called - not currently implemented", requiredIndicatorVisible);
	}

	/** Sets the value of this component (the entity to display). This will update the display to show the entity's detail page and fire value
	 * change events.
	 * @param value the entity to display (can be null to clear display) */
	@Override
	public void setValue(final CProjectItem<?> value) {
		if (readOnly) {
			LOGGER.warn("Cannot set value in read-only mode");
			return;
		}

		LOGGER.debug("Setting value: {}", value != null ? value.getName() : "null");

		// Update the current value
		currentValue = value;

		// Display the entity details in the dynamic page router
		locateEntityInDynamicPage(value);

		// Fire value change event (not from client, programmatic change)
		fireValueChangeEvent(value, false);
	}
}
