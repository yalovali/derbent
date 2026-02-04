package tech.derbent.api.page.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.HasComponents;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.ICrudToolbarOwnerPage;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

public abstract class CDynamicPageViewForEntityEdit extends CDynamicPageBase implements ICrudToolbarOwnerPage {

	public static final String DEFAULT_COLOR = "#91856C"; // OpenWindows Border Dark - entity edit
	public static final String DEFAULT_ICON = "vaadin:database";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDynamicPageViewForEntityEdit.class);
	private static final long serialVersionUID = 1L;
	protected CCrudToolbar crudToolbar;

	public CDynamicPageViewForEntityEdit(CPageEntity pageEntity, ISessionService sessionService, CDetailSectionService detailSectionService)
			throws Exception {
		super(pageEntity, sessionService, detailSectionService);
	}

	protected void createCRUDToolbar(HasComponents splitBottomLayout) {
		// Create toolbar with minimal constructor and configure
		crudToolbar = new CCrudToolbar();
		crudToolbar.setPageBase(this);
		configureCrudToolbar(crudToolbar);
		splitBottomLayout.addComponentAsFirst(crudToolbar);
	}

	/** Get the CRUD toolbar for this view. Used by page services to control button states.
	 * @return the CRUD toolbar, or null if not initialized */
	@Override
	public CCrudToolbar getCrudToolbar() { return crudToolbar; }

	@Override
	protected void initializePage() throws Exception {
		super.initializePage();
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntityCreated(CEntityDB entity) throws Exception {
		try {
			LOGGER.debug("Entity created notification received: {}", entity != null ? entity.getClass().getSimpleName() : "null");
			Check.notNull(entity, "Created entity cannot be null");
			onEntitySelected(entity);
			CNotificationService.showSuccess("New " + getEntityClass().getSimpleName() + " created. Fill in the details and click Save.");
		} catch (final Exception e) {
			LOGGER.error("Error handling entity created notification:" + e.getMessage());
			CNotificationService.showException("Error handling entity created notification", e);
		}
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntityRefreshed(CEntityDB reloaded) throws Exception {
		try {
			LOGGER.debug("Entity refreshed notification received: {}", reloaded != null ? reloaded.getClass().getSimpleName() : "null");
			onEntitySelected(reloaded);
			CNotificationService.showSuccess(getEntityClass().getSimpleName() + " reloaded.");
		} catch (final Exception e) {
			LOGGER.error("Error handling entity refreshed notification:{}", e.getMessage());
			CNotificationService.showException("Error handling entity refreshed notification", e);
		}
	}

	protected void onEntitySelected(CEntityDB<?> selectedEntity) throws Exception {
		try {
			setValue(selectedEntity);
			if (selectedEntity == null) {
				// No selection - clear details
				clearEntityDetails();
			} else {
				// Rebuild details if VIEW_NAME changed or not yet built
				if (currentEntityViewName == null || !selectedEntity.getClass().getField("VIEW_NAME").get(null).equals(currentEntityViewName)) {
					rebuildEntityDetailsById(pageEntity.getDetailSection().getId());
				}
			}
			populateForm();
		} catch (final Exception e) {
			CNotificationService.showException("Error handling entity selection", e);
		}
	}

	/** Overrides setValue to notify the CRUD toolbar about the current entity. This ensures the toolbar buttons are enabled/disabled based on whether
	 * an entity is selected.
	 * @param entity The entity to set as current, or null to clear */
	@Override
	public void setValue(final CEntityDB<?> entity) {
		LOGGER.debug("Setting current entity in dynamic page view for entity edit: {}", entity);
		try {
			super.setValue(entity);
			// Notify CRUD toolbar about the current entity to update button states
			if (crudToolbar != null) {
				crudToolbar.setValue(entity);
			}
		} catch (final Exception e) {
			LOGGER.error("Error setting current entity in toolbar", e);
			throw e;
		}
	}
}
