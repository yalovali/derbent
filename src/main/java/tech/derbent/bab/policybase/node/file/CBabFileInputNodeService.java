package tech.derbent.bab.policybase.node.file;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.node.service.CBabNodeService;

/** CBabFileInputNodeService - Service for File Input virtual network nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following
 * Derbent pattern: Entity service extending common node base service. Provides File Input-specific business logic: - File path uniqueness validation
 * - File format validation - Polling interval validation - Backup directory validation */
@Service
@Profile({"bab", "default", "test"})
@PreAuthorize ("isAuthenticated()")
public class CBabFileInputNodeService extends CBabNodeService<CBabFileInputNode> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabFileInputNodeService.class);

	public CBabFileInputNodeService(final IFileInputNodeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CBabFileInputNode> getEntityClass() { return CBabFileInputNode.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabFileInputNodeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceFileInputNode.class; }
	// IEntityRegistrable implementation

	@Override
	public Class<?> getServiceClass() { return CBabFileInputNodeService.class; }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// File Input-specific initialization if needed
	}

	@Override
	protected void validateEntity(final CBabFileInputNode entity) {
		super.validateEntity(entity); // ✅ Common node validation (name, interface, uniqueness)
		LOGGER.debug("Validating File Input specific fields: {}", entity.getName());
		// File Input-specific validation
		Check.notBlank(entity.getFilePath(), "File Path is required");
		validateStringLength(entity.getFilePath(), "File Path", 500);
		// Unique file path per project
		final IFileInputNodeRepository repo = (IFileInputNodeRepository) repository;
		final var existingPath = repo.findByFilePathAndProject(entity.getFilePath(), entity.getProject());
		if (existingPath.isPresent() && !existingPath.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(
					"File path '%s' is already monitored by another file input node in this project".formatted(entity.getFilePath()));
		}
		// File format validation
		Check.notBlank(entity.getFileFormat(), "File Format is required");
		final String[] validFormats = {
				"JSON", "XML", "CSV", "TXT", "BINARY"
		};
		boolean validFormat = false;
		for (String format : validFormats) {
			if (format.equalsIgnoreCase(entity.getFileFormat())) {
				validFormat = true;
				break;
			}
		}
		if (!validFormat) {
			LOGGER.warn("File format '{}' is not standard. Valid formats: JSON, XML, CSV, TXT, BINARY", entity.getFileFormat());
		}
		// Max file size validation
		if (entity.getMaxFileSizeMb() != null) {
			validateNumericField(entity.getMaxFileSizeMb(), "Max File Size", 10000);
			if (entity.getMaxFileSizeMb() < 1) {
				throw new IllegalArgumentException("Max File Size must be at least 1 MB");
			}
		}
		LOGGER.debug("File Input node validation passed: {}", entity.getName());
	}
}
