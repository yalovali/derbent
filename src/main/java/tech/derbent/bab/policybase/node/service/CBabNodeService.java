package tech.derbent.bab.policybase.node.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** CBabNodeService - Abstract base service for BAB virtual network nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Abstract service with common validation logic. Provides shared validation and business logic for all node types: - Name validation and
 * uniqueness checking - Physical interface validation - Common node field validation Concrete node services extend this and add type-specific
 * validation. */
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public abstract class CBabNodeService<NodeType extends CBabNodeEntity<NodeType>> extends CEntityOfProjectService<NodeType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeService.class);

	protected CBabNodeService(final INodeEntityRepository<NodeType> repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	/** Validate common node fields (name, physical interface). Subclasses MUST call super.validateEntity() first, then add type-specific
	 * validation. */
	@Override
	protected void validateEntity(final NodeType entity) {
		super.validateEntity(entity);
		LOGGER.debug("Validating node common fields: {}", entity.getName());
		// Name validation (MANDATORY for business entities)
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		// Unique name in project - delegated to concrete service (each has own repository interface)
		validateUniqueNameInProject((INodeEntityRepository<NodeType>) repository, entity, entity.getName(), entity.getProject());
		// Physical interface validation
		Check.notBlank(entity.getPhysicalInterface(), "Physical Interface is required");
		validateStringLength(entity.getPhysicalInterface(), "Physical Interface", 100);
		LOGGER.debug("Node common validation passed: {}", entity.getName());
	}
}
