package tech.derbent.api.imports.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.CCompanyService;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportResult;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.api.utils.Check;

/**
 * Runs the committed "system init" workbooks after DB reset.
 *
 * WHY: We want to gradually move sample initialization out of code initializers and into reproducible Excel templates.
 * The reset flow still creates core system entities (companies/projects/users), then Excel adds rich sample content.
 */
@Service
public class CSystemInitExcelBootstrapService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemInitExcelBootstrapService.class);

	private final CCompanyService companyService;
	private final CExcelImportService excelImportService;
	private final CExcelTemplateService excelTemplateService;
	private final CProjectService<?> projectService;
	private final ISessionService sessionService;
	private final CUserService userService;

	public record CBootstrapSummary(int companies, int projects, int totalSuccess, int totalErrors) {
		public String toUiSummary() {
			return "Excel init: companies=" + companies + ", projects=" + projects + ", ok=" + totalSuccess + ", errors=" + totalErrors;
		}
	}

	public CSystemInitExcelBootstrapService(final CCompanyService companyService,
			final CProjectService<?> projectService,
			final CUserService userService,
			final ISessionService sessionService,
			final CExcelImportService excelImportService,
			final CExcelTemplateService excelTemplateService) {
		this.companyService = companyService;
		this.projectService = projectService;
		this.userService = userService;
		this.sessionService = sessionService;
		this.excelImportService = excelImportService;
		this.excelTemplateService = excelTemplateService;
	}

	public CBootstrapSummary bootstrapAllProjects() {
		final byte[] workbookBytes = loadTemplateBytes();
		final CImportOptions options = CImportOptions.defaults();
		options.setDryRun(false);
		options.setRollbackOnError(true);
		options.setSkipUnknownSheets(true);

		int companiesProcessed = 0;
		int projectsProcessed = 0;
		int totalSuccess = 0;
		int totalErrors = 0;
		final List<CCompany> companies = companyService.findActiveCompanies();
		for (final CCompany company : companies) {
			companiesProcessed++;
			// WHY: many services validate against session-scoped company/user; set stable context before importing.
			sessionService.setActiveCompany(company);
			final CUser user = userService.getRandomByCompany(company);
			Check.notNull(user, "No user found for company: " + company.getName());
			sessionService.setActiveUser(user);
			final List<? extends CProject<?>> projects = projectService.listByCompany(company);
			for (final CProject<?> project : projects) {
				projectsProcessed++;
				sessionService.setActiveProject(project);
				final CImportResult result = excelImportService.importExcel(new ByteArrayInputStream(workbookBytes), options, project);
				totalSuccess += result.getTotalSuccess();
				totalErrors += result.getTotalErrors();
				LOGGER.info("Excel init imported into project {}:{} (ok={}, errors={})", project.getId(), project.getName(),
						result.getTotalSuccess(), result.getTotalErrors());
			}
		}
		return new CBootstrapSummary(companiesProcessed, projectsProcessed, totalSuccess, totalErrors);
	}

	private byte[] loadTemplateBytes() {
		try (final InputStream in = excelTemplateService.openSystemInitTemplate()) {
			final byte[] bytes = in.readAllBytes();
			Check.isTrue(bytes.length > 0, "Empty system init workbook");
			return bytes;
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to read system init workbook bytes", e);
		}
	}
}
