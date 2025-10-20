package tech.derbent.app.decisions.service;

import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.app.decisions.domain.CDecisionStatus;

/** CDecisionStatusRepository - Repository interface for CDecisionStatus entities. Layer: Data Access (MVC) Provides data access methods for decision
 * status entities. Since CDecisionStatus extends CStatus which extends CTypeEntity which extends CEntityOfProject, this repository must extend
 * CEntityOfProjectRepository to provide project-aware operations. */
public interface IDecisionStatusRepository extends IEntityOfProjectRepository<CDecisionStatus> {
}
