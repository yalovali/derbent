package tech.derbent.api.setup.dialogs;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CTabSheet;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.constants.CUIConstants;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.ui.notifications.CNotificationService;

/** CEmailTestDialog - Enhanced email configuration test dialog. Layer: View (MVC) Profile: api (Common framework) Provides comprehensive email
 * testing interface with: - SMTP connection verification - Test email sending - Configuration validation - Detailed error reporting Following
 * AGENTS.md patterns: - CDialog base class with proper styling - CTabSheet for organized content - Responsive layout with fixed dialog size -
 * Color-coded results with proper feedback Similar pattern to CLdapTestDialog for consistency.
 * @see CLdapTestDialog
 * @see CSystemSettings */
public final class CEmailTestDialog extends CDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEmailTestDialog.class);
	private static final long serialVersionUID = 1L;
	private CButton buttonClose;
	// Tab content references
	private CButton buttonTestConnection;
	private VerticalLayout connectionTab;
	private VerticalLayout sendTestEmailTab;
	// Dependencies
	private final CSystemSettings<?> settings;
	// UI Components
	private CTabSheet tabSheet;

	/** Constructor for email test dialog.
	 * @param settings the system settings with email configuration */
	public CEmailTestDialog(final CSystemSettings<?> settings) throws Exception {
		this.settings = settings;
		setupDialog();
		// Auto-trigger connection test after dialog is rendered
		autoTriggerConnectionTest();
	}

	/** Auto-trigger connection test after dialog is rendered. Similar to LDAP test dialog pattern. */
	private void autoTriggerConnectionTest() {
		// Auto-trigger after 500ms delay to ensure dialog is fully rendered
		getElement().executeJs("setTimeout(() => { " + "  const button = document.getElementById('email-test-connection-button');"
				+ "  if (button) button.click();" + "}, 500);");
	}

	/** Create a single configuration line display. */
	private HorizontalLayout createConfigLine(final String label, final String value) {
		final HorizontalLayout line = new HorizontalLayout();
		line.setSpacing(true);
		line.setPadding(false);
		final Span labelSpan = new Span(label);
		labelSpan.getStyle().set("font-weight", CUIConstants.FONT_WEIGHT_MEDIUM).set("width", CUIConstants.LABEL_MIN_WIDTH_FORM);
		final Span valueSpan = new Span(value != null ? value : "(not set)");
		valueSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
		line.add(labelSpan, valueSpan);
		return line;
	}

	/** Create configuration display section showing current email settings. */
	private Div createConfigurationDisplay() {
		final Div configDiv = new Div();
		configDiv.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)").set("border-radius", "var(--lumo-border-radius-m)")
				.set("padding", "var(--lumo-space-m)").set("background-color", "var(--lumo-contrast-5pct)");
		final H4 configTitle = new H4("Current Configuration");
		configTitle.getStyle().set("margin", "0 0 var(--lumo-space-s) 0");
		final VerticalLayout configContent = new VerticalLayout();
		configContent.setSpacing(false);
		configContent.setPadding(false);
		configContent.getStyle().set("gap", CUIConstants.GAP_TINY);
		// Display key configuration values
		configContent.add(createConfigLine("SMTP Server:", settings.getSmtpServer()));
		configContent.add(createConfigLine("SMTP Port:", String.valueOf(settings.getSmtpPort())));
		configContent.add(createConfigLine("SMTP Login:", settings.getSmtpLoginName()));
		configContent.add(createConfigLine("Use TLS:", settings.getSmtpUseTls() ? "Yes" : "No"));
		configContent.add(createConfigLine("From Email:", settings.getEmailFrom()));
		configContent.add(createConfigLine("Mailer Type:", settings.getMailerType()));
		configDiv.add(configTitle, configContent);
		return configDiv;
	}

	/** Create SMTP connection test tab. Verifies SMTP server connectivity and authentication. */
	private VerticalLayout createConnectionTestTab() {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(true);
		layout.setSpacing(true);
		layout.getStyle().set("overflow", "hidden");  // No scrollbar on tab itself
		
		// Configuration display - fixed height, no scrolling
		final Div configDiv = createConfigurationDisplay();
		configDiv.getStyle().set("flex-shrink", "0");  // Don't shrink
		layout.add(configDiv);
		
		// Test button
		buttonTestConnection = new CButton("Test Connection", VaadinIcon.REFRESH.create());
		buttonTestConnection.setId("email-test-connection-button");
		buttonTestConnection.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonTestConnection.addClickListener(e -> on_buttonTestConnection_clicked());
		layout.add(buttonTestConnection);
		
		// Results area - scrollable internally
		final Div resultsDiv = new Div();
		resultsDiv.setId("email-test-results");
		resultsDiv.setWidthFull();
		resultsDiv.getStyle()
			.set("border", "1px solid var(--lumo-contrast-20pct)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("padding", "var(--lumo-space-m)")
			.set("background-color", "var(--lumo-contrast-5pct)")
			.set("overflow-y", "auto")  // Results scroll internally
			.set("max-height", CUIConstants.TEXTAREA_HEIGHT_TALL)  // Fixed max height for results
			.set("flex-grow", "1");
		
		final Span waitingMessage = new Span("Click 'Test Connection' to verify SMTP configuration...");
		waitingMessage.getStyle().set("color", "var(--lumo-secondary-text-color)");
		resultsDiv.add(waitingMessage);
		
		layout.add(resultsDiv);
		layout.setFlexGrow(1, resultsDiv);  // Results take remaining space
		
		return layout;
	}

	/** Create send test email tab. Allows sending a test email to verify end-to-end functionality. */
	private VerticalLayout createSendTestEmailTab() {
		final VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(true);
		layout.setSpacing(true);
		layout.getStyle().set("overflow", "hidden");  // No scrollbar on tab itself
		
		// Info message
		final Span infoSpan = new Span("Send a test email to verify your configuration is working correctly.");
		infoSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
		layout.add(infoSpan);
		
		// Recipient email field
		final CTextField textFieldRecipient = new CTextField("Recipient Email");
		textFieldRecipient.setWidthFull();
		textFieldRecipient.setPlaceholder("Enter test email address");
		textFieldRecipient.setValue(settings.getEmailAdministrator() != null ? settings.getEmailAdministrator() : "");
		layout.add(textFieldRecipient);
		
		// Send button
		final CButton buttonSendTest = new CButton("Send Test Email", VaadinIcon.ENVELOPE_OPEN.create());
		buttonSendTest.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonSendTest.addClickListener(e -> on_buttonSendTestEmail_clicked(textFieldRecipient.getValue()));
		layout.add(buttonSendTest);
		
		// Results area - scrollable internally
		final Div resultsDiv = new Div();
		resultsDiv.setId("send-test-results");
		resultsDiv.setWidthFull();
		resultsDiv.getStyle()
			.set("border", "1px solid var(--lumo-contrast-20pct)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("padding", "var(--lumo-space-m)")
			.set("background-color", "var(--lumo-contrast-5pct)")
			.set("overflow-y", "auto")  // Results scroll internally
			.set("max-height", CUIConstants.TEXTAREA_HEIGHT_TALL)  // Fixed max height for results
			.set("flex-grow", "1");
		
		final Span waitingMessage = new Span("Enter recipient and click 'Send Test Email'...");
		waitingMessage.getStyle().set("color", "var(--lumo-secondary-text-color)");
		resultsDiv.add(waitingMessage);
		
		layout.add(resultsDiv);
		layout.setFlexGrow(1, resultsDiv);  // Results take remaining space
		
		return layout;
	}

	@Override
	public String getDialogTitleString() { return "Email Configuration Test"; }

	@Override
	protected Icon getFormIcon() throws Exception { return VaadinIcon.ENVELOPE.create(); }

	@Override
	protected String getFormTitleString() { return "Test Email Configuration"; }

	/** Handle send test email button click. Sends email instantly via SMTP. */
	private void on_buttonSendTestEmail_clicked(final String recipient) {
		if (recipient == null || recipient.trim().isEmpty()) {
			CNotificationService.showError("Please enter a recipient email address");
			return;
		}
		LOGGER.info("Sending test email to: {}", recipient);
		final Div resultsDiv = (Div) sendTestEmailTab.getComponentAt(3);
		resultsDiv.removeAll();
		try {
			// Add loading indicator
			final Span loadingSpan = new Span("Sending test email...");
			loadingSpan.getStyle().set("color", "var(--lumo-primary-color)");
			resultsDiv.add(loadingSpan);
			// Send email instantly using JavaMail
			sendTestEmailInstantly(recipient);
			// Show success
			resultsDiv.removeAll();
			final Icon successIcon = VaadinIcon.CHECK_CIRCLE.create();
			successIcon.setColor("var(--lumo-success-color)");
			final Span successMessage = new Span("Test email sent successfully!");
			successMessage.getStyle().set("color", "var(--lumo-success-text-color)").set("font-weight", CUIConstants.FONT_WEIGHT_MEDIUM);
			final HorizontalLayout successLayout = new HorizontalLayout(successIcon, successMessage);
			successLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
			successLayout.setSpacing(true);
			final Div detailsDiv = new Div();
			detailsDiv.getStyle().set("margin-top", "var(--lumo-space-m)").set("padding", "var(--lumo-space-s)")
					.set("background-color", "var(--lumo-success-color-10pct)").set("border-radius", "var(--lumo-border-radius-m)");
			detailsDiv.add(new Span("✅ Email sent to: " + recipient));
			detailsDiv.add(new Div(new Span("From: " + settings.getEmailFrom())));
			detailsDiv.add(new Div(new Span("Subject: Derbent PLM - Test Email")));
			detailsDiv.add(new Div(new Span("Server: " + settings.getSmtpServer() + ":" + settings.getSmtpPort())));
			resultsDiv.add(successLayout, detailsDiv);
			CNotificationService.showSuccess("Test email sent successfully!");
			LOGGER.info("Test email sent successfully to: {}", recipient);
		} catch (final Exception e) {
			LOGGER.error("Failed to send test email", e);
			resultsDiv.removeAll();
			final Icon errorIcon = VaadinIcon.CLOSE_CIRCLE.create();
			errorIcon.setColor("var(--lumo-error-color)");
			final Span errorMessage = new Span("Failed to send test email: " + e.getMessage());
			errorMessage.getStyle().set("color", "var(--lumo-error-text-color)").set("font-weight", CUIConstants.FONT_WEIGHT_MEDIUM);
			final HorizontalLayout errorLayout = new HorizontalLayout(errorIcon, errorMessage);
			errorLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
			errorLayout.setSpacing(true);
			resultsDiv.add(errorLayout);
			CNotificationService.showError("Failed to send test email: " + e.getMessage());
		}
	}

	/** Handle SMTP connection test button click. */
	private void on_buttonTestConnection_clicked() {
		LOGGER.info("Testing SMTP connection to: {}:{}", settings.getSmtpServer(), settings.getSmtpPort());
		final Div resultsDiv = (Div) connectionTab.getComponentAt(2);
		resultsDiv.removeAll();
		try {
			// Add loading indicator
			final Span loadingSpan = new Span("Testing SMTP connection...");
			loadingSpan.getStyle().set("color", "var(--lumo-primary-color)");
			resultsDiv.add(loadingSpan);
			// Test SMTP connection
			testSmtpConnection();
			// Show success
			resultsDiv.removeAll();
			final Icon successIcon = VaadinIcon.CHECK_CIRCLE.create();
			successIcon.setColor("var(--lumo-success-color)");
			final Span successMessage = new Span("SMTP connection successful!");
			successMessage.getStyle().set("color", "var(--lumo-success-text-color)").set("font-weight", CUIConstants.FONT_WEIGHT_MEDIUM);
			final HorizontalLayout successLayout = new HorizontalLayout(successIcon, successMessage);
			successLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
			successLayout.setSpacing(true);
			final Div detailsDiv = new Div();
			detailsDiv.getStyle().set("margin-top", "var(--lumo-space-m)").set("padding", "var(--lumo-space-s)")
					.set("background-color", "var(--lumo-success-color-10pct)").set("border-radius", "var(--lumo-border-radius-m)");
			detailsDiv.add(new Span("✅ Connected to: " + settings.getSmtpServer() + ":" + settings.getSmtpPort()));
			detailsDiv.add(new Div(new Span("✅ Authentication successful: " + settings.getSmtpLoginName())));
			detailsDiv.add(new Div(new Span("✅ TLS: " + (settings.getSmtpUseTls() != null && settings.getSmtpUseTls() ? "Enabled" : "Disabled"))));
			detailsDiv.add(new Div(new Span("✅ Configuration verified and ready to send emails")));
			resultsDiv.add(successLayout, detailsDiv);
			LOGGER.info("Email connection test completed successfully");
		} catch (final Exception e) {
			LOGGER.error("Email connection test failed", e);
			resultsDiv.removeAll();
			final Icon errorIcon = VaadinIcon.CLOSE_CIRCLE.create();
			errorIcon.setColor("var(--lumo-error-color)");
			final Span errorMessage = new Span("Connection test failed: " + e.getMessage());
			errorMessage.getStyle().set("color", "var(--lumo-error-text-color)").set("font-weight", CUIConstants.FONT_WEIGHT_MEDIUM);
			final HorizontalLayout errorLayout = new HorizontalLayout(errorIcon, errorMessage);
			errorLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
			errorLayout.setSpacing(true);
			resultsDiv.add(errorLayout);
			CNotificationService.showError("Email connection test failed: " + e.getMessage());
		}
	}

	/** Send test email instantly via SMTP. */
	private void sendTestEmailInstantly(final String recipient) throws Exception {
		// Validate configuration
		if (settings.getSmtpServer() == null || settings.getSmtpServer().isBlank()) {
			throw new IllegalArgumentException("SMTP server not configured");
		}
		if (settings.getEmailFrom() == null || settings.getEmailFrom().isBlank()) {
			throw new IllegalArgumentException("From email not configured");
		}
		// Create SMTP session
		final Properties props = new Properties();
		props.put("mail.smtp.host", settings.getSmtpServer());
		props.put("mail.smtp.port", settings.getSmtpPort().toString());
		props.put("mail.smtp.auth", "true");
		if (settings.getSmtpUseTls() != null && settings.getSmtpUseTls()) {
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		}
		final Session session = Session.getInstance(props, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(settings.getSmtpLoginName(), settings.getSmtpLoginPassword());
			}
		});
		// Create message
		final Message message = new MimeMessage(session);
		message.setFrom(
				new InternetAddress(settings.getEmailFrom(), settings.getEmailSenderName() != null ? settings.getEmailSenderName() : "Derbent PLM"));
		if (settings.getEmailReplyTo() != null && !settings.getEmailReplyTo().isBlank()) {
			message.setReplyTo(InternetAddress.parse(settings.getEmailReplyTo()));
		}
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
		message.setSubject("Derbent PLM - Test Email");
		// Create HTML body
		final String htmlBody = """
				<html>
				<body style="font-family: Arial, sans-serif; padding: 20px;">
					<h2 style="color: #2196F3;">✅ Derbent PLM Email Test</h2>
					<p>This is a test email from Derbent PLM to verify your email configuration is working correctly.</p>
					<hr style="border: 1px solid #e0e0e0; margin: 20px 0;">
					<h3>Configuration Details:</h3>
					<ul>
						<li><strong>SMTP Server:</strong> %s</li>
						<li><strong>SMTP Port:</strong> %d</li>
						<li><strong>TLS Enabled:</strong> %s</li>
						<li><strong>From Address:</strong> %s</li>
						<li><strong>Sender Name:</strong> %s</li>
					</ul>
					<hr style="border: 1px solid #e0e0e0; margin: 20px 0;">
					<p style="color: #666; font-size: 12px;">
						This email was sent from Derbent PLM System Settings - Email Configuration Test.
					</p>
				</body>
				</html>
				""".formatted(settings.getSmtpServer(), settings.getSmtpPort(),
				settings.getSmtpUseTls() != null && settings.getSmtpUseTls() ? "Yes" : "No", settings.getEmailFrom(),
				settings.getEmailSenderName() != null ? settings.getEmailSenderName() : "Derbent PLM");
		message.setContent(htmlBody, "text/html; charset=utf-8");
		message.setSentDate(new java.util.Date());
		// Send email
		Transport.send(message);
		LOGGER.info("Test email sent successfully to: {}", recipient);
	}

	@Override
	protected void setupButtons() {
		buttonClose = new CButton("Close", VaadinIcon.CLOSE.create());
		buttonClose.addClickListener(e -> close());
		buttonLayout.add(buttonClose);
	}

	@Override
	protected void setupContent() throws Exception {
		// Set dialog dimensions - same as LDAP dialog for consistency
		setWidth(CUIConstants.DIALOG_WIDTH_WIDE);
		setHeight(CUIConstants.DIALOG_HEIGHT_MEDIUM);  // Fixed height, no scrollbar on dialog
		setResizable(true);
		setDraggable(true);
		
		// Create tab sheet with enhanced styling - matching LDAP dialog
		tabSheet = new CTabSheet();
		tabSheet.setSizeFull();
		tabSheet.getElement().getStyle()
			.set("--lumo-primary-color", "#1976D2")
			.set("--lumo-primary-text-color", "#1976D2");
		
		// Tab 1: SMTP Connection Test
		connectionTab = createConnectionTestTab();
		tabSheet.add("Connection Test", connectionTab);
		
		// Tab 2: Send Test Email
		sendTestEmailTab = createSendTestEmailTab();
		tabSheet.add("Send Test Email", sendTestEmailTab);
		
		// Add tab sheet to main layout (NOT to a wrapper)
		mainLayout.setSizeFull();
		mainLayout.setPadding(false);
		mainLayout.setSpacing(false);
		mainLayout.add(tabSheet);
	}

	/** Test SMTP connection by connecting to server and authenticating. */
	private void testSmtpConnection() throws Exception {
		// Validate configuration
		if (settings.getSmtpServer() == null || settings.getSmtpServer().isBlank()) {
			throw new IllegalArgumentException("SMTP server not configured");
		}
		if (settings.getSmtpPort() == null) {
			throw new IllegalArgumentException("SMTP port not configured");
		}
		if (settings.getSmtpLoginName() == null || settings.getSmtpLoginName().isBlank()) {
			throw new IllegalArgumentException("SMTP username not configured");
		}
		if (settings.getSmtpLoginPassword() == null || settings.getSmtpLoginPassword().isBlank()) {
			throw new IllegalArgumentException("SMTP password not configured");
		}
		// Create SMTP session
		final Properties props = new Properties();
		props.put("mail.smtp.host", settings.getSmtpServer());
		props.put("mail.smtp.port", settings.getSmtpPort().toString());
		props.put("mail.smtp.auth", "true");
		if (settings.getSmtpUseTls() != null && settings.getSmtpUseTls()) {
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		}
		final Session session = Session.getInstance(props, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(settings.getSmtpLoginName(), settings.getSmtpLoginPassword());
			}
		});
		// Test connection
		final Transport transport = session.getTransport("smtp");
		try {
			transport.connect(settings.getSmtpServer(), settings.getSmtpPort(), settings.getSmtpLoginName(), settings.getSmtpLoginPassword());
			LOGGER.info("SMTP connection successful: {}:{}", settings.getSmtpServer(), settings.getSmtpPort());
		} finally {
			transport.close();
		}
	}
}
