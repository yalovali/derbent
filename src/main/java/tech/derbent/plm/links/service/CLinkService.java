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

@Service
public class CLinkService extends CEntityOfCompanyService<CLink> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLinkService.class);

    public CLinkService(
            final ILinkRepository repository,
            final Clock clock,
            final ISessionService sessionService) {
        super(repository, clock, sessionService);
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
