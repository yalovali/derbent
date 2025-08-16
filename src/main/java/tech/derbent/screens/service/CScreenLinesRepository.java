package tech.derbent.screens.service;

import java.util.List;

import tech.derbent.abstracts.services.CAbstractRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;

public interface CScreenLinesRepository extends CAbstractRepository<CScreenLines> {

    @Query(
        "SELECT sl FROM CScreenLines sl " +
        "WHERE sl.screen = :screen " +
        "ORDER BY sl.lineOrder ASC"
    )
    List<CScreenLines> findByScreenOrderByLineOrder(@Param("screen") CScreen screen);

    @Query(
        "SELECT sl FROM CScreenLines sl " +
        "WHERE sl.screen = :screen AND sl.isActive = true " +
        "ORDER BY sl.lineOrder ASC"
    )
    List<CScreenLines> findActiveByScreenOrderByLineOrder(@Param("screen") CScreen screen);

    @Query(
        "SELECT COALESCE(MAX(sl.lineOrder), 0) + 1 FROM CScreenLines sl " +
        "WHERE sl.screen = :screen"
    )
    Integer getNextLineOrder(@Param("screen") CScreen screen);

    @Query(
        "SELECT COUNT(sl) FROM CScreenLines sl " +
        "WHERE sl.screen = :screen"
    )
    Long countByScreen(@Param("screen") CScreen screen);
}