package tech.derbent.plm.agile.service;

import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CUserStoryType;

/**
 * Dynamic page service for user story types.
 *
 * <p>Hierarchy editing for user-story types relies entirely on the inherited metadata-based dynamic
 * page infrastructure.</p>
 */
public class CPageServiceUserStoryType extends CPageServiceDynamicPage<CUserStoryType> {

	public CPageServiceUserStoryType(final IPageServiceImplementer<CUserStoryType> view) {
		super(view);
	}
}
