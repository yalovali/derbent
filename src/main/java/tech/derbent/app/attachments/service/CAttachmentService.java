package tech.derbent.app.attachments.service;

import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.storage.IAttachmentStorage;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/**
 * Service for managing CAttachment entities and file operations.
 * 
 * Provides CRUD operations, file upload/download, and version management.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Menu(icon = "vaadin:paperclip", title = "Project.Attachments")
@PermitAll
public class CAttachmentService extends CEntityOfCompanyService<CAttachment> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAttachmentService.class);
	private final IAttachmentStorage attachmentStorage;
	private final IAttachmentRepository attachmentRepository;

	public CAttachmentService(final IAttachmentRepository repository, final Clock clock, 
			final ISessionService sessionService, final IAttachmentStorage attachmentStorage) {
		super(repository, clock, sessionService);
		this.attachmentStorage = attachmentStorage;
		this.attachmentRepository = repository;
	}

	@Override
	public Class<CAttachment> getEntityClass() {
		return CAttachment.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CAttachmentInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceAttachment.class;
	}

	@Override
	public void initializeNewEntity(final CAttachment entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new attachment entity");
		
		// Get current user from session
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize attachment"));
		
		// Initialize upload date if not set
		if (entity.getUploadDate() == null) {
			entity.setUploadDate(LocalDateTime.now());
		}
		
		// Initialize uploaded by if not set
		if (entity.getUploadedBy() == null) {
			entity.setUploadedBy(currentUser);
		}
		
		// Initialize version number if not set
		if (entity.getVersionNumber() == null) {
			entity.setVersionNumber(1);
		}
		
		// Set default color if not set
		if (entity.getColor() == null || entity.getColor().isBlank()) {
			entity.setColor(CAttachment.DEFAULT_COLOR);
		}
		
		LOGGER.debug("Initialized new attachment: {} (version {})", entity.getFileName(),
				entity.getVersionNumber());
	}

	@Override
	public String checkDeleteAllowed(final CAttachment attachment) {
		// Check if attachment is referenced by newer versions as previousVersion
		final List<CAttachment> newerVersions = attachmentRepository.findByPreviousVersion(attachment);
		
		if (!newerVersions.isEmpty()) {
			return "Cannot delete attachment - it is referenced by newer version(s). Version numbers: " + 
					newerVersions.stream()
						.map(v -> String.valueOf(v.getVersionNumber()))
						.collect(java.util.stream.Collectors.joining(", "));
		}
		
		return super.checkDeleteAllowed(attachment);
	}

	/** Upload a new file and create an attachment entity.
	 * @param fileName the original file name
	 * @param inputStream the file content stream
	 * @param fileSize the file size in bytes
	 * @param fileType the MIME type (optional)
	 * @param documentType the document type classification (optional)
	 * @param description user-provided description (optional)
	 * @return the created attachment entity */
	@Transactional
	public CAttachment uploadFile(final String fileName, final InputStream inputStream,
			final long fileSize, final String fileType, final CDocumentType documentType,
			final String description) throws Exception {

		Objects.requireNonNull(fileName, "File name cannot be null");
		Objects.requireNonNull(inputStream, "Input stream cannot be null");

		// Upload file to storage
		final String contentPath = attachmentStorage.upload(fileName, inputStream, fileSize);

		// Create attachment entity
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new IllegalStateException("No active user found"));

		final CAttachment attachment = new CAttachment(fileName, fileSize, contentPath, currentUser);
		attachment.setFileType(fileType);
		attachment.setDocumentType(documentType);
		attachment.setDescription(description);
		attachment.setUploadDate(LocalDateTime.now());
		attachment.setVersionNumber(1);

		// Save to database
		final CAttachment saved = save(attachment);

		LOGGER.info("Uploaded file: {} (size: {} bytes, id: {})", fileName, fileSize, saved.getId());

		return saved;
	}

	/** Upload a new version of an existing attachment.
	 * @param previousAttachment the previous version
	 * @param fileName the file name
	 * @param inputStream the file content stream
	 * @param fileSize the file size in bytes
	 * @param fileType the MIME type (optional)
	 * @param description user-provided description (optional)
	 * @return the new version attachment */
	@Transactional
	public CAttachment uploadNewVersion(final CAttachment previousAttachment, final String fileName,
			final InputStream inputStream, final long fileSize, final String fileType,
			final String description) throws Exception {

		Objects.requireNonNull(previousAttachment, "Previous attachment cannot be null");
		Objects.requireNonNull(fileName, "File name cannot be null");
		Objects.requireNonNull(inputStream, "Input stream cannot be null");

		// Upload file to storage
		final String contentPath = attachmentStorage.upload(fileName, inputStream, fileSize);

		// Create new version
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new IllegalStateException("No active user found"));

		final CAttachment newVersion = new CAttachment(fileName, fileSize, contentPath, currentUser);
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

		LOGGER.info("Uploaded new version {} of attachment: {} (id: {})",
				saved.getVersionNumber(), fileName, saved.getId());

		return saved;
	}

	/** Download a file.
	 * @param attachment the attachment to download
	 * @return the file content as an input stream
	 * @throws Exception if download fails */
	public InputStream downloadFile(final CAttachment attachment) throws Exception {
		Objects.requireNonNull(attachment, "Attachment cannot be null");
		return attachmentStorage.download(attachment.getContentPath());
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
}
