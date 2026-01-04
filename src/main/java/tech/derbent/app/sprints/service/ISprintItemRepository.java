package tech.derbent.app.sprints.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.app.sprints.domain.CSprintItem;

/** ISprintItemRepository - Repository interface for CSprintItem entity. 
 * Provides database access for sprint items which are progress tracking components.
 */
public interface ISprintItemRepository extends IAbstractRepository<CSprintItem> {

	@Override
	@Query ("""
			SELECT e FROM #{#entityName} e
			LEFT JOIN FETCH e.sprint
			LEFT JOIN FETCH e.responsible
			WHERE e.id = :id
			""")
	Optional<CSprintItem> findById(@Param ("id") Long id);
}
