package tech.derbent.app.workflow.service;

import org.springframework.data.repository.NoRepositoryBean;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.workflow.domain.CWorkflowBase;

@NoRepositoryBean
public interface IWorkflowRepository<EntityClass extends CWorkflowBase<EntityClass>> extends IEntityOfProjectRepository<EntityClass> {/**/
}
