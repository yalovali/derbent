package tech.derbent.api.screens.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.utils.Check;
import tech.derbent.api.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CDetailSectionService extends CEntityOfProjectService<CDetailSection> {

	public CDetailSectionService(final IDetailSectionRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CDetailSection entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Transactional (readOnly = true)
	public List<CDetailSection> findActiveByProject(final CProject<?> project) {
		return ((IDetailSectionRepository) repository).findActiveByProject(project);
	}

	@Transactional (readOnly = true)
	public CDetailSection findByEntityTypeAndProject(final String entityType, final CProject<?> project) {
		Check.notBlank(entityType, "Entity type must not be blank");
		Check.notNull(project, "Project must not be null");
		if (entityType == null || entityType.isBlank()) {
			return null;
		}
		return ((IDetailSectionRepository) repository).findByEntityTypeAndProject(project, entityType).orElse(null);
	}

	@Transactional (readOnly = true)
	public CDetailSection findByIdWithScreenLines(final Long id) {
		Check.notNull(id, "ID must not be null");
		return ((IDetailSectionRepository) repository).findByIdWithScreenLines(id).orElse(null);
	}

	@Transactional (readOnly = true)
	public CDetailSection findByNameAndProject(final CProject<?> project, final String name) {
		Check.notNull(project, "Project must not be null");
		Check.notBlank(name, "Name must not be blank");
		if (name == null || name.isBlank()) {
			return null;
		}
		return ((IDetailSectionRepository) repository).findByNameAndProject(project, name).orElse(null);
	}

	@Override
	protected Class<CDetailSection> getEntityClass() { return CDetailSection.class; }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}
}
