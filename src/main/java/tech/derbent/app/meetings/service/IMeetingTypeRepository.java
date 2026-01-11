package tech.derbent.app.meetings.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.meetings.domain.CMeetingType;
import tech.derbent.app.companies.domain.CCompany;

/** CMeetingTypeRepository - Repository interface for CMeetingType entity. Layer: Service (MVC) Provides data access operations for project-aware
 * meeting types with eager loading support. */
public interface IMeetingTypeRepository extends IEntityOfCompanyRepository<CMeetingType> {

	/** Finds a meeting type by ID with eagerly loaded relationships using generic pattern */
	@Query (
		"SELECT mt FROM #{#entityName} mt LEFT JOIN FETCH mt.company WHERE mt.id = :id"
	)
	Optional<CMeetingType> findByIdWithRelationships(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT mt FROM #{#entityName} mt
			LEFT JOIN FETCH mt.company
			LEFT JOIN FETCH mt.workflow
			WHERE mt.company = :company
			ORDER BY mt.name ASC
			""")
	List<CMeetingType> listByCompanyForPageView(@Param ("company") CCompany company);
}
