package tech.derbent.bab.policybase.node.ip;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.node.service.CBabNodeService;
import tech.derbent.base.session.service.ISessionService;

/**
 * CBabHttpServerNodeService - Service for HTTP Server virtual network nodes.
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Entity service extending common node base service.
 * 
 * Provides HTTP-specific business logic:
 * - HTTP/HTTPS protocol validation
 * - Port uniqueness validation
 * - Endpoint path validation
 * - SSL configuration validation
 */
@Service
@Profile("bab")
@PreAuthorize("isAuthenticated()")
public class CBabHttpServerNodeService extends CBabNodeService<CBabHttpServerNode> 
		implements IEntityRegistrable, IEntityWithView {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabHttpServerNodeService.class);
	
	public CBabHttpServerNodeService(
			final IHttpServerNodeRepository repository,
			final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}
	
	@Override
	public Class<CBabHttpServerNode> getEntityClass() {
		return CBabHttpServerNode.class;
	}
	
	@Override
	protected void validateEntity(final CBabHttpServerNode entity) {
		super.validateEntity(entity);  // ✅ Common node validation (name, interface, uniqueness)
		
		LOGGER.debug("Validating HTTP Server specific fields: {}", entity.getName());
		
		// HTTP-specific validation
		if (entity.getServerPort() == null) {
			throw new IllegalArgumentException("Server Port is required");
		}
		validateNumericField(entity.getServerPort(), "Server Port", 65535);
		if (entity.getServerPort() < 1) {
			throw new IllegalArgumentException("Server Port must be between 1 and 65535");
		}
		
		// Endpoint path validation
		Check.notBlank(entity.getEndpointPath(), "Endpoint Path is required");
		if (!entity.getEndpointPath().startsWith("/")) {
			throw new IllegalArgumentException("Endpoint Path must start with /");
		}
		
		// Protocol validation (Yoda conditions for null safety)
		Check.notBlank(entity.getProtocol(), "Protocol is required");
		if (!"HTTP".equals(entity.getProtocol()) && !"HTTPS".equals(entity.getProtocol())) {
			throw new IllegalArgumentException("Protocol must be HTTP or HTTPS");
		}
		
		// SSL consistency check
		if ("HTTPS".equals(entity.getProtocol()) && !entity.getSslEnabled()) {
			LOGGER.warn("Protocol is HTTPS but SSL is not enabled for node: {}", entity.getName());
		}
		
		// Unique port per project check
		final IHttpServerNodeRepository repo = (IHttpServerNodeRepository) repository;
		final var existingPort = repo.findByServerPortAndProject(entity.getServerPort(), entity.getProject());
		if (existingPort.isPresent() && !existingPort.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(
				"Server port %d is already used by another HTTP server node in this project"
					.formatted(entity.getServerPort()));
		}
		
		LOGGER.debug("HTTP Server node validation passed: {}", entity.getName());
	}
	
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		// HTTP-specific initialization if needed
	}
	
	// IEntityRegistrable implementation
	
	@Override
	public Class<?> getServiceClass() {
		return CBabHttpServerNodeService.class;
	}
	
	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceHttpServerNode.class;
	}
	
	@Override
	public Class<?> getInitializerServiceClass() {
		return CBabHttpServerNodeInitializerService.class;
	}
}
