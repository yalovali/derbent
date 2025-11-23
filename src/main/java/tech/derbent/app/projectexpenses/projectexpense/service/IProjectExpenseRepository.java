package tech.derbent.app.projectexpenses.projectexpense.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.projectexpenses.projectexpense.domain.CProjectExpense;
import tech.derbent.app.projectexpenses.projectexpensetype.domain.CProjectExpenseType;

public interface IProjectExpenseRepository extends IEntityOfProjectRepository<CProjectExpense> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CProjectExpenseType type);
	@Override
	@Query (
		"SELECT r FROM CProjectExpense r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType et LEFT JOIN FETCH et.workflow " + "WHERE r.id = :id"
	)
	Optional<CProjectExpense> findById(@Param ("id") Long id);
}
