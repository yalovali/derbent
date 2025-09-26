package tech.derbent.api.services;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import tech.derbent.api.domains.CEntityDB;

@NoRepositoryBean // 🔥 Bu şart!
public interface CAbstractRepository<EntityClass extends CEntityDB<EntityClass>>
		extends JpaRepository<EntityClass, Long>, JpaSpecificationExecutor<EntityClass> {

	@Override
	abstract Optional<EntityClass> findById(Long id);
	/** Default implementation of eager loading - subclasses should override with specific eager loading queries. Falls back to standard findById if
	 * not overridden. */
}
