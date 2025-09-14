package tech.derbent.page.view;

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
 * Route for Project Overview dynamic page.
 */
@Route(value = "project-overview", layout = tech.derbent.base.ui.view.MainLayout.class)
@PageTitle("Project Overview")
@PermitAll
class CProjectOverviewPage extends Div implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectOverviewPage.class);
	private static final long serialVersionUID = 1L;

	private final CPageService pageService;
	private final CSessionService sessionService;

	@Autowired
	public CProjectOverviewPage(CPageService pageService, CSessionService sessionService) {
		this.pageService = pageService;
		this.sessionService = sessionService;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			showError("No active project selected");
			return;
		}

		// Find the project overview page for the current project
		String targetRoute = "project-overview-" + activeProject.get().getId();
		Optional<CPageEntity> pageEntity = pageService.findByRoute(targetRoute);

		if (pageEntity.isPresent()) {
			CDynamicPageView dynamicPage = new CDynamicPageView(pageEntity.get(), sessionService);
			removeAll();
			add(dynamicPage);
			LOGGER.info("Loaded project overview page for project: {}", activeProject.get().getName());
		} else {
			showError("Project overview page not found for this project");
		}
	}

	private void showError(String message) {
		removeAll();
		add(new H1("Error"), new Paragraph(message));
	}
}

/**
 * Route for Team Directory dynamic page.
 */
@Route(value = "team-directory", layout = tech.derbent.base.ui.view.MainLayout.class)
@PageTitle("Team Directory")
@PermitAll
class CTeamDirectoryPage extends Div implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTeamDirectoryPage.class);
	private static final long serialVersionUID = 1L;

	private final CPageService pageService;
	private final CSessionService sessionService;

	@Autowired
	public CTeamDirectoryPage(CPageService pageService, CSessionService sessionService) {
		this.pageService = pageService;
		this.sessionService = sessionService;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			showError("No active project selected");
			return;
		}

		// Find the team directory page for the current project
		String targetRoute = "team-directory-" + activeProject.get().getId();
		Optional<CPageEntity> pageEntity = pageService.findByRoute(targetRoute);

		if (pageEntity.isPresent()) {
			CDynamicPageView dynamicPage = new CDynamicPageView(pageEntity.get(), sessionService);
			removeAll();
			add(dynamicPage);
			LOGGER.info("Loaded team directory page for project: {}", activeProject.get().getName());
		} else {
			showError("Team directory page not found for this project");
		}
	}

	private void showError(String message) {
		removeAll();
		add(new H1("Error"), new Paragraph(message));
	}
}

/**
 * Route for Resource Library dynamic page.
 */
@Route(value = "resource-library", layout = tech.derbent.base.ui.view.MainLayout.class)
@PageTitle("Resource Library")
@PermitAll
class CResourceLibraryPage extends Div implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CResourceLibraryPage.class);
	private static final long serialVersionUID = 1L;

	private final CPageService pageService;
	private final CSessionService sessionService;

	@Autowired
	public CResourceLibraryPage(CPageService pageService, CSessionService sessionService) {
		this.pageService = pageService;
		this.sessionService = sessionService;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		Optional<tech.derbent.projects.domain.CProject> activeProject = sessionService.getActiveProject();
		if (activeProject.isEmpty()) {
			showError("No active project selected");
			return;
		}

		// Find the resource library page for the current project
		String targetRoute = "resource-library-" + activeProject.get().getId();
		Optional<CPageEntity> pageEntity = pageService.findByRoute(targetRoute);

		if (pageEntity.isPresent()) {
			CDynamicPageView dynamicPage = new CDynamicPageView(pageEntity.get(), sessionService);
			removeAll();
			add(dynamicPage);
			LOGGER.info("Loaded resource library page for project: {}", activeProject.get().getName());
		} else {
			showError("Resource library page not found for this project");
		}
	}

	private void showError(String message) {
		removeAll();
		add(new H1("Error"), new Paragraph(message));
	}
}