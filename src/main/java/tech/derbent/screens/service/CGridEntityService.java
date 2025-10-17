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
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
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
	public String checkDeleteAllowed(final CGridEntity entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public void initializeNewEntity(final CGridEntity entity) {
		super.initializeNewEntity(entity);
		// Additional entity-specific initialization can be added here if needed
	}

	public List<CGridEntity> listForComboboxSelectorByProjectId(final String projectId) {
		Check.notBlank(projectId, "Project must not be null");
		Long id = Long.valueOf(projectId);
		return ((IGridEntityRepository) repository).listByProjectId(id);
	}

	public List<String> getFieldNames(final CGridEntity entity) {
		Check.notNull(entity, "Grid Entity must not be null");
		LOGGER.debug("Getting field names for entity: {}", entity.getName());
		String beanName = entity.getDataServiceBeanName();
		String entityType = CEntityFieldService.extractEntityTypeFromBeanName(beanName);
		Check.notNull(entityType, "Extracted entity type cannot be null");
		List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType);
		return allFields.stream().map(EntityFieldInfo::getFieldName).toList();
	}
}
