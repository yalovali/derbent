package tech.derbent.abstracts.views;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.services.CAbstractService;

public abstract class CAccordionDescription<EntityClass extends CEntityNamed>
	extends CAccordion {

	private static final long serialVersionUID = 1L;

	protected final Class<EntityClass> entityClass;

	private final BeanValidationBinder<EntityClass> binder;

	protected EntityClass currentEntity;

	protected CAbstractService<EntityClass> entityService;

	private List<String> EntityFields;

	/**
	 * Default constructor for CAccordionDescription.
	 * @param entityService
	 */
	public CAccordionDescription(final EntityClass currentEntity,
		final BeanValidationBinder<EntityClass> beanValidationBinder,
		final Class<EntityClass> entityClass,
		final CAbstractService<EntityClass> entityService) {
		super("Description");
		this.entityClass = entityClass;
		this.binder = beanValidationBinder;
		this.entityService = entityService;
		this.currentEntity = currentEntity;
	}

	protected abstract void createPanelContent();

	public BeanValidationBinder<EntityClass> getBinder() { return binder; }

	public List<String> getEntityFields() { return EntityFields; }

	public void populateForm(final EntityClass entity) {
		LOGGER.info("Populating form with activity data: {}",
			entity != null ? entity.getName() : "null");

		if (entity == null) {
			LOGGER.warn("Entity is null, clearing form");
			return;
		}
		currentEntity = entity;
	}

	public void saveEventHandler() {}

	protected void setEntityFields(final List<String> fields) {
		LOGGER.info("Setting entity fields for CPanelActivityDescription: {}", fields);
		EntityFields = fields;
	}

	protected void updatePanelEntityFields() {
		// TODO Auto-generated method stub
	}
}
