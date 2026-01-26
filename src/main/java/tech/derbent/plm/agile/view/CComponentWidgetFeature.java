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
		// Future enhancement: Display parent feature if available via agile parent relation
	}
}
