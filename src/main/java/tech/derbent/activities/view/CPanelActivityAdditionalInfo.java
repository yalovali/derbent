package tech.derbent.activities.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.session.service.SessionService;

/**
 * CPanelActivityAdditionalInfo - Panel for grouping additional information fields of
 * CActivity entity. Layer: View (MVC) Groups fields: acceptanceCriteria, notes
 */
public class CPanelActivityAdditionalInfo extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityAdditionalInfo(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService, final SessionService sessionService) {
		super("Additional Info", currentEntity, beanValidationBinder, entityService, sessionService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Additional Information fields - extra details and documentation
		setEntityFields(List.of("acceptanceCriteria", "notes"));
	}
}