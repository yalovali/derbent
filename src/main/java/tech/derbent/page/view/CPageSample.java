package tech.derbent.page.view;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.domains.CEntityDB;
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
	private VerticalLayout entityDetailsLayout;
	private H3 entityTitle;
	private Paragraph entityDetails;

	public CPageSample(final CSessionService sessionService, final CGridEntityService gridEntityService) {
		super(sessionService);
		this.gridEntityService = gridEntityService;
		createPageContent();
	}

	private void createPageContent() {
		add(new Div("This is a sample page. Project: "
				+ (sessionService.getActiveProject().get() != null ? sessionService.getActiveProject().get().getName() : "None")));
		// Create entity details section
		createEntityDetailsSection();
		// Create and configure grid
		CGridEntity gridEntity =
				gridEntityService.findByNameAndProject(CActivitiesView.VIEW_NAME, sessionService.getActiveProject().orElseThrow()).orElse(null);
		CComponentGridEntity grid = new CComponentGridEntity(gridEntity);
		// Listen for selection changes from the grid
		grid.addSelectionChangeListener(this::onEntitySelected);
		this.add(grid);
	}

	/** Creates the entity details section that will be populated when an entity is selected */
	private void createEntityDetailsSection() {
		entityDetailsLayout = new VerticalLayout();
		entityDetailsLayout.setPadding(true);
		entityDetailsLayout.setSpacing(true);
		entityTitle = new H3("No entity selected");
		entityDetails = new Paragraph("Select an entity from the grid above to see its details here.");
		entityDetailsLayout.add(entityTitle, entityDetails);
		this.add(entityDetailsLayout);
	}

	/** Handles entity selection events from the grid */
	private void onEntitySelected(CComponentGridEntity.SelectionChangeEvent event) {
		CEntityDB<?> selectedEntity = event.getSelectedItem();
		populateEntityDetails(selectedEntity);
	}

	/** Populates the entity details section with information from the selected entity */
	private void populateEntityDetails(CEntityDB<?> entity) {
		if (entity == null) {
			entityTitle.setText("No entity selected");
			entityDetails.setText("Select an entity from the grid above to see its details here.");
			return;
		}
		// Update the title with entity type and ID
		entityTitle.setText(entity.getClass().getSimpleName() + " #" + entity.getId());
		// Build details text using reflection to show entity properties
		StringBuilder details = new StringBuilder();
		details.append("Entity Type: ").append(entity.getClass().getSimpleName()).append("\n");
		details.append("ID: ").append(entity.getId()).append("\n");
		// Try to get common properties using reflection
		try {
			// Try to get name property
			try {
				java.lang.reflect.Method getNameMethod = entity.getClass().getMethod("getName");
				Object name = getNameMethod.invoke(entity);
				if (name != null) {
					details.append("Name: ").append(name.toString()).append("\n");
				}
			} catch (Exception e) {
				// getName method not available, ignore
			}
			// Try to get description property
			try {
				java.lang.reflect.Method getDescriptionMethod = entity.getClass().getMethod("getDescription");
				Object description = getDescriptionMethod.invoke(entity);
				if (description != null) {
					details.append("Description: ").append(description.toString()).append("\n");
				}
			} catch (Exception e) {
				// getDescription method not available, ignore
			}
			// Try to get createdDate property
			try {
				java.lang.reflect.Method getCreatedDateMethod = entity.getClass().getMethod("getCreatedDate");
				Object createdDate = getCreatedDateMethod.invoke(entity);
				if (createdDate != null) {
					details.append("Created Date: ").append(createdDate.toString()).append("\n");
				}
			} catch (Exception e) {
				// getCreatedDate method not available, ignore
			}
		} catch (Exception e) {
			details.append("Could not load entity details: ").append(e.getMessage());
		}
		entityDetails.setText(details.toString());
	}
}
