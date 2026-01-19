package tech.derbent.api.projects.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.api.projects.domain.CProject_Bab;

@Profile ("bab")
public interface IProject_BabRepository extends IProjectRepository<CProject_Bab> {
}
