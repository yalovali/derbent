package tech.derbent.api.ui.component;

import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.pageservice.CPageService;

public interface ICrudToolbarOwnerPage extends IContentOwner {

	CPageService<?> getPageService();
}
