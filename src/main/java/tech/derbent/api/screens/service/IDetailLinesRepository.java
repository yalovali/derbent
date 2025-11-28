package tech.derbent.api.screens.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;

/**
 * IDetailLinesRepository - Repository interface for CDetailLines entity.
 * Provides database access for detail line entries within a detail section.
 *
 * <p>Naming Convention for Query Methods (per coding standards):
 * <ul>
 *   <li>{@code findByMaster(M master)} - Find all child entities by master/parent entity</li>
 *   <li>{@code findActiveByMaster(M master)} - Find active child entities by master/parent</li>
 *   <li>{@code countByMaster(M master)} - Count child entities by master/parent entity</li>
 *   <li>{@code getNextItemOrder(M master)} - Get next order number for new items</li>
 * </ul>
 */
public interface IDetailLinesRepository extends IAbstractRepository<CDetailLines> {

	/** Count all detail lines for a master section.
	 * @param master the detail section
	 * @return count of detail lines */
	@Query ("SELECT COUNT(e) FROM #{#entityName} e WHERE e.detailSection = :master")
	Long countByMaster(@Param ("master") CDetailSection master);

	/** Find all active detail lines for a master section, ordered by itemOrder.
	 * @param master the detail section
	 * @return list of active detail lines ordered by itemOrder ascending */
	@Query ("SELECT e FROM #{#entityName} e LEFT JOIN FETCH e.detailSection WHERE e.detailSection = :master AND e.active = true ORDER BY e.itemOrder ASC")
	List<CDetailLines> findActiveByMaster(@Param ("master") CDetailSection master);

	/** Find all detail lines for a master section, ordered by itemOrder.
	 * @param master the detail section
	 * @return list of detail lines ordered by itemOrder ascending */
	@Query ("SELECT e FROM #{#entityName} e LEFT JOIN FETCH e.detailSection WHERE e.detailSection = :master ORDER BY e.itemOrder ASC")
	List<CDetailLines> findByMaster(@Param ("master") CDetailSection master);

	/** Get the next item order number for new items in a section.
	 * @param master the detail section
	 * @return the next available order number */
	@Query ("SELECT COALESCE(MAX(e.itemOrder), 0) + 1 FROM #{#entityName} e WHERE e.detailSection = :master")
	Integer getNextItemOrder(@Param ("master") CDetailSection master);
}
