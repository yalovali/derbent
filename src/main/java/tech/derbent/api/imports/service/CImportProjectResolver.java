package tech.derbent.api.imports.service;

import java.util.Optional;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.CCompanyService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.service.CProjectService;

/**
 * Resolves project and company references by name during Excel import.
 *
 * WHY: Excel rows for project-scoped entities include a "project" column and for company-scoped
 * entities a "company" column so each row is self-describing. This service does the name→entity
 * lookup without requiring every handler to inject both services directly.
 */
@Service
public class CImportProjectResolver {

	private final CProjectService<?> projectService;
	private final CCompanyService companyService;

	public CImportProjectResolver(final CProjectService<?> projectService, final CCompanyService companyService) {
		this.projectService = projectService;
		this.companyService = companyService;
	}

	/**
	 * Finds a project by name within the given company.
	 * Returns empty if not found.
	 */
	@SuppressWarnings ("unchecked")
	public Optional<CProject<?>> findProjectByNameAndCompany(final String name, final CCompany company) {
		return (Optional<CProject<?>>) (Optional<?>) projectService.listByCompany(company).stream()
				.filter(p -> name.equalsIgnoreCase(p.getName()))
				.findFirst();
	}

	/**
	 * Finds a company by exact name (case-insensitive).
	 * Returns empty if not found.
	 */
	public Optional<CCompany> findCompanyByName(final String name) {
		return companyService.findActiveCompanies().stream()
				.filter(c -> name.equalsIgnoreCase(c.getName()))
				.findFirst();
	}
}
