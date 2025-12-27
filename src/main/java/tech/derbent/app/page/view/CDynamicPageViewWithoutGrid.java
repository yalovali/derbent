package tech.derbent.app.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.service.CKanbanLineService;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.base.session.service.ISessionService;

/** Dynamic page view for rendering database-defined pages. This view displays content stored in CPageEntity instances. */
@PermitAll
public class CDynamicPageViewWithoutGrid extends CDynamicPageBase {

	public static final String DEFAULT_COLOR = "#6B5FA7"; // CDE Purple - dashboard pages
	public static final String DEFAULT_ICON = "vaadin:dashboard";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageViewWithoutGrid.class);
	private static final long serialVersionUID = 1L;

	public CDynamicPageViewWithoutGrid(final CEntityDB<?> entity, final CPageEntity pageEntity, final ISessionService sessionService,
			final CDetailSectionService detailSectionService) throws Exception {
		super(pageEntity, sessionService, detailSectionService);
		LOGGER.debug("Creating dynamic page view for: {}", pageEntity.getPageTitle());
		initializePage();
		if (entity != null) {
			setValue(entity);
			// do we need it? binded ihasvalueandelement implementers already binded with binder
			populateForm();
		}
	}

	/** Create the main page content area.
	 * @throws Exception */
	private void createDetailsSection() throws Exception {
		try {
			LOGGER.debug("Creating detail section for page: {}", pageEntity.getPageTitle());
			Check.notNull(pageEntity, "pageEntity cannot be null");
			Check.notNull(pageEntity.getDetailSection(), "pageEntity detail section cannot be null");
			add(baseDetailsLayout);
			initializeEntityService();
		} catch (final Exception e) {
			LOGGER.error("Error creating detail section for page: {}: {}", pageEntity.getPageTitle(), e.getMessage());
			throw e;
		}
	}

	@Override
	protected <T extends CEntityDB<T>> T createNewEntity() throws Exception {
		// This view doesn't support entity creation since it has no grid/CRUD toolbar
		throw new UnsupportedOperationException("Entity creation not supported in view without grid");
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		return null;
	}

	/** Create page footer with metadata. */
	private void createPageFooter() {
		final Div footer = new Div();
		footer.addClassNames("page-footer");
		if (pageEntity.getLastModifiedDate() != null) {
			final Paragraph lastModified = new Paragraph("Last updated: " + pageEntity.getLastModifiedDate().toString());
			lastModified.addClassNames("last-modified");
			footer.add(lastModified);
		}
		add(footer);
	}

	/** Create the page header with title and description. */
	private void createPageHeader() {
		// Only create header if pageTitle is not empty
		if (pageEntity.getPageTitle() != null && !pageEntity.getPageTitle().trim().isEmpty()) {
			final H1 pageTitle = new H1("x:" + pageEntity.getPageTitle());
			pageTitle.addClassNames("page-title");
			add(pageTitle);
		}
	}

	@Override
	protected void initializePage() throws Exception {
		try {
			super.initializePage();
			LOGGER.debug("Initializing dynamic page view for page: {}", pageEntity != null ? pageEntity.getPageTitle() : "null");
			Check.notNull(pageEntity, "pageEntity cannot be null");
			// setSizeFull();
			if (pageEntity.getPageTitle() != null && !pageEntity.getPageTitle().trim().isEmpty()) {
				getElement().executeJs("document.title = $0", pageEntity.getPageTitle());
			}
			createPageHeader();
			createDetailsSection();
			createPageFooter();
			Check.notNull(pageEntity.getDetailSection(), "pageEntity detail section cannot be null");
			rebuildEntityDetailsById(pageEntity.getDetailSection().getId());
		} catch (final Exception e) {
			LOGGER.error("Error initializing dynamic page view for page '{}': {}", pageEntity != null ? pageEntity.getPageTitle() : "null",
					e.getMessage());
			throw e;
		}
	}

<<<<<<< HEAD
	@Override
	protected void locateFirstEntity() throws Exception {
		LOGGER.debug("Locating first entity for dynamic page view without grid");
		// it triggers entityservice to load entity
		// entityService.findAll().stream().findFirst().ifPresent(this::setValue);
		// entityService.findAll().stream().findFirst().ifPresent(entity -> currentBinder.readBean(entity));
		entityService.findDefault().ifPresent(entity -> {
			try {
				currentBinder.readBean(entity);
			} catch (final Exception e) {
				LOGGER.error("Error setting default entity value: {}", e.getMessage());
			}
		});
	}
=======
        void locateFirstEntity() throws Exception {
                LOGGER.debug("Locating first entity for dynamic page view without grid");
                // this method is called in the constructor,
                Check.notNull(entityService, "Entity service is not initialized");
                if (entityService instanceof CKanbanLineService kanbanLineService) {
                        kanbanLineService.findDefaultForCurrentProject()
                                        .or(() -> kanbanLineService.findAll().stream().findFirst())
                                        .ifPresent(entity -> currentBinder.readBean(entity));
                        return;
                }
                entityService.findAll().stream().findFirst().ifPresent(entity -> currentBinder.readBean(entity));
        }
>>>>>>> branch 'codex/check-redundant-refreshing-in-kanban-board-zzhhza' of https://github.com/yalovali/derbent

	@Override
	protected void locateItemById(final Long pageItemId) {
		return;
	}

	@Override
	protected void on_after_construct() {
		LOGGER.debug("on_after_construct called for dynamic page view without grid");
		if (getValue() == null) {
			try {
				locateFirstEntity();
			} catch (final Exception e) {
				LOGGER.error("Error locating first entity after construct: {}", e.getMessage());
			}
		}
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntityCreated(CEntityDB newEntity) throws Exception {/**/}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntityDeleted(CEntityDB entity) throws Exception {/**/}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntityRefreshed(CEntityDB reloaded) throws Exception {/**/}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntitySaved(CEntityDB savedEntity) throws Exception {/**/}

	@SuppressWarnings ("rawtypes")
	@Override
	public void setValue(CEntityDB entity) {
		LOGGER.debug("Setting current entity in dynamic page view without grid: {}", entity != null ? entity.getId() : "null");
		try {
			super.setValue(entity);
		} catch (final Exception e) {
			throw e;
		}
	}
}
