package tech.derbent.abstracts.views;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;

public abstract class CAccordionDescription<EntityClass extends CEntityDB>
	extends CAccordion {

	private static final long serialVersionUID = 1L;
	protected final Class<EntityClass> entityClass;
	private final BeanValidationBinder<EntityClass> binder;
	protected EntityClass currentEntity;
	protected CAbstractService<EntityClass> entityService;

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
		getBaseLayout().add(
			new Div("This is the description panel. You can add more details here."));
	}

	protected abstract void createPanelContent();

	public BeanValidationBinder<EntityClass> getBinder() { return binder; }

	public abstract void populateForm(EntityClass entity);

	public void saveEventHandler() {}
}
