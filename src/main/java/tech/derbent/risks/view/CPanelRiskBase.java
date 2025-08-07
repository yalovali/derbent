package tech.derbent.risks.view;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;

/**
 * CPanelRiskBase - Abstract base class for all CRisk-related accordion panels. Layer:
 * View (MVC) Provides common functionality for risk entity panels following the same
 * pattern as CPanelActivityBase.
 */
public abstract class CPanelRiskBase extends CAccordionDBEntity<CRisk> {

	private static final long serialVersionUID = 1L;

	public CPanelRiskBase(final String title, final CRisk currentEntity,
		final CEnhancedBinder<CRisk> beanValidationBinder,
		final CRiskService entityService) {
		super(title, currentEntity, beanValidationBinder, CRisk.class, entityService);
	}
}