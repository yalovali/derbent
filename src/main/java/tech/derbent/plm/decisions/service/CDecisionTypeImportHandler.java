package tech.derbent.plm.decisions.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.plm.decisions.domain.CDecisionType;

/** Imports CDecisionType rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CDecisionTypeImportHandler implements IEntityImportHandler<CDecisionType> {

	private final CDecisionTypeService decisionTypeService;
	private final CWorkflowEntityService workflowEntityService;

	public CDecisionTypeImportHandler(final CDecisionTypeService decisionTypeService,
			final CWorkflowEntityService workflowEntityService) {
		this.decisionTypeService = decisionTypeService;
		this.workflowEntityService = workflowEntityService;
	}

	@Override
	public Class<CDecisionType> getEntityClass() { return CDecisionType.class; }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CDecisionType");
		names.add("DecisionType");
		names.add("Decision Type");
		names.add("Decision Types");
		try {
			names.add(CEntityRegistry.getEntityTitleSingular(CDecisionType.class));
			names.add(CEntityRegistry.getEntityTitlePlural(CDecisionType.class));
		} catch (final Exception ignored) { /* registry may not be ready */ }
		return names;
	}

	@Override
	public Map<String, String> getColumnAliases() {
		return Map.of(
				"Name", "name",
				"Color", "color",
				"Sort Order", "sortorder",
				"Level", "level",
				"Can Have Children", "canhavechildren",
				"Non Deletable", "attributenondeletable",
				"Workflow", "workflow",
				"Requires Approval", "requiresapproval");
	}

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("name");
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final String name = rowData.getOrDefault("name", "").trim();
		if (name.isBlank()) {
			return CImportRowResult.error(rowNumber, "Name is required", rowData);
		}
		if (project.getCompany() == null) {
			return CImportRowResult.error(rowNumber, "Project company is required to create decision types", rowData);
		}
		// WHY: system init Excel is intended to be re-runnable (and also safe on top of code-initialized reference data).
		// Upsert-by-name avoids unique constraint violations.
		final CDecisionType type = decisionTypeService.findByNameAndCompany(name, project.getCompany())
				.orElseGet(() -> new CDecisionType(name, project.getCompany()));
		final String color = rowData.getOrDefault("color", "").trim();
		if (!color.isBlank()) {
			type.setColor(color);
		}
		final String sortOrderStr = rowData.getOrDefault("sortorder", "").trim();
		if (!sortOrderStr.isBlank()) {
			try {
				type.setSortOrder(Integer.valueOf(sortOrderStr));
			} catch (final Exception e) {
				return CImportRowResult.error(rowNumber, "Invalid sort order: " + sortOrderStr, rowData);
			}
		}
		final String levelStr = rowData.getOrDefault("level", "").trim();
		if (!levelStr.isBlank()) {
			try {
				type.setLevel(Integer.valueOf(levelStr));
			} catch (final Exception e) {
				return CImportRowResult.error(rowNumber, "Invalid level: " + levelStr, rowData);
			}
		}
		final String canHaveChildrenStr = rowData.getOrDefault("canhavechildren", "").trim();
		if (!canHaveChildrenStr.isBlank()) {
			type.setCanHaveChildren(Set.of("true", "yes", "1").contains(canHaveChildrenStr.toLowerCase()));
		}
		final String nonDeletableStr = rowData.getOrDefault("attributenondeletable", "").trim();
		if (!nonDeletableStr.isBlank()) {
			type.setAttributeNonDeletable(Set.of("true", "yes", "1").contains(nonDeletableStr.toLowerCase()));
		}
		final String workflowName = rowData.getOrDefault("workflow", "").trim();
		if (!workflowName.isBlank()) {
			final var wf = workflowEntityService.findByNameAndCompany(workflowName, project.getCompany()).orElse(null);
			if (wf == null) {
				return CImportRowResult.error(rowNumber, "Workflow '" + workflowName + "' not found", rowData);
			}
			type.setWorkflow(wf);
		}
		final String requiresApprovalStr = rowData.getOrDefault("requiresapproval", "").trim();
		if (!requiresApprovalStr.isBlank()) {
			type.setRequiresApproval(Set.of("true", "yes", "1").contains(requiresApprovalStr.toLowerCase()));
		}
		if (!options.isDryRun()) {
			decisionTypeService.save(type);
		}
		return CImportRowResult.success(rowNumber, name);
	}
}
