package tech.derbent.bab.policybase.node.domain;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/**
 * CFileInputNode - File Input virtual network node entity for file system monitoring.
 * 
 * Layer: Domain (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete entity with @Entity annotation.
 * 
 * Represents file input virtual nodes mapped to file system paths.
 * Example: fileInput mapped to file system for data import/monitoring.
 * 
 * Used in BAB Actions Dashboard policy rule engine for file-based data processing
 * and file system event monitoring in IoT gateway scenarios.
 */
@Entity
@Table(name = "cnode_file_input", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"project_id", "name"}),
    @UniqueConstraint(columnNames = {"project_id", "file_path"})
})
@AttributeOverride(name = "id", column = @Column(name = "file_input_node_id"))
@Profile("bab")
public class CBabFileInputNode extends CBabNodeEntity<CBabFileInputNode> 
    implements IHasAttachments, IHasComments, IHasLinks, IEntityRegistrable {
    
    // Entity constants (MANDATORY - overriding base class constants)
    public static final String DEFAULT_COLOR = "#9C27B0"; // Purple - File/Data processing
    public static final String DEFAULT_ICON = "vaadin:file-text";
    public static final String ENTITY_TITLE_PLURAL = "File Input Nodes";
    public static final String ENTITY_TITLE_SINGULAR = "File Input Node";
    @SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabFileInputNode.class);
    public static final String VIEW_NAME = "File Input Nodes View";
    
    // File input specific fields
    @Column(name = "file_path", length = 500, nullable = false)
    @AMetaData(
        displayName = "File Path",
        required = true,
        readOnly = false,
        description = "File system path to monitor (file or directory)",
        hidden = false,
        maxLength = 500
    )
    private String filePath;
    
    @Column(name = "file_format", length = 20, nullable = false)
    @AMetaData(
        displayName = "File Format",
        required = true,
        readOnly = false,
        description = "Expected file format (JSON, XML, CSV, TXT, BINARY)",
        hidden = false,
        maxLength = 20
    )
    private String fileFormat = "JSON";
    
    @Column(name = "watch_directory", nullable = false)
    @AMetaData(
        displayName = "Watch Directory",
        required = false,
        readOnly = false,
        description = "Monitor entire directory for new files",
        hidden = false
    )
    private Boolean watchDirectory = false;
    
    @Column(name = "file_pattern", length = 100)
    @AMetaData(
        displayName = "File Pattern",
        required = false,
        readOnly = false,
        description = "File name pattern for directory watching (e.g., *.json, data_*.csv)",
        hidden = false,
        maxLength = 100
    )
    private String filePattern;
    
    @Column(name = "polling_interval_seconds", nullable = false)
    @AMetaData(
        displayName = "Polling Interval (seconds)",
        required = false,
        readOnly = false,
        description = "How often to check for file changes (in seconds)",
        hidden = false
    )
    private Integer pollingIntervalSeconds = 60;
    
    @Column(name = "auto_delete_processed", nullable = false)
    @AMetaData(
        displayName = "Auto-delete Processed Files",
        required = false,
        readOnly = false,
        description = "Automatically delete files after successful processing",
        hidden = false
    )
    private Boolean autoDeleteProcessed = false;
    
    @Column(name = "backup_processed_files", nullable = false)
    @AMetaData(
        displayName = "Backup Processed Files",
        required = false,
        readOnly = false,
        description = "Create backup copies of processed files",
        hidden = false
    )
    private Boolean backupProcessedFiles = true;
    
    @Column(name = "backup_directory", length = 500)
    @AMetaData(
        displayName = "Backup Directory",
        required = false,
        readOnly = false,
        description = "Directory path for backing up processed files",
        hidden = false,
        maxLength = 500
    )
    private String backupDirectory;
    
    @Column(name = "max_file_size_mb", nullable = false)
    @AMetaData(
        displayName = "Max File Size (MB)",
        required = false,
        readOnly = false,
        description = "Maximum file size to process (in megabytes)",
        hidden = false
    )
    private Integer maxFileSizeMb = 100;
    
    // Standard composition fields - initialized at declaration (RULE 5)
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_input_node_id")
    @AMetaData(
        displayName = "Attachments",
        required = false,
        readOnly = false,
        description = "File attachments for this file input node",
        hidden = false,
        dataProviderBean = "CAttachmentService",
        createComponentMethod = "createComponent"
    )
    private Set<CAttachment> attachments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_input_node_id")
    @AMetaData(
        displayName = "Comments",
        required = false,
        readOnly = false,
        description = "Comments for this file input node",
        hidden = false,
        dataProviderBean = "CCommentService",
        createComponentMethod = "createComponentComment"
    )
    private Set<CComment> comments = new HashSet<>();
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "file_input_node_id")
    @AMetaData(
        displayName = "Links",
        required = false,
        readOnly = false,
        description = "Related links for this file input node",
        hidden = false,
        dataProviderBean = "CLinkService",
        createComponentMethod = "createComponent"
    )
    private Set<CLink> links = new HashSet<>();
    
    /** Default constructor for JPA. */
    protected CBabFileInputNode() {
        // JPA constructors do NOT call initializeDefaults() (RULE 1)
    }
    
    public CBabFileInputNode(final String name, final CProject<?> project) {
        super(CBabFileInputNode.class, name, project, "FILE_INPUT");
        initializeDefaults(); // Business constructors MUST call this (RULE 2)
    }
    
    public CBabFileInputNode(final String name, final CProject<?> project, final String filePath) {
        super(CBabFileInputNode.class, name, project, "FILE_INPUT");
        this.filePath = filePath;
        initializeDefaults(); // Business constructors MUST call this (RULE 2)
    }
    
    /** Initialize intrinsic defaults (RULE 3). */
    private final void initializeDefaults() {
        // Initialize nullable=false fields with defaults (already done in field declarations)
        
        // File input specific defaults
        if (filePath == null || filePath.isEmpty()) {
            filePath = "/var/data/input";
        }
        if (fileFormat == null || fileFormat.isEmpty()) {
            fileFormat = "JSON";
        }
        if (watchDirectory == null) {
            watchDirectory = false;
        }
        if (pollingIntervalSeconds == null) {
            pollingIntervalSeconds = 60;
        }
        if (autoDeleteProcessed == null) {
            autoDeleteProcessed = false;
        }
        if (backupProcessedFiles == null) {
            backupProcessedFiles = true;
        }
        if (maxFileSizeMb == null) {
            maxFileSizeMb = 100;
        }
        
        // Set default backup directory if backup is enabled
        if (backupProcessedFiles && (backupDirectory == null || backupDirectory.isEmpty())) {
            backupDirectory = "/var/data/backup";
        }
        
        // Set default physical interface if not set
        if (getPhysicalInterface() == null || getPhysicalInterface().isEmpty()) {
            setPhysicalInterface("file");
        }
        
        // Generate initial node configuration JSON
        setNodeConfigJson(generateDefaultNodeConfig());
        
        // MANDATORY: Call service initialization at end (RULE 3)
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }
    
    @Override
    protected String generateDefaultNodeConfig() {
        return String.format("""
            {
                "nodeId": "%s",
                "nodeType": "FILE_INPUT",
                "physicalInterface": "%s",
                "active": %s,
                "priority": %d,
                "fileConfig": {
                    "filePath": "%s",
                    "fileFormat": "%s",
                    "watchDirectory": %s,
                    "filePattern": "%s",
                    "pollingIntervalSeconds": %d,
                    "autoDeleteProcessed": %s,
                    "backupProcessedFiles": %s,
                    "backupDirectory": "%s",
                    "maxFileSizeMb": %d
                }
            }
            """, getId(), getPhysicalInterface(), getIsActive(), getPriorityLevel(),
                filePath, fileFormat, watchDirectory,
                filePattern != null ? filePattern : "",
                pollingIntervalSeconds, autoDeleteProcessed, backupProcessedFiles,
                backupDirectory != null ? backupDirectory : "",
                maxFileSizeMb);
    }
    
    @Override
    public String getEntityColor() {
        return DEFAULT_COLOR;
    }
    
    /**
     * Check if this file input node monitors a directory.
     * @return true if watching a directory
     */
    public boolean isDirectoryWatcher() {
        return watchDirectory != null && watchDirectory;
    }
    
    /**
     * Check if file pattern is defined for directory watching.
     * @return true if file pattern is configured
     */
    public boolean hasFilePattern() {
        return filePattern != null && !filePattern.trim().isEmpty();
    }
    
    /**
     * Check if automatic file cleanup is enabled.
     * @return true if files are automatically deleted or backed up
     */
    public boolean hasAutomaticCleanup() {
        return (autoDeleteProcessed != null && autoDeleteProcessed) || 
               (backupProcessedFiles != null && backupProcessedFiles);
    }
    
    /**
     * Get the effective file pattern for monitoring.
     * @return file pattern or default wildcard
     */
    public String getEffectiveFilePattern() {
        return hasFilePattern() ? filePattern : "*";
    }
    
    // Interface implementations
    @Override
    public Set<CAttachment> getAttachments() { return attachments; }
    
    @Override
    public void setAttachments(Set<CAttachment> attachments) { this.attachments = attachments; }
    
    @Override
    public Set<CComment> getComments() { return comments; }
    
    @Override
    public void setComments(Set<CComment> comments) { this.comments = comments; }
    
    @Override
    public Set<CLink> getLinks() { return links; }
    
    @Override
    public void setLinks(Set<CLink> links) { this.links = links; }
    
    // IEntityRegistrable implementation
    @Override
    public Class<?> getServiceClass() { 
        return Object.class; 
    }
    
    @Override
    public Class<?> getPageServiceClass() { 
        return Object.class; 
    }
    
    // File input specific getters and setters
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { 
        this.filePath = filePath;
        updateLastModified();
    }
    
    public String getFileFormat() { return fileFormat; }
    public void setFileFormat(String fileFormat) { 
        this.fileFormat = fileFormat;
        updateLastModified();
    }
    
    public Boolean getWatchDirectory() { return watchDirectory; }
    public void setWatchDirectory(Boolean watchDirectory) { 
        this.watchDirectory = watchDirectory;
        updateLastModified();
    }
    
    public String getFilePattern() { return filePattern; }
    public void setFilePattern(String filePattern) { 
        this.filePattern = filePattern;
        updateLastModified();
    }
    
    public Integer getPollingIntervalSeconds() { return pollingIntervalSeconds; }
    public void setPollingIntervalSeconds(Integer pollingIntervalSeconds) { 
        this.pollingIntervalSeconds = pollingIntervalSeconds;
        updateLastModified();
    }
    
    public Boolean getAutoDeleteProcessed() { return autoDeleteProcessed; }
    public void setAutoDeleteProcessed(Boolean autoDeleteProcessed) { 
        this.autoDeleteProcessed = autoDeleteProcessed;
        updateLastModified();
    }
    
    public Boolean getBackupProcessedFiles() { return backupProcessedFiles; }
    public void setBackupProcessedFiles(Boolean backupProcessedFiles) { 
        this.backupProcessedFiles = backupProcessedFiles;
        updateLastModified();
    }
    
    public String getBackupDirectory() { return backupDirectory; }
    public void setBackupDirectory(String backupDirectory) { 
        this.backupDirectory = backupDirectory;
        updateLastModified();
    }
    
    public Integer getMaxFileSizeMb() { return maxFileSizeMb; }
    public void setMaxFileSizeMb(Integer maxFileSizeMb) { 
        this.maxFileSizeMb = maxFileSizeMb;
        updateLastModified();
    }
}