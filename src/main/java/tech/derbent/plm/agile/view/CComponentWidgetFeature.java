package tech.derbent.plm.agile.view;

import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.plm.agile.domain.CFeature;

public class CComponentWidgetFeature extends CComponentWidgetEntityOfProject<CFeature> {

	private static final long serialVersionUID = 1L;

	public CComponentWidgetFeature(final CFeature feature) {
		super(feature);
	}

	@Override
	protected void createThirdLine() throws Exception {
		super.createThirdLine();
		final CFeature feature = getEntity();
		if (feature.hasParentActivity()) {
			try {
				final CFeature parentFeature = (CFeature) feature.getParentActivity();
				if (parentFeature != null) {
					final CLabelEntity parentLabel = new CLabelEntity(parentFeature);
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
