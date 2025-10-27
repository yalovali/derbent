package tech.derbent.api.screens.view;

import java.util.List;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.services.CDetailsBuilder;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CButton;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.base.session.service.ISessionService;

public class CPanelDetailSectionPreview extends CPanelDetailSectionBase {

	private static final long serialVersionUID = 1L;
	CDiv divPreview;
	private final ISessionService sessionService;

	public CPanelDetailSectionPreview(final IContentOwner parentContent, final CDetailSection currentEntity,
			final CEnhancedBinder<CDetailSection> beanValidationBinder, final CDetailSectionService entityService,
			final ISessionService sessionService) throws Exception {
		super("Preview", parentContent, beanValidationBinder, entityService);
		this.sessionService = sessionService;
		initPanel();
	}

	@Override
	protected void createPanelContent() throws Exception {
		super.createPanelContent();
		// Add any specific content for the preview panel here if needed
		final CButton previewButton = new CButton("Preview", null, null);
		getBaseLayout().add(previewButton);
		previewButton.addClickListener(e -> {
			populateForm(getCurrentEntity());
		});
		divPreview = new CDiv();
		getBaseLayout().add(divPreview);
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
			final CDetailsBuilder builder = new CDetailsBuilder(sessionService);
			divPreview.removeAll();
			// get service for the class
			final Class<?> screenClass = CAuxillaries.getEntityClass(screen.getEntityType());
			// Instead of creating a new binder, reuse the existing one from the base class
			// This fixes the issue of multiple binders being created unnecessarily
			CEnhancedBinder<?> sharedBinder = getBinder();
			if ((sharedBinder == null) || !sharedBinder.getBeanType().equals(screenClass)) {
				// Only create new binder if current one doesn't match the type
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<CEntityDB<?>> binder = new CEnhancedBinder<CEntityDB<?>>((Class<CEntityDB<?>>) screenClass);
				sharedBinder = binder;
			}
			builder.buildDetails(this, screen, sharedBinder, divPreview);
			// Get related service class for the given class type
			/** ADD SAMPLE DATA ************************************/
			final String serviceClassName =
					screenClass.getPackage().getName().replace("domain", "service") + ".C" + screenClass.getSimpleName().substring(1) + "Service";
			final Class<?> serviceClass = Class.forName(serviceClassName);
			final CAbstractService<?> serviceBean = (CAbstractService<?>) CDetailsBuilder.getApplicationContext().getBean(serviceClass);
			final CEntityDB<?> item = serviceBean.findAll().stream().findFirst().orElse(null);
			if (item != null) {
				// Safe casting for readBean - the binder type should match the item type
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<Object> objectBinder = (CEnhancedBinder<Object>) sharedBinder;
				objectBinder.readBean(item);
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(List.of());
	}
}
