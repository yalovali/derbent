package tech.derbent.plm.decisions.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.plm.decisions.domain.CDecisionType;

/** Imports CDecision rows from Excel (project items). */
@Service
@Profile({"derbent", "default"})
public class CDecisionImportHandler implements IEntityImportHandler<CDecision> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionImportHandler.class);
	private static final DateTimeFormatter[] DATE_FORMATS = {
			DateTimeFormatter.ofPattern("yyyy-MM-dd"),
			DateTimeFormatter.ofPattern("dd/MM/yyyy"),
			DateTimeFormatter.ofPattern("MM/dd/yyyy"),
			DateTimeFormatter.ofPattern("d.M.yyyy"),
	};
	private static final DateTimeFormatter[] DATETIME_FORMATS = {
			DateTimeFormatter.ISO_LOCAL_DATE_TIME,
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
	};

	private final CDecisionService decisionService;
	private final CDecisionTypeService decisionTypeService;
	private final CProjectItemStatusService statusService;
	private final IUserRepository userRepository;

	public CDecisionImportHandler(final CDecisionService decisionService, final CDecisionTypeService decisionTypeService,
			final CProjectItemStatusService statusService, final IUserRepository userRepository) {
		this.decisionService = decisionService;
		this.decisionTypeService = decisionTypeService;
		this.statusService = statusService;
		this.userRepository = userRepository;
	}

	@Override
	public Class<CDecision> getEntityClass() { return CDecision.class; }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CDecision");
		names.add("Decision");
		names.add("Decisions");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CDecision.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CDecision.class);
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
	public Map<String, String> getColumnAliases() {
		return Map.ofEntries(
				Map.entry("Name", "name"),
				Map.entry("Description", "description"),
				Map.entry("Status", "status"),
				Map.entry("Decision Type", "entitytype"),
				Map.entry("Type", "entitytype"),
				Map.entry("Estimated Cost", "estimatedcost"),
				Map.entry("Implementation Date", "implementationdate"),
				Map.entry("Review Date", "reviewdate"),
				Map.entry("Assigned To", "assignedto")
		);
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
		final CDecision decision = decisionService.findByNameAndProject(name, project)
				.orElseGet(() -> new CDecision(name, project));

		final String description = rowData.getOrDefault("description", "").trim();
		if (!description.isBlank()) {
			decision.setDescription(description);
		}

		final String statusName = rowData.getOrDefault("status", "").trim();
		if (!statusName.isBlank()) {
			final CProjectItemStatus status = statusService.findByNameAndCompany(statusName, project.getCompany()).orElse(null);
			if (status == null) {
				return CImportRowResult.error(rowNumber, "Status '" + statusName + "' not found", rowData);
			}
			decision.setStatus(status);
		}

		final String typeName = rowData.getOrDefault("entitytype", "").trim();
		if (!typeName.isBlank()) {
			final CDecisionType type = decisionTypeService.findByNameAndCompany(typeName, project.getCompany()).orElse(null);
			if (type == null) {
				return CImportRowResult.error(rowNumber, "Decision Type '" + typeName + "' not found", rowData);
			}
			decision.setEntityType(type);
		}

		final String costStr = rowData.getOrDefault("estimatedcost", "").trim();
		if (!costStr.isBlank()) {
			try {
				decision.setEstimatedCost(new BigDecimal(costStr));
			} catch (final Exception e) {
				return CImportRowResult.error(rowNumber, "Invalid Estimated Cost: " + costStr, rowData);
			}
		}

		final String implStr = rowData.getOrDefault("implementationdate", "").trim();
		if (!implStr.isBlank()) {
			final LocalDateTime impl = parseDateTime(implStr);
			if (impl == null) {
				return CImportRowResult.error(rowNumber,
						"Cannot parse Implementation Date '" + implStr + "' (use yyyy-MM-dd or yyyy-MM-ddTHH:mm)", rowData);
			}
			decision.setImplementationDate(impl);
		}
		final String reviewStr = rowData.getOrDefault("reviewdate", "").trim();
		if (!reviewStr.isBlank()) {
			final LocalDateTime review = parseDateTime(reviewStr);
			if (review == null) {
				return CImportRowResult.error(rowNumber,
						"Cannot parse Review Date '" + reviewStr + "' (use yyyy-MM-dd or yyyy-MM-ddTHH:mm)", rowData);
			}
			decision.setReviewDate(review);
		}

		final String assignedToLogin = rowData.getOrDefault("assignedto", "").trim();
		if (!assignedToLogin.isBlank()) {
			final var userOpt = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), assignedToLogin);
			if (userOpt.isEmpty()) {
				return CImportRowResult.error(rowNumber,
						"Assigned To user '" + assignedToLogin + "' not found in company.", rowData);
			}
			decision.setAssignedTo(userOpt.get());
		}

		if (!options.isDryRun()) {
			try {
				decisionService.save(decision);
			} catch (final Exception e) {
				LOGGER.error("Failed to save decision '{}' reason={}", name, e.getMessage());
				return CImportRowResult.error(rowNumber, "Save failed: " + e.getMessage(), rowData);
			}
		}
		LOGGER.debug("Imported decision '{}' (row {})", name, rowNumber);
		return CImportRowResult.success(rowNumber, name);
	}

	private static LocalDateTime parseDateTime(final String value) {
		for (final DateTimeFormatter fmt : DATETIME_FORMATS) {
			try {
				return LocalDateTime.parse(value, fmt);
			} catch (final DateTimeParseException ignored) { /* try next */ }
		}
		final LocalDate date = parseDate(value);
		return date != null ? date.atStartOfDay() : null;
	}

	private static LocalDate parseDate(final String value) {
		for (final DateTimeFormatter fmt : DATE_FORMATS) {
			try {
				return LocalDate.parse(value, fmt);
			} catch (final DateTimeParseException ignored) { /* try next */ }
		}
		return null;
	}
}
