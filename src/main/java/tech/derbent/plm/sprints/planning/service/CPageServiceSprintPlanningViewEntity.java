package tech.derbent.plm.sprints.planning.service;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.UI;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.page.domain.CPageEntity;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.page.view.CDynamicPageViewWithoutGrid;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.sprints.planning.domain.CSprintPlanningViewEntity;
import tech.derbent.plm.sprints.planning.view.CComponentSprintPlanningBoard;

public class CPageServiceSprintPlanningViewEntity extends CPageServiceDynamicPage<CSprintPlanningViewEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceSprintPlanningViewEntity.class);

	private CComponentSprintPlanningBoard componentSprintPlanningBoard;
	private CButton buttonOpenBoard;
	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;

	public CPageServiceSprintPlanningViewEntity(final IPageServiceImplementer<CSprintPlanningViewEntity> view) {
		super(view);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		sessionService = CSpringContext.getBean(ISessionService.class);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CSprintPlanningViewEntity");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CSprintPlanningViewEntity> gridView = (CGridViewBaseDBEntity<CSprintPlanningViewEntity>) getView();
			gridView.generateGridReport();
			return;
		}
		super.actionReport();
	}

	@Override
	public void bind() {
		try {
			super.bind();
			if (getView() instanceof CDynamicPageViewWithoutGrid) {
				final var layout = getView().getDetailsBuilder().getFormBuilder().getHorizontalLayout("sprintPlanningBoard");
				Objects.requireNonNull(layout, "Sprint planning board layout must not be null");
				layout.setHeightFull();
			}
		} catch (final Exception e) {
			LOGGER.error("Error binding {}: {}", getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	protected void configureToolbar(final CCrudToolbar toolbar) {
		super.configureToolbar(toolbar);
		if (buttonOpenBoard == null) {
			buttonOpenBoard = CButton.createTertiary("Open Planning", CColorUtils.createStyledIcon(CSprintPlanningViewEntity.DEFAULT_ICON),
				event -> on_actionOpenBoard());
			buttonOpenBoard.setId("cbutton-open-sprint-planning-board");
			buttonOpenBoard.getElement().setAttribute("title", "Open the dedicated sprint planning board page for this view");
		}
		if (buttonOpenBoard.getParent().isEmpty()) {
			toolbar.addCustomComponent(buttonOpenBoard);
		}
	}

	public CComponentSprintPlanningBoard createSprintPlanningBoardComponent() {
		if (componentSprintPlanningBoard == null) {
			componentSprintPlanningBoard = new CComponentSprintPlanningBoard(sessionService);
			registerComponent("sprintPlanningBoard", componentSprintPlanningBoard);
		}
		return componentSprintPlanningBoard;
	}

	private void on_actionOpenBoard() {
		try {
			final CSprintPlanningViewEntity currentEntity = getValue();
			Check.notNull(currentEntity, "Select a Sprint Planning view before opening the board");
			final CPageEntity boardPage = pageEntityService.findByNameAndProject(CSprintPlanningViewEntityInitializerService.BOARD_PAGE_NAME,
				sessionService.getActiveProject().orElse(null)).orElseThrow();
			UI.getCurrent().navigate("cdynamicpagerouter/page:" + boardPage.getId() + "&item:" + currentEntity.getId());
		} catch (final Exception e) {
			CNotificationService.showException("Unable to open sprint planning board", e);
		}
	}
}
