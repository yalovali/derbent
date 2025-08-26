package tech.derbent.abstracts.services;

import org.springframework.data.repository.NoRepositoryBean;
import tech.derbent.abstracts.domains.CEntityOfProject;

@NoRepositoryBean
public interface CProjectItemRespository<EntityClass extends CEntityOfProject<EntityClass>> extends CEntityOfProjectRepository<EntityClass> {
}
