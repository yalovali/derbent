package tech.derbent.api.ui.component;

import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.ui.component.enhanced.CCrudToolbar;

public interface ICrudToolbarOwnerPage extends IContentOwner {

	public CCrudToolbar getCrudToolbar();
	CPageService<?> getPageService();
}
