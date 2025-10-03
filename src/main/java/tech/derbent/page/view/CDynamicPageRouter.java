package tech.derbent.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.interfaces.IPageTitleProvider;
import tech.derbent.api.ui.view.MainLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CAbstractPage;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

/** Router for dynamic pages that handles all database-defined page routes. This acts as a router for dynamic project pages. */
@Route (value = "cdynamicpagerouter", layout = MainLayout.class)
@PageTitle ("Project Pages")
@PermitAll
public class CDynamicPageRouter extends CAbstractPage implements BeforeEnterObserver, HasUrlParameter<Long>, IPageTitleProvider {

	public static final String DEFAULT_COLOR = "#623700";
	public static final String DEFAULT_ICON = "vaadin:cutlery";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageRouter.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Dynamic Page View";
	private final ApplicationContext applicationContext;
	private CPageEntity currentPageEntity = null;
	private final CDetailSectionService detailSectionService;
	private final CGridEntityService gridEntityService;
	private Long pageEntityId = null;
	private final CPageEntityService pageEntityService;
	private final CSessionService sessionService;

	@Autowired
	public CDynamicPageRouter(CPageEntityService pageEntityService, CSessionService sessionService, CDetailSectionService detailSectionService,
			CGridEntityService gridEntityService, ApplicationContext applicationContext) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		Check.notNull(detailSectionService, "CDetailSectionService cannot be null");
		Check.notNull(gridEntityService, "CGridEntityService cannot be null");
		Check.notNull(applicationContext, "ApplicationContext cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
		this.detailSectionService = detailSectionService;
		this.gridEntityService = gridEntityService;
		this.applicationContext = applicationContext;
		LOGGER.info("CDynamicPageRouter initialized with grid and detail section support");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// If we have a page entity ID, try to load that specific page
		loadSpecificPage(pageEntityId, event);
	}

	/** Implementation of IPageTitleProvider - provides the page title from the current CPageEntity */
	@Override
	public String getPageTitle() { return currentPageEntity != null ? currentPageEntity.getPageTitle() : null; }

	/** Check if the page entity has grid and detail section configuration. */
	private boolean hasGridAndDetailConfiguration(CPageEntity pageEntity) {
		Check.notNull(pageEntity, "Page entity cannot be null");
		return pageEntity.getGridEntity() != null && pageEntity.getDetailSection() != null && pageEntity.getGridEntity().getAttributeNone() == false;
	}

	/** Load a specific page by entity ID. */
	private void loadSpecificPage(Long pageEntityId, BeforeEnterEvent event) {
		Check.notNull(pageEntityId, "Page entity ID cannot be null");
		LOGGER.debug("Loading specific page for entity ID: {}", pageEntityId);
		CPageEntity pageEntity =
				pageEntityService.getById(pageEntityId).orElseThrow(() -> new IllegalStateException("No page found for ID: " + pageEntityId));
		// Store the current page entity for title provider
		currentPageEntity = pageEntity;
		// Verify the page is active
		Check.isTrue(pageEntity.getIsActive(), "Page entity must be active");
		// Check authentication if required
		if (pageEntity.getRequiresAuthentication()) {
			sessionService.getActiveUser().orElseThrow(() -> new IllegalStateException("No active user found"));
		}
		// Check project context if needed
		Check.notNull(pageEntity.getProject(), "Page entity project cannot be null");
		CProject activeProjectOpt = sessionService.getActiveProject()
				.orElseThrow(() -> new IllegalStateException("No active project found when loading page: " + pageEntity.getPageTitle()));
		Check.isTrue(pageEntity.getProject().getId().equals(activeProjectOpt.getId()), "Page project mismatch.");
		// Create and display the dynamic page
		try {
			// Check if this page has grid and detail sections configured
			if (hasGridAndDetailConfiguration(pageEntity)) {
				LOGGER.debug("Creating dynamic page with grid and detail sections for: {}", pageEntity.getPageTitle());
				CDynamicPageViewWithSections dynamicPageViewWithSections =
						new CDynamicPageViewWithSections(pageEntity, sessionService, detailSectionService, gridEntityService, applicationContext);
				Check.notNull(dynamicPageViewWithSections, "Dynamic page view with sections cannot be null");
				removeAll();
				add(dynamicPageViewWithSections);
				LOGGER.info("Successfully loaded dynamic page with sections: {} with ID: {}", pageEntity.getPageTitle(), pageEntityId);
			} else {
				LOGGER.debug("Creating standard dynamic page view for: {}", pageEntity.getPageTitle());
				CDynamicPageView dynamicPageView = new CDynamicPageView(pageEntity, sessionService, detailSectionService);
				Check.notNull(dynamicPageView, "Dynamic page view cannot be null");
				removeAll();
				add(dynamicPageView);
				LOGGER.info("Successfully loaded dynamic page: {} with ID: {}", pageEntity.getPageTitle(), pageEntityId);
			}
		} catch (Exception e) {
			LOGGER.error("Failed to create dynamic page view for: {}", pageEntity.getPageTitle(), e);
			throw e;
		}
	}

	@Override
	public void setParameter(com.vaadin.flow.router.BeforeEvent event, Long parameter) {
		pageEntityId = parameter;
		// LOGGER.debug("Dynamic page router called with page entity ID: {}", parameter);
	}

	@Override
	protected void setupToolbar() {
		// TODO Auto-generated method stub
	}
}
