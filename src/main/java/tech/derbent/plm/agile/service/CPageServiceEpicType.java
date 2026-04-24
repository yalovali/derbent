package tech.derbent.plm.agile.service;

import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CEpicType;

/**
 * Dynamic page service for epic types.
 *
 * <p>No custom widget or report logic is needed because the inherited dynamic-page behavior already
 * covers metadata-driven forms and CSV export.</p>
 */
public class CPageServiceEpicType extends CPageServiceDynamicPage<CEpicType> {

	public CPageServiceEpicType(final IPageServiceImplementer<CEpicType> view) {
		super(view);
	}
}
