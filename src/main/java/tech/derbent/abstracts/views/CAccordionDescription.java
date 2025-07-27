package tech.derbent.abstracts.views;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;

public abstract class CAccordionDescription<EntityClass extends CEntityDB>
	extends CAccordion {

	private static final long serialVersionUID = 1L;

	protected final Class<EntityClass> entityClass;

	private final BeanValidationBinder<EntityClass> binder;

	private EntityClass currentEntity;

	protected CAbstractService<EntityClass> entityService;

	private List<String> EntityFields;

	private final ComboBoxDataProvider detailsDataProvider;

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
		this.detailsDataProvider = createComboBoxDataProvider();
	}

	/**
	 * Constructor for CAccordionDescription with custom title.
	 * @param title                custom title for the accordion panel
	 * @param currentEntity        current entity instance
	 * @param beanValidationBinder validation binder
	 * @param entityClass          entity class type
	 * @param entityService        service for the entity
	 */
	public CAccordionDescription(final String title, final EntityClass currentEntity,
		final BeanValidationBinder<EntityClass> beanValidationBinder,
		final Class<EntityClass> entityClass,
		final CAbstractService<EntityClass> entityService) {
		super(title);
		this.entityClass = entityClass;
		this.binder = beanValidationBinder;
		this.entityService = entityService;
		this.currentEntity = currentEntity;
		this.detailsDataProvider = createComboBoxDataProvider();
	}

	protected abstract ComboBoxDataProvider createComboBoxDataProvider();
	protected abstract void createPanelContent();

	public BeanValidationBinder<EntityClass> getBinder() { return binder; }

	public EntityClass getCurrentEntity() { return currentEntity; }

	public ComboBoxDataProvider getDetailsDataProvider() { return detailsDataProvider; }

	public List<String> getEntityFields() { return EntityFields; }

	public void populateForm(final EntityClass entity) {
		LOGGER.info("Populating form with entity data: {}",
			entity != null ? entity.toString() : "null");

		if (entity == null) {
			LOGGER.warn("Entity is null, clearing form");
			return;
		}
		currentEntity = entity;
	}

	public void saveEventHandler() {}

	protected void setEntityFields(final List<String> fields) { EntityFields = fields; }

	protected void updatePanelEntityFields() {
		// TODO Auto-generated method stub
	}
}
