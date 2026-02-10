package tech.derbent.bab.policybase.node.can;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.bab.policybase.node.service.CBabNodeService;
import tech.derbent.base.session.service.ISessionService;

/** CBabCanNodeService - Service for CAN Bus virtual network nodes. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Entity service extending common node base service. Provides CAN-specific business logic: - Bitrate validation - CAN configuration
 * validation - Interface uniqueness validation */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabCanNodeService extends CBabNodeService<CBabCanNode> implements IEntityRegistrable, IEntityWithView {
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabCanNodeService.class);

	public CBabCanNodeService(final ICanNodeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Copy entity-specific fields from source to target. MANDATORY: All entity services must implement this method.
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final CBabCanNode source, final CEntityDB<?> target, final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		// STEP 2: Type-check target
		if (!(target instanceof CBabCanNode)) {
			return;
		}
		final CBabCanNode targetNode = (CBabCanNode) target;
		// STEP 3: Copy CAN-specific fields using DIRECT setter/getter
		targetNode.setBitrate(source.getBitrate());
		targetNode.setErrorWarningLimit(source.getErrorWarningLimit());
		// STEP 4: Log completion
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	public Class<CBabCanNode> getEntityClass() { return CBabCanNode.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabCanNodeInitializerService.class; }
	// IEntityRegistrable implementation

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceCanNode.class; }

	@Override
	public Class<?> getServiceClass() { return CBabCanNodeService.class; }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// CAN-specific initialization if needed
	}

	@Override
	protected void validateEntity(final CBabCanNode entity) {
		super.validateEntity(entity); // ✅ Common node validation (name, interface, uniqueness)
		LOGGER.debug("Validating CAN Bus specific fields: {}", entity.getName());
		// CAN-specific validation
		if (entity.getBitrate() == null) {
			throw new IllegalArgumentException("Bitrate is required");
		}
		validateNumericField(entity.getBitrate(), "Bitrate", 1000000);
		if (entity.getBitrate() < 10000) {
			throw new IllegalArgumentException("Bitrate must be at least 10000 bps");
		}
		// Error warning limit validation
		if (entity.getErrorWarningLimit() != null) {
			validateNumericField(entity.getErrorWarningLimit(), "Error Warning Limit", 255);
		}
		LOGGER.debug("CAN Bus node validation passed: {}", entity.getName());
	}
}
