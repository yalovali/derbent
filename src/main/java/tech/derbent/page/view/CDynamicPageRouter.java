package tech.derbent.page.view;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.session.service.CSessionService;

/** Router for dynamic pages that handles all database-defined page routes. This acts as a router for dynamic project pages. */
@Route (value = "project-pages", layout = tech.derbent.base.ui.view.MainLayout.class)
@PageTitle ("Project Pages")
@PermitAll
public class CDynamicPageRouter extends Div implements BeforeEnterObserver, com.vaadin.flow.router.HasUrlParameter<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageRouter.class);
	private static final long serialVersionUID = 1L;
	private final CPageEntityService pageEntityService;
	private final CSessionService sessionService;
	private String routeParameter = null;

	@Autowired
	public CDynamicPageRouter(CPageEntityService pageEntityService, CSessionService sessionService) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
		LOGGER.info("CDynamicPageRouter initialized");
	}

	@Override
	public void setParameter(com.vaadin.flow.router.BeforeEvent event, String parameter) {
		this.routeParameter = parameter;
		LOGGER.debug("Dynamic page router called with parameter: {}", parameter);
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// If we have a route parameter, try to load that specific page
		if (routeParameter != null && !routeParameter.trim().isEmpty()) {
			loadSpecificPage(routeParameter, event);
			return;
		}
		// Otherwise, show page listing
		LOGGER.debug("Loading project pages overview");
		tech.derbent.projects.domain.CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for project pages"));
		Check.notNull(activeProject, "Active project cannot be null");
		// Get all dynamic pages for the current project
		List<CPageEntity> pages = pageEntityService.findActivePagesByProject(activeProject);
		Check.notNull(pages, "Pages list cannot be null");
		if (pages.isEmpty()) {
			showNoPages();
			return;
		}
		// Show page listing
		showPageListing(pages, activeProject);
	}

	/** Load a specific page by route parameter. */
	private void loadSpecificPage(String route, BeforeEnterEvent event) {
		Check.notBlank(route, "Route parameter cannot be blank");
		LOGGER.debug("Loading specific page for route: {}", route);
		Optional<CPageEntity> pageEntityOpt = pageEntityService.findByRoute(route);
		if (pageEntityOpt.isEmpty()) {
			LOGGER.error("No page entity found for route: {}", route);
			event.rerouteToError(com.vaadin.flow.router.NotFoundException.class, "Page not found for route: " + route);
			return;
		}
		CPageEntity pageEntity = pageEntityOpt.get();
		Check.notNull(pageEntity, "Page entity cannot be null");
		// Verify the page is active
		if (!pageEntity.getIsActive()) {
			LOGGER.error("Attempted to access inactive page: {}", pageEntity.getPageTitle());
			event.rerouteToError(IllegalStateException.class, "Page is not available: " + pageEntity.getPageTitle());
			return;
		}
		// Check authentication if required
		if (pageEntity.getRequiresAuthentication()) {
			Optional<tech.derbent.users.domain.CUser> activeUserOpt = sessionService.getActiveUser();
			if (activeUserOpt.isEmpty()) {
				LOGGER.error("Unauthenticated access attempted to protected page: {}", pageEntity.getPageTitle());
				event.rerouteToError(IllegalAccessException.class, "Authentication required to access: " + pageEntity.getPageTitle());
				return;
			}
		}
		// Check project context if needed
		if (pageEntity.getProject() != null) {
			Optional<tech.derbent.projects.domain.CProject> activeProjectOpt = sessionService.getActiveProject();
			if (activeProjectOpt.isEmpty()) {
				LOGGER.error("No active project found for page: {}", pageEntity.getPageTitle());
				showError("No active project selected. Please select a project to view this page.");
				return;
			}
			tech.derbent.projects.domain.CProject activeProject = activeProjectOpt.get();
			Check.notNull(activeProject, "Active project cannot be null");
			if (!pageEntity.getProject().getId().equals(activeProject.getId())) {
				LOGGER.error("Page project mismatch. Page project: {}, Active project: {}", pageEntity.getProject().getName(),
						activeProject.getName());
				showError("This page belongs to a different project. Please select the correct project.");
				return;
			}
		}
		// Create and display the dynamic page
		try {
			CDynamicPageView dynamicPageView = new CDynamicPageView(pageEntity, sessionService);
			Check.notNull(dynamicPageView, "Dynamic page view cannot be null");
			removeAll();
			add(dynamicPageView);
			LOGGER.info("Successfully loaded dynamic page: {} at route: {}", pageEntity.getPageTitle(), route);
		} catch (Exception e) {
			LOGGER.error("Failed to create dynamic page view for: {}", pageEntity.getPageTitle(), e);
			showError("Failed to load page: " + e.getMessage());
		}
	}

	/** Show a listing of all dynamic pages for the current project. */
	private void showPageListing(List<CPageEntity> pages, tech.derbent.projects.domain.CProject project) {
		Check.notNull(pages, "Pages list cannot be null");
		Check.notNull(project, "Project cannot be null");
		removeAll();
		H1 title = new H1("Project Pages - " + project.getName());
		title.addClassNames("page-title");
		Paragraph description = new Paragraph("Below are the custom pages available for this project. Click on any page to view its content.");
		add(title, description);
		// Create clickable links for each page
		for (CPageEntity page : pages) {
			Check.notNull(page, "Page entity cannot be null");
			Div pageCard = createPageCard(page);
			add(pageCard);
		}
		LOGGER.info("Displayed {} dynamic pages for project: {}", pages.size(), project.getName());
	}

	/** Create a card component for a page entity. */
	private Div createPageCard(CPageEntity page) {
		Div pageCard = new Div();
		pageCard.addClassNames("page-card");
		pageCard.getStyle().set("border", "1px solid #ddd").set("border-radius", "8px").set("padding", "16px").set("margin", "8px 0")
				.set("cursor", "pointer").set("transition", "box-shadow 0.2s");
		// Add hover effect
		pageCard.getElement().addEventListener("mouseover", e -> {
			pageCard.getStyle().set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");
		});
		pageCard.getElement().addEventListener("mouseout", e -> {
			pageCard.getStyle().set("box-shadow", "none");
		});
		// Create page content
		H1 pageTitle = new H1(page.getPageTitle());
		pageTitle.getStyle().set("margin", "0 0 8px 0").set("font-size", "1.5em");
		Paragraph pageDesc = new Paragraph(page.getDescription());
		pageDesc.getStyle().set("margin", "0 0 8px 0").set("color", "#666");
		Paragraph routeInfo = new Paragraph("Route: " + page.getRoute());
		routeInfo.getStyle().set("margin", "0").set("font-size", "0.9em").set("color", "#999");
		pageCard.add(pageTitle, pageDesc, routeInfo);
		// Add click handler to navigate to the specific page
		pageCard.getElement().addEventListener("click", e -> {
			// Create and show the dynamic page view
			CDynamicPageView dynamicPage = new CDynamicPageView(page, sessionService);
			removeAll();
			add(dynamicPage);
			// Update page title
			getElement().executeJs("document.title = $0", page.getPageTitle());
			LOGGER.info("Navigated to dynamic page: {}", page.getPageTitle());
		});
		return pageCard;
	}

	/** Show message when no pages are available. */
	private void showNoPages() {
		removeAll();
		H1 title = new H1("No Project Pages");
		Paragraph message = new Paragraph("No custom pages have been created for this project yet.");
		add(title, message);
	}

	/** Show error message when page cannot be loaded. */
	private void showError(String message) {
		Check.notBlank(message, "Error message cannot be blank");
		removeAll();
		H1 errorTitle = new H1("Page Load Error");
		Paragraph errorMessage = new Paragraph(message);
		errorMessage.getStyle().set("color", "red");
		add(errorTitle, errorMessage);
		LOGGER.error("Showing error to user: {}", message);
	}
}
