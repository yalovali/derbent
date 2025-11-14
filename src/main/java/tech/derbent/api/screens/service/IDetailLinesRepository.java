package tech.derbent.api.screens.service;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entity.service.IAbstractRepository;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;

public interface IDetailLinesRepository extends IAbstractRepository<CDetailLines> {

	@Query ("SELECT COUNT(sl) FROM CDetailLines sl " + "WHERE sl.detailSection = :detailSection")
	Long countByScreen(@Param ("detailSection") CDetailSection detailSection);
	@Query ("SELECT sl FROM CDetailLines sl " + "WHERE sl.detailSection = :detailSection AND sl.active = true " + "ORDER BY sl.lineOrder ASC")
	List<CDetailLines> findActiveByScreen(@Param ("detailSection") CDetailSection detailSection);
	@Query ("SELECT sl FROM CDetailLines sl " + "WHERE sl.detailSection = :detailSection " + "ORDER BY sl.lineOrder ASC")
	List<CDetailLines> findByMaster(@Param ("detailSection") CDetailSection detailSection);
	@Query ("SELECT COALESCE(MAX(sl.lineOrder), 0) + 1 FROM CDetailLines sl WHERE sl.detailSection = :detailSection")
	Integer getNextLineOrder(@Param ("detailSection") CDetailSection detailSection);
}
