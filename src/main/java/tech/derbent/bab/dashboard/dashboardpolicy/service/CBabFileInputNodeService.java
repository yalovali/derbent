package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.node.domain.CBabFileInputNode;
import tech.derbent.base.session.service.ISessionService;

/** CFileInputNodeService - Service for File Input virtual network nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Concrete service with @Service annotation. Provides business logic for file input node management: - File path validation and conflict
 * detection - File format and pattern validation - Directory monitoring configuration - File size and polling interval validation - Backup and
 * cleanup configuration management - Calimero file integration */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabFileInputNodeService extends CBabNodeEntityService<CBabFileInputNode> implements IEntityRegistrable, IEntityWithView {

	private static final Pattern ABSOLUTE_PATH_PATTERN = Pattern.compile("^(/|[A-Z]:[\\\\/]).*");
	// File validation patterns
	private static final Pattern FILE_PATTERN_REGEX = Pattern.compile("^[*a-zA-Z0-9._-]+$");
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabFileInputNodeService.class);
	private static final int MAX_FILE_SIZE_MB = 10000; // 10GB
	private static final int MAX_POLLING_INTERVAL = 86400; // 24 hours
	// File size limits (in MB)
	private static final int MIN_FILE_SIZE_MB = 1;
	// Polling interval limits (in seconds)
	private static final int MIN_POLLING_INTERVAL = 1;
	// Valid file formats
	private static final String[] VALID_FILE_FORMATS = {
			"JSON", "XML", "CSV", "TXT", "BINARY", "LOG", "YAML", "INI", "PROPERTIES"
	};

	public CBabFileInputNodeService(final IFileInputNodeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void copyNodeSpecificFields(final CBabFileInputNode source, final CBabFileInputNode target, final CCloneOptions options) {
		// Copy file input specific fields
		// Make file path unique for copy
		if (source.getFilePath() != null) {
			target.setFilePath(source.getFilePath() + "_copy");
		}
		target.setFileFormat(source.getFileFormat());
		target.setWatchDirectory(source.getWatchDirectory());
		target.setFilePattern(source.getFilePattern());
		target.setPollingIntervalSeconds(source.getPollingIntervalSeconds());
		target.setAutoDeleteProcessed(source.getAutoDeleteProcessed());
		target.setBackupProcessedFiles(source.getBackupProcessedFiles());
		// Copy backup directory if set
		if (source.getBackupDirectory() != null) {
			target.setBackupDirectory(source.getBackupDirectory() + "_copy");
		}
		target.setMaxFileSizeMb(source.getMaxFileSizeMb());
		LOGGER.debug("Copied file input specific fields from '{}' to '{}'", source.getName(), target.getName());
	}

	/** Find file inputs by format. */
	@Transactional (readOnly = true)
	public List<CBabFileInputNode> findByFileFormat(final String fileFormat, final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notBlank(fileFormat, "File format cannot be blank");
		Check.notNull(project, "Project cannot be null");
		return ((IFileInputNodeRepository) repository).findByFileFormatAndProject(fileFormat, project);
	}

	/** Find directory watchers. */
	@Transactional (readOnly = true)
	public List<CBabFileInputNode> findDirectoryWatchers(final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IFileInputNodeRepository) repository).findDirectoryWatchers(project);
	}

	/** Find high-frequency polling file inputs. */
	@Transactional (readOnly = true)
	public List<CBabFileInputNode> findHighFrequencyPolling(final Integer thresholdSeconds,
			final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(thresholdSeconds, "Threshold cannot be null");
		Check.notNull(project, "Project cannot be null");
		return ((IFileInputNodeRepository) repository).findHighFrequencyPolling(thresholdSeconds, project);
	}

	/** Find file inputs with auto-delete enabled. */
	@Transactional (readOnly = true)
	public List<CBabFileInputNode> findWithAutoDelete(final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IFileInputNodeRepository) repository).findWithAutoDelete(project);
	}

	/** Find file inputs with patterns. */
	@Transactional (readOnly = true)
	public List<CBabFileInputNode> findWithFilePattern(final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IFileInputNodeRepository) repository).findWithFilePattern(project);
	}

	@Override
	protected String generateDefaultNodeConfiguration(final CBabFileInputNode entity) {
		return """
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
				        "maxFileSizeMb": %d,
				        "directoryWatcher": %s,
				        "hasFilePattern": %s,
				        "automaticCleanup": %s
				    },
				    "calimeroConfig": {
				        "enabled": true,
				        "exportFormat": "FILE_GATEWAY",
				        "monitoringEnabled": true,
				        "processingQueue": true
				    }
				}
				""".formatted(entity.getId() != null ? entity.getId().toString() : "new", entity.getPhysicalInterface() != null ? entity.getPhysicalInterface() : "file", entity.getIsActive() != null ? entity.getIsActive() : true, entity.getPriorityLevel() != null ? entity.getPriorityLevel() : 50,
				entity.getFilePath() != null ? entity.getFilePath() : "/var/data/input", entity.getFileFormat() != null ? entity.getFileFormat() : "JSON", entity.getWatchDirectory() != null ? entity.getWatchDirectory() : false, entity.getFilePattern() != null ? entity.getFilePattern() : "",
				entity.getPollingIntervalSeconds() != null ? entity.getPollingIntervalSeconds() : 60, entity.getAutoDeleteProcessed() != null ? entity.getAutoDeleteProcessed() : false, entity.getBackupProcessedFiles() != null ? entity.getBackupProcessedFiles() : true, entity.getBackupDirectory() != null ? entity.getBackupDirectory() : "",
				entity.getMaxFileSizeMb() != null ? entity.getMaxFileSizeMb() : 100, entity.isDirectoryWatcher(), entity.hasFilePattern(), entity.hasAutomaticCleanup());
	}

	@Override
	public Class<CBabFileInputNode> getEntityClass() { return CBabFileInputNode.class; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getInitializerServiceClass() {
		return Object.class; // Placeholder - will be updated in Phase 8
	}

	@Override
	public Class<?> getPageServiceClass() {
		return Object.class; // Placeholder - will be updated in Phase 8
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void initializeNodeSpecificDefaults(final CBabFileInputNode entity) {
		// File input specific initialization
		if (entity.getFilePath() == null || entity.getFilePath().isEmpty()) {
			entity.setFilePath("/var/data/input");
		}
		if (entity.getFileFormat() == null || entity.getFileFormat().isEmpty()) {
			entity.setFileFormat("JSON");
		}
		if (entity.getWatchDirectory() == null) {
			entity.setWatchDirectory(false);
		}
		if (entity.getPollingIntervalSeconds() == null) {
			entity.setPollingIntervalSeconds(60);
		}
		if (entity.getAutoDeleteProcessed() == null) {
			entity.setAutoDeleteProcessed(false);
		}
		if (entity.getBackupProcessedFiles() == null) {
			entity.setBackupProcessedFiles(true);
		}
		if (entity.getMaxFileSizeMb() == null) {
			entity.setMaxFileSizeMb(100);
		}
		// Set default backup directory if backup is enabled
		if (entity.getBackupProcessedFiles() && (entity.getBackupDirectory() == null || entity.getBackupDirectory().isEmpty())) {
			entity.setBackupDirectory("/var/data/backup");
		}
		// Set default physical interface for file inputs
		if (entity.getPhysicalInterface() == null || entity.getPhysicalInterface().isEmpty()) {
			entity.setPhysicalInterface("file");
		}
		LOGGER.debug("Initialized file input node '{}' monitoring path '{}' for {} files", entity.getName(), entity.getFilePath(),
				entity.getFileFormat());
	}

	/** Check if file path is already monitored. */
	@Transactional (readOnly = true)
	public boolean isFilePathMonitored(final String filePath, final tech.derbent.api.projects.domain.CProject<?> project, final Long excludeNodeId) {
		Check.notBlank(filePath, "File path cannot be blank");
		Check.notNull(project, "Project cannot be null");
		final boolean exists = ((IFileInputNodeRepository) repository).existsByFilePathAndProject(filePath, project);
		if (!exists) {
			return false;
		}
		// If excludeNodeId is provided, check if the existing node is the same
		if (excludeNodeId == null) {
			return true;
		}
		final var existing = ((IFileInputNodeRepository) repository).findByFilePathAndProject(filePath, project);
		return existing.isPresent() && !existing.get().getId().equals(excludeNodeId);
	}
	// File input specific business methods

	/** Update file monitoring configuration. */
	@Transactional
	public void updateMonitoringConfiguration(final CBabFileInputNode fileInput, final String filePath, final String fileFormat,
			final Integer pollingInterval, final Boolean watchDirectory, final String filePattern) {
		Check.notNull(fileInput, "File input cannot be null");
		// Update configuration
		if (filePath != null) {
			fileInput.setFilePath(filePath);
		}
		if (fileFormat != null) {
			fileInput.setFileFormat(fileFormat);
		}
		if (pollingInterval != null) {
			fileInput.setPollingIntervalSeconds(pollingInterval);
		}
		if (watchDirectory != null) {
			fileInput.setWatchDirectory(watchDirectory);
		}
		if (filePattern != null) {
			fileInput.setFilePattern(filePattern);
		}
		// Regenerate configuration JSON
		regenerateNodeConfiguration(fileInput);
		LOGGER.info("Updated monitoring configuration for file input '{}'", fileInput.getName());
	}

	/** Validate backup configuration consistency. */
	private void validateBackupConfiguration(final CBabFileInputNode entity) {
		final Boolean backupEnabled = entity.getBackupProcessedFiles();
		final String backupDirectory = entity.getBackupDirectory();
		if (backupEnabled != null && backupEnabled) {
			if (backupDirectory == null || backupDirectory.trim().isEmpty()) {
				throw new CValidationException("Backup directory is required when backup is enabled");
			}
			validateStringLength(backupDirectory, "Backup Directory", 500);
			// Validate backup directory path
			try {
				final Path backupPath = Paths.get(backupDirectory);
				if (!backupPath.isAbsolute()) {
					LOGGER.warn("Backup directory '{}' is not absolute. Consider using absolute paths.", backupDirectory);
				}
				// Check if backup directory is the same as source
				if (backupDirectory.equals(entity.getFilePath())) {
					throw new CValidationException("Backup directory cannot be the same as source file path");
				}
				final boolean condition = entity.getWatchDirectory() != null && entity.getWatchDirectory() && (backupDirectory.startsWith(entity.getFilePath() + "/") || backupDirectory.startsWith(entity.getFilePath() + "\\"));
				// Check if backup directory is a subdirectory of source (for directory watching)
				if (condition) {
					throw new CValidationException("Backup directory cannot be a subdirectory of watched directory");
				}
			} catch (final InvalidPathException e) {
				throw new CValidationException("Invalid backup directory '%s': %s".formatted(backupDirectory, e.getMessage()));
			}
		}
		// Validate cleanup configuration consistency
		final Boolean autoDelete = entity.getAutoDeleteProcessed();
		if (autoDelete != null && autoDelete && backupEnabled != null && backupEnabled) {
			LOGGER.warn("Both auto-delete and backup are enabled for '{}'. Files will be backed up before deletion.", entity.getName());
		}
	}

	/** Validate directory watching configuration. */
	private void validateDirectoryWatchingConfiguration(final CBabFileInputNode entity) {
		if (!(entity.getWatchDirectory() != null && entity.getWatchDirectory())) {
			return;
		}
		// When watching a directory, file pattern should be specified
		if (entity.getFilePattern() == null || entity.getFilePattern().trim().isEmpty()) {
			LOGGER.warn("Directory watching is enabled but no file pattern specified for '{}'. " + "All files in directory will be processed.",
					entity.getName());
		}
		// Directory watching typically requires higher polling intervals
		if (entity.getPollingIntervalSeconds() != null && entity.getPollingIntervalSeconds() < 5) {
			LOGGER.warn(
					"Very short polling interval ({} seconds) for directory watching on '{}'. "
							+ "Consider using at least 5 seconds to avoid performance issues.",
					entity.getPollingIntervalSeconds(), entity.getName());
		}
	}

	/** Validate file format. */
	private void validateFileFormat(final String fileFormat) {
		final String upperFormat = fileFormat.toUpperCase();
		for (final String validFormat : VALID_FILE_FORMATS) {
			if (validFormat.equals(upperFormat)) {
				return;
			}
		}
		LOGGER.warn("Unknown file format '{}'. Valid formats are: {}", fileFormat, String.join(", ", VALID_FILE_FORMATS));
	}

	/** Validate file path format and accessibility. */
	private void validateFilePath(final String filePath) {
		try {
			final Path path = Paths.get(filePath);
			// Check if path is absolute
			if (!ABSOLUTE_PATH_PATTERN.matcher(filePath).matches()) {
				LOGGER.warn("File path '{}' is not absolute. Relative paths may cause issues.", filePath);
			}
			// Check for invalid characters (basic validation)
			if (filePath.contains("..")) {
				throw new CValidationException("File path cannot contain '..' (directory traversal)");
			}
			// Check if parent directory exists (for file paths)
			final Path parentPath = path.getParent();
			if (parentPath != null && !Files.exists(parentPath)) {
				LOGGER.warn("Parent directory '{}' does not exist for file path '{}'", parentPath, filePath);
			}
		} catch (final InvalidPathException e) {
			throw new CValidationException("Invalid file path '%s': %s".formatted(filePath, e.getMessage()));
		}
	}

	/** Validate that file path is unique within the project. */
	private void validateFilePathUniqueness(final CBabFileInputNode entity) {
		final IFileInputNodeRepository fileRepo = (IFileInputNodeRepository) repository;
		final boolean filePathExists = fileRepo.existsByFilePathAndProject(entity.getFilePath(), entity.getProject());
		if (!filePathExists) {
			return;
		}
		// Check if it's the same entity (update scenario)
		final var existingFile = fileRepo.findByFilePathAndProject(entity.getFilePath(), entity.getProject());
		if (existingFile.isPresent() && (entity.getId() == null || !existingFile.get().getId().equals(entity.getId()))) {
			throw new CValidationException(
					"File path '%s' is already monitored by file input '%s'".formatted(entity.getFilePath(), existingFile.get().getName()));
		}
	}

	/** Validate file pattern for directory watching. */
	private void validateFilePattern(final String filePattern) {
		if (!FILE_PATTERN_REGEX.matcher(filePattern).matches()) {
			throw new CValidationException("File pattern can only contain alphanumeric characters, dots, hyphens, underscores, and asterisks");
		}
		// Check for common pattern errors
		if (filePattern.startsWith("*") && filePattern.length() == 1) {
			LOGGER.warn("Pattern '*' will match all files. Consider using a more specific pattern.");
		}
		if (!filePattern.contains("*") && !filePattern.contains("?")) {
			LOGGER.warn("Pattern '{}' contains no wildcards and will only match files with this exact name", filePattern);
		}
	}

	@Override
	protected void validateJsonConfiguration(final CBabFileInputNode entity, final String configJson) {
		// Basic JSON validation for file input configuration
		if (!configJson.trim().startsWith("{") || !configJson.trim().endsWith("}")) {
			throw new CValidationException("Configuration must be a valid JSON object");
		}
		// Validate that required fields are present in JSON
		if (!configJson.contains("\"nodeType\"") || !configJson.contains("\"fileConfig\"")) {
			throw new CValidationException("Configuration JSON must contain 'nodeType' and 'fileConfig' fields");
		}
		// Additional file-specific validation
		if (!configJson.contains("\"filePath\"") || !configJson.contains("\"fileFormat\"")) {
			throw new CValidationException("File configuration must contain 'filePath' and 'fileFormat' fields");
		}
	}

	@Override
	protected void validateNodeSpecificFields(final CBabFileInputNode entity) {
		// File input specific validation
		// File path validation
		if (entity.getFilePath() == null || entity.getFilePath().trim().isEmpty()) {
			throw new CValidationException("File path is required");
		}
		validateStringLength(entity.getFilePath(), "File Path", 500);
		validateFilePath(entity.getFilePath());
		// File format validation
		if (entity.getFileFormat() == null || entity.getFileFormat().trim().isEmpty()) {
			throw new CValidationException("File format is required");
		}
		validateStringLength(entity.getFileFormat(), "File Format", 20);
		validateFileFormat(entity.getFileFormat());
		// File pattern validation (if directory watching is enabled)
		if (entity.getWatchDirectory() != null && entity.getWatchDirectory() && entity.getFilePattern() != null
				&& !entity.getFilePattern().trim().isEmpty()) {
			validateStringLength(entity.getFilePattern(), "File Pattern", 100);
			validateFilePattern(entity.getFilePattern());
		}
		// Polling interval validation
		if (entity.getPollingIntervalSeconds() == null) {
			throw new CValidationException("Polling interval is required");
		}
		validateNumericField(entity.getPollingIntervalSeconds(), "Polling Interval", MAX_POLLING_INTERVAL);
		if (entity.getPollingIntervalSeconds() < MIN_POLLING_INTERVAL) {
			throw new CValidationException("Polling interval must be at least %d second".formatted(MIN_POLLING_INTERVAL));
		}
		if (entity.getPollingIntervalSeconds() > MAX_POLLING_INTERVAL) {
			throw new CValidationException("Polling interval cannot exceed %d seconds (24 hours)".formatted(MAX_POLLING_INTERVAL));
		}
		// File size validation
		if (entity.getMaxFileSizeMb() == null) {
			throw new CValidationException("Max file size is required");
		}
		validateNumericField(entity.getMaxFileSizeMb(), "Max File Size", MAX_FILE_SIZE_MB);
		if (entity.getMaxFileSizeMb() < MIN_FILE_SIZE_MB) {
			throw new CValidationException("Max file size must be at least %d MB".formatted(MIN_FILE_SIZE_MB));
		}
		if (entity.getMaxFileSizeMb() > MAX_FILE_SIZE_MB) {
			throw new CValidationException("Max file size cannot exceed %d MB".formatted(MAX_FILE_SIZE_MB));
		}
		// Backup configuration validation
		validateBackupConfiguration(entity);
		// File path uniqueness validation
		validateFilePathUniqueness(entity);
		// Directory watching validation
		validateDirectoryWatchingConfiguration(entity);
	}

	@Override
	protected void validatePhysicalInterfaceFormat(final CBabFileInputNode entity, final String physicalInterface) {
		// File inputs typically use "file" as physical interface
		if (!"file".equalsIgnoreCase(physicalInterface)) {
			LOGGER.warn("Physical interface '{}' is not typical for file input. Expected 'file' for node '{}'", physicalInterface, entity.getName());
		}
		// Call parent for basic validation
		super.validatePhysicalInterfaceFormat(entity, physicalInterface);
	}
}
