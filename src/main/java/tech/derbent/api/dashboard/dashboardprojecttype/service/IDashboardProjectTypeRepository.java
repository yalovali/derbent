package tech.derbent.api.dashboard.dashboardprojecttype.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import tech.derbent.api.dashboard.dashboardprojecttype.domain.CDashboardProjectType;
import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;

@Repository
@Profile ({"derbent", "default", "bab", "test"})
public interface IDashboardProjectTypeRepository extends IEntityOfCompanyRepository<CDashboardProjectType> {}
