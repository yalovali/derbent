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
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.app.documenttypes.domain.CDocumentType;
import tech.derbent.base.users.domain.CUser;

/**
 * CAttachment - Domain entity representing file attachments.
 * 
 * Stores metadata about uploaded files and links them to a single parent entity 
 * (Activity, Risk, Meeting, or Sprint). Files are stored on disk, not in the database. 
 * Supports versioning to track document changes over time.
 * 
 * Pattern: Similar to CComment - each attachment belongs to ONE parent entity,
 * and each parent entity can have MANY attachments.
 * 
 * Layer: Domain (MVC)
 */
@Entity
@Table(name = "cattachment")
@AttributeOverride(name = "id", column = @Column(name = "attachment_id"))
public class CAttachment extends CEntityOfProject<CAttachment> implements IHasIcon {

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

	// Color for display (required by IHasIcon interface)
	@Column(name = "color", nullable = true, length = 20)
	@Size(max = 20)
	@AMetaData(
		displayName = "Color",
		required = false,
		readOnly = false,
		description = "Display color for this attachment",
		hidden = true,
		maxLength = 20
	)
	private String color = DEFAULT_COLOR;

	// Generic parent reference - stores which entity owns this attachment
	// Pattern: Like CProjectItem's parentId/parentType but for attachments
	@Column(name = "owner_entity_id", nullable = true)
	@AMetaData(
		displayName = "Owner Entity ID",
		required = false,
		readOnly = true,
		description = "ID of the parent entity (Activity, Risk, Meeting, Sprint, Project)",
		hidden = true
	)
	private Long ownerEntityId;

	@Column(name = "owner_entity_type", nullable = true, length = 100)
	@Size(max = 100)
	@AMetaData(
		displayName = "Owner Entity Type",
		required = false,
		readOnly = true,
		description = "Type of the parent entity (CActivity, CRisk, CMeeting, CSprint, CProject)",
		hidden = true,
		maxLength = 100
	)
	private String ownerEntityType;

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

	public String getColor() {
		return color != null ? color : DEFAULT_COLOR;
	}

	public void setColor(final String color) {
		this.color = color;
	}

	public Long getOwnerEntityId() {
		return ownerEntityId;
	}

	public void setOwnerEntityId(final Long ownerEntityId) {
		this.ownerEntityId = ownerEntityId;
	}

	public String getOwnerEntityType() {
		return ownerEntityType;
	}

	public void setOwnerEntityType(final String ownerEntityType) {
		this.ownerEntityType = ownerEntityType;
	}

	/** Set the owner entity for this attachment.
	 * @param owner the owner entity (Activity, Risk, Meeting, Sprint, or Project) */
	public void setOwner(final IAttachmentOwner owner) {
		Check.notNull(owner, "Owner cannot be null");
		this.ownerEntityId = owner.getId();
		this.ownerEntityType = owner.getEntityClass().getSimpleName();
	}

	/** Get the parent entity name for display.
	 * @return the parent entity type and ID */
	public String getParentEntityName() {
		if (ownerEntityType != null && ownerEntityId != null) {
			return ownerEntityType + " #" + ownerEntityId;
		}
		return "Unknown";
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

	/** Get the file extension from the filename.
	 * @return file extension in lowercase (e.g., "pdf", "docx", "jpg") or empty string */
	public String getFileExtension() {
		if (fileName == null || !fileName.contains(".")) {
			return "";
		}
		final int lastDotIndex = fileName.lastIndexOf('.');
		if (lastDotIndex >= 0 && lastDotIndex < fileName.length() - 1) {
			return fileName.substring(lastDotIndex + 1).toLowerCase();
		}
		return "";
	}

	@Override
	public String getIconString() {
		return getFileTypeIcon();
	}

	/** Get the Vaadin icon for this file type based on extension or MIME type.
	 * @return Vaadin icon string (e.g., "vaadin:file-text", "vaadin:file-picture") */
	public String getFileTypeIcon() {
		final String extension = getFileExtension();
		
		// PDF documents
		if ("pdf".equals(extension)) {
			return "vaadin:file-text-o";
		}
		
		// Word documents
		if ("doc".equals(extension) || "docx".equals(extension) || "odt".equals(extension)) {
			return "vaadin:file-text";
		}
		
		// Excel/spreadsheets
		if ("xls".equals(extension) || "xlsx".equals(extension) || "ods".equals(extension) || "csv".equals(extension)) {
			return "vaadin:file-table";
		}
		
		// PowerPoint/presentations
		if ("ppt".equals(extension) || "pptx".equals(extension) || "odp".equals(extension)) {
			return "vaadin:file-presentation";
		}
		
		// Images
		if ("jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension) || 
			"gif".equals(extension) || "bmp".equals(extension) || "svg".equals(extension) || 
			"webp".equals(extension)) {
			return "vaadin:file-picture";
		}
		
		// Videos
		if ("mp4".equals(extension) || "avi".equals(extension) || "mov".equals(extension) || 
			"wmv".equals(extension) || "flv".equals(extension) || "webm".equals(extension)) {
			return "vaadin:file-movie";
		}
		
		// Audio
		if ("mp3".equals(extension) || "wav".equals(extension) || "ogg".equals(extension) || 
			"m4a".equals(extension) || "flac".equals(extension)) {
			return "vaadin:file-sound";
		}
		
		// Archives/compressed
		if ("zip".equals(extension) || "rar".equals(extension) || "7z".equals(extension) || 
			"tar".equals(extension) || "gz".equals(extension)) {
			return "vaadin:file-zip";
		}
		
		// Code files
		if ("java".equals(extension) || "js".equals(extension) || "ts".equals(extension) || 
			"py".equals(extension) || "cpp".equals(extension) || "c".equals(extension) || 
			"html".equals(extension) || "css".equals(extension) || "xml".equals(extension) || 
			"json".equals(extension) || "sql".equals(extension)) {
			return "vaadin:file-code";
		}
		
		// Text files
		if ("txt".equals(extension) || "md".equals(extension) || "log".equals(extension)) {
			return "vaadin:file-text-o";
		}
		
		// Default generic file icon
		return "vaadin:file-o";
	}
}
