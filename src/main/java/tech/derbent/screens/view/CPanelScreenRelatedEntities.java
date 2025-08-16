package tech.derbent.screens.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenService;

public class CPanelScreenRelatedEntities extends CPanelScreenBase {

    private static final long serialVersionUID = 1L;

    public CPanelScreenRelatedEntities(final CScreen currentEntity,
                                      final CEnhancedBinder<CScreen> beanValidationBinder,
                                      final CScreenService entityService) {
        super("Related Entities", currentEntity, beanValidationBinder, entityService);
        initPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Related entities panel - relationships to other domain entities
        setEntityFields(List.of("relatedActivity", "relatedMeeting", "relatedRisk"));
    }
}