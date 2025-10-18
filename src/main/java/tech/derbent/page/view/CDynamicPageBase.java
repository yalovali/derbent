package tech.derbent.page.view;

import java.lang.reflect.Field;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import tech.derbent.api.interfaces.IEntityUpdateListener;
import tech.derbent.api.interfaces.IPageTitleProvider;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.ISessionService;

public abstract class CDynamicPageBase extends CPageBaseProjectAware implements BeforeEnterObserver, IEntityUpdateListener, IPageTitleProvider {

	private static final long serialVersionUID = 1L;
	protected final ApplicationContext applicationContext;
	protected Class<?> currentEntityType = null;
	protected String currentEntityViewName = null;
	protected Class<?> entityClass;
	protected final CPageEntity pageEntity;

	public CDynamicPageBase(CPageEntity pageEntity, ISessionService sessionService, CDetailSectionService detailSectionService,
			ApplicationContext applicationContext) {
		super(sessionService, detailSectionService);
		this.applicationContext = applicationContext;
		this.pageEntity = pageEntity;
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		LOGGER.debug("Before enter event for dynamic page: {}", pageEntity.getPageTitle());
		// Security check
		if (pageEntity.getRequiresAuthentication() && sessionService.getActiveUser().isEmpty()) {
			event.rerouteToError(IllegalAccessException.class, "Authentication required");
			return;
		}
		// Check if page is active
		if (!pageEntity.getActive()) {
			event.rerouteToError(IllegalStateException.class, "Page not available");
			return;
		}
	}

	/** Clear entity details and reset state. */
	protected void clearEntityDetails() {
		if (baseDetailsLayout != null) {
			baseDetailsLayout.removeAll();
		}
		currentBinder = null;
		currentEntityViewName = null;
		currentEntityType = null;
	}

	/** Get the page entity this view represents. */
	public CPageEntity getPageEntity() { return pageEntity; }

	/** Implementation of IPageTitleProvider - provides the page title from the CPageEntity */
	@Override
	public String getPageTitle() { return pageEntity != null ? pageEntity.getPageTitle() : null; }

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	protected void rebuildEntityDetails(final Class<?> clazz) throws Exception {
		try {
			final Field viewNameField = clazz.getField("VIEW_NAME");
			final String entityViewName = (String) viewNameField.get(null);
			LOGGER.debug("Rebuilding entity details for view: {}", entityViewName);
			clearEntityDetails();
			currentEntityViewName = entityViewName;
			buildScreen(entityViewName, (Class) entityClass, baseDetailsLayout);
		} catch (final Exception e) {
			LOGGER.error("Error rebuilding entity details for view '{}': {}", clazz.getField("VIEW_NAME"), e.getMessage());
			throw e;
		}
	}

	@Override
	public void setCurrentEntity(final Object entity) {
		super.setCurrentEntity(entity);
	}
}
