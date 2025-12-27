package tech.derbent.api.screens.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.app.projects.domain.CProject;

public interface IDetailSectionRepository extends IEntityOfProjectRepository<CDetailSection> {

	@Query ("SELECT s FROM CDetailSection s " + "WHERE s.project = :project AND s.active = true ORDER BY s.name ASC")
	List<CDetailSection> findActiveByProject(@Param ("project") CProject project);
	@Query ("SELECT s FROM CDetailSection s WHERE s.project = :project AND s.entityType = :entityType")
	Optional<CDetailSection> findByEntityTypeAndProject(@Param ("project") CProject project, @Param ("entityType") String entityType);
	@Query (
		"SELECT s FROM CDetailSection s " + "LEFT JOIN FETCH s.project " + "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
				+ "LEFT JOIN FETCH s.detailLines WHERE s.id = :id"
	)
	Optional<CDetailSection> findByIdWithEagerLoading(@Param ("id") Long id);
	@Query ("SELECT s FROM CDetailSection s LEFT JOIN FETCH s.detailLines WHERE s.id = :id")
	Optional<CDetailSection> findByIdWithScreenLines(@Param ("id") Long id);
	@Query (
		"SELECT s FROM CDetailSection s " + "LEFT JOIN FETCH s.project " + "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
				+ "LEFT JOIN FETCH s.detailLines " + "WHERE s.project = :project AND s.name = :name"
	)
	Optional<CDetailSection> findByNameAndProject(@Param ("project") CProject project, @Param ("name") String name);
	@Override
	@Query (
		"SELECT s FROM CDetailSection s " + "LEFT JOIN FETCH s.project " + "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
				+ "WHERE s.project = :project ORDER BY s.name ASC"
	)
	Page<CDetailSection> listByProject(@Param ("project") CProject project, Pageable pageable);
	@Override
	@Query (
		"SELECT s FROM CDetailSection s " + "LEFT JOIN FETCH s.project " + "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
				+ "LEFT JOIN FETCH s.detailLines " + "WHERE s.project = :project ORDER BY s.name ASC"
	)
	List<CDetailSection> listByProjectForPageView(@Param ("project") CProject project);
}
