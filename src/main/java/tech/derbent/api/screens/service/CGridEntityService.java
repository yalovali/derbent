package tech.derbent.api.screens.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.Check;
import tech.derbent.api.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CGridEntityService extends CEntityOfProjectService<CGridEntity> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGridEntityService.class);

	public static List<String> getComboValuesOfType() {
		return List.of("Grid Chart", "Gannt", "None"); // Replace with actual types
	}

	public static List<String> getComboValuesOfFieldName(final CGridEntity entity) {
		Check.notNull(entity, "Grid Entity must not be null");
		LOGGER.debug("Getting field names for entity: {}", entity.getName());
		final String beanName = entity.getDataServiceBeanName();
		if (beanName == null || beanName.isBlank()) {
			LOGGER.warn("Grid entity {} has no data service bean selected yet", entity.getName());
			return List.of();
		}
		final String entityType = CEntityFieldService.extractEntityTypeFromBeanName(beanName);
		if (entityType == null || entityType.isBlank()) {
			LOGGER.warn("Unable to resolve entity type from bean name {}", beanName);
			return List.of();
		}
		Check.notNull(entityType, "Extracted entity type cannot be null");
		final List<EntityFieldInfo> allFields = CEntityFieldService.getEntityFields(entityType);
		return allFields.stream().map(EntityFieldInfo::getFieldName).toList();
	}

	public CGridEntityService(final IGridEntityRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CGridEntity entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<CGridEntity> findByNameAndProject(final String name, final CProject<?> project) {
		Check.notBlank(name, "Name must not be blank");
		Check.notNull(project, "Project must not be null");
		if (name == null || name.isBlank()) {
			return Optional.empty();
		}
		return ((IGridEntityRepository) repository).findByNameAndProject(project, name);
	}

	@Override
	public Class<CGridEntity> getEntityClass() { return CGridEntity.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CGridEntityInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceGridEntity.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (((CGridEntity) entity).getDataServiceBeanName() == null || ((CGridEntity) entity).getDataServiceBeanName().isBlank()) {
			final List<String> availableBeans = CViewsService.getComboValuesOfBean();
			String defaultBean = null;
			for (final String bean : availableBeans) {
				if ("CGridEntityService".equals(bean)) {
					defaultBean = bean;
					break;
				}
			}
			if (defaultBean == null) {
				for (final String bean : availableBeans) {
					if (bean != null && !bean.isBlank()) {
						defaultBean = bean;
						break;
					}
				}
			}
			Check.notBlank(defaultBean, "Data Service Bean default could not be resolved");
			((CGridEntity) entity).setDataServiceBeanName(defaultBean);
		}
	}

	public List<CGridEntity> getComboValuesOfGridEntityForProject(final Optional<CProject<?>> project) {
		// LOGGER.debug("Listing Grid Entities for ComboBox selector by project: {}", project);
		final Long id = project.map(CProject::getId).orElseThrow(() -> new IllegalArgumentException("Project must be provided"));
		return ((IGridEntityRepository) repository).listByProjectId(id);
	}
}
