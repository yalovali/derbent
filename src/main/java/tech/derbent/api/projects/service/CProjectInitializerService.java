package tech.derbent.api.projects.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.domain.CProject_Bab;
import tech.derbent.api.projects.domain.CProject_Derbent;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;

public class CProjectInitializerService extends CInitializerServiceBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectInitializerService.class);

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			Check.notNull(project, "Project cannot be null");
			if (project instanceof final CProject_Derbent derbentProject) {
				return CProject_DerbentInitializerService.createBasicView(derbentProject);
			}
			if (project instanceof final CProject_Bab babProject) {
				return CProject_BabInitializerService.createBasicView(babProject);
			}
			Check.fail("Unsupported project type: " + project.getClass().getSimpleName());
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error creating project view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		if (project instanceof final CProject_Derbent derbentProject) {
			return CProject_DerbentInitializerService.createGridEntity(derbentProject);
		}
		if (project instanceof final CProject_Bab babProject) {
			return CProject_BabInitializerService.createGridEntity(babProject);
		}
		Check.fail("Unsupported project type: " + project.getClass().getSimpleName());
		return null;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		Check.notNull(project, "Project cannot be null");
		if (project instanceof final CProject_Derbent derbentProject) {
			CProject_DerbentInitializerService.initialize(derbentProject, gridEntityService, detailSectionService, pageEntityService);
			return;
		}
		if (project instanceof final CProject_Bab babProject) {
			CProject_BabInitializerService.initialize(babProject, gridEntityService, detailSectionService, pageEntityService);
			return;
		}
		Check.fail("Unsupported project type: " + project.getClass().getSimpleName());
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		CProject_DerbentInitializerService.initializeSample(company, minimal);
	}

	public static CProject<?> initializeSampleBab(final CCompany company, final boolean minimal) throws Exception {
		return CProject_BabInitializerService.initializeSampleBab(company, minimal);
	}
}
