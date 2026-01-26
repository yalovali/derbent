package tech.derbent.plm.agile.view;

import tech.derbent.api.grid.view.CLabelEntity;
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
		final CEpic epic = getEntity();
		if (epic.hasParentActivity()) {
			try {
				final CEpic parentEpic = (CEpic) epic.getParentActivity();
				if (parentEpic != null) {
					final CLabelEntity parentLabel = new CLabelEntity(parentEpic);
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
