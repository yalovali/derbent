package tech.derbent.api.users.dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.Binder;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.api.utils.Check;

/** CDialogPasswordChange - Dialog for changing user passwords. Features: - Old password verification - New password confirmation - Password strength
 * validation - BCrypt encoding - LDAP user handling */
public class CDialogPasswordChange extends CDialog {

	/** Data class for password change form binding. */
	public static class PasswordChangeData {

		private String confirmPassword = "";
		private String newPassword = "";
		private String oldPassword = "";

		public String getConfirmPassword() { return confirmPassword; }

		public String getNewPassword() { return newPassword; }

		public String getOldPassword() { return oldPassword; }

		public void setConfirmPassword(final String confirmPassword) { this.confirmPassword = confirmPassword; }

		public void setNewPassword(final String newPassword) { this.newPassword = newPassword; }

		public void setOldPassword(final String oldPassword) { this.oldPassword = oldPassword; }
	}

	public static final String ID_BUTTON_CANCEL = "custom-password-change-cancel-button";
	public static final String ID_BUTTON_OK = "custom-password-change-ok-button";
	public static final String ID_FIELD_CONFIRM_PASSWORD = "custom-password-change-confirm-field";
	public static final String ID_FIELD_NEW_PASSWORD = "custom-password-change-new-field";
	public static final String ID_FIELD_OLD_PASSWORD = "custom-password-change-old-field";
	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogPasswordChange.class);
	private static final long serialVersionUID = 1L;
	private final Binder<PasswordChangeData> binder = new Binder<>(PasswordChangeData.class);
	// UI Components
	private CButton buttonCancel;
	private CButton buttonOk;
	private PasswordField fieldConfirmPassword;
	private PasswordField fieldNewPassword;
	private PasswordField fieldOldPassword;
	// Dependencies
	private final CUser user;
	private final CUserService userService;

	/** Constructor for password change dialog.
	 * @param user        the user whose password will be changed
	 * @param userService the user service for saving changes */
	public CDialogPasswordChange(final CUser user, final CUserService userService) throws Exception {
		Check.notNull(user, "User cannot be null");
		Check.notNull(userService, "User service cannot be null");
		this.user = user;
		this.userService = userService;
		setupDialog();
		configureBinder();
	}

	private void configureBinder() {
		// Bind old password field (not validated here - will be validated on save)
		binder.forField(fieldOldPassword).bind(PasswordChangeData::getOldPassword, PasswordChangeData::setOldPassword);
		// Bind new password with validation
		binder.forField(fieldNewPassword)
				.withValidator(password -> password != null && password.length() >= 6, "New password must be at least 6 characters long")
				.bind(PasswordChangeData::getNewPassword, PasswordChangeData::setNewPassword);
		// Bind confirm password with matching validation
		binder.forField(fieldConfirmPassword).withValidator(confirmPassword -> {
			final String newPassword = fieldNewPassword.getValue();
			return confirmPassword != null && confirmPassword.equals(newPassword);
		}, "Password confirmation does not match new password").bind(PasswordChangeData::getConfirmPassword, PasswordChangeData::setConfirmPassword);
		// Set empty bean
		binder.setBean(new PasswordChangeData());
	}

	private void createPasswordFields() {
		// Old password field
		fieldOldPassword = new PasswordField("Current Password");
		fieldOldPassword.setId(ID_FIELD_OLD_PASSWORD);
		fieldOldPassword.setHelperText("Enter your current password to verify your identity");
		fieldOldPassword.setRequired(true);
		fieldOldPassword.setWidth("100%");
		// New password field
		fieldNewPassword = new PasswordField("New Password");
		fieldNewPassword.setId(ID_FIELD_NEW_PASSWORD);
		fieldNewPassword.setHelperText("Password must be at least 6 characters long");
		fieldNewPassword.setRequired(true);
		fieldNewPassword.setWidth("100%");
		// Confirm password field
		fieldConfirmPassword = new PasswordField("Confirm New Password");
		fieldConfirmPassword.setId(ID_FIELD_CONFIRM_PASSWORD);
		fieldConfirmPassword.setHelperText("Re-enter the new password to confirm");
		fieldConfirmPassword.setRequired(true);
		fieldConfirmPassword.setWidth("100%");
		// Create form layout
		final FormLayout formLayout = new FormLayout();
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
		formLayout.add(fieldOldPassword, fieldNewPassword, fieldConfirmPassword);
		mainLayout.add(formLayout);
	}

	@Override
	public String getDialogTitleString() { return "Change Password for " + user.getName(); }

	@Override
	protected Icon getFormIcon() throws Exception { return VaadinIcon.KEY.create(); }

	@Override
	protected String getFormTitleString() { return "Change Password"; }

	private void on_buttonCancel_clicked() {
		LOGGER.debug("Password change cancelled by user");
		close();
	}

	private void on_buttonOk_clicked() {
		try {
			LOGGER.debug("Password change requested for user: {}", user.getLogin());
			// Validate form
			if (!binder.validate().isOk()) {
				CNotificationService.showWarning("Please fix validation errors before continuing");
				return;
			}
			final PasswordChangeData data = binder.getBean();
			// Validate old password (for non-LDAP users)
			if (!user.isLDAPUser()) {
				if (!userService.validateCurrentPassword(user, data.getOldPassword())) {
					CNotificationService.showError("Current password is incorrect");
					fieldOldPassword.focus();
					return;
				}
			}
			// Set new password using service method
			userService.setUserPassword(user, data.getNewPassword());
			// Success
			CNotificationService.showSuccess("Password changed successfully");
			LOGGER.info("Password changed successfully for user: {}", user.getLogin());
			close();
		} catch (final Exception e) {
			LOGGER.error("Failed to change password for user: {}", user.getLogin(), e);
			CNotificationService.showException("Failed to change password", e);
		}
	}

	@Override
	protected void setupButtons() {
		buttonCancel = new CButton("Cancel", null);
		buttonCancel.setId(ID_BUTTON_CANCEL);
		buttonCancel.addClickListener(e -> on_buttonCancel_clicked());
		buttonOk = new CButton("Change Password", null);
		buttonOk.setId(ID_BUTTON_OK);
		buttonOk.addClickListener(e -> on_buttonOk_clicked());
		buttonLayout.add(buttonCancel, buttonOk);
	}

	@Override
	protected void setupContent() throws Exception {
		// LDAP warning if needed
		if (user.isLDAPUser()) {
			final CDiv ldapWarning = createTextBannerSection(
					"⚠️ This user is configured for LDAP authentication. "
							+ "Password changes will only affect local database authentication and may be ignored if LDAP is active.",
					CUIConstants.COLOR_WARNING_TEXT, CUIConstants.GRADIENT_WARNING);
			mainLayout.add(ldapWarning);
		}
		// Password fields form
		createPasswordFields();
	}
}
