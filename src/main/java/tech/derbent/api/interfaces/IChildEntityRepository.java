package tech.derbent.api.interfaces;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.IAbstractRepository;

/** Base repository interface for all master-detail (parent-child) relationships.
 * Provides standard methods for querying child entities by their master entity.
 * 
 * @param <T> Child entity type
 * @param <M> Master (parent) entity type */
@NoRepositoryBean
public interface IChildEntityRepository<T extends CEntityDB<T>, M extends CEntityDB<M>> 
        extends IAbstractRepository<T> {
    
    /** Find all children by master entity.
     * @param master the master entity
     * @return list of child entities ordered appropriately */
    List<T> findByMaster(@Param("master") M master);
    
    /** Find all children by master ID.
     * @param masterId the master entity ID
     * @return list of child entities ordered appropriately */
    List<T> findByMasterId(@Param("masterId") Long masterId);
    
    /** Count children by master.
     * @param master the master entity
     * @return count of child entities */
    Long countByMaster(@Param("master") M master);
    
    /** Get next item order for new items.
     * @param master the master entity
     * @return next available order number */
    Integer getNextItemOrder(@Param("master") M master);
}
