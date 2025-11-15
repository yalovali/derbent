package tech.derbent.app.projectincomes.projectincome.service;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.projectincomes.projectincome.domain.CProjectIncome;
import tech.derbent.app.projectincomes.projectincometype.domain.CProjectIncomeType;

public interface IProjectIncomeRepository extends IEntityOfProjectRepository<CProjectIncome> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :entityType")
	long countByType(@Param ("entityType") CProjectIncomeType type);
	@Override
	@Query (
		"SELECT r FROM CProjectIncome r LEFT JOIN FETCH r.project LEFT JOIN FETCH r.assignedTo LEFT JOIN FETCH r.createdBy LEFT JOIN FETCH r.status LEFT JOIN FETCH r.entityType " + "WHERE r.id = :id"
	)
	Optional<CProjectIncome> findById(@Param ("id") Long id);
}
