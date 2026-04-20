package tech.derbent.plm.gnnt.gnntviewentity.service;

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
import tech.derbent.api.utils.Check;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntViewEntity;
import tech.derbent.plm.gnnt.gnntviewentity.view.CComponentGnntBoard;

public class CPageServiceGnntViewEntity extends CPageServiceDynamicPage<CGnntViewEntity> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceGnntViewEntity.class);

	private CComponentGnntBoard componentGnntBoard;
	private CButton buttonOpenGnntBoard;
	private final CPageEntityService pageEntityService;
	private final ISessionService sessionService;

	public CPageServiceGnntViewEntity(final IPageServiceImplementer<CGnntViewEntity> view) {
		super(view);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		sessionService = CSpringContext.getBean(ISessionService.class);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CGnntViewEntity");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CGnntViewEntity> gridView = (CGridViewBaseDBEntity<CGnntViewEntity>) getView();
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
				final var layout = getView().getDetailsBuilder().getFormBuilder().getHorizontalLayout("gnntBoard");
				Objects.requireNonNull(layout, "Gnnt board layout must not be null");
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
		if (buttonOpenGnntBoard == null) {
			buttonOpenGnntBoard = CButton.createTertiary("Open Gnnt", null, event -> on_actionOpenGnntBoard());
			buttonOpenGnntBoard.setId("cbutton-open-gnnt-board");
			buttonOpenGnntBoard.getElement().setAttribute("title", "Open the dedicated Gnnt board page for this view");
		}
		if (buttonOpenGnntBoard.getParent().isEmpty()) {
			toolbar.addCustomComponent(buttonOpenGnntBoard);
		}
	}

	public CComponentGnntBoard createGnntBoardComponent() {
		if (componentGnntBoard == null) {
			componentGnntBoard = new CComponentGnntBoard(sessionService);
			registerComponent("gnntBoard", componentGnntBoard);
		}
		return componentGnntBoard;
	}

	private void on_actionOpenGnntBoard() {
		try {
			final CGnntViewEntity currentEntity = getValue();
			Check.notNull(currentEntity, "Select a Gnnt view before opening the dedicated board");
			final CPageEntity boardPage = pageEntityService.findByNameAndProject(CGnntViewEntityInitializerService.BOARD_PAGE_NAME,
					sessionService.getActiveProject().orElse(null)).orElseThrow();
			UI.getCurrent().navigate("cdynamicpagerouter/page:" + boardPage.getId() + "&item:" + currentEntity.getId());
		} catch (final Exception e) {
			CNotificationService.showException("Unable to open Gnnt board", e);
		}
	}
}
