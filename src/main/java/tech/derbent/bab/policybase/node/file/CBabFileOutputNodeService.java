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

/** CBabFileOutputNodeService - Service for File Output virtual network nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following
 * Derbent pattern: Entity service extending common node base service. Provides File Output-specific business logic: - File path uniqueness validation
 * - File format validation - Output size validation */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabFileOutputNodeService extends CBabNodeService<CBabFileOutputNode> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabFileOutputNodeService.class);

	public CBabFileOutputNodeService(final IFileOutputNodeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CBabFileOutputNode> getEntityClass() { return CBabFileOutputNode.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabFileOutputNodeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceFileOutputNode.class; }

	@Override
	public Class<?> getServiceClass() { return CBabFileOutputNodeService.class; }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// File Output-specific initialization if needed
	}

	@Override
	protected void validateEntity(final CBabFileOutputNode entity) {
		super.validateEntity(entity); // Common node validation (name, interface, uniqueness)
		LOGGER.debug("Validating File Output specific fields: {}", entity.getName());
		// File Output-specific validation
		Check.notBlank(entity.getFilePath(), "File Path is required");
		validateStringLength(entity.getFilePath(), "File Path", 500);
		// Unique file path per project
		final IFileOutputNodeRepository repo = (IFileOutputNodeRepository) repository;
		final var existingPath = repo.findByFilePathAndProject(entity.getFilePath(), entity.getProject());
		if (existingPath.isPresent() && !existingPath.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(
					"File path '%s' is already used by another file output node in this project".formatted(entity.getFilePath()));
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
		LOGGER.debug("File Output node validation passed: {}", entity.getName());
	}
}
