package tech.derbent.app.links.view;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.ui.component.basic.CTextArea;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.links.domain.CLink;
import tech.derbent.app.links.service.CLinkService;
import tech.derbent.base.session.service.ISessionService;
import com.vaadin.flow.component.html.Span;

/**
 * CDialogLink - Dialog for adding or editing links.
 * <p>
 * Add mode (isNew = true):
 * - Creates new link between source entity and target entity
 * - Target entity type selection via combo box
 * - Target entity ID input (simple for now, picker dialog later)
 * - Link type field (default "Related")
 * - Description text area (optional)
 * <p>
 * Edit mode (isNew = false):
 * - Edits existing link
 * - Source entity info is read-only
 * - Can edit target entity, link type, and description
 */
public class CDialogLink extends CDialogDBEdit<CLink> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDialogLink.class);
    private static final long serialVersionUID = 1L;

    private final CLinkService linkService;
    private final ISessionService sessionService;
    private final CEnhancedBinder<CLink> binder;

    private CComboBox<String> comboTargetEntityType;
    private CTextField textFieldTargetEntityId;
    private CTextField textFieldLinkType;
    private CTextArea textAreaDescription;
    private CEntityDB<?> sourceEntity;

    /**
     * Constructor for both new and edit modes.
     * 
     * @param linkService the link service
     * @param sessionService the session service
     * @param link the link entity (new or existing)
     * @param onSave callback for save action
     * @param isNew true if creating new link, false if editing
     */
    public CDialogLink(final CLinkService linkService, final ISessionService sessionService, final CLink link,
            final Consumer<CLink> onSave, final boolean isNew) throws Exception {
        super(link, onSave, isNew);
        Check.notNull(linkService, "LinkService cannot be null");
        Check.notNull(sessionService, "SessionService cannot be null");
        Check.notNull(link, "Link cannot be null");

        this.linkService = linkService;
        this.sessionService = sessionService;
        this.binder = CBinderFactory.createEnhancedBinder(CLink.class);

        setupDialog();
        populateForm();
    }

    /**
     * Set the source entity for new links.
     * 
     * @param sourceEntity the source entity that owns the link
     */
    public void setSourceEntity(final CEntityDB<?> sourceEntity) {
        this.sourceEntity = sourceEntity;
        if (isNew && sourceEntity != null) {
            getEntity().setSourceEntityType(sourceEntity.getClass().getSimpleName());
            getEntity().setSourceEntityId(sourceEntity.getId());
        }
    }

    private void createFormFields() throws Exception {
        Check.notNull(getDialogLayout(), "Dialog layout must be initialized");

        final CVerticalLayout formLayout = new CVerticalLayout();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        // Source entity display (read-only)
        if (getEntity().getSourceEntityType() != null && getEntity().getSourceEntityId() != null) {
            final String sourceDisplay = String.format("%s #%d", 
                CEntityRegistry.getEntityTitleSingular(getEntity().getSourceEntityType()),
                getEntity().getSourceEntityId());
            final Span sourceLabel = new Span("Source: " + sourceDisplay);
            sourceLabel.getStyle()
                .set("font-size", "0.875rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic")
                .set("margin-bottom", "8px");
            formLayout.add(sourceLabel);
        }

        // Target entity type selection
        comboTargetEntityType = new CComboBox<>("Target Entity Type");
        comboTargetEntityType.setWidthFull();
        comboTargetEntityType.setRequired(true);
        comboTargetEntityType.setItems(CEntityRegistry.getAllRegisteredEntityKeys());
        comboTargetEntityType.setItemLabelGenerator(entityType -> {
            try {
                return CEntityRegistry.getEntityTitleSingular(entityType);
            } catch (final Exception e) {
                LOGGER.warn("Could not get entity title for: {}", entityType);
                return entityType;
            }
        });
        comboTargetEntityType.setPlaceholder("Select target entity type...");
        binder.forField(comboTargetEntityType)
            .asRequired("Target entity type is required")
            .bind(CLink::getTargetEntityType, CLink::setTargetEntityType);
        formLayout.add(comboTargetEntityType);

        // Target entity ID input (simple for now)
        textFieldTargetEntityId = new CTextField("Target Entity ID");
        textFieldTargetEntityId.setWidthFull();
        textFieldTargetEntityId.setRequired(true);
        textFieldTargetEntityId.setPlaceholder("Enter target entity ID...");
        textFieldTargetEntityId.setHelperText("Enter the ID of the entity to link to");
        binder.forField(textFieldTargetEntityId)
            .asRequired("Target entity ID is required")
            .withConverter(
                // String -> Long
                idString -> {
                    if (idString == null || idString.trim().isEmpty()) {
                        return null;
                    }
                    try {
                        return Long.parseLong(idString.trim());
                    } catch (final NumberFormatException e) {
                        return null;
                    }
                },
                // Long -> String
                id -> id != null ? id.toString() : ""
            )
            .bind(CLink::getTargetEntityId, CLink::setTargetEntityId);
        formLayout.add(textFieldTargetEntityId);

        // Link type field
        textFieldLinkType = new CTextField("Link Type");
        textFieldLinkType.setWidthFull();
        textFieldLinkType.setMaxLength(50);
        textFieldLinkType.setPlaceholder("e.g., Related, Depends On, Blocks...");
        textFieldLinkType.setHelperText("Category or type of relationship (max 50 characters)");
        binder.forField(textFieldLinkType)
            .bind(CLink::getLinkType, CLink::setLinkType);
        formLayout.add(textFieldLinkType);

        // Description text area
        textAreaDescription = new CTextArea("Description");
        textAreaDescription.setWidthFull();
        textAreaDescription.setHeight("120px");
        textAreaDescription.setMaxLength(500);
        textAreaDescription.setPlaceholder("Optional description of the link...");
        textAreaDescription.setHelperText("Maximum 500 characters");
        binder.forField(textAreaDescription)
            .bind(CLink::getDescription, CLink::setDescription);
        formLayout.add(textAreaDescription);

        getDialogLayout().add(formLayout);
    }

    @Override
    public String getDialogTitleString() {
        return isNew ? "Add Link" : "Edit Link";
    }

    @Override
    protected Icon getFormIcon() throws Exception {
        return isNew ? VaadinIcon.CONNECT.create() : VaadinIcon.EDIT.create();
    }

    @Override
    protected String getFormTitleString() {
        return isNew ? "New Link" : "Edit Link";
    }

    @Override
    protected String getSuccessCreateMessage() {
        return "Link created successfully";
    }

    @Override
    protected String getSuccessUpdateMessage() {
        return "Link updated successfully";
    }

    @Override
    protected void populateForm() {
        try {
            createFormFields();
            binder.readBean(getEntity());
            
            // Set default link type if new
            if (isNew && (getEntity().getLinkType() == null || getEntity().getLinkType().isEmpty())) {
                textFieldLinkType.setValue("Related");
            }
            
            LOGGER.debug("Form populated for link: {}", getEntity().getId() != null ? getEntity().getId() : "new");
        } catch (final Exception e) {
            LOGGER.error("Error populating form", e);
            CNotificationService.showException("Error loading link data", e);
        }
    }

    @Override
    protected void setupContent() throws Exception {
        super.setupContent();
        setWidth("600px");
    }

    @Override
    protected void validateForm() {
        // Validate using binder
        if (!binder.writeBeanIfValid(getEntity())) {
            throw new IllegalStateException("Please correct validation errors");
        }

        // Validate source entity fields are set
        if (getEntity().getSourceEntityType() == null || getEntity().getSourceEntityId() == null) {
            throw new IllegalStateException("Source entity information is required");
        }

        // Validate target entity type is valid
        final String targetType = getEntity().getTargetEntityType();
        if (targetType != null) {
            try {
                final Class<?> entityClass = CEntityRegistry.getEntityClass(targetType);
                if (entityClass == null) {
                    throw new IllegalStateException("Invalid target entity type: " + targetType);
                }
            } catch (final Exception e) {
                throw new IllegalStateException("Invalid target entity type: " + targetType);
            }
        }

        // Set company from session
        if (getEntity().getCompany() == null) {
            getEntity().setCompany(sessionService.getActiveCompany().orElse(null));
        }

        // Save link
        linkService.save(getEntity());

        LOGGER.debug("Link validated and saved: {}", getEntity().getId());
    }
}
