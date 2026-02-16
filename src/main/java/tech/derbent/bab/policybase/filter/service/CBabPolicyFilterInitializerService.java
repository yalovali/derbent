package tech.derbent.bab.policybase.filter.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;

/** Backward-compatible coordinator for all policy-filter initializers. */
@Service
@Profile ("bab")
public final class CBabPolicyFilterInitializerService extends CInitializerServiceBase {

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		CBabPolicyFilterCSVInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
		CBabPolicyFilterCANInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
		CBabPolicyFilterROSInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		CBabPolicyFilterCSVInitializerService.initializeSample(project, minimal);
		CBabPolicyFilterCANInitializerService.initializeSample(project, minimal);
		CBabPolicyFilterROSInitializerService.initializeSample(project, minimal);
	}

	private CBabPolicyFilterInitializerService() {
		// Utility class
	}
}
