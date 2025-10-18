package tech.derbent.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.utils.Check;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.screens.domain.CDetailSection;
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
		super(pageEntity, sessionService, detailSectionService, applicationContext);
		initializePage();
		LOGGER.debug("Creating dynamic page view for: {}", pageEntity.getPageTitle());
	}

	/** Create the main page content area. */
	private void createDetailSection() {
		LOGGER.debug("Creating detail section for page: {}", pageEntity.getPageTitle());
		Check.notNull(pageEntity, "pageEntity cannot be null");
		Check.notNull(pageEntity.getDetailSection(), "pageEntity detail section cannot be null");
		
		// Initialize entity service and build the detail section
		initializeEntityService();
		
		// Build the detail section using the inherited baseDetailsLayout and buildScreen method
		try {
			final CDetailSection detailSection = pageEntity.getDetailSection();
			final String entityViewName = detailSection.getName();
			LOGGER.debug("Building detail section for view: {}", entityViewName);
			buildScreen(entityViewName, entityClass, baseDetailsLayout);
			add(baseDetailsLayout);
		} catch (final Exception e) {
			LOGGER.error("Error creating detail section for page '{}': {}", pageEntity.getPageTitle(), e.getMessage());
			throw e;
		}
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
		createDetailSection();
		createPageFooter();
	}

	@Override
	protected <T extends CEntityDB<T>> T createNewEntity() throws Exception {
		// This view doesn't support entity creation since it has no grid/CRUD toolbar
		throw new UnsupportedOperationException("Entity creation not supported in view without grid");
	}
}
