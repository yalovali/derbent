package tech.derbent.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.activities.service.CActivityService;
import tech.derbent.activities.service.CActivityTypeService;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;

/**
 * Sample data initializer that ensures basic data exists in the database. This runs after
 * the application starts and creates sample data if the database is empty.
 */
@Component
public class SampleDataInitializer implements ApplicationRunner {

	public SampleDataInitializer(final CProjectService projectService,
		final CUserService userService, final CActivityService activityService,
		final CUserTypeService userTypeService,
		final CActivityTypeService activityTypeService) {}

	@Override
	@Transactional
	public void run(final ApplicationArguments args) throws Exception {
		// DONT DO ANTHING IF THE DATABASE IS NOT EMPTY
	}
}