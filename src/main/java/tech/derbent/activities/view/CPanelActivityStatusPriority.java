package tech.derbent.activities.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

/**
 * CPanelActivityStatusPriority - Panel for grouping status and priority related fields of CActivity entity. Layer: View
 * (MVC) Groups fields: status, priority, progressPercentage
 */
public class CPanelActivityStatusPriority extends CPanelActivityBase {

    private static final long serialVersionUID = 1L;

    public CPanelActivityStatusPriority(final CActivity currentEntity,
            final BeanValidationBinder<CActivity> beanValidationBinder, final CActivityService entityService) {
        super("Status & Priority", currentEntity, beanValidationBinder, entityService);
        openPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Status & Priority fields - workflow and progress management
        setEntityFields(List.of("status", "priority", "progressPercentage"));
    }
}