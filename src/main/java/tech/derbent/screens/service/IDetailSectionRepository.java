package tech.derbent.screens.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CDetailSection;

public interface IDetailSectionRepository extends IEntityOfProjectRepository<CDetailSection> {

	@Query ("SELECT s FROM CDetailSection s " + "WHERE s.project = :project AND s.active = true")
	List<CDetailSection> findActiveByProject(@Param ("project") CProject project);
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
	@Query ("SELECT s FROM CDetailSection s WHERE s.project = :project AND s.entityType = :entityType")
	Optional<CDetailSection> findByEntityTypeAndProject(@Param ("project") CProject project, @Param ("entityType") String entityType);
	@Override
	@Query (
		"SELECT s FROM CDetailSection s " + "LEFT JOIN FETCH s.project " + "LEFT JOIN FETCH s.assignedTo " + "LEFT JOIN FETCH s.createdBy "
				+ "WHERE s.project = :project"
	)
	Page<CDetailSection> listByProject(@Param ("project") CProject project, Pageable pageable);;
}
