package tech.derbent.app.budgets.budget.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.budgets.budget.domain.CBudget;
import tech.derbent.app.budgets.budgettype.domain.CBudgetType;

public interface IBudgetRepository extends IEntityOfProjectRepository<CBudget> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CBudgetType type);
	@Override
	@Query (
		"SELECT r FROM CBudget r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType " + "WHERE r.id = :id"
	)
	Optional<CBudget> findById(@Param ("id") Long id);
}
