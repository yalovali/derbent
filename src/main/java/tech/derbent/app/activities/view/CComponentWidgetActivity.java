package tech.derbent.app.activities.view;

import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.app.activities.domain.CActivity;

public class CComponentWidgetActivity extends CComponentWidgetEntityOfProject<CActivity> {

	private static final long serialVersionUID = 1L;

	public CComponentWidgetActivity(final CActivity activity) {
		super(activity);
		addEditAction();
		addDeleteAction();
	}

	protected void createFirstLine() {
		String name = getEntity().getName();
		if (name == null || name.isEmpty()) {
			name = "(No Name)";
		}
		layoutLineOne.add(new CH3(name));
	}

	protected void createSecondLine() {
		String description = getEntity().getDescription();
		if (description != null && description.length() > MAX_DESCRIPTION_LENGTH) {
			description = description.substring(0, MAX_DESCRIPTION_LENGTH) + "...";
		}
		if (description == null || description.isEmpty()) {
			description = "(No Description)";
		}
		layoutLineTwo.add(new CDiv(description));
	}

	protected void createThirdLine() {
		// responsible user
		String responsibleUser = getEntity().getAssignedTo().getName();
		if (responsibleUser == null || responsibleUser.isEmpty()) {
			responsibleUser = "(No Responsible User)";
		}
		layoutLineThree.add(new CDiv("Responsible: " + responsibleUser));
	}
}
