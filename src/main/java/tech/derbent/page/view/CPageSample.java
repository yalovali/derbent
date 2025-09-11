package tech.derbent.page.view;

import java.lang.reflect.Field;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CEntityUpdateListener;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.activities.view.CActivitiesView;
import tech.derbent.orders.domain.COrder;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.screens.view.CComponentGridEntity;
import tech.derbent.session.service.CSessionService;

@Route ("cpagesample")
@PageTitle ("Sample Page")
@Menu (order = 1.1, icon = "class:tech.derbent.page.view.CPageEntityView", title = "Settings.Sample Page")
@PermitAll // When security is enabled, allow all authenticated users
public class CPageSample extends CPageBaseProjectAware implements CEntityUpdateListener {

	private static final long serialVersionUID = 1L;
	private CComponentGridEntity grid;

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() {
		return COrder.getIconColorCode(); // Use the static method from COrder
	}

	public static String getIconFilename() { return COrder.getIconFilename(); }

	private CGridEntityService gridEntityService;

	public CPageSample(final CSessionService sessionService, final CGridEntityService gridEntityService, final CDetailSectionService screenService) {
		super(sessionService, screenService);
		this.gridEntityService = gridEntityService;
		createPageContent();
	}

	private void createPageContent() {
		// Create and configure grid
		CGridEntity gridEntity =
				gridEntityService.findByNameAndProject(CActivitiesView.VIEW_NAME, sessionService.getActiveProject().orElseThrow()).orElse(null);
		grid = new CComponentGridEntity(gridEntity);
		// Listen for selection changes from the grid
		grid.addSelectionChangeListener(event -> {
			try {
				onEntitySelected(event);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		this.add(grid);
		divDetails = new CDiv();
		this.add(divDetails);
		add(new Div("This is a sample page. Project: "
				+ (sessionService.getActiveProject().get() != null ? sessionService.getActiveProject().get().getName() : "None")));
	}

	/** Handles entity selection events from the grid
	 * @throws Exception */
	private void onEntitySelected(CComponentGridEntity.SelectionChangeEvent event) throws Exception {
		CEntityDB<?> selectedEntity = event.getSelectedItem();
		populateEntityDetails(selectedEntity);
	}

	/** Implementation of CEntityUpdateListener - called when an entity is saved */
	@Override
	public void onEntitySaved(CEntityDB<?> entity) {
		LOGGER.debug("Entity saved notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
		refreshGrid();
	}

	/** Implementation of CEntityUpdateListener - called when an entity is deleted */
	@Override
	public void onEntityDeleted(CEntityDB<?> entity) {
		LOGGER.debug("Entity deleted notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
		refreshGrid();
		// Clear the details section since the entity no longer exists
		getBaseDetailsLayout().removeAll();
	}

	/** Populates the entity details section with information from the selected entity */
	private void populateEntityDetails(CEntityDB<?> entity) throws Exception {
		if (entity == null) {
			return;
		}
		Class<? extends CAbstractEntityDBPage<?>> entityViewClass = entity.getViewClass();
		Check.notNull(entityViewClass, "Entity view class cannot be null for entity: " + entity.getClass().getSimpleName());
		// get view name by invoke static field named VIEW_NAME of entityViewClass
		Field viewNameField = entityViewClass.getField("VIEW_NAME");
		String viewName = (String) viewNameField.get(null);
		buildScreen(viewName);
	}

	/** Refreshes the grid to show updated data */
	private void refreshGrid() {
		if (grid != null) {
			try {
				// Use reflection to call the private refreshGridData method
				java.lang.reflect.Method refreshMethod = grid.getClass().getDeclaredMethod("refreshGridData");
				refreshMethod.setAccessible(true);
				refreshMethod.invoke(grid);
				LOGGER.debug("Grid refreshed successfully");
			} catch (Exception e) {
				LOGGER.warn("Error refreshing grid: {}", e.getMessage());
			}
		}
	}
}
