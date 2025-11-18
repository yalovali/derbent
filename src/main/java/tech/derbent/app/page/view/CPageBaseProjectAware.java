package tech.derbent.app.page.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasContentOwner;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.CCrudToolbar;
import tech.derbent.api.ui.component.CDiv;
import tech.derbent.api.ui.component.CFlexLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.CLayoutService;
import tech.derbent.base.session.service.ISessionService;

public abstract class CPageBaseProjectAware extends CPageBase implements IProjectChangeListener, IContentOwner, IHasContentOwner {

	private static final long serialVersionUID = 1L;
	protected CFlexLayout baseDetailsLayout = CFlexLayout.forEntityPage();
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
	protected void configureCrudToolbar(final CCrudToolbar toolbar) {
		// Default implementation does nothing - subclasses can override to add specific configuration
	}

	/** Abstract method to create a new entity instance with project set
	 * @throws Exception */
	protected abstract <T extends CEntityDB<T>> T createNewEntity() throws Exception;

	public CFlexLayout getBaseDetailsLayout() { return baseDetailsLayout; }

	/** Get the current binder for data binding operations (public access for PageService pattern) */
	public CEnhancedBinder<CEntityDB<?>> getBinder() {
		return currentBinder;
	}

	@Override
	public IContentOwner getContentOwner() { return parentContent; }

	/** Get the current binder for data binding operations */
	protected CEnhancedBinder<CEntityDB<?>> getCurrentBinder() { return currentBinder; }

	@Override
	public CEntityDB<?> getCurrentEntity() { return currentEntity; }

	@Override
	public String getCurrentEntityIdString() {
		LOGGER.debug("Getting current entity ID string for page.");
		if (currentEntity == null) {
			return null;
		}
		if (currentEntity instanceof CEntityDB<?>) {
			final CEntityDB<?> entity = currentEntity;
			return entity.getId().toString();
		}
		return null;
	}

	public CDetailsBuilder getDetailsBuilder() { return detailsBuilder; }

	public ISessionService getSessionService() { return sessionService; }

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
			detailsBuilder.setCurrentEntity(currentEntity);
			// Default implementation - populate current binder if available
			if (currentBinder != null && getCurrentEntity() != null) {
				LOGGER.debug("Populating form for entity: {}", getCurrentEntity());
				currentBinder.setBean(getCurrentEntity());
			} else if (currentBinder != null) {
				LOGGER.debug("Clearing form - no current entity");
				currentBinder.setBean(null);
			}
			// Also populate details builder if available
			if (detailsBuilder != null) {
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
	public void setCurrentEntity(final CEntityDB<?> entity) {
		try {
			if (entity == null) {
				LOGGER.debug("Setting current entity to null.");
			}
			LOGGER.debug("Setting current entity: {}", entity);
			currentEntity = entity;
		} catch (final Exception e) {
			LOGGER.error("Error setting current entity.");
			throw e;
		}
	}

	@Override
	protected void setupToolbar() {
		LOGGER.debug("Setting up toolbar in Sample Page");
	}
}
