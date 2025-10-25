package tech.derbent.app.activities.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IProjectItemRespository;
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.activities.domain.CActivityType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.users.domain.CUser;

public interface IActivityRepository extends IProjectItemRespository<CActivity> {

	/** Counts the number of activities that use the specified activity type using generic pattern */
	@Query ("SELECT COUNT(a) FROM #{#entityName} a WHERE a.entityType = :type")
	long countByType(@Param ("entityType") CActivityType type);
	@Override
	@Query (
		"SELECT a FROM #{#entityName} a LEFT JOIN FETCH a.project LEFT JOIN FETCH a.assignedTo LEFT JOIN FETCH a.createdBy LEFT JOIN FETCH a.entityType LEFT JOIN FETCH a.status WHERE a.id = :id"
	)
	Optional<CActivity> findById(@Param ("id") Long id);
	@Override
	@Query (
		"SELECT a FROM #{#entityName} a LEFT JOIN FETCH a.project LEFT JOIN FETCH a.assignedTo LEFT JOIN FETCH a.createdBy LEFT JOIN FETCH a.entityType LEFT JOIN FETCH a.status WHERE a.project = :project"
	)
	Page<CActivity> listByProject(@Param ("project") CProject project, Pageable pageable);
	// find all activities of projects where the user's company owns the project
	@Query (
		"SELECT a FROM #{#entityName} a LEFT JOIN FETCH a.project p WHERE p IN (SELECT us.project FROM CUserProjectSettings us WHERE us.user = :user)"
	)
	List<CActivity> listByUser(@Param ("user") CUser user);
}
