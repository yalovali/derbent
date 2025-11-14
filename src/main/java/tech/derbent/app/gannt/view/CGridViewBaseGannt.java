package tech.derbent.app.gannt.view;

import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.services.pageservice.implementations.CPageServiceProjectGannt;
import tech.derbent.api.ui.CMasterViewSectionBase;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.CProjectAwareMDPage;
import tech.derbent.app.activities.service.CActivityService;
import tech.derbent.app.gannt.domain.CGanttItem;
import tech.derbent.app.meetings.service.CMeetingService;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.base.session.service.ISessionService;

/* display a Gannt chart for any entity of project type */
public abstract class CGridViewBaseGannt<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CGridViewBaseGannt.class);
	private static final long serialVersionUID = 1L;
	protected final CActivityService activityService;
	protected CEnhancedBinder<CProjectItem<?>> entityBinder;
	protected final CMeetingService meetingService;
	protected final CPageEntityService pageEntityService;

	protected CGridViewBaseGannt(final Class<EntityClass> entityClass, final CEntityOfProjectService<EntityClass> entityService,
			final ISessionService sessionService, final CDetailSectionService screenService, final CActivityService activityService,
			final CMeetingService meetingService, final CPageEntityService pageEntityService) throws Exception {
		super(entityClass, entityService, sessionService, screenService);
		this.activityService = activityService;
		this.meetingService = meetingService;
		this.pageEntityService = pageEntityService;
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		LOGGER.debug("Creating details component for Gantt view - no detail form available.");
	}

	// override this to create a Gannt chart
	@Override
	protected void createMasterComponent() throws Exception {
		// Pass required dependencies to CMasterViewSectionGannt constructor with page entity service for navigation
		masterViewSection =
				new CMasterViewSectionGannt<EntityClass>(entityClass, this, sessionService, activityService, meetingService, pageEntityService);
	}

	/** Gets the entity binder for the actual entity (Activity or Meeting). This is needed for the page service to write binder data before saving.
	 * @return The entity binder */
	public CEnhancedBinder<CProjectItem<?>> getEntityBinder() { return entityBinder; }

	@Override
	public void onEntitySaved(final EntityClass entity) throws Exception {
		LOGGER.debug("Entity saved, refreshing grid");
		refreshGrid();
		populateForm();
		// Show success notification
		CNotificationService.showSaveSuccess();
		navigateToClass();
	}

	/** Override to handle CGanttItem selection - it's a DTO wrapper, not the actual entity. Selection is logged but no form editing occurs since
	 * CGanttItem is read-only. */
	@Override
	protected void onSelectionChanged(final CMasterViewSectionBase.SelectionChangeEvent<EntityClass> event) {
		// CGanttItem ganttItem=event.getSelectedItem();
		final EntityClass value = event.getSelectedItem();
		// Check if selected item is CGanttItem (DTO wrapper)
		if (value != null) {
			Check.instanceOf(value, CGanttItem.class, "Selected item is not a CGanttItem");
			setCurrentEntity(value);
			populateForm();
			return;
		} else {
			// Standard entity selection handling for non-DTO items
			super.onSelectionChanged(event);
		}
	}

	@Override
	public void populateForm() {
		try {
			LOGGER.debug("Populating form for entity: {}", getCurrentEntity() != null ? getCurrentEntity().getName() : "null");
			// Implementation to populate the form with current entity details
			updateDetailsComponent();
		} catch (Exception e) {
			CNotificationService.showException("Error populating form", e);
		}
	}

	@SuppressWarnings ("unchecked")
	@Override
	protected void updateDetailsComponent() throws Exception {
		LOGGER.debug("Updating details component for Gantt view - no detail form available.");
		getBaseDetailsLayout().removeAll();
		if (getCurrentEntity() == null) {
			return;
		}
		// fetch new fresh entities for the gantt item
		CProjectItem<?> ganttEntity = ((CGanttItem) getCurrentEntity()).getGanntItem(activityService, meetingService);
		if (ganttEntity == null) {
			LOGGER.warn("Gantt item entity is null, cannot populate details form.");
			return;
		}
		entityBinder = new CEnhancedBinder<CProjectItem<?>>((Class<CProjectItem<?>>) ganttEntity.getClass());
		final Field viewNameField = ganttEntity.getClass().getField("VIEW_NAME");
		final String entityViewName = (String) viewNameField.get(null);
		buildScreen(entityViewName, entityBinder);
		// final CVerticalLayout formLayout = CFormBuilder.buildForm(ganttEntity.getClass(), entityBinder, null, this);
		// getBaseDetailsLayout().add(formLayout);
		entityBinder.readBean(ganttEntity);
		crudToolbar.setCurrentEntity(ganttEntity);
		// Update the page service with the current actual entity
		if (getPageService() instanceof CPageServiceProjectGannt) {
			((CPageServiceProjectGannt) getPageService()).setCurrentActualEntity(ganttEntity);
		}
	}
}
