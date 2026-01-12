package tech.derbent.app.attachments.domain;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.documenttypes.domain.CDocumentType;
import tech.derbent.app.meetings.domain.CMeeting;
import tech.derbent.app.risks.risk.domain.CRisk;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.base.users.domain.CUser;

/**
 * CAttachment - Domain entity representing file attachments.
 * 
 * Stores metadata about uploaded files and links them to project items (activities, risks, 
 * meetings, sprints). Files are stored on disk, not in the database. Supports versioning
 * to track document changes over time.
 * 
 * Layer: Domain (MVC)
 */
@Entity
@Table(name = "cattachment")
@AttributeOverride(name = "id", column = @Column(name = "attachment_id"))
public class CAttachment extends CEntityOfProject<CAttachment> {

	public static final String DEFAULT_COLOR = "#2F4F4F"; // Dark Slate Gray - files
	public static final String DEFAULT_ICON = "vaadin:paperclip";
	public static final String ENTITY_TITLE_PLURAL = "Attachments";
	public static final String ENTITY_TITLE_SINGULAR = "Attachment";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAttachment.class);
	public static final String VIEW_NAME = "Attachments View";

	// File metadata fields
	@Column(nullable = false, length = 500)
	@NotBlank(message = "File name is required")
	@Size(max = 500)
	@AMetaData(
		displayName = "File Name",
		required = true,
		readOnly = false,
		description = "Original name of the uploaded file",
		hidden = false,
		maxLength = 500
	)
	private String fileName;

	@Column(nullable = false)
	@Min(value = 0, message = "File size must be positive")
	@AMetaData(
		displayName = "File Size (bytes)",
		required = true,
		readOnly = true,
		description = "Size of the file in bytes",
		hidden = false
	)
	private Long fileSize = 0L;

	@Column(nullable = true, length = 200)
	@Size(max = 200)
	@AMetaData(
		displayName = "File Type",
		required = false,
		readOnly = true,
		description = "MIME type of the file (e.g., application/pdf)",
		hidden = false,
		maxLength = 200
	)
	private String fileType;

	@Column(nullable = false, length = 1000)
	@NotBlank(message = "Content path is required")
	@Size(max = 1000)
	@AMetaData(
		displayName = "Content Path",
		required = true,
		readOnly = true,
		description = "Relative path to the file on disk",
		hidden = true,
		maxLength = 1000
	)
	private String contentPath;

	@Column(nullable = false)
	@AMetaData(
		displayName = "Upload Date",
		required = true,
		readOnly = true,
		description = "Date and time when the file was uploaded",
		hidden = false
	)
	private LocalDateTime uploadDate;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "uploaded_by_id", nullable = false)
	@AMetaData(
		displayName = "Uploaded By",
		required = true,
		readOnly = true,
		description = "User who uploaded this file",
		hidden = false,
		dataProviderBean = "CUserService"
	)
	private CUser uploadedBy;

	// Version management fields
	@Column(nullable = false)
	@Min(value = 1, message = "Version number must be at least 1")
	@AMetaData(
		displayName = "Version",
		required = true,
		readOnly = true,
		description = "Version number of this attachment",
		hidden = false
	)
	private Integer versionNumber = 1;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "previous_version_id", nullable = true)
	@AMetaData(
		displayName = "Previous Version",
		required = false,
		readOnly = true,
		description = "Link to the previous version of this document",
		hidden = true
	)
	private CAttachment previousVersion;

	// Document type classification
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "document_type_id", nullable = true)
	@AMetaData(
		displayName = "Document Type",
		required = false,
		readOnly = false,
		description = "Category of this document",
		hidden = false,
		dataProviderBean = "CDocumentTypeService"
	)
	private CDocumentType documentType;

	// Optional description/comment
	@Column(nullable = true, length = 2000)
	@Size(max = 2000)
	@AMetaData(
		displayName = "Description",
		required = false,
		readOnly = false,
		description = "Description or notes about this attachment",
		hidden = false,
		maxLength = 2000
	)
	private String description;

	// Links to various entities - only one should be set
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "activity_id", nullable = true)
	@AMetaData(
		displayName = "Activity",
		required = false,
		readOnly = true,
		description = "Activity this attachment belongs to",
		hidden = true
	)
	private CActivity activity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "risk_id", nullable = true)
	@AMetaData(
		displayName = "Risk",
		required = false,
		readOnly = true,
		description = "Risk this attachment belongs to",
		hidden = true
	)
	private CRisk risk;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meeting_id", nullable = true)
	@AMetaData(
		displayName = "Meeting",
		required = false,
		readOnly = true,
		description = "Meeting this attachment belongs to",
		hidden = true
	)
	private CMeeting meeting;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sprint_id", nullable = true)
	@AMetaData(
		displayName = "Sprint",
		required = false,
		readOnly = true,
		description = "Sprint this attachment belongs to",
		hidden = true
	)
	private CSprint sprint;

	/** Default constructor for JPA. */
	public CAttachment() {
		super();
	}

	/** Constructor with required fields.
	 * @param fileName the original file name
	 * @param fileSize the file size in bytes
	 * @param contentPath the path where the file is stored on disk
	 * @param uploadedBy the user who uploaded the file
	 * @param project the project this attachment belongs to */
	public CAttachment(final String fileName, final Long fileSize, final String contentPath, 
			final CUser uploadedBy, final CProject project) {
		super(CAttachment.class, fileName, project);
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.contentPath = contentPath;
		this.uploadedBy = uploadedBy;
		this.uploadDate = LocalDateTime.now();
		this.versionNumber = 1;
	}

	// Getters and setters
	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(final Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(final String fileType) {
		this.fileType = fileType;
	}

	public String getContentPath() {
		return contentPath;
	}

	public void setContentPath(final String contentPath) {
		this.contentPath = contentPath;
	}

	public LocalDateTime getUploadDate() {
		return uploadDate;
	}

	public void setUploadDate(final LocalDateTime uploadDate) {
		this.uploadDate = uploadDate;
	}

	public CUser getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(final CUser uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public Integer getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(final Integer versionNumber) {
		this.versionNumber = versionNumber;
	}

	public CAttachment getPreviousVersion() {
		return previousVersion;
	}

	public void setPreviousVersion(final CAttachment previousVersion) {
		this.previousVersion = previousVersion;
	}

	public CDocumentType getDocumentType() {
		return documentType;
	}

	public void setDocumentType(final CDocumentType documentType) {
		this.documentType = documentType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public CActivity getActivity() {
		return activity;
	}

	public void setActivity(final CActivity activity) {
		this.activity = activity;
	}

	public CRisk getRisk() {
		return risk;
	}

	public void setRisk(final CRisk risk) {
		this.risk = risk;
	}

	public CMeeting getMeeting() {
		return meeting;
	}

	public void setMeeting(final CMeeting meeting) {
		this.meeting = meeting;
	}

	public CSprint getSprint() {
		return sprint;
	}

	public void setSprint(final CSprint sprint) {
		this.sprint = sprint;
	}

	/** Get formatted file size.
	 * @return human-readable file size */
	public String getFormattedFileSize() {
		if (fileSize == null || fileSize == 0) {
			return "0 B";
		}
		final long bytes = fileSize;
		if (bytes < 1024) {
			return bytes + " B";
		}
		final int exp = (int) (Math.log(bytes) / Math.log(1024));
		final String pre = "KMGTPE".charAt(exp - 1) + "";
		return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
	}

	/** Get the entity this attachment is linked to.
	 * @return the linked entity name or "Project" if only linked to project */
	public String getLinkedEntityName() {
		if (activity != null) {
			return "Activity: " + activity.getName();
		}
		if (risk != null) {
			return "Risk: " + risk.getName();
		}
		if (meeting != null) {
			return "Meeting: " + meeting.getName();
		}
		if (sprint != null) {
			return "Sprint: " + sprint.getName();
		}
		return "Project: " + getProject().getName();
	}
}
