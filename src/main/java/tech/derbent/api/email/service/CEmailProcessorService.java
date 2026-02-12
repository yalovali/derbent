package tech.derbent.api.email.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import tech.derbent.api.email.domain.CEmail;
import tech.derbent.api.email.domain.CEmailQueued;
import tech.derbent.api.email.domain.CEmailSent;
import tech.derbent.api.setup.domain.CSystemSettings;
import tech.derbent.api.setup.service.CSystemSettingsService;
import tech.derbent.api.utils.Check;

/** CEmailProcessorService - SMTP email sending service.
 * <p>
 * Processes queued emails and sends via SMTP using JavaMail API. Integrates with CSystemSettings for SMTP configuration. */
@Service
public class CEmailProcessorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEmailProcessorService.class);
	private static final int MAX_RETRIES = 3;
	private final CEmailQueuedService queuedService;
	private final CEmailSentService sentService;
	private final CSystemSettingsService<?> settingsService;

	public CEmailProcessorService(final CEmailQueuedService queuedService, final CEmailSentService sentService,
			final CSystemSettingsService<?> settingsService) {
		this.queuedService = queuedService;
		this.sentService = sentService;
		this.settingsService = settingsService;
	}

	private void copyEmailFields(final CEmailQueued source, final CEmailSent target) {
		target.setToEmail(source.getToEmail());
		target.setCcEmail(source.getCcEmail());
		target.setBccEmail(source.getBccEmail());
		target.setBodyText(source.getBodyText());
		target.setBodyHtml(source.getBodyHtml());
		target.setEmailType(source.getEmailType());
		target.setPriority(source.getPriority());
		target.setReferenceEntityType(source.getReferenceEntityType());
		target.setReferenceEntityId(source.getReferenceEntityId());
	}

	private void handleFailure(final CEmailQueued queued, final Exception e) {
		LOGGER.error("Failed to send email: {}", queued.getSubject(), e);
		queued.setRetryCount(queued.getRetryCount() + 1);
		queued.setLastError(e.getMessage());
		if (queued.getRetryCount() >= MAX_RETRIES) {
			LOGGER.warn("Email exceeded max retries: {}", queued.getSubject());
			queued.setStatus(CEmail.STATUS_FAILED);
		} else {
			queued.setScheduledFor(LocalDateTime.now().plusMinutes(5 * queued.getRetryCount()));
		}
		queuedService.save(queued);
	}

	private void moveToSent(final CEmailQueued queued) {
		final CEmailSent sent = new CEmailSent(queued.getSubject(), queued.getToEmail(), queued.getCompany());
		copyEmailFields(queued, sent);
		sent.setSentAt(LocalDateTime.now());
		sentService.save(sent);
		queuedService.delete(queued);
	}

	@Transactional
	public void processEmail(final CEmailQueued queued) {
		try {
			LOGGER.debug("Processing email: {}", queued.getSubject());
			final CSystemSettings<?> settings = settingsService.getSystemSettings();
			validateSmtpSettings(settings);
			sendEmail(queued, settings);
			moveToSent(queued);
			LOGGER.info("Email sent successfully: {}", queued.getSubject());
		} catch (final Exception e) {
			handleFailure(queued, e);
		}
	}

	@Transactional
	public void processQueue() {
		final List<CEmailQueued> pendingEmails = queuedService.findPendingEmails();
		if (pendingEmails.isEmpty()) {
			LOGGER.debug("No pending emails to process");
			return;
		}
		LOGGER.info("Processing {} pending emails", pendingEmails.size());
		pendingEmails.forEach(this::processEmail);
	}

	private void sendEmail(final CEmailQueued queued, final CSystemSettings<?> settings) throws Exception {
		final Properties props = new Properties();
		props.put("mail.smtp.host", settings.getSmtpServer());
		props.put("mail.smtp.port", settings.getSmtpPort());
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.protocols", "TLSv1.2");
		final Session session = Session.getInstance(props, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(settings.getSmtpLoginName(), settings.getSmtpLoginPassword());
			}
		});
		final Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(settings.getEmailFrom(), settings.getEmailSenderName()));
		message.setReplyTo(InternetAddress.parse(settings.getEmailReplyTo()));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(queued.getToEmail()));
		if (queued.getCcEmail() != null && !queued.getCcEmail().isBlank()) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(queued.getCcEmail()));
		}
		if (queued.getBccEmail() != null && !queued.getBccEmail().isBlank()) {
			message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(queued.getBccEmail()));
		}
		message.setSubject(queued.getSubject());
		if (queued.getBodyHtml() != null && !queued.getBodyHtml().isBlank()) {
			message.setContent(queued.getBodyHtml(), "text/html; charset=utf-8");
		} else {
			message.setText(queued.getBodyText());
		}
		message.setSentDate(new java.util.Date());
		Transport.send(message);
	}

	private void validateSmtpSettings(final CSystemSettings<?> settings) {
		Check.notBlank(settings.getSmtpServer(), "SMTP server not configured");
		Check.notNull(settings.getSmtpPort(), "SMTP port not configured");
		Check.notBlank(settings.getSmtpLoginName(), "SMTP username not configured");
		Check.notBlank(settings.getSmtpLoginPassword(), "SMTP password not configured");
		Check.notBlank(settings.getEmailFrom(), "From email not configured");
	}
}
