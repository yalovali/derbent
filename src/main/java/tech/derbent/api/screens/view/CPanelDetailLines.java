package tech.derbent.api.screens.view;

import java.util.List;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CViewsService;
import tech.derbent.api.ui.component.CComponentListDetailLines;
import tech.derbent.api.utils.Check;

public class CPanelDetailLines extends CPanelDetailSectionBase {
	private static final long serialVersionUID = 1L;
	private final CComponentListDetailLines listComponent;

	public CPanelDetailLines(final IContentOwner parentContent, final CDetailSection currentEntity,
			final CEnhancedBinder<CDetailSection> beanValidationBinder, final CDetailSectionService entityService,
			final CDetailLinesService screenLinesService, final CEntityFieldService entityFieldService, final CViewsService viewsService)
			throws Exception {
		super("Screen Lines", parentContent, beanValidationBinder, entityService);
		Check.notNull(screenLinesService, "Detail lines service cannot be null");
		
		LOGGER.debug("Creating CPanelDetailLines with CComponentListDetailLines");
		
		// Create the list component
		listComponent = new CComponentListDetailLines(screenLinesService);
		
		initPanel();
		createScreenLinesLayout();
	}

	private void createScreenLinesLayout() {
		// Simply add the list component to the panel content
		addToContent(listComponent);
		
		// Populate with current entity if available
		if (getCurrentEntity() != null) {
			listComponent.setCurrentSection(getCurrentEntity());
		}
		
		LOGGER.debug("Screen lines layout created with CComponentListDetailLines");
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		// Not used in this context - the component handles entity creation
		return null;
	}

	@Override
	public void populateForm(final CDetailSection entity) {
		super.populateForm(entity);
		Check.notNull(listComponent, "List component cannot be null");
		
		LOGGER.debug("Populating form with entity: {}", entity != null ? entity.getId() : "null");
		listComponent.setCurrentSection(entity);
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(List.of());
	}

	/**
	 * Get the list component for external access if needed.
	 * 
	 * @return The list component
	 */
	public CComponentListDetailLines getListComponent() {
		return listComponent;
	}
}
