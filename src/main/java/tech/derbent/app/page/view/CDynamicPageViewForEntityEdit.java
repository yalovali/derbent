package tech.derbent.app.page.view;

import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.ui.component.ICrudToolbarOwnerPage;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.domain.CPageEntity;
import tech.derbent.base.session.service.ISessionService;

public abstract class CDynamicPageViewForEntityEdit extends CDynamicPageBase implements ICrudToolbarOwnerPage {

	public static final String DEFAULT_COLOR = "#91856C"; // OpenWindows Border Dark - entity edit
	public static final String DEFAULT_ICON = "vaadin:database";
	private static final long serialVersionUID = 1L;
	protected CCrudToolbar crudToolbar;

	public CDynamicPageViewForEntityEdit(CPageEntity pageEntity, ISessionService sessionService, CDetailSectionService detailSectionService)
			throws Exception {
		super(pageEntity, sessionService, detailSectionService);
		// TODO Auto-generated constructor stub
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
			// Rebuild details layout for the new entity if it doesn't exist yet
			if ((currentEntityViewName == null) || (currentEntityType == null)) {
				LOGGER.debug("Rebuilding details for newly created entity");
				rebuildEntityDetailsById(null);
			}
			// Set the current entity and populate form
			setCurrentEntity(entity);
			populateForm();
			CNotificationService.showSuccess("New " + getEntityClass().getSimpleName() + " created. Fill in the details and click Save.");
		} catch (final Exception e) {
			LOGGER.error("Error handling entity created notification:" + e.getMessage());
			throw e;
		}
	}

	@SuppressWarnings ("rawtypes")
	@Override
	public void onEntityRefreshed(CEntityDB reloaded) throws Exception {
		
	}

	protected void onEntitySelected(CEntityDB<?> selectedEntity) throws Exception {
		try {
			setCurrentEntity(selectedEntity);
			if (selectedEntity == null) {
				// No selection - clear details
				clearEntityDetails();
				populateForm();
			} else {
				setCurrentEntity(selectedEntity);
				// Rebuild details if VIEW_NAME changed or not yet built
				if ((currentEntityViewName == null) || !selectedEntity.getClass().getField("VIEW_NAME").get(null).equals(currentEntityViewName)) {
					// rebuildEntityDetails(selectedEntity.getClass());
					rebuildEntityDetailsById(pageEntity.getDetailSection().getId());
				}
				// Always attempt to populate form, even if rebuild failed
				populateForm();
			}
		} catch (final Exception e) {
			CNotificationService.showException("Error handling entity selection", e);
		}
	}
}
