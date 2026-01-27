package tech.derbent.plm.agile.view;

import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.plm.agile.domain.CEpic;

public class CComponentWidgetEpic extends CComponentWidgetEntityOfProject<CEpic> {

	private static final long serialVersionUID = 1L;

	public CComponentWidgetEpic(final CEpic epic) {
		super(epic);
	}

	@Override
	protected void createThirdLine() throws Exception {
		super.createThirdLine();
		// Future enhancement: Display parent epic if available via agile parent relation
	}
}
