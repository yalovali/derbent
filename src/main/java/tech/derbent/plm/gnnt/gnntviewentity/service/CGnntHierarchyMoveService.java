package tech.derbent.plm.gnnt.gnntviewentity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.ProxyUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.parentrelation.service.CHierarchyNavigationService;
import tech.derbent.api.parentrelation.service.CParentRelationService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.Check;

/**
 * Shared Gnnt hierarchy move service.
 *
 * <p>The Gnnt board only supports drop-on-parent reparenting for now, so this service keeps the move
 * logic aligned with the same generic hierarchy validation used everywhere else.</p>
 */
@Service
@PreAuthorize("isAuthenticated()")
public class CGnntHierarchyMoveService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGnntHierarchyMoveService.class);

	private final CHierarchyNavigationService hierarchyNavigationService;
	private final CParentRelationService parentRelationService;

	public CGnntHierarchyMoveService(final CHierarchyNavigationService hierarchyNavigationService,
			final CParentRelationService parentRelationService) {
		this.hierarchyNavigationService = hierarchyNavigationService;
		this.parentRelationService = parentRelationService;
	}

	@Transactional
	public void reparentItem(final CProjectItem<?> child, final CProjectItem<?> parent) {
		Check.notNull(child, "Dragged Gnnt item cannot be null");
		Check.notNull(parent, "Drop target Gnnt item cannot be null");
		Check.notNull(child.getId(), "Dragged Gnnt item must be persisted");
		Check.notNull(parent.getId(), "Drop target Gnnt item must be persisted");
		Check.isTrue(!CHierarchyNavigationService.isSameEntity(child, parent), "An item cannot be dropped onto itself");
		Check.isTrue(hierarchyNavigationService.isValidParentCandidate(child, parent),
				"Drop target '%s' is not compatible with '%s' for the current hierarchy."
						.formatted(parent.getName(), child.getName()));

		parentRelationService.setParent(child, parent);
		resolveService(child).save(child);
		LOGGER.info("Reparented Gnnt item '{}' under '{}'", child.getName(), parent.getName());
	}

	@SuppressWarnings({
			"rawtypes", "unchecked"
	})
	private CAbstractService resolveService(final CProjectItem<?> entity) {
		final Class<?> entityClass = ProxyUtils.getUserClass(entity.getClass());
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
		Check.notNull(serviceClass, "No service registered for hierarchy item type: " + entityClass.getSimpleName());
		final Object serviceBean = CSpringContext.getBean(serviceClass);
		Check.isTrue(serviceBean instanceof CAbstractService<?>,
				"Registered service does not extend CAbstractService: " + serviceClass.getSimpleName());
		return (CAbstractService) serviceBean;
	}
}
