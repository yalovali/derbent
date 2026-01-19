package tech.derbent.api.screens.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.vaadin.flow.component.Component;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entity.service.CPageServiceEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CDetailsBuilder;
import tech.derbent.base.session.service.ISessionService;

@SuppressWarnings ("rawtypes")
public class CPanelDetailSectionPreview extends CPanelDetailSectionBase implements IPageServiceImplementer {

	private static final long serialVersionUID = 1L;
	private final Map<String, Component> componentMap = new HashMap<String, Component>();
	CDiv divPreview;
	private final ISessionService sessionService;
	private CPreviewPageServiceContext<?> previewContext;

	public CPanelDetailSectionPreview(final IContentOwner parentContent, final CEnhancedBinder<CDetailSection> beanValidationBinder,
			final CDetailSectionService entityService, final ISessionService sessionService) throws Exception {
		super("Preview", parentContent, beanValidationBinder, entityService);
		this.sessionService = sessionService;
		initPanel();
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		return null;
	}

	@Override
	protected void createPanelContent() throws Exception {
		super.createPanelContent();
		// Add any specific content for the preview panel here if needed
		final CButton previewButton = new CButton("Preview", null, null);
		getBaseLayout().add(previewButton);
		previewButton.addClickListener( event -> {
			populateForm(getValue());
		});
		divPreview = new CDiv();
		getBaseLayout().add(divPreview);
	}

	@Override
	public Map<String, Component> getComponentMap() { return componentMap; }

	@Override
	public CDetailsBuilder getDetailsBuilder() { return null; }

	@Override
	public Class getEntityClass() { return entityClass; }

	@Override
	public CPageService getPageService() {
		Check.instanceOf(parentContent, IPageServiceImplementer.class,
				"Content owner must implement IPageServiceImplementer to use 'view' as data provider bean");
		return ((IPageServiceImplementer) parentContent).getPageService();
	}

	@Override
	public ISessionService getSessionService() { return sessionService; }

	@Override
	public void onEntityCreated(CEntityDB newEntity) throws Exception {/***/
	}

	@Override
	public void onEntityDeleted(CEntityDB entity) throws Exception {/***/
	}

	@Override
	public void onEntitySaved(CEntityDB savedEntity) throws Exception {/***/
	}

	@Override
	public void populateForm(final CDetailSection screen) {
		super.populateForm(screen);
		try {
			Check.notNull(divPreview, "Preview div is not initialized");
			if ((screen == null) || (screen.getEntityType() == null)) {
				divPreview.removeAll();
				return;
			}
			divPreview.removeAll();
			// get service for the class
			final Class<?> screenClass = CEntityRegistry.getEntityClass(screen.getEntityType());
			Check.notNull(screenClass, "Screen class cannot be resolved for preview");
			// Instead of creating a new binder, reuse the existing one from the base class
			// This fixes the issue of multiple binders being created unnecessarily
			CEnhancedBinder<?> sharedBinder = getBinder();
			if ((sharedBinder == null) || !sharedBinder.getBeanType().equals(screenClass)) {
				// Only create new binder if current one doesn't match the type
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<CEntityDB<?>> binder = new CEnhancedBinder<CEntityDB<?>>((Class<CEntityDB<?>>) screenClass);
				sharedBinder = binder;
			}
			// Get related service class for the given class type
			/** ADD SAMPLE DATA ************************************/
			final String serviceClassName =
					screenClass.getPackage().getName().replace("domain", "service") + ".C" + screenClass.getSimpleName().substring(1) + "Service";
			final Class<?> serviceClass = Class.forName(serviceClassName);
			final CAbstractService<?> serviceBean = (CAbstractService<?>) CDetailsBuilder.getApplicationContext().getBean(serviceClass);
			final CEntityDB<?> item = serviceBean.findAll().stream().findFirst().orElse(null);
			previewContext = createPreviewContext(screenClass, sessionService, serviceBean, sharedBinder, item);
			final CDetailsBuilder builder = new CDetailsBuilder(sessionService);
			builder.buildDetails(previewContext, screen, sharedBinder, divPreview);
			if (item != null) {
				// Safe casting for readBean - the binder type should match the item type
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<Object> objectBinder = (CEnhancedBinder<Object>) sharedBinder;
				objectBinder.readBean(item);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static <EntityClass extends CEntityDB<EntityClass>> CPreviewPageServiceContext<EntityClass> createPreviewContext(
			final Class<?> screenClass, final ISessionService sessionService, final CAbstractService<?> serviceBean, final CEnhancedBinder<?> binder,
			final CEntityDB<?> item) {
		@SuppressWarnings ("unchecked")
		final Class<EntityClass> typedClass = (Class<EntityClass>) screenClass;
		@SuppressWarnings ("unchecked")
		final CAbstractService<EntityClass> typedService = (CAbstractService<EntityClass>) serviceBean;
		@SuppressWarnings ("unchecked")
		final CEnhancedBinder<EntityClass> typedBinder = (CEnhancedBinder<EntityClass>) binder;
		@SuppressWarnings ("unchecked")
		final EntityClass typedItem = (EntityClass) item;
		return new CPreviewPageServiceContext<>(typedClass, sessionService, typedService, typedBinder, typedItem);
	}

	private static final class CPreviewPageServiceContext<EntityClass extends CEntityDB<EntityClass>>
			implements IPageServiceImplementer<EntityClass> {

		private final CEnhancedBinder<EntityClass> binder;
		private final Map<String, Component> componentMap;
		private final CAbstractService<EntityClass> entityService;
		private final Class<EntityClass> entityClass;
		private final ISessionService sessionService;
		private final CPageService<EntityClass> pageService;
		private EntityClass currentValue;

		private CPreviewPageServiceContext(final Class<EntityClass> entityClass, final ISessionService sessionService,
				final CAbstractService<EntityClass> entityService, final CEnhancedBinder<EntityClass> binder, final EntityClass currentValue) {
			this.entityClass = entityClass;
			this.sessionService = sessionService;
			this.entityService = entityService;
			this.binder = binder;
			this.currentValue = currentValue;
			componentMap = new HashMap<>();
			pageService = new CPageServiceEntityDB<>(this);
		}

		@Override
		public CEnhancedBinder<EntityClass> getBinder() { return binder; }

		@Override
		public Map<String, Component> getComponentMap() { return componentMap; }

		@Override
		public EntityClass getValue() { return currentValue; }

		@Override
		public CDetailsBuilder getDetailsBuilder() { return null; }

		@Override
		public Class<?> getEntityClass() { return entityClass; }

		@Override
		public CAbstractService<EntityClass> getEntityService() { return entityService; }

		@Override
		public CPageService<EntityClass> getPageService() { return pageService; }

		@Override
		public ISessionService getSessionService() { return sessionService; }

		@Override
		public void selectFirstInGrid() { /**/ }

		@Override
		public CEntityDB<?> createNewEntityInstance() throws Exception { return null; }

		@Override
		public String getCurrentEntityIdString() { return currentValue == null ? "null" : String.valueOf(currentValue.getId()); }

		@Override
		public void populateForm() throws Exception { /**/ }

		@Override
		public void setValue(final CEntityDB<?> entity) { currentValue = entityClass.cast(entity); }

		@Override
		public void onEntityCreated(final EntityClass entity) throws Exception { /**/ }

		@Override
		public void onEntityDeleted(final EntityClass entity) throws Exception { /**/ }

		@Override
		public void onEntitySaved(final EntityClass entity) throws Exception { /**/ }
	}

	@Override
	public void selectFirstInGrid() { /*****/
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(List.of());
	}
}
