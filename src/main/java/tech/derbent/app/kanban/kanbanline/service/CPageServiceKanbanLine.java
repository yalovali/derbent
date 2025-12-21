package tech.derbent.app.kanban.kanbanline.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.app.kanban.kanbanline.view.CComponentKanbanBoard;
import tech.derbent.app.kanban.kanbanline.view.CComponentListKanbanColumns;

public class CPageServiceKanbanLine extends CPageServiceDynamicPage<CKanbanLine> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceKanbanLine.class);
	private static final long serialVersionUID = 1L;
	private CComponentListKanbanColumns componentKanbanColumns;
	private CKanbanColumnService kanbanColumnService;
	private CComponentKanbanBoard componentKanbanBoard;

	public CPageServiceKanbanLine(final IPageServiceImplementer<CKanbanLine> view) {
		super(view);
		try {
			kanbanColumnService = CSpringContext.getBean(CKanbanColumnService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CKanbanColumnService", e);
		}
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CKanbanLine.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CKanbanLine.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	public CComponentKanbanBoard createKanbanBoardComponent() {
		final CKanbanLine currentLine = getView().getCurrentEntity();
		Check.notNull(currentLine, "Kanban line must be available to create board component");
		if (componentKanbanBoard == null) {
			componentKanbanBoard = new CComponentKanbanBoard();
		}
		componentKanbanBoard.setValue(currentLine);
		return componentKanbanBoard;
	}

	public CComponentListKanbanColumns createKanbanColumnsComponent() {
		if (componentKanbanColumns == null) {
			componentKanbanColumns = new CComponentListKanbanColumns(kanbanColumnService);
			componentKanbanColumns.registerWithPageService(this);
		}
		return componentKanbanColumns;
	}
}
