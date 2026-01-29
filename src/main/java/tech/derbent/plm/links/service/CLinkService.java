package tech.derbent.plm.links.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.view.CComponentLink;

@Service
public class CLinkService extends CEntityOfCompanyService<CLink> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CLinkService.class);

	public static CEntityDB<?> getTargetEntity(final CLink link) {
		try {
			final String entityType = link.getTargetEntityType();
			final Long entityId = link.getTargetEntityId();
			if (entityType == null || entityId == null) {
				LOGGER.warn("[LinkGrid] Link #{} has null target - type: {}, id: {}", link.getId(), entityType, entityId);
				return null;
			}
			final Class<?> entityClass = CEntityRegistry.getEntityClass(entityType);
			final CAbstractService<?> service = CSpringContext.getServiceClass(entityClass);
			final CEntityDB<?> entity = service.getById(entityId).orElseThrow();
			return entity;
		} catch (final Exception e) {
			LOGGER.error("[LinkGrid] Error loading target entity: {}", e.getMessage(), e);
			return null;
		}
	}

	public CLinkService(final ILinkRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	public Component createComponent() {
		try {
			final CComponentLink component = new CComponentLink(this, sessionService);
			LOGGER.debug("Created link component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create link component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading link component: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	/**
	 * Service-level method to copy CLink-specific fields.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CLink source, final CEntityDB<?> target, final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CLink)) {
			return;
		}
		final CLink targetLink = (CLink) target;
		
		// Copy link fields
		targetLink.setDescription(source.getDescription());
		targetLink.setLinkType(source.getLinkType());
		
		// CRITICAL: Copy entity references (required fields)
		targetLink.setSourceEntityType(source.getSourceEntityType());
		targetLink.setSourceEntityId(source.getSourceEntityId());
		targetLink.setTargetEntityType(source.getTargetEntityType());
		targetLink.setTargetEntityId(source.getTargetEntityId());
		
		LOGGER.debug("Copied CLink '{}' with options: {}", source.getName(), options);
	}

	@Override
	public Class<CLink> getEntityClass() { return CLink.class; }

	@Override
	public Class<?> getPageServiceClass() { return null; }

	@Override
	public Class<?> getServiceClass() { return CLinkService.class; }

	@Override
	protected void validateEntity(final CLink entity) {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notNull(entity.getSourceEntityId(), "Source Entity ID is required");
		Check.notBlank(entity.getSourceEntityType(), "Source Entity Type is required");
		Check.notNull(entity.getTargetEntityId(), "Target Entity ID is required");
		Check.notBlank(entity.getTargetEntityType(), "Target Entity Type is required");
		// 2. Length Checks
		if (entity.getSourceEntityType().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Source Entity Type cannot exceed %d characters", 100));
		}
		if (entity.getTargetEntityType().length() > 100) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Target Entity Type cannot exceed %d characters", 100));
		}
		if (entity.getLinkType() != null && entity.getLinkType().length() > 50) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Link Type cannot exceed %d characters", 50));
		}
		if (entity.getDescription() != null && entity.getDescription().length() > 500) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength("Description cannot exceed %d characters", 500));
		}
	}
}
