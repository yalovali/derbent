package tech.derbent.activities.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;

public interface CActivityRepository extends CEntityOfProjectRepository<CActivity> {

	@Override
	@Query (
		"SELECT a FROM CActivity a " + "LEFT JOIN FETCH a.project "
			+ "LEFT JOIN FETCH a.assignedTo " + "LEFT JOIN FETCH a.createdBy "
			+ "LEFT JOIN FETCH a.activityType " + "LEFT JOIN FETCH a.status "
			+ "LEFT JOIN FETCH a.parentActivity " + "WHERE a.project = :project"
	)
	List<CActivity> findByProject(@Param ("project") CProject project, Pageable pageable);
	@Query (
		"SELECT a FROM CActivity a " + "LEFT JOIN FETCH a.project "
			+ "LEFT JOIN FETCH a.assignedTo " + "LEFT JOIN FETCH a.createdBy "
			+ "LEFT JOIN FETCH a.activityType " + "LEFT JOIN FETCH a.status "
			+ "LEFT JOIN FETCH a.parentActivity " + "WHERE a.id = :id"
	)
	Optional<CActivity> findByProject(@Param ("id") Long id);
}
