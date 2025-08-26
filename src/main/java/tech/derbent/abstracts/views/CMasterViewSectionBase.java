package tech.derbent.abstracts.views;

import tech.derbent.abstracts.domains.CEntityDB;

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
