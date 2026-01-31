package tech.derbent.bab.node.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.bab.node.domain.CBabNodeROS;
import tech.derbent.base.session.service.ISessionService;

/** Service class for CBabNodeROS entity. Provides business logic for ROS communication node management. Following Derbent pattern: Concrete service
 * with @Service and interfaces. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabNodeROSService extends CBabNodeService<CBabNodeROS> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeROSService.class);

	public CBabNodeROSService(final IBabNodeROSRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CBabNodeROS entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// Add ROS-specific deletion checks here if needed
		return null;
	}

	@Override
	public Class<CBabNodeROS> getEntityClass() { return CBabNodeROS.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CBabNodeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceBabNodeROS.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CBabNodeROS entity) {
		super.validateEntity(entity);
		// ROS-specific validation
		if (entity.getRosMasterUri() != null && !entity.getRosMasterUri().startsWith("http://")) {
			throw new IllegalArgumentException("ROS Master URI must start with http://");
		}
		if (entity.getNamespace() != null && !entity.getNamespace().startsWith("/")) {
			throw new IllegalArgumentException("ROS namespace must start with /");
		}
	}
}
