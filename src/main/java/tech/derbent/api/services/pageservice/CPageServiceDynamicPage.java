package tech.derbent.api.services.pageservice;

import tech.derbent.api.domains.CEntityDB;

public abstract class CPageServiceDynamicPage<EntityClass extends CEntityDB<EntityClass>> extends CPageService<EntityClass> {

	public CPageServiceDynamicPage(IPageServiceImplementer view) {
		super(view);
	}

	@Override
	public void bind() {
		super.bind();
	}
}
