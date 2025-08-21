package tech.derbent.screens.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.services.CDetailsBuilder;
import tech.derbent.abstracts.views.CButton;
import tech.derbent.abstracts.views.CDiv;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CScreenService;

public class CPanelScreenPreview extends CPanelScreenBase {

	private static final long serialVersionUID = 1L;

	CDiv divPreview;

	public CPanelScreenPreview(final CScreen currentEntity,
		final CEnhancedBinder<CScreen> beanValidationBinder,
		final CScreenService entityService) throws NoSuchMethodException,
		SecurityException, IllegalAccessException, InvocationTargetException {
		super("Preview", currentEntity, beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void createPanelContent() throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		super.createPanelContent();
		// Add any specific content for the preview panel here if needed
		final CButton previewButton = new CButton("Preview");
		getBaseLayout().add(previewButton);
		previewButton.addClickListener(event -> {
			populateForm(currentEntity);
		});
		divPreview = new CDiv();
		getBaseLayout().add(divPreview);
	}

	@Override
	public void populateForm(final CScreen screen) {
		super.populateForm(screen);

		try {

			if (screen == null) {

				if (divPreview != null) {
					divPreview.removeAll();
				}
				return;
			}

			if (divPreview != null) {
				final CDetailsBuilder builder = new CDetailsBuilder();
				divPreview.removeAll();
				// get service for the class
				final Class<?> screenClass =
					CEntityFieldService.getEntityClass(screen.getEntityType());
				@SuppressWarnings ("unchecked")
				final CEnhancedBinder<CEntityDB<?>> binder =
					new CEnhancedBinder<CEntityDB<?>>((Class<CEntityDB<?>>) screenClass);
				builder.buildDetails(screen, binder, divPreview);
				// Get related service class for the given class type
				/** ADD SAMPLE DATA ************************************/
				final String serviceClassName =
					screenClass.getPackage().getName().replace("domain", "service") + ".C"
						+ screenClass.getSimpleName().substring(1) + "Service";
				final Class<?> serviceClass = Class.forName(serviceClassName);
				final CAbstractService<?> serviceBean = (CAbstractService<
					?>) CDetailsBuilder.getApplicationContext().getBean(serviceClass);
				final CEntityDB<?> item =
					serviceBean.findAll().stream().findFirst().orElse(null);

				if (item != null) {
					binder.readBean(item);
				}
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