package tech.derbent.plm.tickets.servicedepartment.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.tickets.servicedepartment.domain.CTicketServiceDepartment;

public interface ITicketServiceDepartmentRepository extends IEntityOfCompanyRepository<CTicketServiceDepartment> {

	@Override
	@Query("""
			SELECT DISTINCT d FROM #{#entityName} d
			LEFT JOIN FETCH d.company
			LEFT JOIN FETCH d.departmentManager
			LEFT JOIN FETCH d.responsibleUsers
			WHERE d.company = :company
			ORDER BY d.name ASC
			""")
	List<CTicketServiceDepartment> findByCompany(@Param("company") CCompany company);

	@Override
	@Query("""
			SELECT DISTINCT d FROM #{#entityName} d
			LEFT JOIN FETCH d.company
			LEFT JOIN FETCH d.departmentManager
			LEFT JOIN FETCH d.responsibleUsers
			LEFT JOIN FETCH d.attachments
			LEFT JOIN FETCH d.comments
			WHERE d.company = :company
			ORDER BY d.name ASC
			""")
	List<CTicketServiceDepartment> listByCompanyForPageView(@Param("company") CCompany company);

	@Override
	@Query("""
			SELECT d FROM #{#entityName} d
			LEFT JOIN FETCH d.attachments
			LEFT JOIN FETCH d.comments
			LEFT JOIN FETCH d.company
			LEFT JOIN FETCH d.departmentManager
			LEFT JOIN FETCH d.responsibleUsers
			WHERE d.id = :id
			""")
	Optional<CTicketServiceDepartment> findById(@Param("id") Long id);

	@Query("""
			SELECT DISTINCT d FROM #{#entityName} d
			LEFT JOIN FETCH d.company
			LEFT JOIN FETCH d.departmentManager
			LEFT JOIN FETCH d.responsibleUsers
			WHERE d.departmentManager = :manager
			ORDER BY d.name ASC
			""")
	List<CTicketServiceDepartment> findByManager(@Param("manager") CUser manager);

	@Query("""
			SELECT DISTINCT d FROM #{#entityName} d
			LEFT JOIN FETCH d.company
			LEFT JOIN FETCH d.departmentManager
			LEFT JOIN FETCH d.responsibleUsers u
			WHERE u = :user
			ORDER BY d.name ASC
			""")
	List<CTicketServiceDepartment> findByResponsibleUser(@Param("user") CUser user);

	@Query("""
			SELECT DISTINCT d FROM #{#entityName} d
			LEFT JOIN FETCH d.company
			LEFT JOIN FETCH d.departmentManager
			LEFT JOIN FETCH d.responsibleUsers
			WHERE d.isActive = true AND d.company = :company
			ORDER BY d.name ASC
			""")
	List<CTicketServiceDepartment> findActiveByCompany(@Param("company") CCompany company);
}
