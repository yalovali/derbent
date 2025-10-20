package tech.derbent.api.screens.view;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.CAccordionDBEntity;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.service.CDetailSectionService;

public abstract class CPanelDetailSectionBase extends CAccordionDBEntity<CDetailSection> {

	private static final long serialVersionUID = 1L;

	public CPanelDetailSectionBase(final String title, IContentOwner parentContent, final CEnhancedBinder<CDetailSection> beanValidationBinder,
			final CDetailSectionService entityService) {
		super(title, parentContent, beanValidationBinder, CDetailSection.class, entityService);
	}
}
