package tech.derbent.projects.view;

import java.util.List;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/** CPanelProjectBasicInfo - Panel for grouping basic information fields of CProject entity. Layer: View (MVC) Groups fields: name, description */
public class CPanelProjectBasicInfo extends CPanelProjectBase {

	private static final long serialVersionUID = 1L;

	public CPanelProjectBasicInfo(final CProject currentEntity, final CEnhancedBinder<CProject> beanValidationBinder,
			final CProjectService entityService) throws Exception {
		super("Basic Information", currentEntity, beanValidationBinder, entityService);
		// only open this panel
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Basic Information fields - project identity (CProject only has name and
		// description from CEntityNamed)
		setEntityFields(List.of("name", "description"));
	}
}
