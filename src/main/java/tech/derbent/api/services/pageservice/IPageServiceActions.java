package tech.derbent.api.services.pageservice;

/** Interface defining CRUD actions for page services. Both CPageService and CEntityDBPageService implement this interface to provide consistent CRUD
 * operations for the toolbar. */
public interface IPageServiceActions {

	void actionCreate() throws Exception;

	void actionDelete() throws Exception;

	void actionRefresh() throws Exception;

	void actionSave() throws Exception;
}
