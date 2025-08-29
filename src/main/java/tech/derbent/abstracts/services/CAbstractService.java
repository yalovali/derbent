package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.annotations.CSpringAuxillaries;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.interfaces.CSearchable;
import tech.derbent.abstracts.utils.CPageableUtils;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.session.service.CSessionService;

/** CAbstractService - Abstract base service class for entity operations. Layer: Service (MVC) Provides common CRUD operations and lazy loading
 * support for all entity types. */
public abstract class CAbstractService<EntityClass extends CEntityDB<EntityClass>> {
	protected final Clock clock;
	protected final CAbstractRepository<EntityClass> repository;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected @Nullable CSessionService sessionService;

	public CAbstractService(final CAbstractRepository<EntityClass> repository, final Clock clock) {
		this.clock = clock;
		this.repository = repository;
		this.sessionService = null;
		Check.notNull(repository, "repository cannot be null");
	}

	public CAbstractService(final CAbstractRepository<EntityClass> repository, final Clock clock, final CSessionService sessionService) {
		this.clock = clock;
		this.repository = repository;
		this.sessionService = sessionService;
		Check.notNull(repository, "repository cannot be null");
	}

	protected List<EntityClass> applySort(final List<EntityClass> input, final Sort sort) {
		if ((sort == null) || sort.isUnsorted() || (input == null) || input.isEmpty()) {
			return input;
		}
		final Map<String, Function<EntityClass, ?>> keyFns = getSortKeyExtractors();
		Comparator<EntityClass> chain = null;
		for (final Sort.Order o : sort) {
			final var keyFn = keyFns.get(o.getProperty());
			if (keyFn == null) {
				continue; // tanımadığımız kolonları atla
			}
			// El yapımı comparator: nulls last + Comparable check
			java.util.Comparator<EntityClass> c = (a, b) -> {
				final Object va = keyFn.apply(a);
				final Object vb = keyFn.apply(b);
				if (va == vb) {
					return 0;
				}
				if (va == null) {
					return 1; // nulls last
				}
				if (vb == null) {
					return -1;
				}
				if (va instanceof final Comparable<?> ca && vb instanceof final Comparable<?> cb) {
					@SuppressWarnings ("unchecked")
					final int cmp = ((Comparable<Object>) ca).compareTo(cb);
					return cmp;
				}
				return 0; // karşılaştırılamıyorsa eşit say
			};
			if (o.isDescending()) {
				c = c.reversed();
			}
			chain = (chain == null) ? c : chain.thenComparing(c);
		}
		if (chain == null) {
			return input;
		}
		final ArrayList<EntityClass> copy = new ArrayList<>(input);
		copy.sort(chain);
		return copy;
	}

	public long count() {
		// LOGGER.debug("Counting entities in {}", getClass().getSimpleName());
		return repository.count();
	}

