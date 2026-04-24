package tech.derbent.plm.requirements.requirementtype.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceRequirementType.class);
	private static final long serialVersionUID = 1L;

	public CPageServiceRequirementType(final IPageServiceImplementer<CRequirementType> view) {
		super(view);
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CRequirementType");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CRequirementType> gridView = (CGridViewBaseDBEntity<CRequirementType>) getView();
			gridView.generateGridReport();
			return;
		}
		super.actionReport();
	}
}
