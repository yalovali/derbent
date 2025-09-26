package tech.derbent.decisions.service;

import tech.derbent.api.services.CEntityOfProjectRepository;
import tech.derbent.decisions.domain.CDecisionStatus;

/** CDecisionStatusRepository - Repository interface for CDecisionStatus entities. Layer: Data Access (MVC) Provides data access methods for decision
 * status entities. Since CDecisionStatus extends CStatus which extends CTypeEntity which extends CEntityOfProject, this repository must extend
 * CEntityOfProjectRepository to provide project-aware operations. */
public interface CDecisionStatusRepository extends CEntityOfProjectRepository<CDecisionStatus> {
}
