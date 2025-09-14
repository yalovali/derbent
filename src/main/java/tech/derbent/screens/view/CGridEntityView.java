package tech.derbent.screens.view;

import java.util.List;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.components.CVerticalLayout;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.abstracts.views.grids.CGridViewBaseProject;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CGridEntityService;
import tech.derbent.session.service.CSessionService;

@Route ("cgridentityview")
@PageTitle ("Grids Detail")
@Menu (order = 1.5, icon = "class:tech.derbent.screens.view.CGridEntityView", title = "Setup.UI.Grids")
@PermitAll
public class CGridEntityView extends CGridViewBaseProject<CGridEntity> {

	private static final long serialVersionUID = 1L;

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() {
		return CGridEntity.getStaticIconColorCode(); // Use the static method from CScreen
	}

	public static String getStaticIconFilename() { return CGridEntity.getStaticIconFilename(); }

	private final String ENTITY_ID_FIELD = "grid_entity_id";
	private CFieldSelectionComponent fieldSelectionComponent;

	public CGridEntityView(final CGridEntityService entityService, final CSessionService sessionService, final CDetailSectionService screenService) {
		super(CGridEntity.class, entityService, sessionService, screenService);
	}

	@Override
	public void createGridForEntity(final CGrid<CGridEntity> grid) {
		grid.addIdColumn(CEntityDB::getId, "#", ENTITY_ID_FIELD);
		grid.addColumnEntityNamed(CEntityOfProject::getProject, "Project");
		grid.addShortTextColumn(CEntityNamed::getName, "Name", "name");
		grid.addColumn(CEntityNamed::getDescriptionShort, "Description");
		grid.addShortTextColumn(CGridEntity::getDataServiceBeanName, "Data Service Bean", "dataServiceBeanName");
	}

	@Override
	protected String getEntityRouteIdField() { return ENTITY_ID_FIELD; }

	@Override
	protected void createDetailsComponent() throws Exception {
		// Create the field selection component
		fieldSelectionComponent = new CFieldSelectionComponent("Field Selection", getCurrentEntity().getClass().getSimpleName());
	}

	@Override
	public void updateDetailsComponent() throws Exception {
		// buildScreen(CMeetingViewService.BASE_VIEW_NAME);
		// Create the basic form using the entity annotations
		final List<String> entityFields = List.of("name", "description", "dataServiceBeanName");
		final CVerticalLayout basicFormLayout = CEntityFormBuilder.buildForm(CGridEntity.class, getBinder(), entityFields);
		// Create tabs for basic info and field selection
		Tab basicTab = new Tab("Basic Information");
		Tab fieldsTab = new Tab("Field Selection");
		Tabs tabs = new Tabs(basicTab, fieldsTab);
		tabs.addThemeVariants(TabsVariant.LUMO_MINIMAL);
		tabs.setWidthFull();
		// Create content areas
		CVerticalLayout basicContent = new CVerticalLayout();
		basicContent.add(basicFormLayout);
		basicContent.setVisible(true);
		CVerticalLayout fieldsContent = new CVerticalLayout();
		fieldsContent.add(fieldSelectionComponent);
		fieldsContent.setVisible(false);
		// Handle tab changes
		tabs.addSelectedChangeListener(event -> {
			boolean isBasicSelected = event.getSelectedTab() == basicTab;
			basicContent.setVisible(isBasicSelected);
			fieldsContent.setVisible(!isBasicSelected);
			// Update field selection when switching to fields tab
			if (!isBasicSelected) {
				updateFieldSelectionComponent();
			}
		});
		// Add content to details layout
		getBaseDetailsLayout().add(tabs, basicContent, fieldsContent);
		// Set up data binding for field selection
		setupFieldSelectionBinding();
	}

	private void updateFieldSelectionComponent() {
		CGridEntity currentEntity = getCurrentEntity();
		Check.notNull(currentEntity, "Current entity cannot be null");
		Check.notNull(fieldSelectionComponent, "Field selection component is not initialized");
		Check.notNull(currentEntity.getDataServiceBeanName(), "Data service bean name cannot be null");
		// Extract entity type from bean name (assuming convention like CActivityService -> CActivity)
		String beanName = currentEntity.getDataServiceBeanName();
		String entityType = extractEntityTypeFromBeanName(beanName);
		Check.notBlank(entityType, "Extracted entity type cannot be null or empty");
		fieldSelectionComponent.setEntityType(entityType);
		// Load existing selection
		String existingSelection = currentEntity.getSelectedFields();
		Check.notNull(existingSelection, "Existing selection cannot be null");
		fieldSelectionComponent.setSelectedFieldsFromString(existingSelection);
	}

	private String extractEntityTypeFromBeanName(String beanName) {
		Check.notNull(beanName, "Bean name cannot be null");
		Check.notBlank(beanName, "Bean name cannot be empty");
		// Convert service bean name to entity class name
		// E.g., CActivityService -> CActivity
		Check.isTrue(beanName.length() > "Service".length(), "Bean name is too short to extract entity type");
		Check.isTrue(beanName.endsWith("Service"), "Bean name must end with 'Service'");
		return beanName.substring(0, beanName.length() - "Service".length());
	}

	private void setupFieldSelectionBinding() {
		// The field selection will be handled manually during save process
		// since our custom component doesn't directly implement HasValue<String>
	}

	@Override
	protected void populateForm(CGridEntity entity) {
		super.populateForm(entity);
		updateFieldSelectionComponent();
	}

	@Override
	protected boolean onBeforeSaveEvent() {
		if (!super.onBeforeSaveEvent()) {
			LOGGER.warn("Superclass onBeforeSaveEvent failed, cannot save entity.");
			return false;
		}
		final CEnhancedBinder<CGridEntity> binder = getBinder();
		Check.notNull(binder, "Binder is not initialized");
		CGridEntity bean = binder.getBean();
		Check.notNull(bean, "No entity is bound to the binder");
		Check.notNull(fieldSelectionComponent, "Field selection component is not initialized");
		final String selected = fieldSelectionComponent.getSelectedFieldsAsString();
		bean.setSelectedFields(selected != null ? selected : "");
		return true;
	}
}
