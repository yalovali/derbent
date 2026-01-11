package tech.derbent.app.workflow.service;

import org.springframework.data.repository.NoRepositoryBean;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.workflow.domain.CWorkflowBase;

@NoRepositoryBean
public interface IWorkflowRepository<EntityClass extends CWorkflowBase<EntityClass>> extends IEntityOfCompanyRepository<EntityClass> {
}
