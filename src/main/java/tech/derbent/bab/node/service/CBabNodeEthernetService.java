package tech.derbent.bab.node.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.bab.node.domain.CBabNodeEthernet;
import tech.derbent.api.session.service.ISessionService;

/** Service class for CBabNodeEthernet entity. Provides business logic for Ethernet communication node management. Following Derbent pattern: Concrete
 * service with @Service and interfaces. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabNodeEthernetService extends CBabNodeService<CBabNodeEthernet> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeEthernetService.class);

	public CBabNodeEthernetService(final IBabNodeEthernetRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CBabNodeEthernet entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// Add Ethernet-specific deletion checks here if needed
		return null;
	}

	@Override
	public Class<CBabNodeEthernet> getEntityClass() { return CBabNodeEthernet.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabNodeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabNodeEthernet.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CBabNodeEthernet entity) {
		super.validateEntity(entity);
		// Ethernet-specific validation
		if (!entity.getDhcpEnabled() && entity.getIpAddress() == null) {
			throw new IllegalArgumentException("IP address is required when DHCP is disabled");
		}
		// Basic IP address format validation (simple check)
		if (entity.getIpAddress() != null && !entity.getIpAddress().matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
			throw new IllegalArgumentException("Invalid IP address format");
		}
	}
}
