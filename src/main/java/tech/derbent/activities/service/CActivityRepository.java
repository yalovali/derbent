package tech.derbent.activities.service;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.CProjectItemRespository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.projects.domain.CProject;

public interface CActivityRepository extends CProjectItemRespository<CActivity> {

	@Override
	@Query (
		"SELECT a FROM #{#entityName} a LEFT JOIN FETCH a.project LEFT JOIN FETCH a.assignedTo LEFT JOIN FETCH a.createdBy LEFT JOIN FETCH a.activityType LEFT JOIN FETCH a.status WHERE a.id = :id"
	)
	Optional<CActivity> findById(@Param ("id") Long id);
	@Override
	@Query (
		"SELECT a FROM #{#entityName} a LEFT JOIN FETCH a.project LEFT JOIN FETCH a.assignedTo LEFT JOIN FETCH a.createdBy LEFT JOIN FETCH a.activityType LEFT JOIN FETCH a.status WHERE a.project = :project"
	)
	Page<CActivity> listByProject(@Param ("project") CProject project, Pageable pageable);
	/** Counts the number of activities that use the specified activity type using generic pattern */
	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.activityType = :activityType")
	long countByActivityType(@Param ("activityType") CActivityType activityType);
}
