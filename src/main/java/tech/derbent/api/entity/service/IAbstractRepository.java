package tech.derbent.api.entity.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import tech.derbent.api.entity.domain.CEntityDB;

@NoRepositoryBean // 🔥 Bu şart!
public interface IAbstractRepository<EntityClass extends CEntityDB<EntityClass>>
		extends JpaRepository<EntityClass, Long>, JpaSpecificationExecutor<EntityClass> {

	/** Default implementation of eager loading - subclasses should override with specific eager loading queries. */
	default List<EntityClass> findAllForPageView(final Sort sort) {
		return findAll(sort);
	}
	@Override
	abstract Optional<EntityClass> findById(Long id);
}
