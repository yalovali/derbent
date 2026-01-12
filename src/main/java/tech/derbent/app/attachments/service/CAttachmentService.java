package tech.derbent.app.attachments.service;

import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.storage.IAttachmentStorage;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.risks.risk.domain.CRisk;
import tech.derbent.app.sprints.domain.CSprint;
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
public class CAttachmentService extends CEntityOfProjectService<CAttachment> implements IEntityRegistrable, IEntityWithView {

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
		// Initializer service not yet implemented - optional for now
		return null;
	}

	@Override
	public Class<?> getPageServiceClass() {
		// Page service not yet implemented - optional for now
		return null;
	}

	@Override
	public void initializeNewEntity(final CAttachment entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new attachment entity");
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

	/** Link an attachment to an activity.
	 * @param attachment the attachment
	 * @param activity the activity */
	@Transactional
	public void linkToActivity(final CAttachment attachment, final CActivity activity) {
		Objects.requireNonNull(attachment, "Attachment cannot be null");
		Objects.requireNonNull(activity, "Activity cannot be null");
		attachment.setActivity(activity);
		attachment.setRisk(null);
		attachment.setMeeting(null);
		attachment.setSprint(null);
		save(attachment);
	}

	/** Link an attachment to a risk.
	 * @param attachment the attachment
	 * @param risk the risk */
	@Transactional
	public void linkToRisk(final CAttachment attachment, final CRisk risk) {
		Objects.requireNonNull(attachment, "Attachment cannot be null");
		Objects.requireNonNull(risk, "Risk cannot be null");
		attachment.setActivity(null);
		attachment.setRisk(risk);
		attachment.setMeeting(null);
		attachment.setSprint(null);
		save(attachment);
	}

	/** Link an attachment to a meeting.
	 * @param attachment the attachment
	 * @param meeting the meeting */
	@Transactional
	public void linkToMeeting(final CAttachment attachment, final CMeeting meeting) {
		Objects.requireNonNull(attachment, "Attachment cannot be null");
		Objects.requireNonNull(meeting, "Meeting cannot be null");
		attachment.setActivity(null);
		attachment.setRisk(null);
		attachment.setMeeting(meeting);
		attachment.setSprint(null);
		save(attachment);
	}

	/** Link an attachment to a sprint.
	 * @param attachment the attachment
	 * @param sprint the sprint */
	@Transactional
	public void linkToSprint(final CAttachment attachment, final CSprint sprint) {
		Objects.requireNonNull(attachment, "Attachment cannot be null");
		Objects.requireNonNull(sprint, "Sprint cannot be null");
		attachment.setActivity(null);
		attachment.setRisk(null);
		attachment.setMeeting(null);
		attachment.setSprint(sprint);
		save(attachment);
	}

	/** Get all attachments for a project.
	 * @param project the project
	 * @return list of attachments */
	public List<CAttachment> findByProject(final CProject project) {
		return attachmentRepository.listByProject(project);
	}

	/** Get all attachments for an activity.
	 * @param activity the activity
	 * @return list of attachments */
	public List<CAttachment> findByActivity(final CActivity activity) {
		return attachmentRepository.findByActivity(activity);
	}

	/** Get all attachments for a risk.
	 * @param risk the risk
	 * @return list of attachments */
	public List<CAttachment> findByRisk(final CRisk risk) {
		return attachmentRepository.findByRisk(risk);
	}

	/** Get all attachments for a meeting.
	 * @param meeting the meeting
	 * @return list of attachments */
	public List<CAttachment> findByMeeting(final CMeeting meeting) {
		return attachmentRepository.findByMeeting(meeting);
	}

	/** Get all attachments for a sprint.
	 * @param sprint the sprint
	 * @return list of attachments */
	public List<CAttachment> findBySprint(final CSprint sprint) {
		return attachmentRepository.findBySprint(sprint);
	}

	/** Get version history for an attachment.
	 * @param attachment the attachment
	 * @return list of all versions */
	public List<CAttachment> getVersionHistory(final CAttachment attachment) {
		return attachmentRepository.findVersionHistory(attachment.getId());
	}
}
