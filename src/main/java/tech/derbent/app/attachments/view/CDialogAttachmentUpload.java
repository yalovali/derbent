package tech.derbent.app.attachments.view;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.dialogs.CDialog;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.service.CAttachmentService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** CDialogAttachmentUpload - Dialog for uploading attachments. Provides a user-friendly interface for uploading files with: - Drag and drop support -
 * Auto-detected document type - Optional description - Optional version number (manual entry) - File size and type validation - Progress indication */
public class CDialogAttachmentUpload extends CDialog {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogAttachmentUpload.class);
	private static final long serialVersionUID = 1L;
	private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB
	private final CAttachmentService attachmentService;
	private final ISessionService sessionService;
	private final CEntityDB<?> parentEntity;
	private final Consumer<CAttachment> onUploadSuccess;
	private Upload upload;
	private MemoryBuffer buffer;
	private TextArea textAreaDescription;
	private IntegerField fieldVersionNumber;
	private CButton buttonUpload;
	private CButton buttonCancel;
	private Span statusLabel;
	private String uploadedFileName;
	private Long uploadedFileSize;
	private String uploadedMimeType;
	private boolean uploadInProgress = false;

	/** Constructor for attachment upload dialog.
	 * @param attachmentService   the attachment service
	 * @param sessionService      the session service
	 * @param parentEntity        the parent entity (Activity, Risk, Meeting, Sprint)
	 * @param onUploadSuccess     callback when upload succeeds */
	public CDialogAttachmentUpload(final CAttachmentService attachmentService,
			final ISessionService sessionService, final CEntityDB<?> parentEntity, final Consumer<CAttachment> onUploadSuccess) {
		super();
		Check.notNull(attachmentService, "AttachmentService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		Check.notNull(parentEntity, "Parent entity cannot be null");
		this.attachmentService = attachmentService;
		this.sessionService = sessionService;
		this.parentEntity = parentEntity;
		this.onUploadSuccess = onUploadSuccess;
		try {
			setupDialog();
		} catch (final Exception e) {
			LOGGER.error("Error setting up dialog", e);
			CNotificationService.showException("Error initializing upload dialog", e);
		}
	}

	/** Format file size for display. */
	private String formatFileSize(final long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		}
		final int exp = (int) (Math.log(bytes) / Math.log(1024));
		final String pre = "KMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
	}

	@Override
	public String getDialogTitleString() { return "Upload Attachment"; }

	@Override
	protected Icon getFormIcon() throws Exception { return VaadinIcon.UPLOAD.create(); }

	@Override
	protected String getFormTitleString() { return "Upload File"; }

	/** Link attachment to the parent entity - DEPRECATED, not used with unidirectional pattern. */
	@Deprecated
	private void linkAttachmentToParent(final CAttachment attachment) {
		// NOTE: With unidirectional @OneToMany pattern, parent entity manages the collection
		// Parent will call: parentEntity.getAttachments().add(attachment)
		// No service linkTo* methods needed
	}

	/** Handle upload button click. */
	private void on_buttonUpload_clicked() {
		if (uploadInProgress) {
			LOGGER.warn("Upload already in progress");
			return;
		}
		try {
			uploadInProgress = true;
			buttonUpload.setEnabled(false);
			statusLabel.setText("Uploading...");
			// Get file data from buffer
			Check.notBlank(uploadedFileName, "Upload file name is missing");
			Check.notNull(uploadedFileSize, "Upload file size is missing");
			final String fileName = uploadedFileName;
			final long fileSize = uploadedFileSize;
			final String mimeType = uploadedMimeType;
			// Get current user
			final CUser currentUser = sessionService.getActiveUser().orElseThrow(() -> new IllegalStateException("No active user"));
			// Upload file via service (document type will be auto-detected)
			final CAttachment attachment = attachmentService.uploadFile(fileName, buffer.getInputStream(), fileSize, mimeType,
					textAreaDescription.getValue());
			// Set version number if specified
			if (fieldVersionNumber.getValue() != null) {
				attachment.setVersionNumber(fieldVersionNumber.getValue());
				attachmentService.save(attachment);
			}
			// Important: Parent entity will add attachment to its collection
			// No need to call link methods - unidirectional @OneToMany pattern
			// Save again to persist optional fields
			attachmentService.save(attachment);
			// Notify success
			CNotificationService.showSaveSuccess();
			// Callback (parent will add attachment to its collection)
			if (onUploadSuccess != null) {
				onUploadSuccess.accept(attachment);
			}
			// Close dialog
			close();
		} catch (final Exception e) {
			LOGGER.error("Error uploading attachment", e);
			CNotificationService.showException("Failed to upload attachment", e);
			statusLabel.setText("Upload failed");
		} finally {
			uploadInProgress = false;
			buttonUpload.setEnabled(true);
		}
	}

	@Override
	protected void setupButtons() {
		// Upload button
		buttonUpload = new CButton("Upload", VaadinIcon.UPLOAD.create(), e -> on_buttonUpload_clicked());
		buttonUpload.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonUpload.setEnabled(false); // Disabled until file is selected
		// Cancel button
		buttonCancel = new CButton("Cancel", null, e -> close());
		buttonLayout.add(buttonUpload, buttonCancel);
	}

	@Override
	protected void setupContent() throws Exception {
		final VerticalLayout content = new VerticalLayout();
		content.setPadding(false);
		content.setSpacing(true);
		// Upload component
		buffer = new MemoryBuffer();
		upload = new Upload(buffer);
		upload.setMaxFileSize((int) MAX_FILE_SIZE);
		upload.setDropLabel(new Span("Drop file here or click to browse"));
		upload.setUploadButton(new com.vaadin.flow.component.button.Button("Choose File"));
		// Configure upload listeners
		upload.addSucceededListener(event -> {
			LOGGER.info("File upload succeeded: {}", event.getFileName());
			uploadedFileName = event.getFileName();
			uploadedFileSize = event.getContentLength();
			uploadedMimeType = event.getMIMEType();
			statusLabel.setText("File ready: " + event.getFileName() + " (" + formatFileSize(event.getContentLength()) + ")");
			buttonUpload.setEnabled(true);
		});
		upload.addFailedListener(event -> {
			LOGGER.error("File upload failed: {}", event.getReason().getMessage());
			CNotificationService.showError("Upload failed: " + event.getReason().getMessage());
			statusLabel.setText("Upload failed");
			uploadedFileName = null;
			uploadedFileSize = null;
			uploadedMimeType = null;
			buttonUpload.setEnabled(false);
		});
		upload.addFileRejectedListener(event -> {
			LOGGER.warn("File rejected: {}", event.getErrorMessage());
			CNotificationService.showWarning("File rejected: " + event.getErrorMessage());
			statusLabel.setText("File rejected");
			uploadedFileName = null;
			uploadedFileSize = null;
			uploadedMimeType = null;
			buttonUpload.setEnabled(false);
		});
		content.add(upload);
		// Status label
		statusLabel = new Span("No file selected");
		statusLabel.getStyle().set("font-size", "0.875rem");
		statusLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
		content.add(statusLabel);
		// Version number field
		fieldVersionNumber = new IntegerField("Version Number");
		fieldVersionNumber.setWidthFull();
		fieldVersionNumber.setValue(1);
		fieldVersionNumber.setMin(1);
		fieldVersionNumber.setMax(999);
		fieldVersionNumber.setHelperText("Auto-incremented if uploading new version");
		content.add(fieldVersionNumber);
		// Description field
		textAreaDescription = new TextArea("Description");
		textAreaDescription.setWidthFull();
		textAreaDescription.setMaxLength(2000);
		textAreaDescription.setPlaceholder("Optional notes about this file");
		content.add(textAreaDescription);
		mainLayout.add(content);
	}
}
