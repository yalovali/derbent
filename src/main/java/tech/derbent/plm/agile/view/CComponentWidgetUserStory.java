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
		final CUserStory userStory = getEntity();
		if (userStory.hasParentActivity()) {
			try {
				final CUserStory parentUserStory = (CUserStory) userStory.getParentActivity();
				if (parentUserStory != null) {
					final CLabelEntity parentLabel = new CLabelEntity(parentUserStory);
					parentLabel.getStyle().set("font-style", "italic");
					parentLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
					layoutLineThree.add(parentLabel);
				}
			} catch (@SuppressWarnings ("unused") final Exception e) {
				// Silently ignore if parent cannot be loaded
			}
		}
	}
}
