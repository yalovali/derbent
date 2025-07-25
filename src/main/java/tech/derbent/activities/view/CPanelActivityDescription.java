package tech.derbent.activities.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityService;


/**
 * CPanelActivityDescription - Accordion panel for displaying and editing CActivity entity definitive fields.
 * Layer: View (MVC)
 * Extends CAccordionDescription to provide activity-specific form functionality.
 */
public class CPanelActivityDescription extends CAccordionDescription<CActivity> {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for CPanelActivityDescription.
	 * @param currentEntity the current CActivity entity being edited
	 * @param beanValidationBinder the validation binder for the entity
	 * @param entityService the service for CActivity operations
	 */
	public CPanelActivityDescription(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService) {
		super(currentEntity, beanValidationBinder, CActivity.class, entityService);
		LOGGER.info("Initializing CPanelActivityDescription with entity: {}", 
			currentEntity != null ? currentEntity.getName() : "null");
		createPanelContent();
		// open the panel by default using the new convenience method
		openPanel();
	}

	@Override
	protected void createPanelContent() {
		LOGGER.info("Creating panel content for CPanelActivityDescription");
		// Use annotation-based data provider resolution for ComboBoxes
		// Both CActivityType and CActivityStatus are configured with proper @MetaData annotations
		LOGGER.debug("Using annotation-based data provider resolution for CActivity form");
		getBaseLayout()
			.add(CEntityFormBuilder.buildForm(CActivity.class, getBinder()));
	}

	@Override
	public void populateForm(final CActivity entity) {
		LOGGER.info("Populating form with activity data: {}", 
			entity != null ? entity.getName() : "null");
		if (entity == null) {
			LOGGER.warn("Entity is null, clearing form");
			return;
		}
		currentEntity = entity;
	}

	@Override
	public void saveEventHandler() {
		LOGGER.info("Handling save event for activity: {}", 
			currentEntity != null ? currentEntity.getName() : "null");
		// Additional save logic specific to CActivity can be added here
		// For now, the base save functionality is handled by the parent view
	}
}