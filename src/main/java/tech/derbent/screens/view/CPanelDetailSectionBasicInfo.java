package tech.derbent.screens.view;

import java.util.List;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailSectionService;

public class CPanelDetailSectionBasicInfo extends CPanelDetailSectionBase {

	private static final long serialVersionUID = 1L;

	public CPanelDetailSectionBasicInfo(IContentOwner parentContent, final CDetailSection currentEntity,
			final CEnhancedBinder<CDetailSection> beanValidationBinder, final CDetailSectionService entityService) throws Exception {
		super("Basic Information", parentContent,beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// setEntityFields(List.of("entityType"));
		setEntityFields(List.of("name", "description", "entityType", "screenTitle", "headerText", "isActive"));
	}
}
