package tech.derbent.app.kanban.kanbanline.view;

import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.utils.Check;

/** CComponentPostit - A compact post-it style widget for displaying project items inside kanban columns. */
public class CComponentPostit extends CComponentWidgetEntity<CProjectItem<?>> {

	private static final long serialVersionUID = 1L;

	public CComponentPostit(final CProjectItem<?> item) {
		super(item);
		Check.notNull(item, "Project item cannot be null for postit");
		getStyle().set("background-color", "#FFFDE7").set("border-radius", "8px").set("padding", "10px").set("box-shadow",
				"0 1px 3px rgba(0, 0, 0, 0.12)").set("width", "100%");
	}

	@Override
	protected void createFirstLine() throws Exception {
		layoutLineOne.setWidthFull();
		layoutLineOne.add(CLabelEntity.createH3Label(entity));
	}

	@Override
	protected void createSecondLine() throws Exception {
		if (entity.getStatus() != null) {
			layoutLineTwo.add(new CLabelEntity(entity.getStatus()));
		}
		if (entity.getResponsible() != null) {
			layoutLineTwo.add(CLabelEntity.createUserLabel(entity.getResponsible()));
		}
	}

	@Override
	protected void createThirdLine() {
		if (entity.getStartDate() != null || entity.getEndDate() != null) {
			layoutLineThree.add(CLabelEntity.createDateRangeLabel(entity.getStartDate(), entity.getEndDate()));
		}
	}
}
