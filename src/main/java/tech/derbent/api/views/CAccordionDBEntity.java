package tech.derbent.api.views;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.component.basic.CAccordion;

public abstract class CAccordionDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CAccordion implements IContentOwner {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAccordionDBEntity.class);
	private static final long serialVersionUID = 1L;
	private final CEnhancedBinder<EntityClass> binder;
	protected final Class<EntityClass> entityClass;
	private List<String> EntityFields = null;
	protected CAbstractService<EntityClass> entityService;
	private boolean isPanelInitialized = false;
	protected IContentOwner parentContent;

	public CAccordionDBEntity(final String title, IContentOwner parentContent, final CEnhancedBinder<EntityClass> beanValidationBinder,
			final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService) {
		super(title);
		this.entityClass = entityClass;
		binder = beanValidationBinder;
		this.entityService = entityService;
		this.parentContent = parentContent;
	}

	public void clearForm() {
		binder.readBean(null);
	}

	// Override if you need to customize the panel content creation
	protected void createPanelContent() throws Exception {
		addToContent(CFormBuilder.buildForm(entityClass, getBinder(), getEntityFields()));
	}

	public CEnhancedBinder<EntityClass> getBinder() { return binder; }

	@Override
	public String getCurrentEntityIdString() {
		LOGGER.debug("Getting current entity ID string for accordion: {}", getAccordionTitle());
		return getValue() != null ? getValue().getId().toString() : null;
	}

	public List<String> getEntityFields() { return EntityFields; }

	@Override
	public CAbstractService<EntityClass> getEntityService() { return entityService; }

	@SuppressWarnings ("unchecked")
	@Override
	public EntityClass getValue() { return (EntityClass) parentContent.getValue(); }

	protected void initPanel() throws Exception {
		updatePanelEntityFields();
		createPanelContent();
		openPanel();
		isPanelInitialized = true;
	}

	@Override
	public void populateForm() throws Exception {
		// Delegate to parent content owner
		if (parentContent != null) {
			parentContent.populateForm();
		}
	}

	public void populateForm(final EntityClass entity) {
		assert isPanelInitialized : "Panel must be initialized before populating form";
		if (entity == null) {
			clearForm();
			return;
		}
		// LOGGER.debug("Populating form with entity: {}", entity); Populate the form
		// fields with the entity data
		LOGGER.debug("Populating accordion {} with entity: {}", getAccordionTitle(), entity.getId() != null ? entity.getId() : "null");
		binder.readBean(entity);
	}

	protected void setEntityFields(final List<String> fields) { EntityFields = fields; }

	@Override
	public void setValue(CEntityDB<?> entity) {
		// Delegate to parent content owner
		if (parentContent != null) {
			parentContent.setValue(entity);
		}
	}

	protected abstract void updatePanelEntityFields();
}
