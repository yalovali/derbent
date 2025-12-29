package tech.derbent.app.kanban.kanbanline.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.screens.service.IOrderedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.base.session.service.ISessionService;

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

	/** Enforces unique default column and status ownership across a line. */
	private void applyStatusAndDefaultConstraints(final CKanbanColumn saved) {
		Check.notNull(saved, "Kanban column cannot be null when applying constraints");
		Check.notNull(saved.getId(), "Kanban column must be saved before enforcing constraints");
		final CKanbanLine line = saved.getKanbanLine();
		Check.notNull(line, "Kanban line cannot be null when enforcing constraints");
		final List<CKanbanColumn> columns = findByMaster(line);
		final Set<Long> includedStatusIds = saved.getIncludedStatuses() == null ? Set.of()
				: saved.getIncludedStatuses().stream().filter(status -> status != null && status.getId() != null).map(status -> status.getId())
						.collect(Collectors.toCollection(HashSet::new));
		final boolean isDefaultColumn = saved.getDefaultColumn();
		for (final CKanbanColumn column : columns) {
			if (column.getId().equals(saved.getId())) {
				continue;
			}
			boolean changed = false;
			if (isDefaultColumn && column.getDefaultColumn()) {
				column.setDefaultColumn(false);
				changed = true;
			}
			if (!includedStatusIds.isEmpty() && column.getIncludedStatuses() != null && !column.getIncludedStatuses().isEmpty()) {
				final List<Long> remainingStatusIds = column.getIncludedStatuses().stream().filter(status -> status != null && status.getId() != null)
						.map(status -> status.getId()).filter(id -> !includedStatusIds.contains(id)).collect(Collectors.toList());
				if (remainingStatusIds.size() != column.getIncludedStatuses().size()) {
					final List<tech.derbent.api.entityOfCompany.domain.CProjectItemStatus> remainingStatuses = column.getIncludedStatuses().stream()
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
	public void initializeNewEntity(final CKanbanColumn entity) {
		super.initializeNewEntity(entity);
		Check.notNull(entity, "Kanban column cannot be null");
		if (entity.getName() == null || entity.getName().isBlank()) {
			entity.setName("Column");
		}
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

	/** Validates column naming and uniqueness within the line. */
	@Override
	protected void validateEntity(final CKanbanColumn entity) {
		super.validateEntity(entity);
		Check.notBlank(entity.getName(), "Kanban column name cannot be blank");
		final CKanbanLine line = entity.getKanbanLine();
		Check.notNull(line, "Kanban line cannot be null for column validation");
		Check.notNull(line.getId(), "Kanban line ID cannot be null for column validation");
		final String trimmedName = entity.getName().trim();
		final CKanbanColumn existing = getTypedRepository().findByMasterAndNameIgnoreCase(line, trimmedName).orElse(null);
		if (existing == null) {
			return;
		}
		if (entity.getId() != null && entity.getId().equals(existing.getId())) {
			return;
		}
		throw new CValidationException("Kanban column name must be unique within the kanban line");
	}
}
