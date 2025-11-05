package tech.derbent.api.views.components;

import tech.derbent.api.interfaces.IContentOwner;

public interface ICrudToolbarOwnerPage extends IContentOwner {

	void actionCreate();
	void actionDelete();
	void actionRefresh();
	void actionSave();
}
