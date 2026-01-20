package tech.derbent.plm.links.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.view.CComponentListLinks;

import tech.derbent.api.validation.ValidationMessages;

@Service
public class CLinkService extends CEntityOfCompanyService<CLink> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLinkService.class);

    public CLinkService(
            final ILinkRepository repository,
            final Clock clock,
            final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }

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

    public Component createComponent() {
        try {
            final CComponentListLinks component = new CComponentListLinks(this, sessionService);
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

    @Override
    public Class<CLink> getEntityClass() {
        return CLink.class;
    }
}
