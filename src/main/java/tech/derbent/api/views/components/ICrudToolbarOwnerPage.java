package tech.derbent.api.views.components;

import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.pageservice.CPageService;

public interface ICrudToolbarOwnerPage extends IContentOwner {

	CPageService<?> getPageService();
}
