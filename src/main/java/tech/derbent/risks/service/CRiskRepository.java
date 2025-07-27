package tech.derbent.risks.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tech.derbent.abstracts.services.CAbstractNamedRepository;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;

public interface CRiskRepository extends CAbstractNamedRepository<CRisk> {

    List<CRisk> findByProject(CProject project);

    Page<CRisk> findByProject(CProject project, Pageable pageable);
}