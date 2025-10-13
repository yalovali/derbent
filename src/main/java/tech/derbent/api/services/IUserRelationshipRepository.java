package tech.derbent.api.services;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityDB;

@NoRepositoryBean
public interface IUserRelationshipRepository<EntityClass extends CEntityDB<EntityClass>> extends IAbstractUserEntityRelationRepository<EntityClass> {

	@Query ("SELECT r FROM #{#entityName} r LEFT JOIN FETCH r.user WHERE r.user.id = :userId")
	List<EntityClass> findByUserIdWithUser(@Param ("userId") Long userId);
	@Query ("SELECT r FROM #{#entityName} r WHERE r.user.id = :userId AND r.isActive = true")
	List<EntityClass> findActiveByUserId(@Param ("userId") Long userId);
	@Modifying
	@Transactional
	@Query ("DELETE FROM #{#entityName} r WHERE r.user.id = :userId")
	void deleteByUserId(@Param ("userId") Long userId);
}
