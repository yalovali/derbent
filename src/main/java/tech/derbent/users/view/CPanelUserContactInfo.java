package tech.derbent.users.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * CPanelUserContactInfo - Panel for grouping contact information fields
 * of CUser entity.
 * Layer: View (MVC)
 * Groups fields: email, phone
 */
public class CPanelUserContactInfo extends CPanelUserBase {

	private static final long serialVersionUID = 1L;

	public CPanelUserContactInfo(final CUser currentEntity,
		final BeanValidationBinder<CUser> beanValidationBinder,
		final CUserService entityService) {
		super("Contact Information", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Contact Information fields - communication details
		setEntityFields(List.of("email", "phone"));
	}
}