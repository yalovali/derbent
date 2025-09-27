package tech.derbent.api.services;

import org.springframework.data.repository.NoRepositoryBean;
import tech.derbent.api.domains.CEntityOfProject;

@NoRepositoryBean
public interface IProjectItemRespository<EntityClass extends CEntityOfProject<EntityClass>> extends IEntityOfProjectRepository<EntityClass> {
}
