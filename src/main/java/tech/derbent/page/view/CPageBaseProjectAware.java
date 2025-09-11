package tech.derbent.page.view;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.router.BeforeEnterEvent;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CProjectChangeListener;
import tech.derbent.abstracts.services.CDetailsBuilder;
import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.CSessionService;

public abstract class CPageBaseProjectAware extends CPageBase implements CProjectChangeListener {

	private static final long serialVersionUID = 1L;
	protected final CSessionService sessionService;
	protected CDiv divDetails;
	protected final CDetailsBuilder detailsBuilder = new CDetailsBuilder();
	private CDetailSectionService screenService;
	protected CEnhancedBinder<CEntityDB<?>> currentBinder; // Store current binder for data binding

	protected CPageBaseProjectAware(final CSessionService sessionService, CDetailSectionService screenService) {
		super();
		this.screenService = screenService;
		this.sessionService = sessionService;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		LOGGER.debug("Entering Sample Page");
	}

	@Override
	protected void onAttach(final AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		// Register this component to receive project change notifications
		sessionService.addProjectChangeListener(this);
	}

	/** Called when the component is detached from the UI. Unregisters the project change listener to prevent memory leaks. */
	@Override
	protected void onDetach(final DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		// Unregister this component to prevent memory leaks
		sessionService.removeProjectChangeListener(this);
	}

	/** Implementation of CProjectChangeListener interface. Called when the active project changes via the SessionService.
	 * @param newProject The newly selected project */
	@Override
	public void onProjectChanged(final CProject newProject) {
		LOGGER.debug("Project change notification received: {}", newProject != null ? newProject.getName() : "null");
	}

	public HasComponents getBaseDetailsLayout() { return divDetails; }

	@Override
	protected void setupToolbar() {
		LOGGER.debug("Setting up toolbar in Sample Page");
	}

	protected void buildScreen(final String baseViewName) {
		try {
			// Clear previous content from details layout to avoid accumulation
			getBaseDetailsLayout().removeAll();
			final CDetailSection screen = screenService.findByNameAndProject(sessionService.getActiveProject().orElse(null), baseViewName);
			if (screen == null) {
				final String errorMsg = "Screen not found: " + baseViewName + " for project: "
						+ sessionService.getActiveProject().map(CProject::getName).orElse("No Project");
				getBaseDetailsLayout().add(new CDiv(errorMsg));
				currentBinder = null; // Clear binder if screen not found
				return;
			}
			// Create a local binder for this specific screen instead of using page-level binder
			@SuppressWarnings ("unchecked")
			final CEnhancedBinder<CEntityDB<?>> localBinder = new CEnhancedBinder<>((Class<CEntityDB<?>>) (Class<?>) CEntityDB.class);
			currentBinder = localBinder; // Store the binder for data binding
			detailsBuilder.buildDetails(screen, localBinder, getBaseDetailsLayout());
		} catch (final Exception e) {
			final String errorMsg = "Error building details layout for screen: " + baseViewName;
			e.printStackTrace();
			getBaseDetailsLayout().add(new CDiv(errorMsg));
			currentBinder = null; // Clear binder on error
		}
	}

	/** Get the current binder for data binding operations */
	protected CEnhancedBinder<CEntityDB<?>> getCurrentBinder() { return currentBinder; }
}
