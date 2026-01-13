package tech.derbent.app.attachments.view;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.documenttypes.service.CDocumentTypeService;

/**
 * CDialogAttachmentEdit - Dialog for editing attachment metadata using form builder pattern.
 * 
 * Editable fields:
 * - Document type (displayed, user can manually correct if auto-detection was wrong)
 * - Version number (with validation)
 * - Description
 * 
 * Read-only fields (safety):
 * - File name (cannot be changed - upload new version instead)
 * - File size
 * - Uploaded by/date
 */
public class CDialogAttachmentEdit extends CDialogDBEdit<CAttachment> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogAttachmentEdit.class);
	private static final long serialVersionUID = 1L;
	private final CEnhancedBinder<CAttachment> binder;
	private final CDocumentTypeService documentTypeService;

	public CDialogAttachmentEdit(final CAttachment attachment, 
			final CDocumentTypeService documentTypeService,
			final Consumer<CAttachment> onSave) {
		super(attachment, onSave, false);
		Check.notNull(attachment, "Attachment cannot be null");
		Check.notNull(documentTypeService, "DocumentTypeService cannot be null");
		this.documentTypeService = documentTypeService;
		this.binder = CBinderFactory.createEnhancedBinder(CAttachment.class);
		
		try {
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error populating form", e);
			CNotificationService.showException("Error loading attachment data", e);
		}
	}

	@Override
	public String getDialogTitleString() {
		return "Edit Attachment";
	}

	@Override
	protected Icon getFormIcon() throws Exception {
		return VaadinIcon.EDIT.create();
	}

	@Override
	protected String getFormTitleString() {
		return "Edit Attachment Metadata";
	}

	@Override
	protected void populateForm() {
		try {
			// Build form using CFormBuilder with specific editable fields
			final java.util.List<String> editableFields = java.util.List.of(
				"fileName",       // Read-only display
				"fileSize",       // Read-only display
				"documentType",   // Editable - user can correct auto-detection
				"versionNumber",  // Editable
				"description"     // Editable
			);
			
			final java.util.Map<String, com.vaadin.flow.component.Component> componentMap = new java.util.HashMap<>();
			final tech.derbent.api.ui.component.basic.CVerticalLayoutTop formLayout = 
				CFormBuilder.buildForm(CAttachment.class, binder, editableFields, componentMap, null, 
					new tech.derbent.api.ui.component.basic.CVerticalLayoutTop(false, false, false));
			
			// Load entity data into form first
			binder.setBean(getEntity());
			
			// Mark file metadata as read-only (safety - cannot replace file)
			if (componentMap.containsKey("fileName")) {
				((com.vaadin.flow.component.HasValue<?, ?>) componentMap.get("fileName")).setReadOnly(true);
			}
			if (componentMap.containsKey("fileSize")) {
				((com.vaadin.flow.component.HasValue<?, ?>) componentMap.get("fileSize")).setReadOnly(true);
			}
			
			// Add form to dialog layout
			getDialogLayout().add(formLayout);
			
			// Add safety notice
			final com.vaadin.flow.component.html.Span safetyNote = new com.vaadin.flow.component.html.Span(
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
			
		} catch (final Exception e) {
			LOGGER.error("Error building attachment edit form", e);
			throw new RuntimeException("Failed to build edit form", e);
		}
	}

	@Override
	protected void validateForm() {
		// Validation is handled by binder and entity annotations
		Check.notNull(getEntity().getFileName(), "File name cannot be null");
		Check.isTrue(getEntity().getVersionNumber() != null && getEntity().getVersionNumber() >= 1, 
			"Version number must be at least 1");
	}
}