	@Transactional
	public EntityClass createEntity() {
		try {
			final EntityClass entity = newEntity();
			repository.saveAndFlush(entity);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	@Transactional
	public void delete(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(entity.getId(), "Entity ID cannot be null");
		LOGGER.debug("Deleting entity: {}", CSpringAuxillaries.safeToString(entity));
		repository.deleteById(entity.getId());
	}

	@Transactional
	public void delete(final Long id) {
		Check.notNull(id, "Entity ID cannot be null");
		LOGGER.debug("Deleting entity with ID: {}", id);
		repository.deleteById(id);
	}

	/** Enhanced delete method that attempts soft delete using reflection before hard delete.
	 * @param entity the entity to delete */
	@Transactional
	public void deleteWithReflection(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		// Try soft delete first using reflection
		if (entity.performSoftDelete()) {
			// Soft delete was successful, save the entity
			repository.save(entity);
			LOGGER.info("Performed soft delete for entity: {}", entity.getClass().getSimpleName());
		} else {
			// No soft delete field found, perform hard delete
			repository.delete(entity);
			LOGGER.info("Performed hard delete for entity: {}", entity.getClass().getSimpleName());
		}
	}

	/** Enhanced delete by ID method that attempts soft delete using reflection.
	 * @param id the ID of the entity to delete */
	@Transactional
	public void deleteWithReflection(final Long id) {
		final EntityClass entity = repository.findById(id).orElseThrow(() -> new RuntimeException("Entity not found with ID: " + id));
		deleteWithReflection(entity);
	}

	@PreAuthorize ("permitAll()")
	public List<EntityClass> findAll() {
		return repository.findAll();
	}

	@Transactional (readOnly = true)
	public Optional<EntityClass> getById(final Long id) {
		if (id == null) {
			return Optional.empty();
		}
		final Optional<EntityClass> entity = repository.findById(id);
		return entity;
	}

	protected abstract Class<EntityClass> getEntityClass();

	/** Varsayılan sıralama anahtarları. İstediğiniz entity servisinde override edebilirsiniz. */
	protected Map<String, Function<EntityClass, ?>> getSortKeyExtractors() {
		return Map.of();
	}

	protected void initializeLazyRelationship(final Object relationshipEntity, final String relationshipName) {
		if (relationshipEntity == null) {
			return;
		}
		try {
			final boolean success = CSpringAuxillaries.initializeLazily(relationshipEntity);
			if (!success) {
				LOGGER.warn("Failed to initialize lazy relationship '{}': {}", relationshipName, CSpringAuxillaries.safeToString(relationshipEntity));
			}
		} catch (final Exception e) {
			LOGGER.warn("Error initializing lazy relationship '{}': {}", relationshipName, CSpringAuxillaries.safeToString(relationshipEntity), e);
		}
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable) {
		LOGGER.debug("Listing entities with pageable");
		// Validate and fix pageable to prevent "max-results cannot be negative" error
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		// LOGGER.debug("Listing entities with pageable: {}", safePage);
		final Page<EntityClass> entities = repository.findAll(safePage);
		// Initialize lazy fields for all entities
		return entities;
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable, final Specification<EntityClass> filter) {
		LOGGER.debug("Listing entities with filter specification");
		// Validate and fix pageable to prevent "max-results cannot be negative" error
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		// LOGGER.debug("Listing entities with filter and pageable");
		final Page<EntityClass> page = repository.findAll(filter, safePage);
		// Initialize lazy fields for all entities in the page
		return page;
	}

	@Transactional (readOnly = true)
	public Page<EntityClass> list(final Pageable pageable, final String searchText) {
		final Pageable safePage = CPageableUtils.validateAndFix(pageable);
		final String term = (searchText == null) ? "" : searchText.trim();
		// Pull all for project (ensure repo method DOES NOT fetch to-many relations!)
		final List<EntityClass> all = repository.findAll(Pageable.unpaged()).getContent();
		final boolean searchable = CSearchable.class.isAssignableFrom(getEntityClass());
		final List<EntityClass> filtered = (term.isEmpty() || !searchable) ? all : all.stream().filter(e -> ((CSearchable) e).matches(term)).toList();
		// --- apply sort from Pageable (name/id supported here; override to extend)
		final List<EntityClass> sorted = applySort(filtered, safePage.getSort());
		// --- slice
		final int start = (int) Math.min(safePage.getOffset(), sorted.size());
		final int end = Math.min(start + safePage.getPageSize(), sorted.size());
		final List<EntityClass> content = sorted.subList(start, end);
		return new PageImpl<>(content, safePage, filtered.size());
	}

	public EntityClass newEntity() {
		try {
			// Get constructor that takes a String parameter and invoke it with the name
			final Object instance = getEntityClass().getDeclaredConstructor().newInstance();
			if (!getEntityClass().isInstance(instance)) {
				throw new IllegalStateException("Created object is not instance of T");
			}
			@SuppressWarnings ("unchecked")
			final EntityClass entity = ((EntityClass) instance);
			return entity;
		} catch (final Exception e) {
			throw new RuntimeException("Failed to create instance of " + getEntityClass().getName(), e);
		}
	}

	public boolean onBeforeSaveEvent(final EntityClass entity) {
		return true;
	}

	@Transactional
	public EntityClass save(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		return repository.save(entity);
	}

	protected void validateEntity(final EntityClass entity) {
		Check.notNull(entity, "Entity cannot be null");
		// Add more validation logic in subclasses if needed
	}
}
