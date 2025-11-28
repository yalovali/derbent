package tech.derbent.api.screens.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;

public interface IDetailLinesRepository extends IAbstractRepository<CDetailLines> {

	@Query ("SELECT COUNT(a) FROM #{#entityName} a " + "WHERE a.detailSection = :detailSection")
	Long countByScreen(@Param ("detailSection") CDetailSection detailSection);
	@Query (
		"SELECT a FROM #{#entityName} a  LEFT JOIN FETCH detailSection " + "WHERE a.detailSection = :detailSection AND a.active = true "
				+ "ORDER BY a.itemOrder ASC"
	)
	List<CDetailLines> findActiveByScreen(@Param ("detailSection") CDetailSection detailSection);
	@Query ("SELECT a FROM #{#entityName} a LEFT JOIN FETCH detailSection " + "WHERE a.detailSection = :master " + "ORDER BY a.itemOrder ASC")
	List<CDetailLines> findByMaster(@Param ("detailSection") CDetailSection master);
	@Query ("SELECT COALESCE(MAX(a.itemOrder), 0) + 1 FROM #{#entityName} a WHERE a.detailSection = :detailSection")
	Integer getNextitemOrder(@Param ("detailSection") CDetailSection detailSection);
}
