package tech.derbent.screens.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenService;

public class CPanelScreenBasicInfo extends CPanelScreenBase {

    private static final long serialVersionUID = 1L;

    public CPanelScreenBasicInfo(final CScreen currentEntity, final CEnhancedBinder<CScreen> beanValidationBinder,
            final CScreenService entityService) {
        super("Basic Information", currentEntity, beanValidationBinder, entityService);
        initPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // setEntityFields(List.of("entityType"));
        setEntityFields(List.of("name", "description", "entityType", "screenTitle", "headerText", "isActive"));
    }
}