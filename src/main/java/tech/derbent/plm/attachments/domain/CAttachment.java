package tech.derbent.plm.attachments.domain;

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
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.documenttypes.domain.CDocumentType;

/** CAttachment - Company-scoped domain entity representing file attachments. Stores metadata about uploaded files. Files are stored on disk, not in
 * database. Supports versioning to track document changes over time. Company-scoped for multi-tenant support and universal usage across all entities.
 * Pattern: Unidirectional @OneToMany from parent entities. Parent entities (Activity, Risk, Meeting, Sprint, Project, User, etc.) have:
 * @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
 * @JoinColumn(name = "activity_id") // or risk_id, user_id, etc. private Set<CAttachment> attachments = new HashSet<>(); CAttachment has NO
 *                  back-reference to parents (clean unidirectional pattern). Layer: Domain (MVC) */
@Entity
@Table (name = "cattachment")
@AttributeOverride (name = "id", column = @Column (name = "attachment_id"))
public final class CAttachment extends CEntityOfCompany<CAttachment> implements IHasIcon {

	public static final String DEFAULT_COLOR = "#2F4F4F"; // Dark Slate Gray - files
	public static final String DEFAULT_ICON = "vaadin:paperclip";
	public static final String ENTITY_TITLE_PLURAL = "Attachments";
	public static final String ENTITY_TITLE_SINGULAR = "Attachment";
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CAttachment.class);
	public static final String VIEW_NAME = "Attachments View";
	// Color for display (required by IHasIcon interface)
	@Column (name = "color", nullable = true, length = 20)
	@Size (max = 20)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, description = "Display color for this attachment", hidden = true,
			maxLength = 20
	)
	private String color = DEFAULT_COLOR;
	@Column (nullable = false, length = 1000)
	@NotBlank (message = "Content path is required")
	@Size (max = 1000)
	@AMetaData (
			displayName = "Content Path", required = true, readOnly = true, description = "Relative path to the file on disk", hidden = true,
			maxLength = 1000
	)
	private String contentPath;
	// Document type classification
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "document_type_id", nullable = true)
	@AMetaData (
			displayName = "Document Type", required = false, readOnly = false, description = "Category of this document", hidden = false,
			dataProviderBean = "CDocumentTypeService"
	)
	private CDocumentType documentType;
	// File metadata fields
	@Column (nullable = false, length = 500)
	@NotBlank (message = "File name is required")
	@Size (max = 500)
	@AMetaData (
			displayName = "File Name", required = true, readOnly = false, description = "Original name of the uploaded file", hidden = false,
			maxLength = 500
	)
	private String fileName;
	@Column (nullable = false)
	@Min (value = 0, message = "File size must be positive")
	@AMetaData (displayName = "File Size (bytes)", required = true, readOnly = true, description = "Size of the file in bytes", hidden = false)
	private Long fileSize = 0L;
	@Column (nullable = true, length = 200)
	@Size (max = 200)
	@AMetaData (
			displayName = "File Type", required = false, readOnly = true, description = "MIME type of the file (e.g., application/pdf)",
			hidden = false, maxLength = 200
	)
	private String fileType;
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "previous_version_id", nullable = true)
	@AMetaData (
			displayName = "Previous Version", required = false, readOnly = true, description = "Link to the previous version of this document",
			hidden = true
	)
	private CAttachment previousVersion;
	@Column (nullable = false)
	@AMetaData (
			displayName = "Upload Date", required = true, readOnly = true, description = "Date and time when the file was uploaded", hidden = false
	)
	private LocalDateTime uploadDate;
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "uploaded_by_id", nullable = false)
	@AMetaData (
			displayName = "Uploaded By", required = true, readOnly = true, description = "User who uploaded this file", hidden = false,
			dataProviderBean = "CUserService"
	)
	private CUser uploadedBy;
	// Version management fields
	@Column (nullable = false)
	@Min (value = 1, message = "Version number must be at least 1")
	@AMetaData (displayName = "Version", required = true, readOnly = true, description = "Version number of this attachment", hidden = false)
	private Integer versionNumber = 1;

	/** Default constructor for JPA. */
										/** Default constructor for JPA. */
	protected CAttachment() {}

	/** Constructor with required fields.
	 * @param fileName    the original file name
	 * @param fileSize    the file size in bytes
	 * @param contentPath the path where the file is stored on disk
	 * @param uploadedBy  the user who uploaded the file */
	public CAttachment(final String fileName, final Long fileSize, final String contentPath, final CUser uploadedBy) {
		initializeDefaults();
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.contentPath = contentPath;
		this.uploadedBy = uploadedBy;
	}

	@Override
	public String getColor() { return color != null ? color : DEFAULT_COLOR; }

	public String getContentPath() { return contentPath; }

	public CDocumentType getDocumentType() { return documentType; }

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

	// Getters and setters
	public String getFileName() { return fileName; }

	public Long getFileSize() { return fileSize; }

	public String getFileType() { return fileType; }

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
		if ("jpg".equals(extension) || "jpeg".equals(extension) || "png".equals(extension) || "gif".equals(extension) || "bmp".equals(extension)
				|| "svg".equals(extension) || "webp".equals(extension)) {
			return "vaadin:file-picture";
		}
		// Videos
		if ("mp4".equals(extension) || "avi".equals(extension) || "mov".equals(extension) || "wmv".equals(extension) || "flv".equals(extension)
				|| "webm".equals(extension)) {
			return "vaadin:file-movie";
		}
		// Audio
		if ("mp3".equals(extension) || "wav".equals(extension) || "ogg".equals(extension) || "m4a".equals(extension) || "flac".equals(extension)) {
			return "vaadin:file-sound";
		}
		// Archives/compressed
		if ("zip".equals(extension) || "rar".equals(extension) || "7z".equals(extension) || "tar".equals(extension) || "gz".equals(extension)) {
			return "vaadin:file-zip";
		}
		// Code files
		if ("java".equals(extension) || "js".equals(extension) || "ts".equals(extension) || "py".equals(extension) || "cpp".equals(extension)
				|| "c".equals(extension) || "html".equals(extension) || "css".equals(extension) || "xml".equals(extension) || "json".equals(extension)
				|| "sql".equals(extension)) {
			return "vaadin:file-code";
		}
		// Text files
		if ("txt".equals(extension) || "md".equals(extension) || "log".equals(extension)) {
			return "vaadin:file-text-o";
		}
		// Default generic file icon
		return "vaadin:file-o";
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
		return "%.1f %sB".formatted(bytes / Math.pow(1024, exp), pre);
	}

	@Override
	public String getIconString() { return getFileTypeIcon(); }

	public CAttachment getPreviousVersion() { return previousVersion; }

	public LocalDateTime getUploadDate() { return uploadDate; }

	public CUser getUploadedBy() { return uploadedBy; }

	public Integer getVersionNumber() { return versionNumber; }

	private final void initializeDefaults() {
		uploadDate = LocalDateTime.now();
		versionNumber = 1;
		color = DEFAULT_COLOR;
		contentPath = "";
		documentType = null;
		fileName = "";
		fileSize = 0L;
		fileType = "";
		previousVersion = null;
		uploadDate = LocalDateTime.now();
		uploadedBy = null;
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setColor(final String color) { this.color = color; }

	public void setContentPath(final String contentPath) { this.contentPath = contentPath; }

	public void setDocumentType(final CDocumentType documentType) { this.documentType = documentType; }

	public void setFileName(final String fileName) { this.fileName = fileName; }

	public void setFileSize(final Long fileSize) { this.fileSize = fileSize; }

	public void setFileType(final String fileType) { this.fileType = fileType; }

	public void setPreviousVersion(final CAttachment previousVersion) { this.previousVersion = previousVersion; }

	public void setUploadDate(final LocalDateTime uploadDate) { this.uploadDate = uploadDate; }

	public void setUploadedBy(final CUser uploadedBy) { this.uploadedBy = uploadedBy; }

	public void setVersionNumber(final Integer versionNumber) { this.versionNumber = versionNumber; }
}
