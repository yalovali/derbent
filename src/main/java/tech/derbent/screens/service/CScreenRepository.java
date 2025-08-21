package tech.derbent.screens.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;

public interface CScreenRepository extends CEntityOfProjectRepository<CScreen> {

	@Query (
		"SELECT s FROM CScreen s " + "WHERE s.project = :project AND s.isActive = true"
	)
	List<CScreen> findActiveByProject(@Param ("project") CProject project);
	@Query (
		"SELECT s FROM CScreen s " + "LEFT JOIN FETCH s.project "
			+ "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
			+ "LEFT JOIN FETCH s.relatedActivity " + "LEFT JOIN FETCH s.relatedMeeting "
			+ "LEFT JOIN FETCH s.relatedRisk " + "LEFT JOIN FETCH s.screenLines "
			+ "WHERE s.id = :id"
	)
	Optional<CScreen> findByIdWithEagerLoading(@Param ("id") Long id);
	@Query ("SELECT s FROM CScreen s LEFT JOIN FETCH s.screenLines WHERE s.id = :id")
	Optional<CScreen> findByIdWithScreenLines(@Param ("id") Long id);
	@Override
	@Query (
		"SELECT s FROM CScreen s " + "LEFT JOIN FETCH s.project "
			+ "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
			+ "LEFT JOIN FETCH s.relatedActivity " + "LEFT JOIN FETCH s.relatedMeeting "
			+ "LEFT JOIN FETCH s.relatedRisk " + "WHERE s.project = :project"
	)
	List<CScreen> findByProject(@Param ("project") CProject project, Pageable pageable);
	@Query (
		"SELECT s FROM CScreen s "
			+ "WHERE s.project = :project AND s.entityType = :entityType"
	)
	List<CScreen> findByProjectAndEntityType(@Param ("project") CProject project,
		@Param ("entityType") String entityType);
}