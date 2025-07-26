package tech.derbent.users.view;

import java.util.List;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * CPanelUserSystemAccess - Panel for grouping system access related fields
 * of CUser entity.
 * Layer: View (MVC)
 * Groups fields: password, roles, userRole, enabled
 */
public class CPanelUserSystemAccess extends CPanelUserBase {

	private static final long serialVersionUID = 1L;

	public CPanelUserSystemAccess(final CUser currentEntity,
		final BeanValidationBinder<CUser> beanValidationBinder,
		final CUserService entityService) {
		super("System Access", currentEntity, beanValidationBinder, entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// System Access fields - authentication and authorization
		setEntityFields(List.of("password", "roles", "userRole", "enabled"));
	}
}