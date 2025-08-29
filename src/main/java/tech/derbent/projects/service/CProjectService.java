package tech.derbent.projects.service;

import java.time.Clock;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.events.ProjectListChangeEvent;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CProjectService extends CAbstractNamedEntityService<CProject> {
	private final ApplicationEventPublisher eventPublisher;

	public CProjectService(final CProjectRepository repository, final Clock clock, final CSessionService sessionService,
			final ApplicationEventPublisher eventPublisher) {
		super(repository, clock, sessionService);
		this.eventPublisher = eventPublisher;
	}

	@Override
	@Transactional
	public void delete(final CProject entity) {
		super.delete(entity);
		// Publish project list change event after deletion
		eventPublisher.publishEvent(new ProjectListChangeEvent(this, entity, ProjectListChangeEvent.ChangeType.DELETED));
	}

	@Override
	@PreAuthorize ("permitAll()")
	public List<CProject> findAll() {
		return repository.findAll();
	}

	/** Gets a project with all its user settings loaded (to avoid lazy loading issues) */
	@Transactional (readOnly = true)
	public CProject findByIdWithUserSettings(final Long projectId) {
		return ((CProjectRepository) repository).findByIdWithUserSettings(projectId).orElse(null);
	}

	@Override
	protected Class<CProject> getEntityClass() { // TODO Auto-generated method stub
		return CProject.class;
	}

	@PreAuthorize ("permitAll()")
	public long getTotalProjectCount() { return repository.count(); }

	@Override
	@Transactional
	public CProject save(final CProject entity) {
		LOGGER.debug("save called with entity: {}", entity);
		final boolean isNew = entity.getId() == null;
		final CProject savedEntity = super.save(entity);
		// Publish project list change event after saving
		final ProjectListChangeEvent.ChangeType changeType =
				isNew ? ProjectListChangeEvent.ChangeType.CREATED : ProjectListChangeEvent.ChangeType.UPDATED;
		eventPublisher.publishEvent(new ProjectListChangeEvent(this, savedEntity, changeType));
		return savedEntity;
	}
}
