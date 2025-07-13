package tech.derbent.abstracts.services;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import tech.derbent.abstracts.domains.CEntityDB;

@NoRepositoryBean // 🔥 Bu şart!
public interface CAbstractRepository<EntityClass extends CEntityDB> extends JpaRepository<EntityClass, Long>, JpaSpecificationExecutor<EntityClass> {

	Slice<EntityClass> findAllBy(Pageable pageable);
}
