package tech.derbent.api.workflow.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CEntityOfCompanyImportHandler;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.workflow.domain.CWorkflowEntity;

/** Imports CWorkflowEntity rows from Excel (company-scoped reference data). */
@Service
public class CWorkflowEntityImportHandler extends CEntityOfCompanyImportHandler<CWorkflowEntity> {

	private final CWorkflowEntityService workflowEntityService;

	public CWorkflowEntityImportHandler(final CWorkflowEntityService workflowEntityService) {
		this.workflowEntityService = workflowEntityService;
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of("Name", "name", "Color", "color", "Icon", "icon");
	}

	@Override
	public Class<CWorkflowEntity> getEntityClass() { return CWorkflowEntity.class; }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CWorkflowEntity");
		names.add("WorkflowEntity");
		names.add("Workflow Entity");
		names.add("Workflows");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CWorkflowEntity.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CWorkflowEntity.class);
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
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final CExcelRow row = row(rowData);
		final var nameError = validateEntityNamed(row, rowNumber, rowData);
		if (nameError.isPresent()) {
			return nameError.get();
		}
		final var company = project.getCompany();
		final String name = row.string("name");
		// WHY: system init Excel is intended to be re-runnable; upsert-by-name avoids unique constraint violations.
		final CWorkflowEntity workflow = workflowEntityService.findByNameAndCompany(name, company)
				.orElseGet(() -> new CWorkflowEntity(name, company));
		applyEntityNamedFields(workflow, row);
		applyEntityOfCompanyFields(workflow, company);
		// NOTE: workflow entity currently has no color/icon fields; keep columns reserved for future.
		if (!options.isDryRun()) {
			workflowEntityService.save(workflow);
		}
		return CImportRowResult.success(rowNumber, name);
	}
}
