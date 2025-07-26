package tech.derbent.projects.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/**
 * CPanelProjectBasicInfo - Panel for grouping basic information fields
 * of CProject entity.
 * Layer: View (MVC)
 * Groups fields: name, description
 */
public class CPanelProjectBasicInfo extends CPanelProjectBase {

	private static final long serialVersionUID = 1L;

	public CPanelProjectBasicInfo(final CProject currentEntity,
		final BeanValidationBinder<CProject> beanValidationBinder,
		final CProjectService entityService) {
		super("Basic Information", currentEntity, beanValidationBinder, entityService);
		// only open this panel
		openPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Basic Information fields - project identity (CProject only has name and description from CEntityNamed)
		setEntityFields(List.of("name", "description"));
	}
}