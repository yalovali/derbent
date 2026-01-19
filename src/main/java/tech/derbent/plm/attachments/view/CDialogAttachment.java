package tech.derbent.plm.attachments.view;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.service.CAttachmentService;
import tech.derbent.base.session.service.ISessionService;

/** CDialogAttachment - Unified dialog for uploading new attachments or editing existing ones. Upload mode (isNew = true): - Shows file upload
 * component with drag-and-drop - Auto-detects document type from file extension - Creates new attachment entity Edit mode (isNew = false): - Uses
 * CFormBuilder to display editable fields - Document type editable (user can correct auto-detection) - File metadata read-only (safety - no file
 * replacement) */
public class CDialogAttachment extends CDialogDBEdit<CAttachment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogAttachment.class);
	private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB
	private static final long serialVersionUID = 1L;

	private static String formatFileSize(final long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		}
		final int exp = (int) (Math.log(bytes) / Math.log(1024));
		final String pre = "KMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
	}

	private final CAttachmentService attachmentService;
	private final CEnhancedBinder<CAttachment> binder;
	private MemoryBuffer buffer;
	private final CFormBuilder<CAttachment> formBuilder;
	private final CEntityDB<?> parentEntity;
	private final ISessionService sessionService;
	private Span statusLabel;
	// Upload mode fields (custom UI for file selection)
	private Upload upload;
	private String uploadedFileName;
	private Long uploadedFileSize;
	private String uploadedMimeType;
	private boolean uploadInProgress = false;
	// Note: Both upload and edit modes use FormBuilder for form fields

	/** Constructor for edit mode (existing attachment). */
	public CDialogAttachment(final CAttachment attachment, final Consumer<CAttachment> onSave) throws Exception {
		super(attachment, onSave, false);
		Check.notNull(attachment, "Attachment cannot be null");
		attachmentService = null; // Not needed in edit mode
		sessionService = null; // Not needed in edit mode
		parentEntity = null; // Not needed in edit mode
		binder = CBinderFactory.createEnhancedBinder(CAttachment.class);
		formBuilder = new CFormBuilder<>();
		setupDialog();
		populateForm();
	}

	/** Constructor for upload mode (new attachment). */
	public CDialogAttachment(final CAttachmentService attachmentService, final ISessionService sessionService, final CEntityDB<?> parentEntity,
			final Consumer<CAttachment> onSave) throws Exception {
		super(new CAttachment(), onSave, true);
		Check.notNull(attachmentService, "AttachmentService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		Check.notNull(parentEntity, "Parent entity cannot be null");
		this.attachmentService = attachmentService;
		this.sessionService = sessionService;
		this.parentEntity = parentEntity;
		binder = CBinderFactory.createEnhancedBinder(CAttachment.class);
		formBuilder = new CFormBuilder<>();
		setupDialog();
		populateForm();
	}

	private void createFormFields() throws Exception {
		Check.notNull(getDialogLayout(), "Dialog layout must be initialized");
		// Upload mode: add file upload component first
		if (isNew) {
			final CVerticalLayout uploadSection = new CVerticalLayout();
			uploadSection.setPadding(false);
			uploadSection.setSpacing(true);
			// Upload component (for file selection)
			buffer = new MemoryBuffer();
			upload = new Upload(buffer);
			upload.setMaxFileSize((int) MAX_FILE_SIZE);
			upload.setDropLabel(new Span("Drop file here or click to browse"));
			upload.setUploadButton(new Button("Choose File"));
			// Upload listeners - populate entity fields when file is selected
			upload.addSucceededListener(event -> {
				uploadedFileName = event.getFileName();
				uploadedFileSize = event.getContentLength();
				uploadedMimeType = event.getMIMEType();
				// Populate entity fields
				getEntity().setFileName(event.getFileName());
				getEntity().setFileSize(event.getContentLength());
				getEntity().setFileType(event.getMIMEType());
				// Refresh form to show populated values
				binder.readBean(getEntity());
				statusLabel.setText("File ready: " + event.getFileName() + " (" + formatFileSize(event.getContentLength()) + ")");
				LOGGER.debug("File selected: {}, size: {}", event.getFileName(), event.getContentLength());
			});
			upload.addFailedListener(event -> {
				LOGGER.error("File upload failed: {}", event.getReason().getMessage());
				CNotificationService.showError("Upload failed: " + event.getReason().getMessage());
				statusLabel.setText("Upload failed");
				uploadedFileName = null;
				uploadedFileSize = null;
				uploadedMimeType = null;
			});
			upload.addFileRejectedListener(event -> {
				CNotificationService.showWarning("File rejected: " + event.getErrorMessage());
				statusLabel.setText("File rejected");
				uploadedFileName = null;
				uploadedFileSize = null;
				uploadedMimeType = null;
			});
			uploadSection.add(upload);
			// Status label
			statusLabel = new Span("No file selected");
			statusLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)");
			uploadSection.add(statusLabel);
			getDialogLayout().add(uploadSection);
		}
		// Both modes: Use FormBuilder to create form fields from entity annotations
		final List<String> fields = List.of("fileName", "fileSize", "versionNumber", "description");
		getDialogLayout().add(formBuilder.build(CAttachment.class, binder, fields));
		// Mark file metadata as read-only (safety - cannot change file content)
		if (formBuilder.getComponentMap().containsKey("fileName")) {
			((HasValue<?, ?>) formBuilder.getComponentMap().get("fileName")).setReadOnly(true);
		}
		if (formBuilder.getComponentMap().containsKey("fileSize")) {
			((HasValue<?, ?>) formBuilder.getComponentMap().get("fileSize")).setReadOnly(true);
		}
		// Edit mode: add safety notice
		if (!isNew) {
			final Span safetyNote = new Span("⚠ File content cannot be changed for safety. To replace a file, upload a new version instead.");
			safetyNote.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic")
					.set("padding", "var(--lumo-space-s)").set("background-color", "var(--lumo-contrast-5pct)")
					.set("border-radius", "var(--lumo-border-radius-m)").set("margin-top", "var(--lumo-space-m)");
			getDialogLayout().add(safetyNote);
		}
		// Upload mode: set entity defaults
		if (isNew) {
			getEntity().setVersionNumber(1);
			binder.readBean(getEntity());
		}
	}

	@Override
	public String getDialogTitleString() { return isNew ? "Upload Attachment" : "Edit Attachment"; }

	@Override
	protected Icon getFormIcon() throws Exception { return isNew ? VaadinIcon.UPLOAD.create() : VaadinIcon.EDIT.create(); }

	@Override
	protected String getFormTitleString() { return isNew ? "Upload File" : "Edit Attachment Metadata"; }

	@Override
	protected String getSuccessCreateMessage() { return "Attachment uploaded successfully"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Attachment updated successfully"; }

	@Override
	protected void populateForm() {
		if (getEntity() != null && !isNew) {
			binder.readBean(getEntity());
		}
	}

	@Override
	protected void save() throws Exception {
		if (isNew) {
			saveUpload();
		} else {
			saveEdit();
		}
	}

	private void saveEdit() throws Exception {
		try {
			validateForm();
			// Write form data back to entity
			binder.writeBean(getEntity());
			// Entity should be saved by callback
			if (onSave != null) {
				onSave.accept(getEntity());
			}
			close();
			CNotificationService.showSaveSuccess();
		} catch (final Exception e) {
			LOGGER.error("Error saving attachment", e);
			CNotificationService.showException("Failed to save attachment", e);
			throw e;
		}
	}

	private void saveUpload() throws Exception {
		if (uploadInProgress) {
			LOGGER.warn("Upload already in progress");
			return;
		}
		uploadInProgress = true;
		statusLabel.setText("Uploading...");
		try {
			validateForm();
			// Write form data to entity
			binder.writeBean(getEntity());
			// Upload file and create attachment (auto-detects document type if not set)
			final CAttachment attachment = attachmentService.uploadFile(uploadedFileName, buffer.getInputStream(), uploadedFileSize, uploadedMimeType,
					getEntity().getDescription());
			// Copy user-entered fields from form to saved attachment
			attachment.setVersionNumber(getEntity().getVersionNumber());
			attachment.setDocumentType(getEntity().getDocumentType());
			attachment.setDescription(getEntity().getDescription());
			attachmentService.save(attachment);
			// Callback
			if (onSave != null) {
				onSave.accept(attachment);
			}
			close();
		} finally {
			uploadInProgress = false;
		}
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		setWidth("600px");
		setResizable(true);
		setHeight(isNew ? "600px" : "550px");
		createFormFields();
	}

	@Override
	protected void validateForm() {
		if (isNew) {
			Check.notBlank(uploadedFileName, "No file selected - please choose a file first");
			Check.notNull(uploadedFileSize, "File size is missing");
			try {
				binder.validate();
			} catch (final Exception e) {
				throw new IllegalStateException("Failed to validate attachment form", e);
			}
		} else {
			Check.notNull(getEntity(), "Attachment entity cannot be null");
			try {
				binder.validate();
			} catch (final Exception e) {
				throw new IllegalStateException("Failed to validate attachment", e);
			}
		}
	}
}
