package tech.derbent.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.page.service.CPageEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.ISessionService;

/** Route for Project Overview dynamic page. */
@Route (value = "project-overview", layout = tech.derbent.api.ui.view.MainLayout.class)
@PageTitle ("Project Overview")
@PermitAll
class CProjectOverviewPage extends Div implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectOverviewPage.class);
	private static final long serialVersionUID = 1L;
	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;

	@Autowired
	public CProjectOverviewPage(CPageEntityService pageEntityService, ISessionService sessionService) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for project overview"));
		// Find the project overview page for the current project
		String targetRoute = "project-overview-" + activeProject.getId();
		CPageEntity pageEntity = pageEntityService.findByRoute(targetRoute)
				.orElseThrow(() -> new IllegalStateException("No project overview page found for this project"));
		CDynamicPageView dynamicPage = new CDynamicPageView(pageEntity, sessionService);
		removeAll();
		add(dynamicPage);
		LOGGER.info("Loaded project overview page for project: {}", activeProject.getName());
	}
}

/** Route for Resource Library dynamic page. */
@Route (value = "resource-library", layout = tech.derbent.api.ui.view.MainLayout.class)
@PageTitle ("Resource Library")
@PermitAll
class CResourceLibraryPage extends Div implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CResourceLibraryPage.class);
	private static final long serialVersionUID = 1L;
	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;

	@Autowired
	public CResourceLibraryPage(CPageEntityService pageEntityService, ISessionService sessionService) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for resource library"));
		// Find the resource library page for the current project
		String targetRoute = "resource-library-" + activeProject.getId();
		CPageEntity pageEntity = pageEntityService.findByRoute(targetRoute)
				.orElseThrow(() -> new IllegalStateException("No resource library page found for this project"));
		CDynamicPageView dynamicPage = new CDynamicPageView(pageEntity, sessionService);
		removeAll();
		add(dynamicPage);
		LOGGER.info("Loaded resource library page for project: {}", activeProject.getName());
	}
}

/** Route for Team Directory dynamic page. */
@Route (value = "team-directory", layout = tech.derbent.api.ui.view.MainLayout.class)
@PageTitle ("Team Directory")
@PermitAll
class CTeamDirectoryPage extends Div implements BeforeEnterObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTeamDirectoryPage.class);
	private static final long serialVersionUID = 1L;
	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;

	@Autowired
	public CTeamDirectoryPage(CPageEntityService pageEntityService, ISessionService sessionService) {
		Check.notNull(pageEntityService, "CPageEntityService cannot be null");
		Check.notNull(sessionService, "CSessionService cannot be null");
		this.pageEntityService = pageEntityService;
		this.sessionService = sessionService;
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		tech.derbent.projects.domain.CProject activeProject =
				sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found for team directory"));
		// Find the team directory page for the current project
		String targetRoute = "team-directory-" + activeProject.getId();
		CPageEntity pageEntity = pageEntityService.findByRoute(targetRoute)
				.orElseThrow(() -> new IllegalStateException("No team directory page found for this project"));
		CDynamicPageView dynamicPage = new CDynamicPageView(pageEntity, sessionService);
		removeAll();
		add(dynamicPage);
		LOGGER.info("Loaded team directory page for project: {}", activeProject.getName());
	}
}
