package tech.derbent.screens.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CDetailSectionService extends CEntityOfProjectService<CDetailSection> {

	public CDetailSectionService(final IDetailSectionRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Transactional (readOnly = true)
	public List<CDetailSection> findActiveByProject(final CProject project) {
		return ((IDetailSectionRepository) repository).findActiveByProject(project);
	}

	@Transactional (readOnly = true)
	public CDetailSection findByEntityTypeAndProject(final String entityType, final CProject project) {
		Check.notBlank(entityType, "Entity type must not be blank");
		Check.notNull(project, "Project must not be null");
		if ((project == null) || (entityType == null) || entityType.isBlank()) {
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
	public CDetailSection findByNameAndProject(final CProject project, final String name) {
		Check.notNull(project, "Project must not be null");
		Check.notBlank(name, "Name must not be blank");
		if ((project == null) || (name == null) || name.isBlank()) {
			return null;
		}
		return ((IDetailSectionRepository) repository).findByNameAndProject(project, name).orElse(null);
	}

	@Override
	protected Class<CDetailSection> getEntityClass() { return CDetailSection.class; }
}
