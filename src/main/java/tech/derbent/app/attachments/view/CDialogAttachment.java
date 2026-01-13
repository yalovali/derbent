package tech.derbent.app.attachments.view;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
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
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.service.CAttachmentService;
import tech.derbent.app.documenttypes.service.CDocumentTypeService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/**
 * CDialogAttachment - Unified dialog for uploading new attachments or editing existing ones.
 * 
 * Upload mode (isNew = true):
 * - Shows file upload component with drag-and-drop
 * - Auto-detects document type from file extension
 * - Creates new attachment entity
 * 
 * Edit mode (isNew = false):
 * - Uses CFormBuilder to display editable fields
 * - Document type editable (user can correct auto-detection)
 * - File metadata read-only (safety - no file replacement)
 */
public class CDialogAttachment extends CDialogDBEdit<CAttachment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogAttachment.class);
	private static final long serialVersionUID = 1L;
	private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100 MB
	
	private final CAttachmentService attachmentService;
	private final ISessionService sessionService;
	private final CEntityDB<?> parentEntity;
	private final CEnhancedBinder<CAttachment> binder;
	private final CFormBuilder<CAttachment> formBuilder;
	
	// Upload mode fields
	private Upload upload;
	private MemoryBuffer buffer;
	private Span statusLabel;
	private String uploadedFileName;
	private Long uploadedFileSize;
	private String uploadedMimeType;
	private boolean uploadInProgress = false;
	
	// Edit mode - form fields managed by CFormBuilder
	private IntegerField fieldVersionNumber;
	private TextArea textAreaDescription;

	/**
	 * Constructor for upload mode (new attachment).
	 */
	public CDialogAttachment(final CAttachmentService attachmentService,
			final ISessionService sessionService,
			final CEntityDB<?> parentEntity,
			final Consumer<CAttachment> onSave) throws Exception {
		super(new CAttachment(), onSave, true);
		Check.notNull(attachmentService, "AttachmentService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		Check.notNull(parentEntity, "Parent entity cannot be null");
		
		this.attachmentService = attachmentService;
		this.sessionService = sessionService;
		this.parentEntity = parentEntity;
		this.binder = CBinderFactory.createEnhancedBinder(CAttachment.class);
		this.formBuilder = new CFormBuilder<>();
		
		setupDialog();
		populateForm();
	}

	/**
	 * Constructor for edit mode (existing attachment).
	 */
	public CDialogAttachment(final CAttachment attachment,
			final Consumer<CAttachment> onSave) throws Exception {
		super(attachment, onSave, false);
		Check.notNull(attachment, "Attachment cannot be null");
		
		this.attachmentService = null; // Not needed in edit mode
		this.sessionService = null; // Not needed in edit mode
		this.parentEntity = null; // Not needed in edit mode
		this.binder = CBinderFactory.createEnhancedBinder(CAttachment.class);
		this.formBuilder = new CFormBuilder<>();
		
		setupDialog();
		populateForm();
	}

	private void createEditFormFields() throws Exception {
		Check.notNull(getDialogLayout(), "Dialog layout must be initialized");
		
		// Use FormBuilder to create form from entity annotations
		final List<String> editableFields = List.of(
			"fileName", "fileSize", "documentType", "versionNumber", "description"
		);
		
		getDialogLayout().add(formBuilder.build(CAttachment.class, binder, editableFields));
		
		// Mark file metadata as read-only (safety - no file replacement)
		if (formBuilder.getComponentMap().containsKey("fileName")) {
			((com.vaadin.flow.component.HasValue<?, ?>) formBuilder.getComponentMap().get("fileName")).setReadOnly(true);
		}
		if (formBuilder.getComponentMap().containsKey("fileSize")) {
			((com.vaadin.flow.component.HasValue<?, ?>) formBuilder.getComponentMap().get("fileSize")).setReadOnly(true);
		}
		
		// Safety notice
		final Span safetyNote = new Span(
			"⚠ File content cannot be changed for safety. To replace a file, upload a new version instead."
		);
		safetyNote.getStyle()
			.set("font-size", "0.875rem")
			.set("color", "var(--lumo-secondary-text-color)")
			.set("font-style", "italic")
			.set("padding", "var(--lumo-space-s)")
			.set("background-color", "var(--lumo-contrast-5pct)")
			.set("border-radius", "var(--lumo-border-radius-m)")
			.set("margin-top", "var(--lumo-space-m)");
		getDialogLayout().add(safetyNote);
	}

	private void createUploadFormFields() {
		Check.notNull(getDialogLayout(), "Dialog layout must be initialized");
		
		final CVerticalLayout content = new CVerticalLayout();
		content.setPadding(false);
		content.setSpacing(true);
		
		// Upload component
		buffer = new MemoryBuffer();
		upload = new Upload(buffer);
		upload.setMaxFileSize((int) MAX_FILE_SIZE);
		upload.setDropLabel(new Span("Drop file here or click to browse"));
		upload.setUploadButton(new com.vaadin.flow.component.button.Button("Choose File"));
		
		// Upload listeners
		upload.addSucceededListener(event -> {
			uploadedFileName = event.getFileName();
			uploadedFileSize = event.getContentLength();
			uploadedMimeType = event.getMIMEType();
			statusLabel.setText("File ready: " + event.getFileName() + " (" + formatFileSize(event.getContentLength()) + ")");
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
		
		content.add(upload);
		
		// Status label
		statusLabel = new Span("No file selected");
		statusLabel.getStyle()
			.set("font-size", "0.875rem")
			.set("color", "var(--lumo-secondary-text-color)");
		content.add(statusLabel);
		
		// Version number
		fieldVersionNumber = new IntegerField("Version Number");
		fieldVersionNumber.setWidthFull();
		fieldVersionNumber.setValue(1);
		fieldVersionNumber.setMin(1);
		fieldVersionNumber.setMax(999);
		fieldVersionNumber.setHelperText("Auto-incremented if uploading new version");
		content.add(fieldVersionNumber);
		
		// Description
		textAreaDescription = new TextArea("Description");
		textAreaDescription.setWidthFull();
		textAreaDescription.setMaxLength(2000);
		textAreaDescription.setPlaceholder("Optional notes about this file");
		content.add(textAreaDescription);
		
		getDialogLayout().add(content);
	}

	@Override
	public String getDialogTitleString() {
		return isNew ? "Upload Attachment" : "Edit Attachment";
	}

	@Override
	protected Icon getFormIcon() throws Exception {
		return isNew ? VaadinIcon.UPLOAD.create() : VaadinIcon.EDIT.create();
	}

	@Override
	protected String getFormTitleString() {
		return isNew ? "Upload File" : "Edit Attachment Metadata";
	}

	@Override
	protected String getSuccessCreateMessage() {
		return "Attachment uploaded successfully";
	}

	@Override
	protected String getSuccessUpdateMessage() {
		return "Attachment updated successfully";
	}

	@Override
	protected void populateForm() {
		if (getEntity() != null && !isNew) {
			binder.readBean(getEntity());
		}
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		setWidth("600px");
		setResizable(true);
		
		if (isNew) {
			setHeight("500px");
			createUploadFormFields();
		} else {
			setHeight("550px");
			createEditFormFields();
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
			
			final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new IllegalStateException("No active user"));
			
			// Upload file and create attachment (auto-detects document type)
			final CAttachment attachment = attachmentService.uploadFile(
				uploadedFileName, 
				buffer.getInputStream(), 
				uploadedFileSize, 
				uploadedMimeType,
				textAreaDescription.getValue()
			);
			
			// Set version number if specified
			if (fieldVersionNumber.getValue() != null) {
				attachment.setVersionNumber(fieldVersionNumber.getValue());
				attachmentService.save(attachment);
			}
			
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
	protected void validateForm() {
		if (isNew) {
			Check.notBlank(uploadedFileName, "No file selected");
			Check.notNull(uploadedFileSize, "File size is missing");
		} else {
			Check.notNull(getEntity(), "Attachment entity cannot be null");
			try {
				binder.validate();
			} catch (final Exception e) {
				throw new IllegalStateException("Failed to validate attachment", e);
			}
		}
	}

	private String formatFileSize(final long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		}
		final int exp = (int) (Math.log(bytes) / Math.log(1024));
		final String pre = "KMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
	}
}
