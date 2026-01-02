package tech.derbent.app.page.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasContentOwner;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CFlexLayout;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.CLayoutService;
import tech.derbent.base.session.service.ISessionService;

@SuppressWarnings ("rawtypes")
public abstract class CPageBaseProjectAware extends CPageBase
		implements IProjectChangeListener, IContentOwner, IHasContentOwner, IPageServiceImplementer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageBaseProjectAware.class);
	private static final long serialVersionUID = 1L;

	private static List<Field> getAllFields(final Class<?> clazz) {
		final List<Field> fields = new ArrayList<>();
		Class<?> currentClass = clazz;
		while (currentClass != null) {
			for (final Field field : currentClass.getDeclaredFields()) {
				fields.add(field);
			}
			currentClass = currentClass.getSuperclass();
		}
		return fields;
	}

	private static boolean isSingleValueRelationField(final Field field) {
		return field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class);
	}

	protected CFlexLayout baseDetailsLayout = CFlexLayout.forEntityPage();
	protected Map<String, Component> componentMap = new HashMap<String, Component>();
	protected CEnhancedBinder<CEntityDB<?>> currentBinder; // Store current binder for data binding
	private CEntityDB<?> currentEntity; // Field to store current entity
	protected final CDetailsBuilder detailsBuilder;
	protected CLayoutService layoutService;
	private IContentOwner parentContent;
	private final CDetailSectionService screenService;
	private final ISessionService sessionService;
	protected SplitLayout splitLayout = new SplitLayout();

	protected CPageBaseProjectAware(final ISessionService sessionService, final CDetailSectionService screenService) {
		super();
		this.screenService = screenService;
		this.sessionService = sessionService;
		detailsBuilder = new CDetailsBuilder(sessionService);
		baseDetailsLayout.setSizeFull();
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		LOGGER.debug("Entering Sample Page");
	}

	protected <T extends CEntityDB<?>> void buildScreen(final Long detailId, final Class<T> entityClass, final CFlexLayout detailsLayout)
			throws Exception {
		try {
			LOGGER.debug("Building screen '{}' for entity type: {}", detailId, entityClass.getSimpleName());
			detailsLayout.removeAll();
			if (detailId == null) {
				return;
			}
			final CDetailSection screen = screenService.findByIdWithScreenLines(detailId);
			Check.notNull(screen, "Screen not found: " + detailId);
			// Only create binder if not already set for this entity type or if no current binder exists
			if (currentBinder == null || !currentBinder.getBeanType().equals(entityClass)) {
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<CEntityDB<?>> localBinder = new CEnhancedBinder<>((Class<CEntityDB<?>>) (Class<?>) entityClass);
				currentBinder = localBinder;
			}
			detailsBuilder.buildDetails(this, screen, currentBinder, detailsLayout);
		} catch (final Exception e) {
			LOGGER.error("Error building details layout for screen '{}': {}", detailId, e.getMessage());
			throw e;
		}
	}

	protected void buildScreen(final String baseViewName) {
		buildScreen(baseViewName, CEntityDB.class, getBaseDetailsLayout());
	}

	protected <T extends CEntityDB<?>> void buildScreen(final String baseViewName, final Class<T> entityClass) {
		buildScreen(baseViewName, entityClass, getBaseDetailsLayout());
	}

	/** Build screen with optional toolbar integration and parameterized details layout.
	 * @param baseViewName  the view name to build
	 * @param entityClass   the entity class type
	 * @param toolbar       optional toolbar that will use the same binder (can be null)
	 * @param detailsLayout the layout to build the screen into */
	protected <T extends CEntityDB<?>> void buildScreen(final String baseViewName, final Class<T> entityClass, final CFlexLayout detailsLayout) {
		try {
			LOGGER.debug("Building screen '{}' for entity type: {}", baseViewName, entityClass.getSimpleName());
			detailsLayout.removeAll();
			final CDetailSection screen = screenService.findByNameAndProject(
					getSessionService().getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for new activity.")),
					baseViewName);
			Check.notNull(screen, "Screen not found: " + baseViewName);
			// Only create binder if not already set for this entity type or if no current binder exists
			if (currentBinder == null || !currentBinder.getBeanType().equals(entityClass)) {
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<CEntityDB<?>> localBinder = new CEnhancedBinder<>((Class<CEntityDB<?>>) (Class<?>) entityClass);
				currentBinder = localBinder;
			}
			detailsBuilder.buildDetails(this, screen, currentBinder, detailsLayout);
		} catch (final Exception e) {
			final String errorMsg = "Error building details layout for screen: " + baseViewName;
			LOGGER.error("Error building details layout for screen '{}': {}", baseViewName, e.getMessage());
			detailsLayout.add(new CDiv(errorMsg));
			currentBinder = null; // Clear binder on error
		}
	}

	/** Hook method for subclasses to configure the CRUD toolbar with specific behavior like dependency checking */
	protected void configureCrudToolbar(@SuppressWarnings ("unused") final CCrudToolbar toolbar) {
		// Default implementation does nothing - subclasses can override to add specific configuration
	}

	/** Abstract method to create a new entity instance with project set
	 * @throws Exception */
	protected abstract <T extends CEntityDB<T>> T createNewEntity() throws Exception;

	public CFlexLayout getBaseDetailsLayout() { return baseDetailsLayout; }

	/** Get the current binder for data binding operations (public access for PageService pattern) */
	@Override
	public CEnhancedBinder<CEntityDB<?>> getBinder() { return currentBinder; }

	@Override
	public Map<String, Component> getComponentMap() { return componentMap; }

	@Override
	public IContentOwner getContentOwner() { return parentContent; }

	/** Get the current binder for data binding operations */
	protected CEnhancedBinder<CEntityDB<?>> getCurrentBinder() { return currentBinder; }

	@Override
	public String getCurrentEntityIdString() {
		LOGGER.debug("Getting current entity ID string for page.");
		if (currentEntity == null) {
			return null;
		}
		return currentEntity.getId().toString();
	}

	@Override
	public CDetailsBuilder getDetailsBuilder() { return detailsBuilder; }

	@Override
	public ISessionService getSessionService() { return sessionService; }

	@Override
	public CEntityDB<?> getValue() { return currentEntity; }

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Register this component to receive project change notifications
		getSessionService().addProjectChangeListener(this);
	}

	/** Called when the component is detached from the UI. Unregisters the project change listener to prevent memory leaks. */
	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		// Unregister this component to prevent memory leaks
		getSessionService().removeProjectChangeListener(this);
	}

	/** Implementation of CProjectChangeListener interface. Called when the active project changes via the SessionService.
	 * @param newProject The newly selected project */
	@Override
	public void onProjectChanged(final CProject newProject) {
		LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
	}

	@Override
	public void populateForm() throws Exception {
		try {
			// Use unified binder pattern: populateForm(entity) handles both:
			// 1. Bound fields via binder.setBean(entity)
			// 2. IContentOwner components via setValue(entity) + populateForm()
			if (detailsBuilder != null && getValue() != null) {
				LOGGER.debug("Populating form for entity: {}", getValue());
				detailsBuilder.populateForm(getValue());
			} else if (detailsBuilder != null) {
				LOGGER.debug("Clearing form - no current entity");
				detailsBuilder.populateForm();
			}
		} catch (final Exception e) {
			LOGGER.error("Error populating form.");
			throw e; // Rethrow to notify caller
		}
	}

	@Override
	public void setContentOwner(final IContentOwner owner) { parentContent = owner; }

	@Override
	protected void setupToolbar() {
		LOGGER.debug("Setting up toolbar in Sample Page");
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		try {
			if (entity == null) {
				LOGGER.debug("Setting current entity to null.");
				currentEntity = null;
				return;
			}
			// Fail fast if lazy relations were not initialized before binding.
			entity.initializeAllFields();
			validateLazyFieldsInitialized(entity);
			// LOGGER.debug("Setting current entity: {}", entity);
			currentEntity = entity;
		} catch (final Exception e) {
			LOGGER.error("Error setting current entity.");
			throw e;
		}
	}

	@SuppressWarnings ("static-method")
	private void validateLazyFieldsInitialized(final CEntityDB<?> entity) {
		Check.notNull(entity, "Entity cannot be null while validating lazy fields");
		for (final Field field : getAllFields(entity.getClass())) {
			if (!isSingleValueRelationField(field)) {
				continue;
			}
			field.setAccessible(true);
			try {
				final Object value = field.get(entity);
				if (value != null) {
					Check.isInitialized(value, "Lazy field '" + field.getName() + "' is not initialized for " + entity.getClass().getSimpleName());
				}
			} catch (final IllegalAccessException e) {
				LOGGER.error("Failed to validate lazy field {} for entity {}", field.getName(), entity.getClass().getSimpleName(), e);
				throw new IllegalStateException("Unable to validate lazy fields for " + entity.getClass().getSimpleName(), e);
			}
		}
	}
}
