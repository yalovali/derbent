package tech.derbent.api.views.components;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;

public abstract class CComponentDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CVerticalLayout
		implements IContentOwner, IHasContentOwner {

	private static final long serialVersionUID = 1L;
	protected final ApplicationContext applicationContext;
	private final CEnhancedBinder<EntityClass> binder;
	protected IContentOwner contentOwner = null;
	private EntityClass currentEntity;
	protected final Class<EntityClass> entityClass;
	private List<String> EntityFields = null;
	protected CAbstractService<EntityClass> entityService;
	private boolean isPanelInitialized = false;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@SuppressWarnings ("unchecked")
	public CComponentDBEntity(final String title, final Class<EntityClass> entityClass, ApplicationContext applicationContext) {
		super(false, true, false); // no padding, with spacing, no margin
		this.applicationContext = applicationContext;
		Check.notNull(applicationContext, "Application context cannot be null");
		this.entityClass = entityClass;
		Check.notNull(entityClass, "Entity class cannot be null");
		binder = new CEnhancedBinder<>(entityClass);
		Check.notNull(binder, "Binder cannot be null");
		entityService = (CAbstractService<EntityClass>) applicationContext.getBean(CAuxillaries.getServiceClassForEntity(entityClass));
		Check.notNull(entityService, "Entity service cannot be null for entity: " + entityClass.getSimpleName());
	}

	public void clearForm() {
		try {
			Check.notNull(binder, "Binder cannot be null when clearing form");
			binder.readBean(null);
		} catch (Exception e) {
			LOGGER.error("Failed to clear form.");
			throw e;
		}
	}

	public void closePanel() {
		setVisible(false);
	}

	public CEnhancedBinder<EntityClass> getBinder() { return binder; }

	@Override
	public IContentOwner getContentOwner() { return contentOwner; }

	@Override
	public EntityClass getCurrentEntity() { return currentEntity; }

	public Class<EntityClass> getEntityClass() { return entityClass; }

	public List<String> getEntityFields() {
		if (EntityFields == null) {
			updatePanelEntityFields();
		}
		return EntityFields;
	}

	protected String getPanelTitle() { return entityClass.getSimpleName() + " Settings"; }

	protected final void initComponent() throws Exception {
		Check.isTrue(!isPanelInitialized, "Panel is already initialized");
		addClassName("c-component-db-entity");
		setSpacing(true);
		setPadding(true);
		setWidthFull();
		initPanel();
		isPanelInitialized = true;
	}

	protected abstract void initPanel() throws Exception;

	public boolean isPanelVisible() { return true; }

	protected void onPanelOpened() {
		LOGGER.debug("Component panel opened for entity class: {}", entityClass.getSimpleName());
	}

	public void openPanel() {
		setVisible(true);
	}

	@Override
	public void populateForm() {
		try {
			LOGGER.debug("Populating form for entity class: {}", entityClass.getSimpleName());
			Check.isTrue(isPanelInitialized, "Panel must be initialized before populating form");
			// Use current entity from content owner if available, otherwise use our own
			EntityClass entityToUse = getCurrentEntity();
			if (entityToUse == null && contentOwner != null) {
				// Try to get entity from parent content owner
				Object parentEntity = contentOwner.getCurrentEntity();
				if (parentEntity != null && entityClass.isInstance(parentEntity)) {
					entityToUse = entityClass.cast(parentEntity);
				}
			}
			// Default implementation - populate binder if available
			if (entityToUse != null) {
				LOGGER.debug("Populating form for entity: {}", entityToUse);
				// If there's a binder, set the bean
				if (binder != null) {
					binder.setBean(entityToUse);
				}
			} else {
				LOGGER.debug("Clearing form - no current entity");
				if (binder != null) {
					binder.setBean(null);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Failed to populate form for entity {}: {}", entityClass.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	protected void saveFormData() throws Exception {
		try {
			EntityClass entity = getCurrentEntity();
			Check.notNull(entity, "Current entity cannot be null when saving form data");
			Check.notNull(binder, "Binder cannot be null when saving form data");
			binder.writeBean(entity);
			entityService.save(entity);
			LOGGER.debug("Form data saved for entity: {}", entity);
		} catch (Exception e) {
			LOGGER.error("Failed to save form data for entity {}: {}", entityClass.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	public void setContentOwner(IContentOwner parentContent) { contentOwner = parentContent; }

	@SuppressWarnings ("unchecked")
	@Override
	public void setCurrentEntity(Object entity) { currentEntity = (EntityClass) entity; }

	protected void setEntityFields(final List<String> fields) {
		Check.notNull(fields, "Entity fields list cannot be null");
		EntityFields = fields;
	}

	protected abstract void updatePanelEntityFields();

	public boolean validateForm() {
		try {
			return binder != null && binder.validate().isOk();
		} catch (Exception e) {
			LOGGER.error("Failed to validate form for entity {}: {}", entityClass.getSimpleName(), e.getMessage());
			return false;
		}
	}
}
