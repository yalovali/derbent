package tech.derbent.risks.service;

import org.springframework.stereotype.Repository;
import tech.derbent.api.services.CEntityOfProjectRepository;
import tech.derbent.risks.domain.CRiskStatus;

/** CRiskStatusRepository - Repository interface for CRiskStatus entities. Layer: Data Access (MVC) Provides data access methods for risk status
 * entities including standard CRUD operations through inheritance from CEntityOfProjectRepository. */
@Repository
public interface CRiskStatusRepository extends CEntityOfProjectRepository<CRiskStatus> {
}
