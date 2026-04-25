package tech.derbent.plm.sprints.planning.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.plm.gnnt.gnntviewentity.domain.EGnntGridType;
import tech.derbent.plm.sprints.planning.domain.CSprintPlanningViewEntity;

@Service
@Profile({
		"derbent", "default"
})
@PreAuthorize("isAuthenticated()")
public class CSprintPlanningViewEntityService extends CEntityOfProjectService<CSprintPlanningViewEntity> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintPlanningViewEntityService.class);

	public CSprintPlanningViewEntityService(final ISprintPlanningViewEntityRepository repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/**
	 * Copies sprint planning view specific fields during clone/copy operations.
	 *
	 * @param source  source entity
	 * @param target  target entity
	 * @param options clone options
	 */
	@Override
	public void copyEntityFieldsTo(final CSprintPlanningViewEntity source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		if (!(target instanceof CSprintPlanningViewEntity)) {
			return;
		}
		final CSprintPlanningViewEntity targetEntity = (CSprintPlanningViewEntity) target;
		targetEntity.setBacklogGridType(source.getBacklogGridType());
		LOGGER.debug("Copied CSprintPlanningViewEntity '{}' with options: {}", source.getName(), options);
	}

	@Override
	public Class<CSprintPlanningViewEntity> getEntityClass() {
		return CSprintPlanningViewEntity.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CSprintPlanningViewEntityInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceSprintPlanningViewEntity.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return getClass();
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (entity instanceof CSprintPlanningViewEntity planningView) {
			// Default to TREE so planning keeps context while filtering (Scrum backlog typically has hierarchy).
			planningView.setBacklogGridType(EGnntGridType.TREE);
		}
	}

	@Override
	protected void validateEntity(final CSprintPlanningViewEntity entity) {
		super.validateEntity(entity);
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		entity.setBacklogGridType(entity.getBacklogGridType());
		validateUniqueNameInProject((ISprintPlanningViewEntityRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
