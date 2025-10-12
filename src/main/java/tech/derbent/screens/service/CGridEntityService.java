package tech.derbent.screens.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.services.CEntityOfProjectService;
import tech.derbent.api.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CGridEntityService extends CEntityOfProjectService<CGridEntity> {

	public CGridEntityService(final IGridEntityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CGridEntity> findByNameAndProject(final String name, final CProject project) {
		Check.notBlank(name, "Name must not be blank");
		Check.notNull(project, "Project must not be null");
		if ((project == null) || (name == null) || name.isBlank()) {
			return Optional.empty();
		}
		return ((IGridEntityRepository) repository).findByNameAndProject(project, name);
	}

	public List<String> getAvailableTypes() {
		return List.of("Grid Chart", "Gannt", "None"); // Replace with actual types
	}

	@Override
	protected Class<CGridEntity> getEntityClass() { return CGridEntity.class; }

	@Override
	public String checkDependencies(final CGridEntity entity) {
		final String superCheck = super.checkDependencies(entity);
		if (superCheck != null) {
			return superCheck;
		}
		return null;
	}

	@Override
	public void initializeNewEntity(final CGridEntity entity) {
		super.initializeNewEntity(entity);
		tech.derbent.api.utils.Check.notNull(entity, "Entity cannot be null");
		// Stub for future implementation
	}

	public List<CGridEntity> listForComboboxSelectorByProjectId(final String projectId) {
		Check.notBlank(projectId, "Project must not be null");
		Long id = Long.valueOf(projectId);
		return ((IGridEntityRepository) repository).listByProjectId(id);
	}
}
