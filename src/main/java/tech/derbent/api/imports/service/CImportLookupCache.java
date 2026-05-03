package tech.derbent.api.imports.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.projects.domain.CProject;

/**
 * In-memory lookup cache for import operations.
 * <p>
 * WHY: During large Excel imports, repeatedly querying the database for the same
 * companies/projects is wasteful. This cache stores lookups for the duration of one import
 * transaction, dramatically improving performance for multi-sheet workbooks.
 * </p>
 * <p>
 * LIFECYCLE: Create one instance per import transaction; discard after commit/rollback.
 * </p>
 */
public class CImportLookupCache {

	private final Map<String, CCompany> companiesByName = new HashMap<>();
	private final Map<CompanyProjectKey, CProject<?>> projectsByNameAndCompany = new HashMap<>();

	/**
	 * Gets or loads a company by name.
	 */
	public Optional<CCompany> getCompany(final String name, final CImportProjectResolver resolver) {
		if (name == null || name.isBlank()) {
			return Optional.empty();
		}
		final String key = name.toLowerCase().trim();
		if (companiesByName.containsKey(key)) {
			return Optional.ofNullable(companiesByName.get(key));
		}
		final Optional<CCompany> result = resolver.findCompanyByName(name);
		companiesByName.put(key, result.orElse(null));
		return result;
	}

	/**
	 * Gets or loads a project by name and company.
	 */
	public Optional<CProject<?>> getProject(final String projectName, final CCompany company,
			final CImportProjectResolver resolver) {
		if (projectName == null || projectName.isBlank() || company == null) {
			return Optional.empty();
		}
		final CompanyProjectKey key = new CompanyProjectKey(company.getId(), projectName.toLowerCase().trim());
		if (projectsByNameAndCompany.containsKey(key)) {
			return Optional.ofNullable(projectsByNameAndCompany.get(key));
		}
		final Optional<CProject<?>> result = resolver.findProjectByNameAndCompany(projectName, company);
		projectsByNameAndCompany.put(key, result.orElse(null));
		return result;
	}

	/**
	 * Clears all cached data. Called after import completion.
	 */
	public void clear() {
		companiesByName.clear();
		projectsByNameAndCompany.clear();
	}

	private record CompanyProjectKey(Long companyId, String projectNameLower) {}
}
