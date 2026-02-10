package tech.derbent.api.page.view;

import java.lang.reflect.Field;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.view.CAbstractPage;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.ui.view.MainLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.api.session.service.ISessionService;

/** Router for dynamic pages that handles all database-defined page routes. This acts as a router for dynamic project pages. */
@Route (value = "cdynamicpagerouter", layout = MainLayout.class)
@PageTitle ("Project Pages")
@PermitAll
public class CDynamicPageRouter extends CAbstractPage implements HasUrlParameter<String> {

	public static final String DEFAULT_COLOR = "#BDB76B"; // X11 DarkKhaki - dynamic pages (darker)
	public static final String DEFAULT_ICON = "vaadin:file";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageRouter.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Dynamic Page View";

	/** Displays the provided project item in the dynamic one-pager slider using the configured router.
	 * @param onepagerEntity the entity to show, can be null to clear the view
	 * @param pageRouter     the router responsible for rendering the one-pager
	 * @param sessionService session service used to resolve the active project
	 * @param contentOwner   optional content owner to be notified of entity changes, can be null */
	public static void displayEntityInDynamicOnepager(final CProjectItem<?> onepagerEntity, final CDynamicPageRouter pageRouter,
			final ISessionService sessionService, final IContentOwner contentOwner) {
		Check.notNull(pageRouter, "Dynamic page router cannot be null");
		Check.notNull(sessionService, "Session service cannot be null");
		try {
			LOGGER.debug("Locating entity in dynamic page: {}", onepagerEntity != null ? onepagerEntity.getName() : "null");
			if (onepagerEntity == null) {
				pageRouter.loadSpecificPage(null, null, true, contentOwner);
				return;
			}
			final CPageEntityService pageService = CSpringContext.getBean(CPageEntityService.class);
			final Field viewNameField = onepagerEntity.getClass().getField("VIEW_NAME");
			final String entityViewName = (String) viewNameField.get(null);
			final CPageEntity page = pageService.findByNameAndProject(entityViewName, sessionService.getActiveProject().orElse(null)).orElseThrow();
			Check.notNull(page, "Screen service cannot be null");
			pageRouter.loadSpecificPage(page.getId(), onepagerEntity.getId(), true, contentOwner);
		} catch (final Exception e) {
			CNotificationService.showException("Error creating dynamic page for entity", e);
		}
	}

	public static void navigateToEntity(CEntityDB<?> entity) throws Exception {
		Check.notNull(entity, "Entity cannot be null for navigation");
		try {
			final CPageEntityService pageService = CSpringContext.getBean(CPageEntityService.class);
			final ISessionService sessionService = CSpringContext.getBean(ISessionService.class);
			final Class<?> clazz = entity.getClass();
			final Field viewNameField = clazz.getField("VIEW_NAME");
			final String entityViewName = (String) viewNameField.get(null);
			
			// Check if a page exists for this entity
			final Optional<CPageEntity> pageOpt = pageService.findByNameAndProject(entityViewName, sessionService.getActiveProject().orElse(null));
			if (pageOpt.isEmpty()) {
				LOGGER.warn("No page found for entity '{}' with view name '{}'. Entity was copied but navigation skipped.", 
					entity.toString(), entityViewName);
				// Show success notification instead of navigating
				CNotificationService.showSuccess("Entity copied successfully: " + entity.toString());
				return;
			}
			
			final CPageEntity page = pageOpt.get();
			Check.notNull(page, "Page entity cannot be null for navigation");
			final String route = "cdynamicpagerouter/page:" + page.getId() + "&item:" + entity.getId();
			LOGGER.debug("Navigating to entity page route: {}", route);
			// Navigate to the dynamic page router with parameters
			// Note: This requires access to the current UI context
			UI.getCurrent().navigate(route);
		} catch (final Exception e) {
			LOGGER.error("Error navigating to entity page for '{}': {}", entity.toString(), e.getMessage());
			throw e;
		}
	}

	private CPageEntity currentPageEntity = null;
	private final CDetailSectionService detailSectionService;
	private final CGridEntityService gridEntityService;
	private Long pageEntityId = null;
	private final CPageEntityService pageEntityService;
	private Long pageItemId = null;
	private final ISessionService sessionService;

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
		// LOGGER.info("CDynamicPageRouter initialized with grid and detail section support");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		try {
			loadSpecificPage(pageEntityId, pageItemId, false, null);
		} catch (final Exception e) {
			LOGGER.error("Error loading dynamic page for entity ID {}: {}", pageEntityId, e.getMessage());
			e.printStackTrace();
		}
	}

	/** Implementation of IPageTitleProvider - provides the page title from the current CPageEntity */
	@Override
	public String getPageTitle() {
		Check.notNull(currentPageEntity, "Current page entity cannot be null when getting page title");
		return currentPageEntity.getPageTitle();
	}

	/** Load a specific page by entity ID with optional content owner.
	 * @param pageEntityId1     Page entity ID
	 * @param pageItemId1       Page item ID
	 * @param AsDetailComponent Whether to load as detail component
	 * @param contentOwner      Optional content owner for updates
	 * @param object
	 * @throws Exception if page cannot be loaded */
	public void loadSpecificPage(Long pageEntityId1, Long pageItemId1, boolean AsDetailComponent, IContentOwner contentOwner) throws Exception {
		if (pageEntityId1 == null) {
			LOGGER.debug("No page entity ID provided, clearing dynamic page router content.");
			removeAll();
			return;
		}
		LOGGER.debug("Loading specific page for entity ID: {}", pageEntityId1);
		currentPageEntity =
				pageEntityService.getById(pageEntityId1).orElseThrow(() -> new IllegalStateException("No page found for ID: " + pageEntityId1));
		if (currentPageEntity.getRequiresAuthentication()) {
			sessionService.getActiveUser().orElseThrow(() -> new IllegalStateException("No active user found"));
		}
		try {
			CDynamicPageBase page = null;
			// Check if this page has grid and detail sections configured
			if (currentPageEntity.getGridEntity().getAttributeNone() == false) {
				if (AsDetailComponent) {
					page = new CDynamicSingleEntityPageView(currentPageEntity, sessionService, detailSectionService, pageItemId1);
				} else {
					page = new CDynamicPageViewWithSections(currentPageEntity, sessionService, detailSectionService, gridEntityService);
				}
			} else {
				page = new CDynamicPageViewWithoutGrid(null, currentPageEntity, sessionService, detailSectionService);
			}
			Check.notNull(page, "Dynamic page view cannot be null after instantiation");
			// Set content owner if provided to enable parent notification on entity changes
			if (contentOwner != null) {
				page.setContentOwner(contentOwner);
				LOGGER.debug("Set content owner for dynamic page: {}", contentOwner.getClass().getSimpleName());
			}
			if (pageItemId1 != null) {
				// Locate specific item on the page or just load it
				page.locateItemById(pageItemId1);
			} else {
				page.locateFirstEntity();
			}
			page.on_after_construct();
			removeAll();
			add(page);
		} catch (final Exception e) {
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
	protected void setupToolbar() { /*****/
	}
}
