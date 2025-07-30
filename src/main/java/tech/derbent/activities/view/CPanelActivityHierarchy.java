package tech.derbent.activities.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

/**
 * CPanelActivityHierarchy - Panel for grouping hierarchy related fields of CActivity
 * entity. Layer: View (MVC) Groups fields: parentActivity
 */
public class CPanelActivityHierarchy extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityHierarchy(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService) {
		super("Hierarchy", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Hierarchy fields - parent-child relationships
		setEntityFields(List.of("parentActivity"));
	}
}