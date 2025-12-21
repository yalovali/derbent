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
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.screens.service.IOrderedEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CKanbanColumnService extends CAbstractService<CKanbanColumn>
		implements IEntityRegistrable, IOrderedEntityService<CKanbanColumn> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanColumnService.class);

	public CKanbanColumnService(final IKanbanColumnRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CKanbanColumn> getEntityClass() { return CKanbanColumn.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CKanbanColumnInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return null; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	public List<CKanbanColumn> findByMaster(final CKanbanLine master) {
		Check.notNull(master, "Master kanban line cannot be null");
		if (master.getId() == null) {
			return List.of();
		}
		return getTypedRepository().findByMaster(master);
	}

	public Integer getNextItemOrder(final CKanbanLine master) {
		Check.notNull(master, "Master kanban line cannot be null");
		if (master.getId() == null) {
			return 1;
		}
		return getTypedRepository().getNextItemOrder(master);
	}

	private IKanbanColumnRepository getTypedRepository() { return (IKanbanColumnRepository) repository; }

	@Override
	public void initializeNewEntity(final CKanbanColumn entity) {
		super.initializeNewEntity(entity);
		Check.notNull(entity, "Kanban column cannot be null");
		if (entity.getName() == null || entity.getName().isBlank()) {
			entity.setName("Column");
		}
	}

	@Override
	@Transactional
	public void moveItemDown(final CKanbanColumn childItem) {
		Check.notNull(childItem, "Kanban column cannot be null");
		final CKanbanLine master = childItem.getKanbanLine();
		Check.notNull(master, "Kanban line cannot be null for column");
		final List<CKanbanColumn> items = normalizeItemOrder(master);
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(childItem.getId()) && (i < (items.size() - 1))) {
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

	@Override
	@Transactional
	public void moveItemUp(final CKanbanColumn childItem) {
		Check.notNull(childItem, "Kanban column cannot be null");
		final CKanbanLine master = childItem.getKanbanLine();
		Check.notNull(master, "Kanban line cannot be null for column");
		final List<CKanbanColumn> items = normalizeItemOrder(master);
		for (int i = 0; i < items.size(); i++) {
			if (items.get(i).getId().equals(childItem.getId()) && (i > 0)) {
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

	@Override
	@Transactional
	public CKanbanColumn save(final CKanbanColumn entity) {
		Check.notNull(entity, "Kanban column cannot be null");
		if (entity.getItemOrder() == null || entity.getItemOrder() <= 0) {
			entity.setItemOrder(getNextItemOrder(entity.getKanbanLine()));
		}
		final CKanbanColumn saved = super.save(entity);
		applyStatusAndDefaultConstraints(saved);
		return saved;
	}

	@Override
	@Transactional
	public void delete(final CKanbanColumn entity) {
		Check.notNull(entity, "Kanban column cannot be null");
		LOGGER.debug("Deleting kanban column: {}", entity.getId());
		if (entity.getKanbanLine() != null) {
			entity.getKanbanLine().removeKanbanColumn(entity);
		}
		super.delete(entity);
	}

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
}
