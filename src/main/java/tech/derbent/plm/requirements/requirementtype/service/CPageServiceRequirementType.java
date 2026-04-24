package tech.derbent.plm.requirements.requirementtype.service;

import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.plm.requirements.requirementtype.domain.CRequirementType;

/**
 * Dynamic page service for requirement types.
 *
 * <p>The inherited hierarchy level combo provider is what makes the metadata-driven integer ComboBox
 * available on the type form.</p>
 */
public class CPageServiceRequirementType extends CPageServiceDynamicPage<CRequirementType> {

	public CPageServiceRequirementType(final IPageServiceImplementer<CRequirementType> view) {
		super(view);
	}
}
