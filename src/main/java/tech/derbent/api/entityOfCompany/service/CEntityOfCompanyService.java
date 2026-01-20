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
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.ISearchable;
import tech.derbent.api.utils.CPageableUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.base.session.service.ISessionService;

import tech.derbent.api.validation.ValidationMessages;

public abstract class CEntityOfCompanyService<EntityClass extends CEntityOfCompany<EntityClass>> extends CEntityNamedService<EntityClass> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityOfCompanyService.class);

	public CEntityOfCompanyService(final IEntityOfCompanyRepository<EntityClass> repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected void validateEntity(final EntityClass entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notNull(entity.getCompany(), ValidationMessages.COMPANY_REQUIRED);
		
		// 2. Unique Checks
		// Name must be unique within company
		final String trimmedName = entity.getName() != null ? entity.getName().trim() : "";
		if (!trimmedName.isEmpty()) {
			final Optional<EntityClass> existing = ((IEntityOfCompanyRepository<EntityClass>) repository)
					.findByNameIgnoreCaseAndCompany(trimmedName, entity.getCompany()).filter(existingEntity -> {
						// Exclude self if updating
						return entity.getId() == null || !existingEntity.getId().equals(entity.getId());
					});
			
			if (existing.isPresent()) {
				throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
			}
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
			final Optional<EntityClass> entities =
					((IEntityOfCompanyRepository<EntityClass>) repository).findByNameIgnoreCaseAndCompany(name, company);
			return entities;
		} catch (final Exception e) {
			LOGGER.error("Error finding entities by project '{}' in {}: {}", company.getName(), getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	@Override
	public EntityClass getRandom() {
		Check.fail("getRandom without company context is not supported");
		return null;
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

	public List<EntityClass> listByCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		try {
			final List<EntityClass> entities = ((IEntityOfCompanyRepository<EntityClass>) repository).findByCompany(company);
			for (final EntityClass entity : entities) {
				entity.initializeAllFields();
			}
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
		for (final EntityClass entity : all) {
			entity.initializeAllFields();
		}
		final boolean searchable = ISearchable.class.isAssignableFrom(getEntityClass());
		final List<EntityClass> filtered = term.isEmpty() || !searchable ? all : all.stream().filter(e -> ((ISearchable) e).matches(term)).toList();
		final int start = (int) Math.min(safePage.getOffset(), filtered.size());
		final int end = Math.min(start + safePage.getPageSize(), filtered.size());
		final List<EntityClass> content = filtered.subList(start, end);
		return new PageImpl<>(content, safePage, filtered.size());
	}

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

	protected void setNameOfEntity(final EntityClass entity, final String prefix) {
		try {
			final Optional<CCompany> activeCompany = sessionService.getActiveCompany();
			if (activeCompany.isPresent()) {
				final long priorityCount = ((IEntityOfCompanyRepository<?>) repository).countByCompany(activeCompany.get());
				final String autoName = String.format(prefix + " %02d", priorityCount + 1);
				entity.setName(autoName);
			}
		} catch (final Exception e) {
			LOGGER.error("Error setting name of entity: {}", e.getMessage());
			throw e;
		}
	}
}
