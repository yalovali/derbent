package tech.derbent.api.entityOfCompany.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.base.session.service.ISessionService;

public abstract class CEntityOfCompanyService<EntityClass extends CEntityOfCompany<EntityClass>> extends CEntityNamedService<EntityClass> {

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

	public EntityClass getRandom(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		final List<EntityClass> all = listByCompany(company);
		if (all.isEmpty()) {
			throw new IllegalStateException("No entities found for company: " + company.getName());
		}
		final int randomIndex = (int) (Math.random() * all.size());
		return all.get(randomIndex);
	}

	public List<EntityClass> listByCompany(final CCompany company) {
		Check.notNull(company, "Project cannot be null");
		try {
			final List<EntityClass> entities = ((IEntityOfCompanyRepository<EntityClass>) repository).findByCompany(company);
			return entities;
		} catch (final RuntimeException ex) {
			LOGGER.error("findByProject failed (company: {}): {}", Optional.ofNullable(company.getName()).orElse("<no-name>"), ex.toString(), ex);
			throw ex; // Spring’in exception translation’ını koru
		}
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
