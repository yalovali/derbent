package tech.derbent.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
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
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.ISessionService;

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
	private final ISessionService sessionService;

	@Autowired
	public CDynamicPageRouter(CPageEntityService pageEntityService, ISessionService sessionService, CDetailSectionService detailSectionService,
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
		loadSpecificPage(pageEntityId);
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
	private void loadSpecificPage(Long pageEntityId) {
		Check.notNull(pageEntityId, "Page entity ID cannot be null");
		LOGGER.debug("Loading specific page for entity ID: {}", pageEntityId);
		currentPageEntity =
				pageEntityService.getById(pageEntityId).orElseThrow(() -> new IllegalStateException("No page found for ID: " + pageEntityId));
		if (currentPageEntity.getRequiresAuthentication()) {
			sessionService.getActiveUser().orElseThrow(() -> new IllegalStateException("No active user found"));
		}
		try {
			CDynamicPageBase page = null;
			// Check if this page has grid and detail sections configured
			if (currentPageEntity.getGridEntity().getAttributeNone() == false) {
				LOGGER.debug("Creating dynamic page with grid and detail sections for: {}", currentPageEntity.getPageTitle());
				page = new CDynamicPageViewWithSections(currentPageEntity, sessionService, detailSectionService, gridEntityService,
						applicationContext);
			} else {
				LOGGER.debug("Creating standard dynamic page view for: {}", currentPageEntity.getPageTitle());
				page = new CDynamicPageViewWithoutGrid(currentPageEntity, sessionService, detailSectionService, applicationContext);
			}
			Check.notNull(page, "Dynamic page view cannot be null");
			removeAll();
			add(page);
		} catch (Exception e) {
			LOGGER.error("Failed to create dynamic page view for: {}", currentPageEntity.getPageTitle(), e);
			throw e;
		}
	}

	@Override
	public void setParameter(BeforeEvent event, Long parameter) {
		pageEntityId = parameter;
	}

	@Override
	protected void setupToolbar() {}
}
