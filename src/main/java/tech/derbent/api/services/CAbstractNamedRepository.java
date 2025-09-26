package tech.derbent.api.services;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.domains.CEntityNamed;

/** CAbstractNamedRepository - Abstract repository interface for entities that extend CEntityNamed. Layer: Service (MVC) - Repository interface
 * Provides common query methods for named entities with case-insensitive operations. */
@NoRepositoryBean
public interface CAbstractNamedRepository<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractRepository<EntityClass> {

	@Query ("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE LOWER(e.name) = LOWER(:name)")
	boolean existsByName(@Param ("name") String name);
	@Query ("SELECT e FROM #{#entityName} e WHERE LOWER(e.name) = LOWER(:name)")
	Optional<EntityClass> findByName(@Param ("name") String name);
}
