package tech.derbent.screens.view;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.service.CScreenService;

public abstract class CPanelScreenBase extends CAccordionDBEntity<CScreen> {

    private static final long serialVersionUID = 1L;

    public CPanelScreenBase(final String title, final CScreen currentEntity,
                           final CEnhancedBinder<CScreen> beanValidationBinder,
                           final CScreenService entityService) {
        super(title, currentEntity, beanValidationBinder, CScreen.class, entityService);
    }
}