package tech.derbent.activities.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

public class CPanelActivityResourceManagement extends CPanelActivityBase {

    private static final long serialVersionUID = 1L;

    public CPanelActivityResourceManagement(final CActivity currentEntity,
            final CEnhancedBinder<CActivity> beanValidationBinder, final CActivityService entityService)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        super("Resource Management", currentEntity, beanValidationBinder, entityService);
        initPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        setEntityFields(List.of("assignedTo", "createdBy"));
    }
}