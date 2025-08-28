package tech.derbent.abstracts.services;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.abstracts.domains.CEntityNamed;

/** CAbstractNamedRepository - Abstract repository interface for entities that extend CEntityNamed. Layer: Service (MVC) - Repository interface
 * Provides common query methods for named entities with case-insensitive operations. */
@NoRepositoryBean
public interface CAbstractNamedRepository<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractRepository<EntityClass> {
	/** Checks if an entity name already exists (case-insensitive).
	 * @param name the name to check
	 * @return true if the name exists, false otherwise */
	@Query ("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE LOWER(e.name) = LOWER(:name)")
	boolean existsByNameIgnoreCase(@Param ("name") String name);

	/** Finds an entity by exact name match (case-insensitive).
	 * @param name the entity name
	 * @return Optional containing the entity if found, empty otherwise */
	@Query ("SELECT e FROM #{#entityName} e WHERE LOWER(e.name) = LOWER(:name)")
	Optional<EntityClass> findByNameIgnoreCase(@Param ("name") String name);
}
