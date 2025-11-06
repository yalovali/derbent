package tech.derbent.api.services.pageservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.views.CAbstractEntityDBPage;

/** Page service implementation for CAbstractEntityDBPage that delegates CRUD actions to the page's existing action methods. This allows pages
 * extending CAbstractEntityDBPage to work with the new ICrudToolbarOwnerPage pattern without changing their structure. This is a lightweight wrapper
 * that does not extend CPageService since CAbstractEntityDBPage does not extend CDynamicPageBase. */
public class CEntityDBPageService<EntityClass extends CEntityDB<EntityClass>> implements IPageServiceActions {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityDBPageService.class);
	private final CAbstractEntityDBPage<EntityClass> page;

	public CEntityDBPageService(final CAbstractEntityDBPage<EntityClass> page) {
		this.page = page;
	}

	public void actionCreate() throws Exception {
		LOGGER.debug("CEntityDBPageService: delegating create to page");
		page.actionCreate();
	}

	public void actionDelete() throws Exception {
		LOGGER.debug("CEntityDBPageService: delegating delete to page");
		page.actionDelete();
	}

	public void actionRefresh() throws Exception {
		LOGGER.debug("CEntityDBPageService: delegating refresh to page");
		page.actionRefresh();
	}

	public void actionSave() throws Exception {
		LOGGER.debug("CEntityDBPageService: delegating save to page");
		page.actionSave();
	}
}
