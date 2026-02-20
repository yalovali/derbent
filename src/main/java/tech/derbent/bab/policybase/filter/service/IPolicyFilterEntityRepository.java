package tech.derbent.bab.policybase.filter.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Base repository contract for policy-filter entities. */
@Profile ("bab")
@NoRepositoryBean
public interface IPolicyFilterEntityRepository<FilterType extends CBabPolicyFilterBase<FilterType>> extends IAbstractNamedRepository<FilterType> {

	@Query ("SELECT COUNT(f) FROM #{#entityName} f WHERE f.parentNode.project = :project")
	long countByProject(@Param ("project") CProject<?> project);

	@Query ("SELECT f FROM #{#entityName} f WHERE f.parentNode.project = :project AND f.cacheEnabled = true ORDER BY f.executionOrder ASC")
	List<FilterType> findCachedFilters(@Param ("project") CProject<?> project);

	@Query ("SELECT f FROM #{#entityName} f WHERE f.parentNode.project = :project AND f.isEnabled = true ORDER BY f.executionOrder ASC")
	List<FilterType> findEnabledByProject(@Param ("project") CProject<?> project);

	@Query ("SELECT f FROM #{#entityName} f WHERE f.parentNode.project = :project ORDER BY f.executionOrder ASC, f.name ASC")
	List<FilterType> listByProject(@Param ("project") CProject<?> project);

	@Query ("SELECT f FROM #{#entityName} f WHERE f.parentNode = :parentNode ORDER BY f.executionOrder ASC, f.name ASC")
	List<FilterType> findByParentNode(@Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("SELECT f FROM #{#entityName} f WHERE f.parentNode = :parentNode AND f.isEnabled = true ORDER BY f.executionOrder ASC, f.name ASC")
	List<FilterType> findEnabledByParentNode(@Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("SELECT f FROM #{#entityName} f WHERE f.parentNode = :parentNode AND lower(f.name) = lower(:name)")
	Optional<FilterType> findByNameAndParentNode(@Param ("name") String name, @Param ("parentNode") CBabNodeEntity<?> parentNode);

	@Query ("""
			SELECT f FROM #{#entityName} f
			WHERE f.parentNode.project = :project
			AND f.isEnabled = true
			AND (
				(:nodeType = 'can' AND f.canNodeEnabled = true) OR
				(:nodeType = 'modbus' AND f.modbusNodeEnabled = true) OR
				(:nodeType = 'http' AND f.httpNodeEnabled = true) OR
				(:nodeType = 'file' AND f.fileNodeEnabled = true) OR
				(:nodeType = 'syslog' AND f.syslogNodeEnabled = true) OR
				(:nodeType = 'ros' AND f.rosNodeEnabled = true)
			)
			ORDER BY f.executionOrder ASC
			""")
	List<FilterType> findEnabledForNodeType(@Param ("project") CProject<?> project, @Param ("nodeType") String nodeType);

	@Query ("""
			SELECT DISTINCT f FROM #{#entityName} f
			LEFT JOIN FETCH f.parentNode
			LEFT JOIN FETCH f.attachments
			LEFT JOIN FETCH f.comments
			LEFT JOIN FETCH f.links
			WHERE f.id = :id
			""")
	Optional<FilterType> findById(@Param ("id") Long id);

	@Query ("""
			SELECT DISTINCT f FROM #{#entityName} f
			LEFT JOIN FETCH f.parentNode p
			LEFT JOIN FETCH f.attachments
			LEFT JOIN FETCH f.comments
			LEFT JOIN FETCH f.links
			WHERE p.project = :project
			ORDER BY f.executionOrder ASC, f.name ASC
			""")
	List<FilterType> listByProjectForPageView(@Param ("project") CProject<?> project);
}
