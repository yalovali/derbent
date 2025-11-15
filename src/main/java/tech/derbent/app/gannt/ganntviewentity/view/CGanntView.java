package tech.derbent.app.gannt.ganntviewentity.view;

import tech.derbent.api.ui.component.CDiv;
import tech.derbent.app.gannt.ganntviewentity.domain.CGanntViewEntity;

public class CGanntView extends CDiv {

	private static final long serialVersionUID = 1L;
	private CGanntViewEntity entity;

	public CGanntView() {
		super("This is Gannt View");
	}

	private void populateView() {
		if (entity == null) {
			this.add("No Gannt View Entity set.");
			return;
		}
	}

	public void setGanntViewEntity(final CGanntViewEntity entity) {
		this.entity = entity;
		populateView();
	}
}
