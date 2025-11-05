package tech.derbent.api.services.pageservice;

import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.page.view.CDynamicPageBase;

public abstract class CPageService<EntityClass extends CEntityDB<EntityClass>> {

	final protected CDynamicPageBase view;

	public CPageService(CDynamicPageBase view) {
		this.view = view;
	}

	public abstract void actionCreate();

	public void bind() {}

	protected Class<?> getEntityClass() {
		Check.notNull(view, "View is not set in page service");
		return view.getEntityClass();
	}

	@SuppressWarnings ("unchecked")
	protected CAbstractService<EntityClass> getEntityService() {
		Check.notNull(view, "View is not set in page service");
		return (CAbstractService<EntityClass>) view.getEntityService();
	}
}
