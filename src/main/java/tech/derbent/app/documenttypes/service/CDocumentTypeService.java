package tech.derbent.app.documenttypes.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.documenttypes.domain.CDocumentType;
import tech.derbent.base.session.service.ISessionService;

/**
 * Service for managing CDocumentType entities.
 * 
 * Provides CRUD operations and business logic for document type management.
 * Document types are company-scoped and can be used to categorize attachments.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Menu(icon = "vaadin:file-text-o", title = "Settings.Document Types")
@PermitAll
public class CDocumentTypeService extends CEntityOfCompanyService<CDocumentType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDocumentTypeService.class);

	public CDocumentTypeService(final IDocumentTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CDocumentType> getEntityClass() {
		return CDocumentType.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CDocumentTypeInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		// Page service not yet implemented - optional for now
		return null;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CDocumentType entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new document type entity");
		// Set default color if not already set
		if (entity.getColor() == null || entity.getColor().isBlank()) {
			entity.setColor(CDocumentType.DEFAULT_COLOR);
		}
	}
}
