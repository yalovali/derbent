package tech.derbent.plm.kanban.kanbanline.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.screens.service.IOrderedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanLine;

@Service
@PreAuthorize ("isAuthenticated()")
public class CKanbanColumnService extends CAbstractService<CKanbanColumn> implements IEntityRegistrable, IOrderedEntityService<CKanbanColumn> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanColumnService.class);
	private final CKanbanLineService kanbanLineService;

	/** Creates the service with dependencies and session context. */
	public CKanbanColumnService(final IKanbanColumnRepository repository, final Clock clock, final ISessionService sessionService,
			final CKanbanLineService kanbanLineService) {
		super(repository, clock, sessionService);
		this.kanbanLineService = kanbanLineService;
	}

	/**
	 * Service-level method to copy CKanbanColumn-specific fields.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CKanbanColumn source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CKanbanColumn)) {
			return;
		}
		final CKanbanColumn targetColumn = (CKanbanColumn) target;
		
		// Copy basic fields
		targetColumn.setColor(source.getColor());
		targetColumn.setDefaultColumn(source.getDefaultColumn());
		targetColumn.setItemOrder(source.getItemOrder());
		targetColumn.setServiceClass(source.getServiceClass());
		targetColumn.setWipLimit(source.getWipLimit());
		targetColumn.setWipLimitEnabled(source.getWipLimitEnabled());
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			targetColumn.setKanbanLine(source.getKanbanLine());
			if (source.getIncludedStatuses() != null) {
				targetColumn.setIncludedStatuses(new ArrayList<>(source.getIncludedStatuses()));
			}
		}
		
		LOGGER.debug("Copied CKanbanColumn '{}' with options: {}", source.getName(), options);
	}

	/** Enforces unique default column and status ownership across a line. This method ensures that: 1. Only one column can be marked as the default
	 * column (fallback for unmapped statuses) 2. Each status is mapped to at most ONE column (prevents status overlap/duplication) When a column is
	 * saved with statuses or default flag, this method automatically: - Removes the default flag from other columns if this column becomes default -
	 * Removes any overlapping statuses from other columns to maintain status uniqueness This is a critical business rule: status overlap would cause
	 * ambiguity in kanban board display and drag-drop operations, as the system wouldn't know which column should display an item with a given
	 * status. */
	private void applyStatusAndDefaultConstraints(final CKanbanColumn saved) {
		Check.notNull(saved, "Kanban column cannot be null when applying constraints");
		Check.notNull(saved.getId(), "Kanban column must be saved before enforcing constraints");
		final CKanbanLine line = saved.getKanbanLine();
		Check.notNull(line, "Kanban line cannot be null when enforcing constraints");
		final List<CKanbanColumn> columns = findByMaster(line);
		final Set<Long> includedStatusIds = saved.getIncludedStatuses() == null ? Set.of()
				: saved.getIncludedStatuses().stream().filter(status -> status != null && status.getId() != null).map(CProjectItemStatus::getId)
						.collect(Collectors.toCollection(HashSet::new));
		final boolean isDefaultColumn = saved.getDefaultColumn();
		// Track status removals for debug logging
		int statusRemovalCount = 0;
		for (final CKanbanColumn column : columns) {
			if (column.getId().equals(saved.getId())) {
				continue;
			}
			boolean changed = false;
			// Enforce single default column rule
			if (isDefaultColumn && column.getDefaultColumn()) {
				LOGGER.debug("[KanbanValidation] Removing default flag from column '{}' (ID: {}) because column '{}' (ID: {}) is now the default",
						column.getName(), column.getId(), saved.getName(), saved.getId());
				column.setDefaultColumn(false);
				changed = true;
			}
			// Enforce status uniqueness: remove overlapping statuses from other columns
			if (!includedStatusIds.isEmpty() && column.getIncludedStatuses() != null && !column.getIncludedStatuses().isEmpty()) {
				final List<Long> remainingStatusIds = column.getIncludedStatuses().stream().filter(status -> status != null && status.getId() != null)
						.map(CProjectItemStatus::getId).filter(id -> !includedStatusIds.contains(id)).collect(Collectors.toList());
				if (remainingStatusIds.size() != column.getIncludedStatuses().size()) {
					final int removedCount = column.getIncludedStatuses().size() - remainingStatusIds.size();
					statusRemovalCount += removedCount;
					LOGGER.debug("[KanbanValidation] Removing {} overlapping status(es) from column '{}' (ID: {}) to maintain status uniqueness",
							removedCount, column.getName(), column.getId());
					final List<CProjectItemStatus> remainingStatuses = column.getIncludedStatuses().stream()
							.filter(status -> status != null && status.getId() != null && remainingStatusIds.contains(status.getId()))
							.collect(Collectors.toList());
					column.setIncludedStatuses(remainingStatuses);
					changed = true;
				}
			}
			if (changed) {
				repository.save(column);
			}
		}
		if (statusRemovalCount > 0) {
			LOGGER.info(
					"[KanbanValidation] Enforced status uniqueness: removed {} overlapping status mapping(s) from other columns in kanban line '{}'",
					statusRemovalCount, line.getName());
		}
	}

	/** Deletes a column through the parent line to preserve relationships. */
	@Override
	@Transactional
	public void delete(final CKanbanColumn entity) {
		Check.notNull(entity, "Kanban column cannot be null");
		LOGGER.debug("Deleting kanban column: {}", entity.getId());
		final CKanbanLine line = resolveLineForDelete(entity);
		kanbanLineService.deleteKanbanColumn(line, entity);
	}

	/** Deletes a column by id with its line loaded. */
	@Override
	@Transactional
	public void delete(final Long id) {
		Check.notNull(id, "Entity ID cannot be null");
		final CKanbanColumn entity = getTypedRepository().findByIdWithLine(id).orElse(null);
		Check.notNull(entity, "Kanban column not found for delete");
		delete(entity);
	}

	/** Lists columns for a line in display order. */
	public List<CKanbanColumn> findByMaster(final CKanbanLine master) {
		Check.notNull(master, "Master kanban line cannot be null");
		if (master.getId() == null) {
			return List.of();
		}
		return getTypedRepository().findByMaster(master);
	}

	/** Returns the managed entity class. */
	@Override
	public Class<CKanbanColumn> getEntityClass() { return CKanbanColumn.class; }

	/** Calculates the next item order within a line. */
	public Integer getNextItemOrder(final CKanbanLine master) {
		Check.notNull(master, "Master kanban line cannot be null");
		if (master.getId() == null) {
			return 1;
		}
		return getTypedRepository().getNextItemOrder(master);
	}

	/** Kanban columns do not expose a page service. */
	@Override
	public Class<?> getPageServiceClass() { return null; }

	/** Returns the service runtime class. */
	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Returns the repository with kanban-specific methods. */
	private IKanbanColumnRepository getTypedRepository() {
		return (IKanbanColumnRepository) repository;
	}

	/** Initializes defaults for a new column entity. */
	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	/** Moves a column one position down within its line. */
	@Override
	@Transactional
	public void moveItemDown(final CKanbanColumn childItem) {
		Check.notNull(childItem, "Kanban column cannot be null");
		final CKanbanLine master = childItem.getKanbanLine();
		Check.notNull(master, "Kanban line cannot be null for column");
		final List<CKanbanColumn> items = normalizeItemOrder(master);
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(childItem.getId()) && i < items.size() - 1) {
				final CKanbanColumn nextColumn = items.get(i + 1);
				final Integer currentOrder = childItem.getItemOrder();
				final Integer nextOrder = nextColumn.getItemOrder();
				childItem.setItemOrder(nextOrder);
				nextColumn.setItemOrder(currentOrder);
				save(childItem);
				save(nextColumn);
				break;
			}
		}
	}

	/** Moves a column one position up within its line. */
	@Override
	@Transactional
	public void moveItemUp(final CKanbanColumn childItem) {
		Check.notNull(childItem, "Kanban column cannot be null");
		final CKanbanLine master = childItem.getKanbanLine();
		Check.notNull(master, "Kanban line cannot be null for column");
		final List<CKanbanColumn> items = normalizeItemOrder(master);
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(childItem.getId()) && i > 0) {
				final CKanbanColumn previousColumn = items.get(i - 1);
				final Integer currentOrder = childItem.getItemOrder();
				final Integer previousOrder = previousColumn.getItemOrder();
				childItem.setItemOrder(previousOrder);
				previousColumn.setItemOrder(currentOrder);
				save(childItem);
				save(previousColumn);
				break;
			}
		}
	}

	/** Normalizes column ordering and persists corrections. */
	private List<CKanbanColumn> normalizeItemOrder(final CKanbanLine master) {
		Check.notNull(master, "Kanban line cannot be null when normalizing order");
		final List<CKanbanColumn> items = new ArrayList<>(findByMaster(master));
		items.sort(Comparator.comparing(CKanbanColumn::getItemOrder, Comparator.nullsLast(Integer::compareTo)));
		boolean needsUpdate = false;
		int expectedOrder = 1;
		for (final CKanbanColumn item : items) {
			if (item.getItemOrder() == null || item.getItemOrder() <= 0 || !item.getItemOrder().equals(expectedOrder)) {
				needsUpdate = true;
				break;
			}
			expectedOrder++;
		}
		if (needsUpdate) {
			expectedOrder = 1;
			for (final CKanbanColumn item : items) {
				if (item.getItemOrder() == null || item.getItemOrder() <= 0 || !item.getItemOrder().equals(expectedOrder)) {
					item.setItemOrder(expectedOrder);
					repository.save(item);
				}
				expectedOrder++;
			}
		}
		return items;
	}

	/** Reloads a saved column by line and name. */
	private CKanbanColumn reloadByName(final CKanbanLine line, final String name) {
		final CKanbanColumn saved = getTypedRepository().findByMasterAndNameIgnoreCase(line, name).orElse(null);
		Check.notNull(saved, "Saved kanban column could not be reloaded");
		return saved;
	}

	/** Resolves a managed line instance for delete operations. */
	private CKanbanLine resolveLineForDelete(final CKanbanColumn entity) {
		Check.notNull(entity.getKanbanLine(), "Kanban line reference missing for column delete");
		Check.notNull(entity.getKanbanLine().getId(), "Kanban line ID missing for column delete");
		final CKanbanLine line = kanbanLineService.getById(entity.getKanbanLine().getId()).orElse(null);
		Check.notNull(line, "Kanban line could not be loaded for column delete");
		return line;
	}

	/** Resolves a managed line instance for save operations. */
	private CKanbanLine resolveLineForSave(final CKanbanColumn entity) {
		Check.notNull(entity.getKanbanLine(), "Kanban line reference missing for column save");
		Check.notNull(entity.getKanbanLine().getId(), "Kanban line ID missing for column save");
		final CKanbanLine line = kanbanLineService.getById(entity.getKanbanLine().getId()).orElse(null);
		Check.notNull(line, "Kanban line could not be loaded for column save");
		return line;
	}

	/** Saves a column and enforces ordering and status constraints. */
	@Override
	@Transactional
	public CKanbanColumn save(final CKanbanColumn entity) {
		Check.notNull(entity, "Kanban column cannot be null");
		Check.notNull(entity.getKanbanLine(), "Kanban line cannot be null for column save");
		Check.notNull(entity.getKanbanLine().getId(), "Kanban line ID cannot be null for column save");
		Check.notBlank(entity.getName(), "Kanban column name cannot be blank");
		final CKanbanLine line = resolveLineForSave(entity);
		entity.setKanbanLine(line);
		validateEntity(entity);
		if (entity.getItemOrder() == null || entity.getItemOrder() <= 0) {
			entity.setItemOrder(getNextItemOrder(line));
		}
		if (entity.getId() == null) {
			line.addKanbanColumn(entity);
			kanbanLineService.save(line);
			final CKanbanColumn saved = reloadByName(line, entity.getName());
			applyStatusAndDefaultConstraints(saved);
			return saved;
		}
		final CKanbanColumn saved = super.save(entity);
		applyStatusAndDefaultConstraints(saved);
		return saved;
	}

	/** Validates column naming and uniqueness within the line. Also performs critical validation to detect status overlap across columns in the same
	 * kanban line. Status overlap is considered a data error because it creates ambiguity: if a status is mapped to multiple columns, the system
	 * cannot determine which column should display items with that status. This validation is fail-fast: it throws CValidationException immediately
	 * when overlap is detected, preventing the invalid configuration from being saved to the database. */
	@Override
	protected void validateEntity(final CKanbanColumn entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getKanbanLine(), "Kanban line is required");
		if (entity.getKanbanLine().getId() == null) {
			throw new IllegalArgumentException("Kanban line ID cannot be null for column validation");
		}
		if (entity.getColor() != null && entity.getColor().length() > 7) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Color code cannot exceed %d characters", 7));
		}
		// 3. Unique Checks
		final String trimmedName = entity.getName().trim();
		// Prevent creating columns named "Backlog" (reserved name)
		if ("backlog".equalsIgnoreCase(trimmedName)) {
			throw new CValidationException("Column name 'Backlog' is reserved and cannot be used. Please choose a different name.");
		}
		final CKanbanLine line = entity.getKanbanLine();
		final CKanbanColumn existing = getTypedRepository().findByMasterAndNameIgnoreCase(line, trimmedName).orElse(null);
		if (existing != null && (entity.getId() == null || !entity.getId().equals(existing.getId()))) {
			throw new CValidationException("Kanban column name must be unique within the kanban line");
		}
		// 4. Numeric Checks
		if (entity.getItemOrder() != null && entity.getItemOrder() < 0) {
			throw new IllegalArgumentException("Item order cannot be negative");
		}
		if (entity.getWipLimit() != null && entity.getWipLimit() < 0) {
			throw new IllegalArgumentException("WIP limit cannot be negative");
		}
		// CRITICAL VALIDATION: Check for status overlap across columns
		validateStatusUniqueness(entity);
	}

	/** Validates that no status in this column is already mapped to another column in the same kanban line. This is a critical business rule
	 * validation. Status overlap across columns would cause: 1. Ambiguity in kanban board display (which column should show items with overlapping
	 * status?) 2. Incorrect drag-drop behavior (which column should accept drops for overlapping status?) 3. Data inconsistency (items could appear
	 * in multiple columns simultaneously) This method throws CValidationException immediately when overlap is detected (fail-fast pattern).
	 * IMPORTANT: This validation checks BOTH persisted columns (from database) AND in-memory columns (from the parent line's collection). This
	 * catches overlaps during batch initialization when multiple columns are created simultaneously before any are saved.
	 * @param entity The column being validated
	 * @throws CValidationException if any status in this column is already mapped to another column */
	private void validateStatusUniqueness(final CKanbanColumn entity) {
		// Skip validation if column has no statuses mapped
		if (entity.getIncludedStatuses() == null || entity.getIncludedStatuses().isEmpty()) {
			LOGGER.debug("[KanbanValidation] Column '{}' has no statuses, skipping overlap validation", entity.getName());
			return;
		}
		final CKanbanLine line = entity.getKanbanLine();
		// CRITICAL: Check BOTH persisted columns AND in-memory columns from parent line
		// This catches overlaps during batch initialization when multiple columns are created together
		final List<CKanbanColumn> persistedColumns = findByMaster(line);
		final Set<CKanbanColumn> allColumns = new HashSet<>(persistedColumns);
		// Add in-memory columns from the parent line's collection (may not be persisted yet)
		if (line.getKanbanColumns() != null) {
			allColumns.addAll(line.getKanbanColumns());
			LOGGER.debug("[KanbanValidation] Checking {} total columns ({} persisted + {} in-memory) for status overlap", allColumns.size(),
					persistedColumns.size(), line.getKanbanColumns().size());
		}
		// Build map of status ID -> column name for debugging and error reporting
		final Map<Long, String> statusToColumnMap = new HashMap<>();
		for (final CKanbanColumn column : allColumns) {
			// Skip the current column being validated
			if (entity.equals(column)) {
				continue;
			}
			// Skip if same ID (for persisted entities)
			if (entity.getId() != null && column.getId() != null && column.getId().equals(entity.getId())) {
				continue;
			}
			// Check each status in the existing column
			if (column.getIncludedStatuses() != null) {
				column.getIncludedStatuses().stream().filter(status -> status != null && status.getId() != null).forEach(status -> statusToColumnMap.put(status.getId(), column.getName()));
			}
		}
		// Now check if any status in the entity being validated is already in the map (overlap detected)
		final List<String> overlappingStatuses = new ArrayList<>();
		entity.getIncludedStatuses().stream().filter((final var status) -> status != null && status.getId() != null).forEach((final var status) -> {
			final String existingColumnName = statusToColumnMap.get(status.getId());
			if (existingColumnName != null) {
				overlappingStatuses.add("'%s' (already in column '%s')".formatted(status.getName(), existingColumnName));
				LOGGER.warn("[KanbanValidation] Status overlap detected: status '{}' (ID: {}) is mapped to both column '{}' and column '{}'",
						status.getName(), status.getId(), existingColumnName, entity.getName());
			}
		});
		// Fail-fast: throw exception if any overlap detected
		if (!overlappingStatuses.isEmpty()) {
			final String errorMessage = String.format(
					"Status overlap detected in kanban line '%s': The following statuses are already mapped to other columns: %s. "
							+ "Each status must be mapped to exactly one column to avoid ambiguity in kanban board display.",
					line.getName(), String.join(", ", overlappingStatuses));
			LOGGER.error("[KanbanValidation] FAIL-FAST: {}", errorMessage);
			throw new CValidationException(errorMessage);
		}
		LOGGER.debug("[KanbanValidation] Status uniqueness validated successfully for column '{}' with {} status(es)", entity.getName(),
				entity.getIncludedStatuses().size());
	}
}
