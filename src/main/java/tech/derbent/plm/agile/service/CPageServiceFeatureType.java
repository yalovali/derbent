package tech.derbent.plm.agile.service;

import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.agile.domain.CFeatureType;

/**
 * Dynamic page service for feature types.
 *
 * <p>The type form is fully metadata-driven, so this class intentionally stays minimal.</p>
 */
public class CPageServiceFeatureType extends CPageServiceDynamicPage<CFeatureType> {

	public CPageServiceFeatureType(final IPageServiceImplementer<CFeatureType> view) {
		super(view);
	}
}
