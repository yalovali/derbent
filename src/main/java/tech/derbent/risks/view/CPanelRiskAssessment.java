package tech.derbent.risks.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;

/**
 * CPanelRiskAssessment - Panel for grouping risk assessment fields
 * of CRisk entity.
 * Layer: View (MVC)
 * Groups fields: riskSeverity
 */
public class CPanelRiskAssessment extends CPanelRiskBase {

	private static final long serialVersionUID = 1L;

	public CPanelRiskAssessment(final CRisk currentEntity,
		final BeanValidationBinder<CRisk> beanValidationBinder,
		final CRiskService entityService) {
		super("Risk Assessment", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Risk Assessment fields - severity and evaluation
		setEntityFields(List.of("riskSeverity"));
	}
}