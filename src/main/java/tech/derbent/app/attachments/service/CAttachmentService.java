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
		
		// Note: fileName, fileSize, contentPath are required and must be set before saving
		// Note: project is required and must be set before saving
	}

	@Override
	public String checkDeleteAllowed(final CAttachment attachment) {
		// Check if attachment is used as a previous version reference
		final List<CAttachment> newerVersions = attachmentRepository.findByFileNameAndProject(
				attachment.getFileName(), attachment.getProject());
		
		for (final CAttachment version : newerVersions) {
			if (version.getPreviousVersion() != null && 
					version.getPreviousVersion().getId().equals(attachment.getId())) {
				return "Cannot delete attachment - it is referenced by a newer version (version " + 
						version.getVersionNumber() + ")";
			}
		}
		
		return super.checkDeleteAllowed(attachment);
	}

	/** Upload a new file and create an attachment record.
	 * @param fileName the original file name
	 * @param fileType the MIME type
	 * @param fileSize the file size in bytes
	 * @param contentStream the file content
	 * @param project the project
	 * @param uploadedBy the uploading user
	 * @return the created attachment
	 * @throws Exception if upload fails */
	@Transactional
	public CAttachment uploadFile(final String fileName, final String fileType, final long fileSize,
			final InputStream contentStream, final CProject project, final CUser uploadedBy) throws Exception {
		Objects.requireNonNull(fileName, "File name cannot be null");
		Objects.requireNonNull(contentStream, "Content stream cannot be null");
		Objects.requireNonNull(project, "Project cannot be null");
		Objects.requireNonNull(uploadedBy, "Uploading user cannot be null");

		// Upload file to storage
		final String contentPath = attachmentStorage.upload(fileName, contentStream, fileSize);

		// Create attachment record
		final CAttachment attachment = new CAttachment(fileName, fileSize, contentPath, uploadedBy, project);
		attachment.setFileType(fileType);
		attachment.setUploadDate(LocalDateTime.now());

		return save(attachment);
	}

	/** Upload a new version of an existing file.
	 * @param previousAttachment the previous version
	 * @param fileName the new file name
	 * @param fileType the MIME type
	 * @param fileSize the file size in bytes
	 * @param contentStream the file content
	 * @param uploadedBy the uploading user
	 * @return the new version attachment
	 * @throws Exception if upload fails */
	@Transactional
	public CAttachment uploadNewVersion(final CAttachment previousAttachment, final String fileName, 
			final String fileType, final long fileSize, final InputStream contentStream, 
			final CUser uploadedBy) throws Exception {
		Objects.requireNonNull(previousAttachment, "Previous attachment cannot be null");

		// Upload file to storage
		final String contentPath = attachmentStorage.upload(fileName, contentStream, fileSize);

		// Create new version
		final CAttachment newVersion = new CAttachment(fileName, fileSize, contentPath, 
				uploadedBy, previousAttachment.getProject());
		newVersion.setFileType(fileType);
		newVersion.setUploadDate(LocalDateTime.now());
		newVersion.setVersionNumber(previousAttachment.getVersionNumber() + 1);
		newVersion.setPreviousVersion(previousAttachment);
		newVersion.setDocumentType(previousAttachment.getDocumentType());
		newVersion.setDescription(previousAttachment.getDescription());

		// Copy entity links
		newVersion.setActivity(previousAttachment.getActivity());
		newVersion.setRisk(previousAttachment.getRisk());
		newVersion.setMeeting(previousAttachment.getMeeting());
		newVersion.setSprint(previousAttachment.getSprint());

		return save(newVersion);
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
