package tech.derbent.api.entityOfProject.service;

import org.springframework.data.repository.NoRepositoryBean;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;

@NoRepositoryBean
public interface IProjectItemRespository<EntityClass extends CEntityOfProject<EntityClass>> extends IEntityOfProjectRepository<EntityClass> {
	/**/
}
