package tech.derbent.plm.agile.view;

import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.plm.agile.domain.CUserStory;

public class CComponentWidgetUserStory extends CComponentWidgetEntityOfProject<CUserStory> {

	private static final long serialVersionUID = 1L;

	public CComponentWidgetUserStory(final CUserStory userStory) {
		super(userStory);
	}

	@Override
	protected void createThirdLine() throws Exception {
		super.createThirdLine();
		// Future enhancement: Display parent user story if available via agile parent relation
	}
}
