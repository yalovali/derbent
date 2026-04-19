package tech.derbent.api.pagequery.ui;

import tech.derbent.api.pagequery.domain.CPageViewFilterVisibility;
import tech.derbent.api.screens.view.CComponentGridEntity;
import tech.derbent.api.session.service.ISessionService;

/** IDetailsMasterToolbarExtensionFactory - Factory for creating per-view master toolbar filter extensions.
 * <p>
 * Factories are Spring beans (singleton, thread-safe) and create per-toolbar instances holding UI state.
 * This avoids storing Vaadin components inside singleton beans.
 * </p>
 */
public interface IDetailsMasterToolbarExtensionFactory {

	boolean supports(Class<?> entityClass);

	IDetailsMasterToolbarExtensionInstance create(CComponentGridEntity grid, CPageViewFilterVisibility visibility,
			ISessionService sessionService) throws Exception;
}
