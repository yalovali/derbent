package tech.derbent.meetings.service;

import org.springframework.stereotype.Repository;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.meetings.domain.CMeetingStatus;

/** CMeetingStatusRepository - Repository interface for CMeetingStatus entities. Layer: Data Access (MVC) Provides data access operations for meeting
 * status management. Since CMeetingStatus extends CStatus which extends CTypeEntity which extends CEntityOfProject, this repository must extend
 * CEntityOfProjectRepository to provide project-aware operations. */
@Repository
public interface CMeetingStatusRepository extends CEntityOfProjectRepository<CMeetingStatus> {
}
