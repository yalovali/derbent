package tech.derbent.risks.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;

/**
 * CPanelRiskBasicInfo - Panel for grouping basic information fields of CRisk entity. Layer: View (MVC) Groups fields:
 * name, description, project
 */
public class CPanelRiskBasicInfo extends CPanelRiskBase {

    private static final long serialVersionUID = 1L;

    public CPanelRiskBasicInfo(final CRisk currentEntity, final BeanValidationBinder<CRisk> beanValidationBinder,
            final CRiskService entityService) {
        super("Basic Information", currentEntity, beanValidationBinder, entityService);
        // only open this panel
        openPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Basic Information fields - risk identity and project context
        setEntityFields(List.of("name", "description", "project"));
    }
}