package tech.derbent.plm.activities.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.imports.service.CAbstractSimpleTypeImportHandler;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.plm.activities.domain.CActivityPriority;

/** Imports CActivityPriority rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CActivityPriorityImportHandler extends CAbstractSimpleTypeImportHandler<CActivityPriority> {

	private final CActivityPriorityService priorityService;

	public CActivityPriorityImportHandler(final CActivityPriorityService priorityService) {
		this.priorityService = priorityService;
	}

	@Override
	public Class<CActivityPriority> getEntityClass() {
		return CActivityPriority.class;
	}

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CActivityPriority");
		names.add("ActivityPriority");
		names.add("Activity Priority");
		names.add("Activity Priorities");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CActivityPriority.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CActivityPriority.class);
			if (singular != null && !singular.isBlank()) {
				names.add(singular);
			}
			if (plural != null && !plural.isBlank()) {
				names.add(plural);
			}
		} catch (final Exception ignored) { /* registry may not be ready */ }
		return names;
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of(
				"Name", "name",
				"Color", "color",
				"Sort Order", "sortorder",
				"Priority Level", "prioritylevel",
				"Is Default", "isdefault");
	}

	@Override
	protected Optional<CActivityPriority> findByNameAndCompany(final String name, final CCompany company) {
		return priorityService.findByNameAndCompany(name, company);
	}

	@Override
	protected CActivityPriority createNew(final String name, final CCompany company) {
		return new CActivityPriority(name, company);
	}

	@Override
	protected void save(final CActivityPriority entity) {
		priorityService.save(entity);
	}

	@Override
	protected void applyExtraFields(final CActivityPriority entity, final CExcelRow row,
			final CProject<?> project, final int rowNumber) {
		row.optionalInt("prioritylevel").ifPresent(entity::setPriorityLevel);
		row.optionalBoolean("isdefault").ifPresent(entity::setIsDefault);
	}
}
