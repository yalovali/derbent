package tech.derbent.activities.view;

import java.lang.reflect.InvocationTargetException;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

public class CPanelActivityHierarchy extends CPanelActivityBase {
	private static final long serialVersionUID = 1L;

	public CPanelActivityHierarchy(final CActivity currentEntity, final CEnhancedBinder<CActivity> beanValidationBinder,
			final CActivityService entityService) throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		super("Hierarchy", currentEntity, beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Hierarchy fields - parent-child relationships
		// setEntityFields(List.of("parentActivity"));
	}
}
