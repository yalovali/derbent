package tech.derbent.plm.kanban.kanbanline.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.plm.project.domain.CProject_Derbent;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CKanbanLineService extends CEntityOfCompanyService<CKanbanLine> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanLineService.class);

	/** Normalizes column ordering to a contiguous sequence. */
	private static void normalizeKanbanColumnOrder(final CKanbanLine line) {
		Check.notNull(line, "Kanban line cannot be null when normalizing column order");
		Check.notNull(line.getKanbanColumns(), "Kanban columns cannot be null when normalizing order");
		final List<CKanbanColumn> columns = new ArrayList<>(line.getKanbanColumns());
		columns.sort(Comparator.comparing(CKanbanColumn::getItemOrder, Comparator.nullsLast(Integer::compareTo)));
		int expectedOrder = 1;
		for (final CKanbanColumn column : columns) {
			if (column.getItemOrder() == null || column.getItemOrder() <= 0 || !column.getItemOrder().equals(expectedOrder)) {
				column.setItemOrder(expectedOrder);
			}
			expectedOrder++;
		}
	}

	/** Resolves the matching column instance inside the line. */
	private static CKanbanColumn resolveColumnForDelete(final CKanbanLine line, final CKanbanColumn column) {
		if (line.getKanbanColumns().contains(column)) {
			return column;
		}
		final CKanbanColumn resolved =
				line.getKanbanColumns().stream().filter(item -> item.getId() != null && item.getId().equals(column.getId())).findFirst().orElse(null);
		Check.notNull(resolved, "Kanban column not found on kanban line for delete");
		return resolved;
	}

	private final Comparator<CKanbanLine> recencyComparator;

	/** Creates the service with repository, clock, and session context. */
	public CKanbanLineService(final IKanbanLineRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
		recencyComparator =
				Comparator.<CKanbanLine, LocalDateTime>comparing(CKanbanLine::getLastModifiedDate, Comparator.nullsLast(LocalDateTime::compareTo))
						.thenComparing(CKanbanLine::getCreatedDate, Comparator.nullsLast(LocalDateTime::compareTo))
						.thenComparing(CKanbanLine::getId, Comparator.nullsLast(Long::compareTo)).reversed();
	}

	/**
	 * Service-level method to copy CKanbanLine-specific fields.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CKanbanLine source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CKanbanLine)) {
			return;
		}
		final CKanbanLine targetLine = (CKanbanLine) target;
		
		// Copy kanban columns conditionally
		if (options.includesRelations() && source.getKanbanColumns() != null) {
			// Deep copy columns (each column needs to be cloned)
			for (final CKanbanColumn sourceColumn : source.getKanbanColumns()) {
				final CKanbanColumn targetColumn = new CKanbanColumn(sourceColumn.getName(), targetLine);
				targetColumn.setColor(sourceColumn.getColor());
				targetColumn.setDefaultColumn(sourceColumn.getDefaultColumn());
				targetColumn.setItemOrder(sourceColumn.getItemOrder());
				targetColumn.setWipLimit(sourceColumn.getWipLimit());
				targetColumn.setWipLimitEnabled(sourceColumn.getWipLimitEnabled());
				targetColumn.setServiceClass(sourceColumn.getServiceClass());
				if (sourceColumn.getIncludedStatuses() != null) {
					targetColumn.setIncludedStatuses(new ArrayList<>(sourceColumn.getIncludedStatuses()));
				}
				targetLine.addKanbanColumn(targetColumn);
			}
		}
		
		LOGGER.debug("Copied CKanbanLine '{}' with {} columns, options: {}", 
			source.getName(), source.getKanbanColumns() != null ? source.getKanbanColumns().size() : 0, options);
	}

	/** Removes a column from a line and reorders the remainder. */
	@Transactional
	public void deleteKanbanColumn(final CKanbanLine line, final CKanbanColumn column) {
		Check.notNull(line, "Kanban line cannot be null for column delete");
		Check.notNull(column, "Kanban column cannot be null for delete");
		Check.notNull(line.getId(), "Kanban line ID cannot be null for column delete");
		Check.notNull(column.getId(), "Kanban column ID cannot be null for delete");
		Check.notNull(line.getKanbanColumns(), "Kanban columns cannot be null for delete");
		final CKanbanColumn target = resolveColumnForDelete(line, column);
		line.removeKanbanColumn(target);
		normalizeKanbanColumnOrder(line);
		save(line);
	}

	// override finddefault
	/** Resolves the default line using project then company context. */
	@Override
	public Optional<CKanbanLine> findDefault() {
		// Resolve defaults in the same order the UI expects: project → company → most recent line
		// across all tenants. This keeps the Kanban board deterministic when context is missing.
		final Optional<CProject<?>> activeProject = sessionService.getActiveProject();
		if (activeProject.isPresent()) {
			final Optional<CKanbanLine> projectDefault = findDefaultForProject(activeProject.get());
			if (projectDefault.isPresent()) {
				return projectDefault;
			}
		}
		final Optional<CCompany> activeCompany = sessionService.getActiveCompany();
		if (activeCompany.isPresent()) {
			final Optional<CKanbanLine> companyDefault = findDefaultForCompany(activeCompany.get());
			if (companyDefault.isPresent()) {
				return companyDefault;
			}
		}
		return resolveDefaultLine(findAll());
	}

	/** Finds the default line for a company. */
	@Transactional (readOnly = true)
	public Optional<CKanbanLine> findDefaultForCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null when locating default Kanban line");
		final List<CKanbanLine> lines = listByCompany(company);
		return resolveDefaultLine(lines);
	}

	/** Finds the default line for the current project. */
	@Transactional (readOnly = true)
	public Optional<CKanbanLine> findDefaultForCurrentProject() {
		LOGGER.debug("active project {} company {}", sessionService.getActiveProject(), sessionService.getActiveCompany());
		return sessionService.getActiveProject().flatMap(this::findDefaultForProject);
	}

	/** Finds the default line for a specific project. */
	@Transactional (readOnly = true)
	public Optional<CKanbanLine> findDefaultForProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null when locating default Kanban line");
		// Only Derbent projects have kanban lines
		if (project instanceof CProject_Derbent) {
			final CProject_Derbent derbentProject = (CProject_Derbent) project;
			if (derbentProject.getKanbanLine() != null) {
				final Long identifier = derbentProject.getKanbanLine().getId();
				if (identifier != null) {
					final Optional<CKanbanLine> persisted = getById(identifier);
					if (persisted.isPresent()) {
						return persisted;
					}
				}
				return Optional.of(derbentProject.getKanbanLine());
			}
		}
		final CCompany company = project.getCompany() != null ? project.getCompany() : sessionService.getActiveCompany().orElse(null);
		if (company != null) {
			return findDefaultForCompany(company);
		}
		LOGGER.warn("Cannot resolve default Kanban line without a company context");
		return Optional.empty();
	}

	/** Returns the managed entity class. */
	@Override
	public Class<CKanbanLine> getEntityClass() { return CKanbanLine.class; }

	/** Returns the initializer service class. */
	@Override
	public Class<?> getInitializerServiceClass() { return CKanbanLineInitializerService.class; }

	/** Returns the page service class. */
	@Override
	public Class<?> getPageServiceClass() { return CPageServiceKanbanLine.class; }

	/** Returns the service runtime class. */
	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Initializes defaults for a new line entity. */
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	/** Picks the most recently modified line as default. */
	private Optional<CKanbanLine> resolveDefaultLine(final List<CKanbanLine> lines) {
		if (lines == null || lines.isEmpty()) {
			return Optional.empty();
		}
		return lines.stream().max(recencyComparator);
	}

	@Override
	@Transactional
	public CKanbanLine save(final CKanbanLine entity) {
		final CKanbanLine saved = super.save(entity);
		if (saved.getId() == null) {
			return saved;
		}
		return ((IKanbanLineRepository) repository).findById(saved.getId()).orElse(saved);
	}

	/** Validates name uniqueness within the company. */
	@Override
	protected void validateEntity(final CKanbanLine entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getCompany(), ValidationMessages.COMPANY_REQUIRED);
		
		// 2. Unique name validation - MANDATORY use of helper
		validateUniqueNameInCompany((IKanbanLineRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
