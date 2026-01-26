package tech.derbent.plm.agile.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.agile.domain.CFeature;
import tech.derbent.plm.agile.domain.CFeatureType;
import tech.derbent.plm.sprints.domain.CSprint;

public interface IFeatureRepository extends IAgileRepository<CFeature> {

	/** Counts the number of features that use the specified feature type using generic pattern */
	@Query ("SELECT COUNT(f) FROM #{#entityName} f WHERE f.entityType = :type")
	long countByType(@Param ("type") CFeatureType type);

	@Override
	@Query ("""
			SELECT f FROM #{#entityName} f
			LEFT JOIN FETCH f.project
			LEFT JOIN FETCH f.assignedTo
			LEFT JOIN FETCH f.createdBy
			LEFT JOIN FETCH f.attachments
			LEFT JOIN FETCH f.comments
			LEFT JOIN FETCH f.links
			LEFT JOIN FETCH f.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH f.status
			LEFT JOIN FETCH f.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE f.id = :id
			""")
	Optional<CFeature> findById(@Param ("id") Long id);

	@Override
	@Query ("""
			SELECT f FROM #{#entityName} f
			LEFT JOIN FETCH f.project
			LEFT JOIN FETCH f.assignedTo
			LEFT JOIN FETCH f.createdBy
			LEFT JOIN FETCH f.attachments
			LEFT JOIN FETCH f.comments
			LEFT JOIN FETCH f.links
			LEFT JOIN FETCH f.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH f.status
			LEFT JOIN FETCH f.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE f.project = :project
			ORDER BY f.id DESC
			""")
	Page<CFeature> listByProject(@Param ("project") CProject<?> project, Pageable pageable);

	@Override
	@Query ("""
			SELECT f FROM #{#entityName} f
			LEFT JOIN FETCH f.project
			LEFT JOIN FETCH f.assignedTo
			LEFT JOIN FETCH f.createdBy
			LEFT JOIN FETCH f.attachments
			LEFT JOIN FETCH f.comments
			LEFT JOIN FETCH f.links
			LEFT JOIN FETCH f.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH f.status
			LEFT JOIN FETCH f.sprintItem si
			LEFT JOIN FETCH si.sprint
			WHERE f.project = :project
			ORDER BY f.id DESC
			""")
	List<CFeature> listByProjectForPageView(@Param ("project") CProject<?> project);

	/** Find all features that are in the backlog (not assigned to any sprint).
	 * @param project the project
	 * @return list of features in backlog ordered by sprint item order */
	@Query ("""
			SELECT f FROM #{#entityName} f
			LEFT JOIN FETCH f.project
			LEFT JOIN FETCH f.assignedTo
			LEFT JOIN FETCH f.createdBy
			LEFT JOIN FETCH f.attachments
			LEFT JOIN FETCH f.comments
			LEFT JOIN FETCH f.links
			LEFT JOIN FETCH f.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH f.status
			LEFT JOIN FETCH f.sprintItem si
			WHERE f.project = :project
			and (si.sprint IS NULL OR si.sprint.id IS NULL)
			ORDER BY si.itemOrder ASC NULLS LAST, f.id DESC
			""")
	List<CFeature> listForProjectBacklog(@Param ("project") CProject<?> project);

	/** Find all features that are members of a specific sprint (via sprintItem relation).
	 * @param sprint the sprint
	 * @return list of features ordered by sprint item order */
	@Query ("""
			SELECT f FROM #{#entityName} f
			LEFT JOIN FETCH f.project
			LEFT JOIN FETCH f.assignedTo
			LEFT JOIN FETCH f.createdBy
			LEFT JOIN FETCH f.attachments
			LEFT JOIN FETCH f.comments
			LEFT JOIN FETCH f.links
			LEFT JOIN FETCH f.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH f.status
			LEFT JOIN FETCH f.sprintItem si
			LEFT JOIN FETCH si.sprint s
			WHERE s = :sprint
			ORDER BY si.itemOrder ASC
			""")
	List<CFeature> listForSprint(@Param ("sprint") CSprint sprint);

	/** Find feature by sprint item ID - loads without sprint item to prevent circular loading.
	 * @param sprintItemId the sprint item ID
	 * @return the feature if found */
	@Query ("""
			SELECT f FROM #{#entityName} f
			LEFT JOIN FETCH f.project
			LEFT JOIN FETCH f.assignedTo
			LEFT JOIN FETCH f.createdBy
			LEFT JOIN FETCH f.attachments
			LEFT JOIN FETCH f.comments
			LEFT JOIN FETCH f.links
			LEFT JOIN FETCH f.entityType et
			LEFT JOIN FETCH et.workflow
			LEFT JOIN FETCH f.status
			WHERE f.sprintItem.id = :sprintItemId
			""")
	Optional<CFeature> findBySprintItemId(@Param ("sprintItemId") Long sprintItemId);

	/** Find feature by name and project.
	 * @param name    the name
	 * @param project the project
	 * @return the feature if found */
	@Query ("SELECT f FROM #{#entityName} f WHERE f.name = :name AND f.project = :project")
	Optional<CFeature> findByNameAndProject(@Param ("name") String name, @Param ("project") CProject<?> project);
}
