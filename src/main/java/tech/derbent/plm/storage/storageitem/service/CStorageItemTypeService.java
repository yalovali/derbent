package tech.derbent.plm.storage.storageitem.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.storage.storageitem.domain.CStorageItemType;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CStorageItemTypeService extends CTypeEntityService<CStorageItemType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CStorageItemTypeService.class);
	private final IStorageItemRepository storageItemRepository;

	public CStorageItemTypeService(final IStorageItemTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IStorageItemRepository storageItemRepository) {
		super(repository, clock, sessionService);
		this.storageItemRepository = storageItemRepository;
	}

	@Override
	public String checkDeleteAllowed(final CStorageItemType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = storageItemRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for storage item type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CStorageItemType> getEntityClass() { return CStorageItemType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CStorageItemTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceStorageItemType.class; }

	@Override
	public Class<?> getServiceClass() { return getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		final CCompany activeCompany = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = ((IStorageItemTypeRepository) repository).countByCompany(activeCompany);
		final String autoName = String.format("StorageItemType %02d", typeCount + 1);
		((CEntityNamed<?>) entity).setName(autoName);
	}

	@Override
	protected void validateEntity(final CStorageItemType entity) {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CStorageItemType> existing =
				((IStorageItemTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
