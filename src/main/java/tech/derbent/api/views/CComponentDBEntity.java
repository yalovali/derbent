package tech.derbent.api.views;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.annotations.CFormBuilder.ComboBoxDataProvider;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.views.components.CVerticalLayout;

public abstract class CComponentDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CVerticalLayout implements IContentOwner {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected final Class<EntityClass> entityClass;
	private final CEnhancedBinder<EntityClass> binder;
	protected CAbstractService<EntityClass> entityService;
	private List<String> EntityFields = null;
	private boolean isPanelInitialized = false;
	protected IContentOwner parentContent;

	public CComponentDBEntity(final String title, IContentOwner parentContent, final CEnhancedBinder<EntityClass> beanValidationBinder,
			final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService) {
		super(false, true, false); // no padding, with spacing, no margin
		this.entityClass = entityClass;
		this.binder = beanValidationBinder;
		this.entityService = entityService;
		this.parentContent = parentContent;
		addClassName("c-component-db-entity");
		setWidthFull();
	}

	public void clearForm() {
		binder.readBean(null);
	}

	protected ComboBoxDataProvider createComboBoxDataProvider() {
		return null;
	}

	// Override if you need to customize the panel content creation
	protected void createPanelContent() throws Exception {
		add(CFormBuilder.buildForm(entityClass, getBinder(), getEntityFields()));
	}

	public CEnhancedBinder<EntityClass> getBinder() { return binder; }

	@Override
	public EntityClass getCurrentEntity() {
		if (parentContent != null) {
			return (EntityClass) parentContent.getCurrentEntity();
		}
		return null;
	}

	/** Override this method in subclasses to provide local context values specific to this component.
	 * @param contextName the context name to resolve
	 * @return the local context value or null if not found */
	public Object getLocalContextValue(final String contextName) {
		return null;
	}

	public List<String> getEntityFields() {
		if (EntityFields == null) {
			updatePanelEntityFields();
		}
		return EntityFields;
	}

	/** Gets the default help text for the panel */
	protected String getHelpText() { return "Configure settings for this entity."; }

	/** Sets the entity fields list */
	protected void setEntityFields(final List<String> fields) { EntityFields = fields; }

	/** Abstract method for subclasses to set their specific entity fields */
	protected abstract void updatePanelEntityFields();

	/** Initializes the panel by creating its content and setting up data bindings. This method is called automatically when the panel is first
	 * displayed. Subclasses can override this to customize initialization. */
	public void initPanel() throws Exception {
		if (!isPanelInitialized) {
			LOGGER.debug("Initializing component panel for entity class: {}", entityClass.getSimpleName());
			createPanelContent();
			isPanelInitialized = true;
		}
	}

	/** Override to check panel visibility based on business logic */
	public boolean isPanelVisible() { return true; }

	public void openPanel() {
		setVisible(true);
	}

	/** Closes the panel by making it not visible */
	public void closePanel() {
		setVisible(false);
	}

	/** Override to perform actions when the panel is opened */
	protected void onPanelOpened() {
		LOGGER.debug("Component panel opened for entity class: {}", entityClass.getSimpleName());
	}

	/** Gets the default title for this panel */
	protected String getPanelTitle() { return entityClass.getSimpleName() + " Settings"; }

	/** Saves the current form data to the entity */
	protected void saveFormData() throws Exception {
		EntityClass entity = getCurrentEntity();
		if (entity != null && binder != null) {
			binder.writeBean(entity);
			entityService.save(entity);
			LOGGER.debug("Form data saved for entity: {}", entity);
		}
	}

	/** Validates the current form data */
	public boolean validateForm() {
		if (binder != null) {
			return binder.validate().isOk();
		}
		return true;
	}
}
