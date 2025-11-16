package tech.derbent.api.services.pageservice;

import tech.derbent.api.entity.domain.CEntityDB;

public abstract class CPageServiceDynamicPage<EntityClass extends CEntityDB<EntityClass>> extends CPageService<EntityClass> {

	public CPageServiceDynamicPage(final IPageServiceImplementer<EntityClass> view) {
		super(view);
	}

	@Override
	public void bind() {
		super.bind();
	}
}
