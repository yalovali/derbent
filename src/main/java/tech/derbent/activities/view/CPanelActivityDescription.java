package tech.derbent.activities.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityTypeService;

/**
 * CPanelActivityDescription - Accordion panel for displaying and editing CActivity entity definitive fields.
 * Layer: View (MVC)
 * Extends CAccordionDescription to provide activity-specific form functionality.
 */
public class CPanelActivityDescription extends CAccordionDescription<CActivity> {

	private static final long serialVersionUID = 1L;
	private final CActivityTypeService activityTypeService;

	/**
	 * Constructor for CPanelActivityDescription.
	 * @param currentEntity the current CActivity entity being edited
	 * @param beanValidationBinder the validation binder for the entity
	 * @param entityService the service for CActivity operations
	 * @param activityTypeService the service for CActivityType data provider
	 */
	public CPanelActivityDescription(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService, final CActivityTypeService activityTypeService) {
		super(currentEntity, beanValidationBinder, CActivity.class, entityService);
		LOGGER.info("Initializing CPanelActivityDescription with entity: {}", 
			currentEntity != null ? currentEntity.getName() : "null");
		this.activityTypeService = activityTypeService;
		createPanelContent();
		// open the panel by default using the new convenience method
		openPanel();
	}

	@Override
	protected void createPanelContent() {
		LOGGER.info("Creating panel content for CPanelActivityDescription");
		// Create data provider for ComboBoxes
		final CEntityFormBuilder.ComboBoxDataProvider dataProvider =
			new CEntityFormBuilder.ComboBoxDataProvider() {

				@Override
				@SuppressWarnings("unchecked")
				public <T extends CEntityDB> java.util.List<T>
					getItems(final Class<T> entityType) {
					LOGGER.debug("Getting items for entity type: {}", entityType.getSimpleName());
					if (entityType == CActivityType.class) {
						return (java.util.List<T>) activityTypeService
							.list(org.springframework.data.domain.Pageable.unpaged());
					}
					return java.util.Collections.emptyList();
				}
			};
		getBaseLayout()
			.add(CEntityFormBuilder.buildForm(CActivity.class, getBinder(), dataProvider));
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