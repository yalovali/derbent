package tech.derbent.app.links.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CTextArea;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.ItemsProvider;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.links.domain.CLink;
import tech.derbent.app.links.domain.IHasLinks;
import tech.derbent.app.links.service.CLinkService;
import tech.derbent.base.session.service.ISessionService;

/** CDialogLink - Dialog for adding or editing links between entities.
 * <p>
 * Follows the selection dialog pattern with: - Entity type selector and filtered grid of available targets - Standard filter toolbar for
 * ID/Name/Description/Status - Link type and description fields - Proper validation
 * </p>
 * <p>
 * Add mode (isNew = true): - Creates new link between source entity and selected target - Target entity selection via filtered grid - Link type field
 * (default "Related") - Description text area (optional)
 * </p>
 * <p>
 * Edit mode (isNew = false): - Edits existing link - Source entity info is read-only - Can edit target entity, link type, and description
 * </p>
 */
public class CDialogLink extends CDialogDBEdit<CLink> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogLink.class);
	private static final long serialVersionUID = 1L;
	private static final String TARGET_SELECTION_ID = "link-target-selection";
	private final CEnhancedBinder<CLink> binder;
	private final CLinkService linkService;
	private final ISessionService sessionService;
	private CEntityDB<?> sourceEntity;
	private List<EntityTypeConfig<?>> targetEntityTypes = new ArrayList<>();
	private CComponentEntitySelection<CEntityDB<?>> targetSelection;
	private CTextArea textAreaDescription;
	private CTextField textFieldLinkType;

	/** Constructor for both new and edit modes.
	 * @param linkService    the link service
	 * @param sessionService the session service
	 * @param link           the link entity (new or existing)
	 * @param onSave         callback for save action
	 * @param isNew          true if creating new link, false if editing */
	public CDialogLink(final CLinkService linkService, final ISessionService sessionService, final CLink link, final Consumer<CLink> onSave,
			final boolean isNew) throws Exception {
		super(link, onSave, isNew);
		Check.notNull(linkService, "LinkService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		Check.notNull(link, "Link cannot be null");
		this.linkService = linkService;
		this.sessionService = sessionService;
		binder = CBinderFactory.createEnhancedBinder(CLink.class);
		setupDialog();
		populateForm();
	}

	private List<EntityTypeConfig<?>> buildTargetEntityTypes() {
		final List<EntityTypeConfig<?>> configs = new ArrayList<>();
		for (final String key : CEntityRegistry.getAllRegisteredEntityKeys()) {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(key);
			if (!CEntityDB.class.isAssignableFrom(entityClass)) {
				continue;
			}
			if (!IHasLinks.class.isAssignableFrom(entityClass)) {
				continue;
			}
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
			final Object serviceBean = CSpringContext.getBean(serviceClass);
			Check.notNull(serviceBean, "Service bean not found for entity type: " + entityClass.getSimpleName());
			configs.add(createConfigUnchecked(entityClass, serviceBean));
		}
		configs.sort(Comparator.comparing(EntityTypeConfig::getDisplayName, String.CASE_INSENSITIVE_ORDER));
		return configs;
	}

	@SuppressWarnings ("unchecked")
	private <E extends CEntityDB<E>> EntityTypeConfig<E> createConfigUnchecked(final Class<?> entityClass, final Object serviceBean) {
		return EntityTypeConfig.createWithRegistryName((Class<E>) entityClass, (CAbstractService<E>) serviceBean);
	}

	private void createFormFields() throws Exception {
		Check.notNull(getDialogLayout(), "Dialog layout must be initialized");
		final CVerticalLayout formLayout = new CVerticalLayout();
		formLayout.setPadding(false);
		formLayout.setSpacing(false);
		formLayout.getStyle().set("gap", "12px");
		// Source entity display (read-only)
		if (getEntity().getSourceEntityType() != null && getEntity().getSourceEntityId() != null) {
			try {
				final Class<?> sourceClass = CEntityRegistry.getEntityClass(getEntity().getSourceEntityType());
				final String sourceDisplay =
						String.format("%s #%d", CEntityRegistry.getEntityTitleSingular(sourceClass), getEntity().getSourceEntityId());
				final Span sourceLabel = new Span("Source: " + sourceDisplay);
				sourceLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic")
						.set("margin-bottom", "8px");
				formLayout.add(sourceLabel);
			} catch (final Exception e) {
				LOGGER.debug("Could not display source entity: {}", e.getMessage());
			}
		}
		targetSelection = createTargetSelectionComponent();
		formLayout.add(targetSelection);
		// Link type field
		textFieldLinkType = new CTextField("Link Type");
		textFieldLinkType.setWidthFull();
		textFieldLinkType.setMaxLength(50);
		textFieldLinkType.setPlaceholder("e.g., Related, Depends On, Blocks...");
		textFieldLinkType.setHelperText("Category or type of relationship (max 50 characters)");
		binder.forField(textFieldLinkType).bind(CLink::getLinkType, CLink::setLinkType);
		formLayout.add(textFieldLinkType);
		// Description text area
		textAreaDescription = new CTextArea("Description");
		textAreaDescription.setWidthFull();
		textAreaDescription.setHeight("120px");
		textAreaDescription.setMaxLength(500);
		textAreaDescription.setPlaceholder("Optional description of the link...");
		textAreaDescription.setHelperText("Maximum 500 characters");
		binder.forField(textAreaDescription).bind(CLink::getDescription, CLink::setDescription);
		formLayout.add(textAreaDescription);
		getDialogLayout().add(formLayout);
	}

	private CComponentEntitySelection<CEntityDB<?>> createTargetSelectionComponent() throws Exception {
		targetEntityTypes = buildTargetEntityTypes();
		Check.notEmpty(targetEntityTypes, "No linkable entity types available for selection");
		final ItemsProvider<CEntityDB<?>> itemsProvider = this::loadEntitiesForConfig;
		final CComponentEntitySelection<CEntityDB<?>> selection =
				new CComponentEntitySelection<>(targetEntityTypes, itemsProvider, this::onTargetSelectionChanged, false);
		selection.setId(TARGET_SELECTION_ID);
		selection.setDynamicHeight("320px");
		return selection;
	}

	private List<CEntityDB<?>> filterOutSourceEntity(final List<? extends CEntityDB<?>> items) {
		if (sourceEntity == null) {
			return new ArrayList<>(items);
		}
		final List<CEntityDB<?>> filtered = new ArrayList<>();
		for (final CEntityDB<?> item : items) {
			if (item == null) {
				continue;
			}
			if (sourceEntity.getId() != null && sourceEntity.getId().equals(item.getId()) && sourceEntity.getClass().equals(item.getClass())) {
				continue;
			}
			filtered.add(item);
		}
		return filtered;
	}

	private EntityTypeConfig<?> findEntityTypeConfig(final Class<?> entityClass) {
		for (final EntityTypeConfig<?> config : targetEntityTypes) {
			if (config.getEntityClass().equals(entityClass)) {
				return config;
			}
		}
		return null;
	}

	private Optional<CEntityDB<?>> findTargetEntity(final EntityTypeConfig<?> config, final Long targetId) {
		try {
			final CAbstractService<?> service = config.getService();
			return service.getById(targetId).map(entity -> (CEntityDB<?>) entity);
		} catch (final Exception e) {
			LOGGER.error("Failed to load target entity for link dialog", e);
			CNotificationService.showException("Failed to load target entity", e);
			return Optional.empty();
		}
	}

	@Override
	public String getDialogTitleString() { return isNew ? "Add Link" : "Edit Link"; }

	@Override
	protected Icon getFormIcon() throws Exception { return isNew ? VaadinIcon.CONNECT.create() : VaadinIcon.EDIT.create(); }

	@Override
	protected String getFormTitleString() { return isNew ? "New Link" : "Edit Link"; }

	@Override
	protected String getSuccessCreateMessage() { return "Link created successfully"; }

	@Override
	protected String getSuccessUpdateMessage() { return "Link updated successfully"; }

	private List<CEntityDB<?>> loadEntitiesForConfig(final EntityTypeConfig<?> config) {
		try {
			Check.notNull(config, "Entity type config cannot be null");
			final CAbstractService<?> service = config.getService();
			Check.notNull(service, "Service cannot be null for entity type: " + config.getDisplayName());
			final List<? extends CEntityDB<?>> items = loadItemsFromService(service);
			return filterOutSourceEntity(items);
		} catch (final Exception e) {
			LOGGER.error("Error loading entities for selection", e);
			CNotificationService.showException("Error loading entities", e);
			return List.of();
		}
	}

	private List<? extends CEntityDB<?>> loadItemsFromService(final CAbstractService<?> service) throws Exception {
		if (service instanceof final CEntityOfProjectService<?> projectService) {
			final CProject project = sessionService.getActiveProject()
					.orElseThrow(() -> new IllegalStateException("Active project is required to list project entities"));
			return projectService.listByProject(project);
		}
		if (service instanceof final CEntityOfCompanyService<?> companyService) {
			final CCompany company = sessionService.getActiveCompany()
					.orElseThrow(() -> new IllegalStateException("Active company is required to list company entities"));
			return companyService.listByCompany(company);
		}
		return service.findAll();
	}

	private void onTargetSelectionChanged(final Set<CEntityDB<?>> selectedItems) {
		if (selectedItems == null || selectedItems.isEmpty()) {
			getEntity().setTargetEntityId(null);
			getEntity().setTargetEntityType(null);
			return;
		}
		final CEntityDB<?> selected = selectedItems.iterator().next();
		getEntity().setTargetEntityId(selected.getId());
		getEntity().setTargetEntityType(selected.getClass().getSimpleName());
	}

	@Override
	protected void populateForm() {
		try {
			createFormFields();
			binder.readBean(getEntity());
			restoreTargetSelection();
			if (isNew && (getEntity().getLinkType() == null || getEntity().getLinkType().isEmpty())) {
				textFieldLinkType.setValue("Related");
			}
			LOGGER.debug("Form populated for link: {}", getEntity().getId() != null ? getEntity().getId() : "new");
		} catch (final Exception e) {
			LOGGER.error("Error populating form", e);
			CNotificationService.showException("Error loading link data", e);
		}
	}

	private void restoreTargetSelection() {
		if (targetSelection == null) {
			return;
		}
		final String targetType = getEntity().getTargetEntityType();
		final Long targetId = getEntity().getTargetEntityId();
		if (targetType == null || targetId == null) {
			return;
		}
		final Class<?> entityClass = CEntityRegistry.getEntityClass(targetType);
		final EntityTypeConfig<?> config = findEntityTypeConfig(entityClass);
		if (config == null) {
			return;
		}
		targetSelection.setEntityType(config);
		findTargetEntity(config, targetId).ifPresent(entity -> targetSelection.setValue(Set.of(entity)));
	}

	/** Set the source entity for new links.
	 * @param sourceEntity the source entity that owns the link */
	public void setSourceEntity(final CEntityDB<?> sourceEntity) {
		this.sourceEntity = sourceEntity;
		if (isNew && sourceEntity != null) {
			getEntity().setSourceEntityType(sourceEntity.getClass().getSimpleName());
			getEntity().setSourceEntityId(sourceEntity.getId());
		}
	}

	@Override
	protected void setupContent() throws Exception {
		super.setupContent();
		setWidthFull();
		setMaxWidth("600px");
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
		// Validate target entity type and ID are set
		if (getEntity().getTargetEntityType() == null) {
			throw new IllegalStateException("Please select a target entity type");
		}
		if (getEntity().getTargetEntityId() == null) {
			throw new IllegalStateException("Please select a target entity");
		}
		// Validate target entity type is valid and implements IHasLinks
		final String targetType = getEntity().getTargetEntityType();
		try {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(targetType);
			if (entityClass == null) {
				throw new IllegalStateException("Invalid target entity type: " + targetType);
			}
			if (!IHasLinks.class.isAssignableFrom(entityClass)) {
				throw new IllegalStateException("Target entity type does not support links: " + targetType);
			}
		} catch (final Exception e) {
			throw new IllegalStateException("Invalid target entity type: " + targetType);
		}
		// Validate not linking to self
		if (getEntity().getSourceEntityType().equals(getEntity().getTargetEntityType())
				&& getEntity().getSourceEntityId().equals(getEntity().getTargetEntityId())) {
			throw new IllegalStateException("Cannot create a link from an entity to itself");
		}
		// Set company from session if not set
		if (getEntity().getCompany() == null) {
			final CCompany company =
					sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("Active company is required to save links"));
			getEntity().setCompany(company);
		}
		// Save link
		linkService.save(getEntity());
		LOGGER.debug("Link validated and saved: {}", getEntity().getId());
	}
}
