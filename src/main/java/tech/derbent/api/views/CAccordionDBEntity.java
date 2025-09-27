package tech.derbent.api.views;

import java.util.List;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.annotations.CFormBuilder.ComboBoxDataProvider;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.views.components.CAccordion;

public abstract class CAccordionDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CAccordion implements IContentOwner {

	private static final long serialVersionUID = 1L;
	protected final Class<EntityClass> entityClass;
	private final CEnhancedBinder<EntityClass> binder;
	protected CAbstractService<EntityClass> entityService;
	private List<String> EntityFields = null;
	private boolean isPanelInitialized = false;
	protected IContentOwner parentContent;

	public CAccordionDBEntity(final String title, IContentOwner parentContent, final CEnhancedBinder<EntityClass> beanValidationBinder,
			final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService) {
		super(title);
		this.entityClass = entityClass;
		this.binder = beanValidationBinder;
		this.entityService = entityService;
		this.parentContent = parentContent;
	}

	public void clearForm() {
		binder.readBean(null);
	}

	protected ComboBoxDataProvider createComboBoxDataProvider() {
		return null;
	}

	// Override if you need to customize the panel content creation
	protected void createPanelContent() throws Exception {
		addToContent(CFormBuilder.buildForm(entityClass, getBinder(), getEntityFields()));
	}

	public CEnhancedBinder<EntityClass> getBinder() { return binder; }

	@Override
	public EntityClass getCurrentEntity() { return (EntityClass) parentContent.getCurrentEntity(); }

	/** Override this method in subclasses to provide local context values specific to this accordion panel.
	 * @param contextName the context name to resolve
	 * @return the local context value, or null if not found locally */
	protected Object getLocalContextValue(String contextName) {
		// Subclasses can override to provide specific context values
		return null;
	}

	public List<String> getEntityFields() { return EntityFields; }

	protected void initPanel() throws Exception {
		updatePanelEntityFields();
		createPanelContent();
		openPanel();
		isPanelInitialized = true;
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

	// used if there is a specific save logic for the entity
	public void saveEventHandler() {}

	protected void setEntityFields(final List<String> fields) { EntityFields = fields; }

	protected abstract void updatePanelEntityFields();
}
