package tech.derbent.plm.tickets.ticketpriority.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.tickets.ticketpriority.domain.CTicketPriority;

/**
 * ITicketPriorityRepository - Repository interface for CTicketPriority entities.
 * Layer: Data Access (MVC)
 * Provides data access operations for ticket priority management.
 */
@Repository
public interface ITicketPriorityRepository extends IEntityOfCompanyRepository<CTicketPriority> {

	@Query("SELECT p FROM CTicketPriority p WHERE p.isDefault = true and p.company = :company")
	Optional<CTicketPriority> findByIsDefaultTrue(@Param("company") CCompany company);

	@Override
	@Query("""
			SELECT p FROM CTicketPriority p
			LEFT JOIN FETCH p.company
			LEFT JOIN FETCH p.workflow
			WHERE p.company = :company
			ORDER BY p.name ASC
			""")
	List<CTicketPriority> listByCompanyForPageView(@Param("company") CCompany company);
}
