package tech.derbent.api.entityOfCompany.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.session.service.ISessionService;

public abstract class CEntityOfCompanyService<EntityClass extends CEntityOfCompany<EntityClass>> extends CEntityNamedService<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityOfCompanyService.class);

	/** Validates that entity name is unique within company scope. Checks both for new entities and updates, excluding current entity ID.
	 * @param repository the repository to query
	 * @param entity     the entity being validated
	 * @param name       the name to check for uniqueness (trimmed)
	 * @param company    the company scope
	 * @param <T>        the entity type
	 * @throws CValidationException if name is not unique */
	protected static <T extends CEntityOfCompany<T>> void validateUniqueNameInCompany(final IEntityOfCompanyRepository<T> repository, final T entity,
			final String name, final CCompany company) {
		Check.notNull(repository, "Repository cannot be null");
		Check.notNull(entity, "Entity cannot be null");
		Check.notBlank(name, "Name cannot be null or empty");
		Check.notNull(company, "Company cannot be null");
		final Optional<T> existing = repository.findByNameIgnoreCaseAndCompany(name.trim(), company);
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			final T existingEntity = existing.get();
			throw new CValidationException(ValidationMessages.formatDuplicate(
				ValidationMessages.DUPLICATE_NAME_IN_COMPANY, 
				name.trim(), 
				existingEntity.getId()));
		}
	}

	public CEntityOfCompanyService(final IEntityOfCompanyRepository<EntityClass> repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public long count() {
		return countByCompany(sessionService.getActiveCompany()
				.orElseThrow(() -> new IllegalStateException("No active company selected, cannot count entities without company context")));
	}

	@Transactional (readOnly = true)
	public long countByCompany(final CCompany company) {
		Check.notNull(company, "Project cannot be null");
		try {
			return ((IEntityOfCompanyRepository<EntityClass>) repository).countByCompany(company);
		} catch (final Exception e) {
			LOGGER.error("Error counting entities by project '{}' in {}: {}", company.getName(), getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	public List<EntityClass> findAll() {
		final CCompany company = sessionService.getActiveCompany()
				.orElseThrow(() -> new IllegalStateException("No active project selected, cannot list entities without company context"));
		return listByCompany(company);
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> findByCompany(final CCompany company, final Pageable pageable) {
		Check.notNull(company, "Company cannot be null");
		final Pageable safe = CPageableUtils.validateAndFix(pageable);
		try {
			return ((IEntityOfCompanyRepository<EntityClass>) repository).findByCompany(company, safe);
		} catch (final RuntimeException ex) {
			LOGGER.error("listByCompany failed (company: {}, page: {}): {}", Optional.ofNullable(company.getName()).orElse("<no-name>"), safe,
					ex.toString(), ex);
			throw ex; // Spring’in exception translation’ını koru
		}
	}

	@Transactional (readOnly = true)
	public Optional<EntityClass> findByNameAndCompany(final String name, final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		Check.notBlank(name, "Entity name cannot be null or empty");
		try {
			return ((IEntityOfCompanyRepository<EntityClass>) repository).findByNameIgnoreCaseAndCompany(name, company);
		} catch (final Exception e) {
			LOGGER.error("Error finding entities by project '{}' in {}: {}", company.getName(), getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	@Transactional (readOnly = true)
	public Optional<EntityClass> getById(final Long id) {
		final Optional<EntityClass> entity = super.getById(id);
		entity.ifPresent(CEntityOfCompany::initializeAllFields);
		return entity;
	}

	@Override
	public EntityClass getRandom() {
		return getRandom(sessionService.getActiveCompany()
				.orElseThrow(() -> new IllegalStateException("No active company selected, cannot get random entity without company context")));
	}

	public EntityClass getRandom(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		final List<EntityClass> all = listByCompany(company);
		if (all.isEmpty()) {
			throw new IllegalStateException("No entities found for company " + company.getName() + " of type " + getEntityClass().getSimpleName());
		}
		final int randomIndex = (int) (Math.random() * all.size());
		return all.get(randomIndex);
	}

	@Override
	public void initializeNewEntity(final Object entity) {
		// LOGGER.debug("Initializing new user entity");
		try {
			super.initializeNewEntity(entity);
			final CCompany currentCompany = CSpringContext.getBean(ISessionService.class).getCurrentCompany();
			Check.notNull(currentCompany, "No active company in session - company context is required to create users");
			((CEntityOfCompany<?>) entity).setCompany(currentCompany);
		} catch (final Exception e) {
			LOGGER.error("Error initializing new user: {}", e.getMessage());
			throw e;
		}
	}

	public List<EntityClass> listByCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		try {
			final List<EntityClass> entities = ((IEntityOfCompanyRepository<EntityClass>) repository).findByCompany(company);
			entities.forEach(EntityClass::initializeAllFields);
			return entities;
		} catch (final RuntimeException ex) {
			LOGGER.error("findByProject failed (company: {}): {}", Optional.ofNullable(company.getName()).orElse("<no-name>"), ex.toString(), ex);
			throw ex; // Spring’in exception translation’ını koru
		}
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> listByCompanyForPageView(final CCompany company, final Pageable pageable, final String searchText) {
		Check.notNull(company, "Company cannot be null");
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final String term = searchText == null ? "" : searchText.trim();
		final List<EntityClass> all = ((IEntityOfCompanyRepository<EntityClass>) repository).listByCompanyForPageView(company);
		all.forEach(EntityClass::initializeAllFields);
		final boolean searchable = ISearchable.class.isAssignableFrom(getEntityClass());
		final List<EntityClass> filtered = term.isEmpty() || !searchable ? all : all.stream().filter(e -> ((ISearchable) e).matches(term)).toList();
		final int start = (int) Math.min(safePage.getOffset(), filtered.size());
		final int end = Math.min(start + safePage.getPageSize(), filtered.size());
		final List<EntityClass> content = filtered.subList(start, end);
		return new PageImpl<>(content, safePage, filtered.size());
	}
	// ========== Static Validation Helper Methods ==========

	@Override
	@Transactional (readOnly = true)
	public Page<EntityClass> listForPageView(final Pageable pageable, final String searchText) throws Exception {
		final CCompany company = sessionService.getActiveCompany()
				.orElseThrow(() -> new IllegalStateException("No active company selected, cannot list entities without company context"));
		return listByCompanyForPageView(company, pageable, searchText);
	}

	@Override
	@Transactional
	public EntityClass newEntity(final String name) {
		final CCompany company = sessionService.getActiveCompany()
				.orElseThrow(() -> new IllegalStateException("No active company selected, cannot list entities without company context"));
		return newEntity(name, company);
	}

	@Transactional
	public EntityClass newEntity(final String name, final CCompany company) {
		Check.notNull(company, "Project cannot be null");
		Check.notBlank(name, "Entity name cannot be null or empty");
		try {
			final Object instance = getEntityClass().getDeclaredConstructor(String.class, CCompany.class).newInstance(name, company);
			Check.instanceOf(instance, getEntityClass(), "Created object is not instance of EntityClass");
			@SuppressWarnings ("unchecked")
			final EntityClass entity = (EntityClass) instance;
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	@Deprecated
	protected void setNameOfEntityXXX(final EntityClass entity, final String prefix) {
		try {
			final Optional<CCompany> activeCompany = sessionService.getActiveCompany();
			activeCompany.map(value -> ((IEntityOfCompanyRepository<?>) repository).countByCompany(value))
					.map(priorityCount -> String.format(prefix + " %02d", priorityCount + 1)).ifPresent(entity::setName);
		} catch (final Exception e) {
			LOGGER.error("Error setting name of entity: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	protected void validateEntity(final EntityClass entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notNull(entity.getCompany(), ValidationMessages.COMPANY_REQUIRED);
		// name is not required always, such as comments etc.
	}

	protected void validateEntityName(final EntityClass entity) {
		// 2. Unique Checks
		// Name must be unique within company
		final String trimmedName = entity.getName() != null ? entity.getName().trim() : "";
		if (trimmedName.isEmpty()) {
			return;
		}
		// Exclude self if updating
		final Optional<EntityClass> existing =
				((IEntityOfCompanyRepository<EntityClass>) repository).findByNameIgnoreCaseAndCompany(trimmedName, entity.getCompany())
						.filter(existingEntity -> entity.getId() == null || !existingEntity.getId().equals(entity.getId()));
		if (existing.isPresent()) {
			final EntityClass existingEntity = existing.get();
			throw new CValidationException(ValidationMessages.formatDuplicate(
				ValidationMessages.DUPLICATE_NAME_IN_COMPANY, 
				trimmedName, 
				existingEntity.getId()));
		}
	}
	
	/** Service-level method to copy CEntityOfCompany-specific fields using direct setters/getters.
	 * Override in concrete services to add entity-specific field copying.
	 * Always call super.copyEntityFieldsTo() first!
	 * 
	 * @param source the source entity to copy from
	 * @param target the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final EntityClass source, final CEntityDB<?> target,
			final CCloneOptions options) {
		// Call parent to copy named entity fields
		super.copyEntityFieldsTo(source, target, options);
		
		// Copy company-scoped entity fields if target supports them
		if (!(target instanceof CEntityOfCompany)) {
			return;
		}
		final CEntityOfCompany<?> targetCompanyEntity = (CEntityOfCompany<?>) target;
		
		// Copy company reference - direct setter/getter
		targetCompanyEntity.setCompany(source.getCompany());
		
		LOGGER.debug("Copied company entity fields for: {}", source.getName());
	}
}
