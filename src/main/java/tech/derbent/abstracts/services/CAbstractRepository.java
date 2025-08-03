package tech.derbent.abstracts.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import tech.derbent.abstracts.domains.CEntityDB;

@NoRepositoryBean // 🔥 Bu şart!
public interface CAbstractRepository<EntityClass extends CEntityDB<EntityClass>>
        extends JpaRepository<EntityClass, Long>, JpaSpecificationExecutor<EntityClass> {

    /**
     * Finds all entities with pagination support.
     * 
     * @param pageable
     *            the pagination information
     * @return a slice of entities
     */
    Slice<EntityClass> findAllBy(Pageable pageable);
}
