package tech.derbent.activities.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

/**
 * CPanelActivityAdditionalInfo - Panel for grouping additional information fields
 * of CActivity entity.
 * Layer: View (MVC)
 * Groups fields: acceptanceCriteria, notes
 */
public class CPanelActivityAdditionalInfo extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityAdditionalInfo(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService) {
		super("Additional Info", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Additional Information fields - extra details and documentation
		setEntityFields(List.of("acceptanceCriteria", "notes"));
	}
}