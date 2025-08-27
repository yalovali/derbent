package tech.derbent.abstracts.views.grids;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.components.CDiv;

public abstract class CMasterViewSectionBase<EntityClass extends CEntityDB<EntityClass>> extends CDiv {
	private static final long serialVersionUID = 1L;

	public CMasterViewSectionBase() {
		super();
		setSizeFull();
		// Initialization code here
	}

	public abstract void createMasterView();
	// Additional methods and properties can be added here
}
