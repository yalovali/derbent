package tech.derbent.app.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.view.CAbstractPage;
import tech.derbent.api.interfaces.IPageTitleProvider;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.ui.view.MainLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.base.session.service.ISessionService;

/** Router for dynamic pages that handles all database-defined page routes. This acts as a router for dynamic project pages. */
@Route (value = "cdynamicpagerouter", layout = MainLayout.class)
@PageTitle ("Project Pages")
@PermitAll
public class CDynamicPageRouter extends CAbstractPage implements BeforeEnterObserver, HasUrlParameter<String>, IPageTitleProvider {

	public static final String DEFAULT_COLOR = "#623700";
	public static final String DEFAULT_ICON = "vaadin:cutlery";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageRouter.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Dynamic Page View";
	private CPageEntity currentPageEntity = null;
	private final CDetailSectionService detailSectionService;
	private final CGridEntityService gridEntityService;
	private Long pageEntityId = null;
	private final CPageEntityService pageEntityService;
	private Long pageItemId = null;
	private final ISessionService sessionService;

	@Autowired
	public CDynamicPageRouter(CPageEntityService pageEntityService, ISessionService sessionService, CDetailSectionService detailSectionService,
			CGridEntityService gridEntityService) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		Check.notNull(detailSectionService, "CDetailSectionService cannot be null");
		// Check.notNull(gridEntityService, "CGridEntityService cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
		this.detailSectionService = detailSectionService;
		this.gridEntityService = gridEntityService;
		LOGGER.info("CDynamicPageRouter initialized with grid and detail section support");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		try {
			loadSpecificPage(pageEntityId, pageItemId, false);
		} catch (Exception e) {
			LOGGER.error("Error loading dynamic page for entity ID {}: {}", pageEntityId, e.getMessage());
			e.printStackTrace();
		}
	}

	/** Implementation of IPageTitleProvider - provides the page title from the current CPageEntity */
	@Override
	public String getPageTitle() { return currentPageEntity != null ? currentPageEntity.getPageTitle() : null; }

	/** Load a specific page by entity ID.
	 * @param pageItemId
	 * @throws Exception */
	public void loadSpecificPage(Long pageEntityId, Long pageItemId, boolean AsDetailComponent) throws Exception {
		if (pageEntityId == null || pageItemId == null) {
			LOGGER.warn("Page entity ID or page item ID is null. Cannot load specific page.");
			return;
		}
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
				if (AsDetailComponent) {
					page = new CDynamicSingleEntityPageView(currentPageEntity, sessionService, detailSectionService);
				} else {
					page = new CDynamicPageViewWithSections(currentPageEntity, sessionService, detailSectionService, gridEntityService);
				}
			} else {
				page = new CDynamicPageViewWithoutGrid(null, currentPageEntity, sessionService, detailSectionService);
			}
			Check.notNull(page, "Dynamic page view cannot be null after instantiation");
			page.locateItemById(pageItemId);
			removeAll();
			add(page);
		} catch (Exception e) {
			LOGGER.error("Failed to create dynamic page view for: {}", currentPageEntity.getPageTitle(), e);
			throw e;
		}
	}

	@Override
	public void setParameter(BeforeEvent event, String parameter) {
		// format is page:{id}/item:{id}
		if (parameter.startsWith("page:")) {
			pageEntityId = Long.parseLong(parameter.substring(5).split("&")[0]);
			pageItemId = parameter.contains("&item:") ? Long.parseLong(parameter.split("&item:")[1]) : null;
		} else {
			LOGGER.warn("Invalid parameter format for CDynamicPageRouter: {}", parameter);
		}
	}

	@Override
	protected void setupToolbar() {}
}
