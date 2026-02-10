package tech.derbent.bab.policybase.node.ip;

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
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.node.service.CBabNodeService;
import tech.derbent.base.session.service.ISessionService;

/**
 * CBabSyslogNodeService - Service for Syslog server virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Entity service extending common node base service.
 * 
 * Provides Syslog-specific business logic:
 * - Port validation
 * - Protocol validation (UDP/TCP)
 * - Facility and severity validation
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabSyslogNodeService extends CBabNodeService<CBabSyslogNode> 
		implements IEntityRegistrable, IEntityWithView {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabSyslogNodeService.class);
	
	public CBabSyslogNodeService(
			final ISyslogNodeRepository repository,
			final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}
	
	@Override
	public Class<CBabSyslogNode> getEntityClass() {
		return CBabSyslogNode.class;
	}
	
	@Override
	protected void validateEntity(final CBabSyslogNode entity) {
		super.validateEntity(entity);  // ✅ Common node validation (name, interface, uniqueness)
		
		LOGGER.debug("Validating Syslog specific fields: {}", entity.getName());
		
		// Syslog-specific validation
		if (entity.getListenPort() == null) {
			throw new IllegalArgumentException("Listen Port is required");
		}
		validateNumericField(entity.getListenPort(), "Listen Port", 65535);
		if (entity.getListenPort() < 1) {
			throw new IllegalArgumentException("Listen Port must be between 1 and 65535");
		}
		
		// Protocol validation (Yoda conditions for null safety)
		Check.notBlank(entity.getProtocol(), "Protocol is required");
		if (!"UDP".equals(entity.getProtocol()) && !"TCP".equals(entity.getProtocol())) {
			throw new IllegalArgumentException("Protocol must be UDP or TCP");
		}
		
		// Facility validation
		Check.notBlank(entity.getFacility(), "Facility is required");
		
		// Severity level validation
		Check.notBlank(entity.getSeverityLevel(), "Severity Level is required");
		
		// Log file path validation
		Check.notBlank(entity.getLogFilePath(), "Log File Path is required");
		if (!entity.getLogFilePath().startsWith("/")) {
			throw new IllegalArgumentException("Log File Path must be absolute (start with /)");
		}
		
		// Max message size validation
		if (entity.getMaxMessageSize() != null) {
			validateNumericField(entity.getMaxMessageSize(), "Max Message Size", 65535);
			if (entity.getMaxMessageSize() < 480) {
				throw new IllegalArgumentException("Max Message Size must be at least 480 bytes");
			}
		}
		
		// TLS consistency check
		if ("UDP".equals(entity.getProtocol()) && entity.getEnableTls() != null && entity.getEnableTls()) {
			LOGGER.warn("TLS is enabled but protocol is UDP for node: {}", entity.getName());
		}
		
		// Unique port per interface per project check
		final ISyslogNodeRepository repo = (ISyslogNodeRepository) repository;
		final var existingPort = repo.findByListenPortAndInterfaceAndProject(
			entity.getListenPort(), 
			entity.getPhysicalInterface(), 
			entity.getProject());
		if (existingPort.isPresent() && !existingPort.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(
				"Listen port %d is already used by another Syslog node on interface %s in this project"
					.formatted(entity.getListenPort(), entity.getPhysicalInterface()));
		}
		
		LOGGER.debug("Syslog node validation passed: {}", entity.getName());
	}
	
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// Syslog-specific initialization if needed
	}
	
	// IEntityRegistrable implementation
	
	@Override
	public Class<?> getServiceClass() {
		return CBabSyslogNodeService.class;
	}
	
	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceSyslogNode.class;
	}
	
	@Override
	public Class<?> getInitializerServiceClass() {
		return CBabSyslogNodeInitializerService.class;
	}
	
	/**
	 * Copy entity-specific fields from source to target.
	 * MANDATORY: All entity services must implement this method.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CBabSyslogNode source, final CEntityDB<?> target,
			final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		
		// STEP 2: Type-check target
		if (!(target instanceof CBabSyslogNode)) {
			return;
		}
		final CBabSyslogNode targetNode = (CBabSyslogNode) target;
		
		// STEP 3: Copy Syslog-specific fields using DIRECT setter/getter
		targetNode.setListenPort(source.getListenPort());
		targetNode.setProtocol(source.getProtocol());
		targetNode.setFacility(source.getFacility());
		targetNode.setSeverityLevel(source.getSeverityLevel());
		targetNode.setLogFilePath(source.getLogFilePath());
		targetNode.setMaxMessageSize(source.getMaxMessageSize());
		targetNode.setEnableTls(source.getEnableTls());
		
		// STEP 4: Log completion
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}
}
