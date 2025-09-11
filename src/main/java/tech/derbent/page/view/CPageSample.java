package tech.derbent.page.view;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.orders.domain.COrder;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.view.CComponentGridEntity;
import tech.derbent.session.service.CSessionService;

@Route ("cpagesample")
@PageTitle ("Sample Page")
@Menu (order = 1.1, icon = "class:tech.derbent.page.view.CPageEntityView", title = "Settings.Sample Page")
@PermitAll // When security is enabled, allow all authenticated users
public class CPageSample extends CPageBaseProjectAware {

	private static final long serialVersionUID = 1L;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return COrder.getIconColorCode(); // Use the static method from COrder
	}

	public static String getIconFilename() { return COrder.getIconFilename(); }

	private CGridEntityService gridEntityService;

	public CPageSample(final CSessionService sessionService, final CGridEntityService gridEntityService) {
		super(sessionService);
		this.gridEntityService = gridEntityService;
		createPageContent();
	}

	private void createPageContent() {
		add(new Div("This is a sample page. Project: "
				+ (sessionService.getActiveProject().get() != null ? sessionService.getActiveProject().get().getName() : "None")));
		CGridEntity gridEntity =
				gridEntityService.findByNameAndProject(CActivitiesView.VIEW_NAME, sessionService.getActiveProject().orElseThrow()).orElse(null);
		CComponentGridEntity grid = new CComponentGridEntity(gridEntity);
		this.add(grid);
	}
}
