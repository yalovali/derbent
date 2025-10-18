package tech.derbent.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.session.service.ISessionService;

/** Dynamic page view for rendering database-defined pages. This view displays content stored in CPageEntity instances. */
@PermitAll
public class CDynamicPageViewWithoutGrid extends CDynamicPageBase {

	public static final String DEFAULT_COLOR = "#4b2900";
	public static final String DEFAULT_ICON = "vaadin:dashboard";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageViewWithoutGrid.class);
	private static final long serialVersionUID = 1L;

	public CDynamicPageViewWithoutGrid(final CPageEntity pageEntity, final ISessionService sessionService,
			final CDetailSectionService detailSectionService, final ApplicationContext applicationContext) {
		// For backward compatibility, we need to create the missing services
		super(pageEntity, sessionService, detailSectionService, applicationContext);
		initializePage();
		LOGGER.debug("Creating dynamic page view for: {}", pageEntity.getPageTitle());
	}

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		// Security check
		if (pageEntity.getRequiresAuthentication() && sessionService.getActiveUser().isEmpty()) {
			LOGGER.warn("Unauthenticated access attempted to page: {}", pageEntity.getPageTitle());
			event.rerouteToError(IllegalAccessException.class, "Authentication required");
			return;
		}
		// Check if page is active
		if (!pageEntity.getActive()) {
			LOGGER.warn("Access attempted to inactive page: {}", pageEntity.getPageTitle());
			event.rerouteToError(IllegalStateException.class, "Page not available");
			return;
		}
		LOGGER.debug("User accessing page: {}", pageEntity.getPageTitle());
	}

	/** Create the main page content area. */
	private void createGridAndDetailSections() {
		LOGGER.debug("Creating page content area for page: {}", pageEntity.getPageTitle());
		Check.notNull(pageEntity, "pageEntity cannot be null");
		Check.notNull(pageEntity.getContent(), "pageEntity content cannot be null");
		// add(pageEntity.getContent());
	}

	/** Create page footer with metadata. */
	private void createPageFooter() {
		Div footer = new Div();
		footer.addClassNames("page-footer");
		if (pageEntity.getLastModifiedDate() != null) {
			Paragraph lastModified = new Paragraph("Last updated: " + pageEntity.getLastModifiedDate().toString());
			lastModified.addClassNames("last-modified");
			footer.add(lastModified);
		}
		add(footer);
	}

	/** Create the page header with title and description. */
	private void createPageHeader() {
		// Only create header if pageTitle is not empty
		if (pageEntity.getPageTitle() != null && !pageEntity.getPageTitle().trim().isEmpty()) {
			H1 pageTitle = new H1("x:" + pageEntity.getPageTitle());
			pageTitle.addClassNames("page-title");
			add(pageTitle);
		}
	}

	/** Get the page entity this view represents. */
	@Override
	public CPageEntity getPageEntity() { return pageEntity; }

	/** Implementation of IPageTitleProvider - provides the page title from the CPageEntity */
	@Override
	public String getPageTitle() { return pageEntity != null ? pageEntity.getPageTitle() : null; }

	/** Initialize the entity service based on the configured entity type. */
	protected void initializeEntityService() {
		try {
			// Try to get the service bean from the configured grid entity
			CGridEntity gridEntity = pageEntity.getGridEntity();
			Check.notNull(gridEntity, "Grid entity cannot be null");
			Check.notBlank(gridEntity.getDataServiceBeanName(), "Data service bean name cannot be blank");
			// Get the service bean from the application context
			Object serviceBean = applicationContext.getBean(gridEntity.getDataServiceBeanName());
			Check.notNull(serviceBean, "Service bean not found: " + gridEntity.getDataServiceBeanName());
			Check.instanceOf(serviceBean, CAbstractService.class, "Service bean is not an instance of CAbstractService: " + serviceBean.getClass());
			entityService = (CAbstractService<?>) serviceBean;
			// Get the entity class from the detail section
			CDetailSection detailSection = pageEntity.getDetailSection();
			Check.notNull(detailSection, "Detail section cannot be null");
			Check.notBlank(detailSection.getEntityType(), "Entity type cannot be blank");
			entityClass = CAuxillaries.getEntityClass(detailSection.getEntityType());
			Check.notNull(entityClass, "Entity class not found for type: " + detailSection.getEntityType());
			Check.isTrue(CEntityDB.class.isAssignableFrom(entityClass), "Entity class does not extend CEntityDB: " + entityClass);
		} catch (Exception e) {
			LOGGER.error("Failed to initialize entity service for entity type", e);
			throw e;
		}
	}

	/** Initialize the page layout and content. */
	@Override
	protected void initializePage() {
		LOGGER.debug("Initializing dynamic page view for page: {}", pageEntity != null ? pageEntity.getPageTitle() : "null");
		Check.notNull(pageEntity, "pageEntity cannot be null");
		// setSizeFull();
		if (pageEntity.getPageTitle() != null && !pageEntity.getPageTitle().trim().isEmpty()) {
			getElement().executeJs("document.title = $0", pageEntity.getPageTitle());
		}
		createPageHeader();
		createGridAndDetailSections();
		createPageFooter();
	}
}
