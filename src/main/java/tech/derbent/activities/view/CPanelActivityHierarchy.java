package tech.derbent.activities.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

/**
 * CPanelActivityHierarchy - Panel for grouping hierarchy related fields of CActivity
 * entity. Layer: View (MVC) Groups fields: parentActivity
 */
public class CPanelActivityHierarchy extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityHierarchy(final CActivity currentEntity,
		final CEnhancedBinder<CActivity> beanValidationBinder,
		final CActivityService entityService) throws NoSuchMethodException,
		SecurityException, IllegalAccessException, InvocationTargetException {
		super("Hierarchy", currentEntity, beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Hierarchy fields - parent-child relationships
		setEntityFields(List.of("parentActivity"));
	}
}