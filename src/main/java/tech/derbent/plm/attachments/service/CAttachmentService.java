package tech.derbent.plm.attachments.service;

import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.storage.IAttachmentStorage;
import tech.derbent.plm.attachments.view.CComponentListAttachments;
import tech.derbent.plm.documenttypes.service.CDocumentTypeService;

/** Service for managing CAttachment entities and file operations. Provides CRUD operations, file upload/download, and version management. */
@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:paperclip", title = "Project.Attachments")
@PermitAll
public class CAttachmentService extends CEntityOfCompanyService<CAttachment> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAttachmentService.class);
	private final IAttachmentRepository attachmentRepository;
	private final IAttachmentStorage attachmentStorage;
	private final CDocumentTypeService typeService;

	public CAttachmentService(final IAttachmentRepository repository, final Clock clock, final ISessionService sessionService,
			final IAttachmentStorage attachmentStorage, final CDocumentTypeService documentTypeService) {
		super(repository, clock, sessionService);
		this.attachmentStorage = attachmentStorage;
		this.typeService = documentTypeService;
		attachmentRepository = repository;
	}

	@Override
	public String checkDeleteAllowed(final CAttachment attachment) {
		// Check if attachment is referenced by newer versions as previousVersion
		final List<CAttachment> newerVersions = attachmentRepository.findByPreviousVersion(attachment);
		if (!newerVersions.isEmpty()) {
			return "Cannot delete attachment - it is referenced by newer version(s). Version numbers: "
					+ newerVersions.stream().map(v -> String.valueOf(v.getVersionNumber())).collect(Collectors.joining(", "));
		}
		return super.checkDeleteAllowed(attachment);
	}

	public Component createComponent() {
		try {
			final CComponentListAttachments component = new CComponentListAttachments(this, sessionService);
			LOGGER.debug("Created attachment component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create attachment component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading attachment component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	/** Delete an attachment and its file.
	 * @param attachment the attachment to delete */
	@Transactional
	@Override
	public void delete(final CAttachment attachment) {
		Objects.requireNonNull(attachment, "Attachment cannot be null");
		// Delete file from storage
		try {
			attachmentStorage.delete(attachment.getContentPath());
		} catch (final Exception e) {
			LOGGER.error("Failed to delete file from storage: {}", attachment.getContentPath(), e);
		}
		// Delete database record
		super.delete(attachment);
	}

	/** Delete the stored file for an attachment without removing the entity.
	 * @param attachment the attachment whose file should be removed */
	public void deleteStoredFile(final CAttachment attachment) {
		Objects.requireNonNull(attachment, "Attachment cannot be null");
		Objects.requireNonNull(attachment.getContentPath(), "Content path cannot be null");
		final boolean deleted = attachmentStorage.delete(attachment.getContentPath());
		Check.isTrue(deleted, "Attachment file could not be deleted");
	}

	/** Download a file.
	 * @param attachment the attachment to download
	 * @return the file content as an input stream
	 * @throws Exception if download fails */
	public InputStream downloadFile(final CAttachment attachment) throws Exception {
		Objects.requireNonNull(attachment, "Attachment cannot be null");
		return attachmentStorage.download(attachment.getContentPath());
	}

	@Override
	public Class<CAttachment> getEntityClass() { return CAttachment.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CAttachmentInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceAttachment.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Get version history for an attachment (all versions linked via previousVersion chain).
	 * @param attachment the attachment
	 * @return list of all versions (newest first) */
	public List<CAttachment> getVersionHistory(final CAttachment attachment) {
		if (attachment == null || attachment.getId() == null) {
			return new ArrayList<>();
		}
		final List<CAttachment> history = new ArrayList<>();
		history.add(attachment);
		// Find all newer versions that reference this attachment
		CAttachment current = attachment;
		while (current != null) {
			final List<CAttachment> newerVersions = attachmentRepository.findByPreviousVersion(current);
			if (!newerVersions.isEmpty()) {
				current = newerVersions.get(0); // Get the next version
				history.add(0, current); // Add to beginning (newest first)
			} else {
				current = null;
			}
		}
		// Find all older versions by traversing previousVersion chain
		CAttachment older = attachment.getPreviousVersion();
		while (older != null) {
			history.add(older); // Add to end (oldest last)
			older = older.getPreviousVersion();
		}
		return history;
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		((CAttachment) entity).setUploadedBy(sessionService.getActiveUser().orElseThrow());
	}

	/** Upload a new file and create an attachment entity.
	 * @param fileName    the original file name
	 * @param inputStream the file content stream
	 * @param fileSize    the file size in bytes
	 * @param fileType    the MIME type (optional)
	 * @param description user-provided description (optional)
	 * @return the created attachment entity */
	@Transactional
	public CAttachment uploadFile(final String fileName, final InputStream inputStream, final long fileSize, final String fileType,
			final String description) throws Exception {
		Objects.requireNonNull(fileName, "File name cannot be null");
		Objects.requireNonNull(inputStream, "Input stream cannot be null");
		// Upload file to storage
		final String contentPath = attachmentStorage.upload(fileName, inputStream, fileSize);
		// Create attachment entity
		final CUser currentUser = sessionService.getActiveUser().orElseThrow(() -> new IllegalStateException("No active user found"));
		final CAttachment attachment = new CAttachment(fileName, fileSize, contentPath, currentUser);
		attachment.setName(fileName);
		attachment.setFileType(fileType);
		// Auto-detect document type based on file extension
		typeService.detectDocumentType(fileName, fileType).ifPresent(attachment::setDocumentType);
		attachment.setDescription(description);
		attachment.setUploadDate(LocalDateTime.now());
		attachment.setVersionNumber(1);
		// Save to database
		final CAttachment saved = save(attachment);
		LOGGER.info("Uploaded file: {} (size: {} bytes, id: {}, type: {})", fileName, fileSize, saved.getId(),
				saved.getDocumentType() != null ? saved.getDocumentType().getName() : "none");
		return saved;
	}

	/** Upload a new version of an existing attachment.
	 * @param previousAttachment the previous version
	 * @param fileName           the file name
	 * @param inputStream        the file content stream
	 * @param fileSize           the file size in bytes
	 * @param fileType           the MIME type (optional)
	 * @param description        user-provided description (optional)
	 * @return the new version attachment */
	@Transactional
	public CAttachment uploadNewVersion(final CAttachment previousAttachment, final String fileName, final InputStream inputStream,
			final long fileSize, final String fileType, final String description) throws Exception {
		Objects.requireNonNull(previousAttachment, "Previous attachment cannot be null");
		Objects.requireNonNull(fileName, "File name cannot be null");
		Objects.requireNonNull(inputStream, "Input stream cannot be null");
		// Upload file to storage
		final String contentPath = attachmentStorage.upload(fileName, inputStream, fileSize);
		// Create new version
		final CUser currentUser = sessionService.getActiveUser().orElseThrow(() -> new IllegalStateException("No active user found"));
		final CAttachment newVersion = new CAttachment(fileName, fileSize, contentPath, currentUser);
		newVersion.setName(fileName);
		newVersion.setFileType(fileType);
		newVersion.setDocumentType(previousAttachment.getDocumentType()); // Inherit document type
		newVersion.setDescription(description);
		newVersion.setUploadDate(LocalDateTime.now());
		newVersion.setVersionNumber(previousAttachment.getVersionNumber() + 1);
		newVersion.setPreviousVersion(previousAttachment);
		// Important: New version must be in same company as previous version
		newVersion.setCompany(previousAttachment.getCompany());
		// Save to database
		final CAttachment saved = save(newVersion);
		LOGGER.info("Uploaded new version {} of attachment: {} (id: {})", saved.getVersionNumber(), fileName, saved.getId());
		return saved;
	}

	@Override
	protected void validateEntity(final CAttachment entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getFileName(), "File Name is required");
		Check.notBlank(entity.getContentPath(), "Content Path is required");
		Check.notNull(entity.getUploadedBy(), "Uploaded By is required");
		Check.notNull(entity.getUploadDate(), "Upload Date is required");
		// 2. Length Checks
		if (entity.getFileName().length() > 500) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("File Name cannot exceed %d characters", 500));
		}
		if (entity.getContentPath().length() > 1000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Content Path cannot exceed %d characters", 1000));
		}
		if (entity.getFileType() != null && entity.getFileType().length() > 200) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("File Type cannot exceed %d characters", 200));
		}
		if (entity.getDescription() != null && entity.getDescription().length() > 2000) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Description cannot exceed %d characters", 2000));
		}
		// 3. Numeric Checks
		if (entity.getFileSize() != null && entity.getFileSize() < 0) {
			throw new IllegalArgumentException("File size must be positive");
		}
		if (entity.getVersionNumber() != null && entity.getVersionNumber() < 1) {
			throw new IllegalArgumentException("Version number must be at least 1");
		}
	}
}
