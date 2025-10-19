package tech.derbent.api.services.pageservice;

import tech.derbent.api.domains.CEntityDB;
import tech.derbent.page.view.CDynamicPageBase;

public abstract class CPageServiceDynamicPage<EntityClass extends CEntityDB<EntityClass>> extends CPageService<EntityClass> {

	public CPageServiceDynamicPage(CDynamicPageBase view) {
		super(view);
	}

	@Override
	public void bind() {
		super.bind();
	}
}
