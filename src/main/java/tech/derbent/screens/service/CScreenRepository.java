package tech.derbent.screens.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.abstracts.services.CEntityOfProjectRepository;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;

public interface CScreenRepository extends CEntityOfProjectRepository<CScreen> {
	@Query ("SELECT s FROM CScreen s " + "WHERE s.project = :project AND s.isActive = true")
	List<CScreen> findActiveByProject(@Param ("project") CProject project);

	@Query (
		"SELECT s FROM CScreen s " + "LEFT JOIN FETCH s.project " + "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
				+ "LEFT JOIN FETCH s.screenLines WHERE s.id = :id"
	)
	Optional<CScreen> findByIdWithEagerLoading(@Param ("id") Long id);

	@Query ("SELECT s FROM CScreen s LEFT JOIN FETCH s.screenLines WHERE s.id = :id")
	Optional<CScreen> findByIdWithScreenLines(@Param ("id") Long id);

	@Query (
		"SELECT s FROM CScreen s " + "LEFT JOIN FETCH s.project " + "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
				+ "LEFT JOIN FETCH s.screenLines " + "WHERE s.project = :project AND s.name = :name"
	)
	Optional<CScreen> findByNameAndProject(@Param ("project") CProject project, @Param ("name") String name);

	@Override
	@Query (
		"SELECT s FROM CScreen s " + "LEFT JOIN FETCH s.project " + "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
				+ "WHERE s.project = :project"
	)
	Page<CScreen> listByProject(@Param ("project") CProject project, Pageable pageable);;
}
