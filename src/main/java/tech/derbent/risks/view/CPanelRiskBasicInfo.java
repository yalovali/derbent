package tech.derbent.risks.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;

/**
 * CPanelRiskBasicInfo - Panel for grouping basic information fields of CRisk entity.
 * Layer: View (MVC) Groups fields: name, description, project
 */
public class CPanelRiskBasicInfo extends CPanelRiskBase {

	private static final long serialVersionUID = 1L;

	public CPanelRiskBasicInfo(final CRisk currentEntity,
		final CEnhancedBinder<CRisk> beanValidationBinder,
		final CRiskService entityService) throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {
		super("Basic Information", currentEntity, beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Basic Information fields - risk identity and project context
		setEntityFields(List.of("name", "description", "project"));
	}
}