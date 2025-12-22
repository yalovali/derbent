package tech.derbent.app.kanban.kanbanline.service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanColumn;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CKanbanLineService extends CEntityOfCompanyService<CKanbanLine> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanLineService.class);

	public CKanbanLineService(final IKanbanLineRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CKanbanLine> getEntityClass() { return CKanbanLine.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CKanbanLineInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceKanbanLine.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CKanbanLine entity) {
		super.initializeNewEntity(entity);
		Check.notNull(entity, "Entity cannot be null");
		LOGGER.debug("Initializing new Kanban line");
		setNameOfEntity(entity, "Kanban Line");
		if (entity.getKanbanColumns() == null) {
			entity.setKanbanColumns(new java.util.LinkedHashSet<>());
		}
	}

	@Override
	protected void validateEntity(final CKanbanLine entity) {
		super.validateEntity(entity);
		Check.notBlank(entity.getName(), "Kanban line name cannot be blank");
		final CCompany company =
				entity.getCompany() != null ? entity.getCompany() : sessionService.getActiveCompany().orElse(null);
		Check.notNull(company, "Company cannot be null for kanban line validation");
		final String trimmedName = entity.getName().trim();
		final CKanbanLine existing = findByNameAndCompany(trimmedName, company).orElse(null);
		if (existing == null) {
			return;
		}
		if (entity.getId() != null && entity.getId().equals(existing.getId())) {
			return;
		}
		throw new CValidationException("Kanban line name must be unique within the company");
	}

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

	private void normalizeKanbanColumnOrder(final CKanbanLine line) {
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

	private CKanbanColumn resolveColumnForDelete(final CKanbanLine line, final CKanbanColumn column) {
		if (line.getKanbanColumns().contains(column)) {
			return column;
		}
		final CKanbanColumn resolved = line.getKanbanColumns().stream()
				.filter(item -> item.getId() != null && item.getId().equals(column.getId())).findFirst().orElse(null);
		Check.notNull(resolved, "Kanban column not found on kanban line for delete");
		return resolved;
	}
}
