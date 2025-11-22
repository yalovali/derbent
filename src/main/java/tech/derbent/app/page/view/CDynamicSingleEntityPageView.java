package tech.derbent.app.page.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.orderedlayout.Scroller;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.base.session.service.ISessionService;

/** Single entity dynamic page view for displaying pageEntity without grid. This page is used for displaying settings, user's single company, etc.
 * where there is only one item per user or per project or per application wide. Only works with pageEntity.getGridEntity().getAttributeNone() ==
 * true */
@PermitAll
public class CDynamicSingleEntityPageView extends CDynamicPageViewForEntityEdit {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicSingleEntityPageView.class);
	private static final long serialVersionUID = 1L;

	public CDynamicSingleEntityPageView(final CPageEntity pageEntity, final ISessionService sessionService,
			final CDetailSectionService detailSectionService) throws Exception {
		super(pageEntity, sessionService, detailSectionService);
		try {
			initializePage();
		} catch (Exception e) {
			CNotificationService.showException("Failed to initialize dynamic page view with sections for: " + pageEntity.getPageTitle(), e);
		}
	}

	@Override
	protected <T extends CEntityDB<T>> T createNewEntity() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/** Override to create only details section without grid/master section */
	private void createSingleEntityLayout() {
		try {
			initializeEntityService();
			final Scroller detailsScroller = new Scroller();
			detailsScroller.setContent(baseDetailsLayout);
			detailsScroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
			detailsScroller.setSizeFull();
			// Create toolbar with minimal constructor and configure
			crudToolbar = new CCrudToolbar();
			crudToolbar.setPageBase(this);
			configureCrudToolbar(crudToolbar);
			add(crudToolbar);
			add(detailsScroller);
			loadAndDisplaySingleEntity();
		} catch (Exception e) {
			LOGGER.error("Failed to create single entity layout for page: {}", getPageEntity().getPageTitle(), e);
			throw e;
		}
	}

	/** Override the parent's createGridAndDetailSections to create only details section
	 * @throws Exception */
	@Override
	protected void initializePage() throws Exception {
		super.initializePage();
		if (getPageEntity().getPageTitle() != null && !getPageEntity().getPageTitle().trim().isEmpty()) {
			getElement().executeJs("document.title = $0", getPageEntity().getPageTitle());
		}
		createSingleEntityLayout();
	}

	/** Loads the single entity from the data source and displays it. Shows warning if more than 1 item is returned and displays the first item. */
	private void loadAndDisplaySingleEntity() {
		try {
			List<? extends CEntityDB<?>> entities = entityService.findAll();
			Check.notEmpty(entities, "No entities found for single entity page.");
			CEntityDB<?> entity = entities.get(0);
			onEntitySelected(entity);
		} catch (Exception e) {
			LOGGER.error("Error loading single entity for page: {}", getPageEntity().getPageTitle(), e);
		}
	}

	@Override
	protected void locateItemById(Long pageItemId) {
		try {
			if (pageItemId == null) {
				return;
			}
			Check.notNull(pageItemId, "Page item ID cannot be null");
			LOGGER.debug("Locating item by ID: {}", pageItemId);
			final CEntityDB<?> entity = entityService.getById(pageItemId).orElse(null);
			Check.notNull(entity, "No entity found for ID: " + pageItemId);
			onEntitySelected(entity);
		} catch (final Exception e) {
			LOGGER.error("Error locating item by ID {}: {}", pageItemId, e.getMessage());
			throw new IllegalStateException("Error locating item by ID " + pageItemId + ": " + e.getMessage());
		}
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntityDeleted(CEntityDB entity) throws Exception {
		loadAndDisplaySingleEntity();
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntitySaved(CEntityDB entity) throws Exception {
		onEntitySelected(entity);
	}
}
