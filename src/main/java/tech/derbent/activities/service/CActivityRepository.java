package tech.derbent.activities.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import tech.derbent.abstracts.services.CAbstractRepository;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;

public interface CActivityRepository extends CAbstractRepository<CActivity> {
	
	List<CActivity> findByProject(CProject project);
	
	Page<CActivity> findByProject(CProject project, Pageable pageable);
}
