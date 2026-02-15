package tech.derbent.bab.policybase.node.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** INodeEntityRepository - Abstract repository interface for virtual network nodes. Layer: Service (MVC) Active when: 'bab' profile is active
 * Following Derbent pattern: Abstract repository with @NoRepositoryBean annotation. Base repository for all virtual network node entities (HTTP
 * servers, vehicles, file inputs). Provides common query methods for node management and eager loading patterns. Note: Uses @NoRepositoryBean to
 * prevent Spring instantiation of this abstract interface. Concrete repositories (IHttpServerNodeRepository, etc.) implement the actual HQL
 * queries. */
@Profile ("bab")
@NoRepositoryBean // MANDATORY - Abstract repositories are not beans
public interface INodeEntityRepository<NodeType extends CBabNodeEntity<NodeType>> extends IEntityOfProjectRepository<NodeType> {
	// Abstract method declarations for node-specific queries
	// No HQL queries in abstract interface - only method signatures
	/** Count total nodes by project. Inherited from base but can be overridden for optimization. */
	@Override
	@Query ("SELECT COUNT(n) FROM #{#entityName} n WHERE n.project = :project")
	long countByProject(@Param ("project") CProject<?> project);
}
