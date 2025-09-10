package tech.derbent.screens.view;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailSectionService;

public abstract class CPanelDetailSectionBase extends CAccordionDBEntity<CDetailSection> {

	private static final long serialVersionUID = 1L;

	public CPanelDetailSectionBase(final String title, final CDetailSection currentEntity, final CEnhancedBinder<CDetailSection> beanValidationBinder,
			final CDetailSectionService entityService) {
		super(title, currentEntity, beanValidationBinder, CDetailSection.class, entityService);
	}
}
