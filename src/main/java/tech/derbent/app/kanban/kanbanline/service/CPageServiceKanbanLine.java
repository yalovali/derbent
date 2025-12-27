package tech.derbent.app.kanban.kanbanline.service;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.kanban.kanbanline.view.CComponentKanbanBoard;
import tech.derbent.app.kanban.kanbanline.view.CComponentListKanbanColumns;
import tech.derbent.app.page.view.CDynamicPageViewWithoutGrid;

public class CPageServiceKanbanLine extends CPageServiceDynamicPage<CKanbanLine> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceKanbanLine.class);
	private CComponentKanbanBoard componentKanbanBoard;
	private CComponentListKanbanColumns componentKanbanColumns;
	private CKanbanColumnService kanbanColumnService;
	private CKanbanLineService kanbanLineService;

	/** Creates the page service and resolves kanban dependencies. */
	public CPageServiceKanbanLine(final IPageServiceImplementer<CKanbanLine> view) {
		super(view);
		try {
			kanbanColumnService = CSpringContext.getBean(CKanbanColumnService.class);
			kanbanLineService = CSpringContext.getBean(CKanbanLineService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize Kanban services", e);
		}
	}

	/** Binds the kanban line page and adjusts layout sizing. */
	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CKanbanLine.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
			if (getView() instanceof CDynamicPageViewWithoutGrid) {
				// after form creation
				final CHorizontalLayout layout = getView().getDetailsBuilder().getFormBuilder().getHorizontalLayout("kanbanBoard");
				Objects.requireNonNull(layout, "Kanban board layout must not be null");
				layout.setHeightFull();
			}
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CKanbanLine.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	/** Builds or returns the cached kanban board component. */
	public CComponentKanbanBoard createKanbanBoardComponent() {
		// it is null when ui is created
		// Check.notNull(currentLine, "Kanban line must be available to create board component");
		if (componentKanbanBoard == null) {
			componentKanbanBoard = new CComponentKanbanBoard();
		}
		// this is always null here, no problem
		// componentKanbanBoard.setValue(currentLine); let the binder handle this
		return componentKanbanBoard;
	}

	/** Builds or returns the cached kanban columns list component. */
	public CComponentListKanbanColumns createKanbanColumnsComponent() {
		LOGGER.debug("Creating Kanban columns component for Kanban line page service.");
		if (componentKanbanColumns == null) {
			componentKanbanColumns = new CComponentListKanbanColumns(kanbanLineService, kanbanColumnService);
			componentKanbanColumns.registerWithPageService(this);
		}
		return componentKanbanColumns;
	}

	/** Hook executed after binding for optional post-load work. */
	public void on_load_after_bind() throws Exception {
		// todo: implement if needed
	}
}
