package tech.derbent.api.views.components;

import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.pageservice.IPageServiceActions;

public interface ICrudToolbarOwnerPage extends IContentOwner {

	IPageServiceActions getPageService();
}
