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
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageService;
import tech.derbent.session.service.CSessionService;

/**
 * Router for dynamic pages that handles all database-defined page routes.
 * This acts as a router for dynamic project pages.
 */
@Route(value = "project-pages", layout = tech.derbent.base.ui.view.MainLayout.class)
@PageTitle("Project Pages")
@PermitAll
public class CDynamicPageRouter extends Div implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageRouter.class);
	private static final long serialVersionUID = 1L;

	private final CPageService pageService;
	private final CSessionService sessionService;

	@Autowired
	public CDynamicPageRouter(CPageService pageService, CSessionService sessionService) {
		this.pageService = pageService;
		this.sessionService = sessionService;
		LOGGER.info("CDynamicPageRouter initialized");
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		LOGGER.debug("Loading project pages overview");

		Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			showNoActiveProject();
			return;
		}

		// Get all dynamic pages for the current project
		List<CPageEntity> pages = pageService.getMenuPagesForProject(activeProject.get());
		
		if (pages.isEmpty()) {
			showNoPages();
			return;
		}

		// Show page listing
		showPageListing(pages, activeProject.get());
	}

	/**
	 * Show a listing of all dynamic pages for the current project.
	 */
	private void showPageListing(List<CPageEntity> pages, tech.derbent.projects.domain.CProject project) {
		removeAll();
		
		H1 title = new H1("Project Pages - " + project.getName());
		title.addClassNames("page-title");
		
		Paragraph description = new Paragraph(
				"Below are the custom pages available for this project. Click on any page to view its content."
		);
		
		add(title, description);
		
		// Create clickable links for each page
		for (CPageEntity page : pages) {
			Div pageCard = new Div();
			pageCard.addClassNames("page-card");
			pageCard.getStyle()
				.set("border", "1px solid #ddd")
				.set("border-radius", "8px")
				.set("padding", "16px")
				.set("margin", "8px 0")
				.set("cursor", "pointer")
				.set("transition", "box-shadow 0.2s");
			
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
			
			add(pageCard);
		}
		
		LOGGER.info("Displayed {} dynamic pages for project: {}", pages.size(), project.getName());
	}

	/**
	 * Show error when no active project is selected.
	 */
	private void showNoActiveProject() {
		removeAll();
		
		H1 title = new H1("No Active Project");
		Paragraph message = new Paragraph(
				"Please select an active project to view project pages."
		);
		
		add(title, message);
	}

	/**
	 * Show message when no pages are available.
	 */
	private void showNoPages() {
		removeAll();
		
		H1 title = new H1("No Project Pages");
		Paragraph message = new Paragraph(
				"No custom pages have been created for this project yet."
		);
		
		add(title, message);
	}
}