package tech.derbent.app.customers.customer.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.customers.customer.domain.CCustomer;
import tech.derbent.app.customers.customertype.domain.CCustomerType;
import tech.derbent.api.projects.domain.CProject;

public interface ICustomerRepository extends IEntityOfProjectRepository<CCustomer> {

	@Query("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param("entityType") CCustomerType type);

	@Override
	@Query("""
			SELECT c FROM CCustomer c
			LEFT JOIN FETCH c.project
			LEFT JOIN FETCH c.assignedTo
			LEFT JOIN FETCH c.createdBy
			LEFT JOIN FETCH c.status
			LEFT JOIN FETCH c.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH c.attachments
			LEFT JOIN FETCH c.comments
			WHERE c.id = :id
			""")
	Optional<CCustomer> findById(@Param("id") Long id);

	@Override
	@Query("""
			SELECT c FROM CCustomer c
			LEFT JOIN FETCH c.project
			LEFT JOIN FETCH c.assignedTo
			LEFT JOIN FETCH c.createdBy
			LEFT JOIN FETCH c.status
			LEFT JOIN FETCH c.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH c.attachments
			LEFT JOIN FETCH c.comments
			WHERE c.project = :project
			ORDER BY c.name ASC
			""")
	List<CCustomer> listByProjectForPageView(@Param("project") CProject project);
}
