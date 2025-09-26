package tech.derbent.activities.view;

import java.util.List;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.session.service.CSessionService;

/** CPanelActivityAdditionalInfo - Panel for grouping additional information fields of CActivity entity. Layer: View (MVC) Groups fields:
 * acceptanceCriteria, notes */
public class CPanelActivityAdditionalInfo extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityAdditionalInfo(IContentOwner parentContent, final CActivity currentEntity,
			final CEnhancedBinder<CActivity> beanValidationBinder, final CActivityService entityService, final CSessionService sessionService)
			throws Exception {
		super("Additional Info", parentContent,beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Additional Information fields - extra details and documentation
		setEntityFields(List.of("acceptanceCriteria", "notes"));
	}
}
