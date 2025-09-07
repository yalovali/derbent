package tech.derbent.abstracts.views;

import java.util.List;
import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.views.components.CAccordion;

public abstract class CAccordionDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CAccordion {
	private static final long serialVersionUID = 1L;
	protected final Class<EntityClass> entityClass;
	private final CEnhancedBinder<EntityClass> binder;
	protected EntityClass currentEntity;
	protected CAbstractService<EntityClass> entityService;
	private List<String> EntityFields = null;
	private boolean isPanelInitialized = false;

	public CAccordionDBEntity(final String title, final EntityClass currentEntity, final CEnhancedBinder<EntityClass> beanValidationBinder,
			final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService) {
		super(title);
		this.entityClass = entityClass;
		this.binder = beanValidationBinder;
		this.entityService = entityService;
		this.currentEntity = currentEntity;
	}

	public void clearForm() {
		binder.readBean(null);
	}

	protected ComboBoxDataProvider createComboBoxDataProvider() {
		return null;
	}

	// Override if you need to customize the panel content creation
	protected void createPanelContent() throws Exception {
		addToContent(CEntityFormBuilder.buildForm(entityClass, getBinder(), getEntityFields()));
	}

	public CEnhancedBinder<EntityClass> getBinder() { return binder; }

	public EntityClass getCurrentEntity() { return currentEntity; }

	public List<String> getEntityFields() { return EntityFields; }

	protected void initPanel() throws Exception {
		LOGGER.debug("Initializing panel for entity: {}", currentEntity != null ? currentEntity.getId() : "null");
		updatePanelEntityFields();
		createPanelContent();
		openPanel();
		isPanelInitialized = true;
	}

	public void populateForm(final EntityClass entity) {
		assert isPanelInitialized : "Panel must be initialized before populating form";
		currentEntity = entity;
		if (entity == null) {
			clearForm();
			return;
		}
		// LOGGER.debug("Populating form with entity: {}", entity); Populate the form
		// fields with the entity data
		LOGGER.debug("Populating accordion {} with entity: {}", getAccordionTitle(), entity.getId() != null ? entity.getId() : "null");
		binder.readBean(entity);
	}

	// used if there is a specific save logic for the entity
	public void saveEventHandler() {}

	protected void setEntityFields(final List<String> fields) { EntityFields = fields; }

	protected abstract void updatePanelEntityFields();
}
