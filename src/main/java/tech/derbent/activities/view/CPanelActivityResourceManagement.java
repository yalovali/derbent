package tech.derbent.activities.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.session.service.SessionService;

/**
 * CPanelActivityResourceManagement - Panel for grouping resource management related fields
 * of CActivity entity.
 * Layer: View (MVC)
 * Groups fields: assignedTo, createdBy
 */
public class CPanelActivityResourceManagement extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityResourceManagement(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService, final SessionService sessionService) {
		super("Resource Management", currentEntity, beanValidationBinder, entityService, sessionService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Resource Management fields - user assignments and tracking
		setEntityFields(List.of("assignedTo", "createdBy"));
	}
}