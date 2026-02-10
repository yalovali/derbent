package tech.derbent.plm.links.view;

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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayoutTop;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.EntityTypeConfig;
import tech.derbent.api.ui.component.enhanced.CComponentEntitySelection.ItemsProvider;
import tech.derbent.api.ui.dialogs.CDialogDBEdit;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.links.service.CLinkService;

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
	private final CFormBuilder<CLink> formBuilder;
	private final CLinkService linkService;
	private final ISessionService sessionService;
	private CEntityDB<?> sourceEntity;
	private List<EntityTypeConfig<?>> targetEntityTypes = new ArrayList<>();
	private Span targetInfoLabel; // Header label showing current target selection
	private CComponentEntitySelection<CEntityDB<?>> targetSelection;

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
		formBuilder = new CFormBuilder<>();
		setupDialog();
		populateForm();
	}

	private List<EntityTypeConfig<?>> buildTargetEntityTypes() {
		final List<EntityTypeConfig<?>> configs = new ArrayList<>();
		for (final String key : CEntityRegistry.getAllRegisteredEntityKeys()) {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(key);
			// RULE: Links only support CEntityNamed and above (not raw CEntityDB)
			if (!CEntityNamed.class.isAssignableFrom(entityClass)) {
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
		formLayout.setSizeFull(); // Fill dialog space
		formLayout.getStyle().set("gap", "12px").set("width", "100%") // Explicit width
				.set("box-sizing", "border-box"); // Include padding in width calculation
		// Header with Source and Target labels (read-only, side by side)
		final HorizontalLayout headerLayout = new HorizontalLayout();
		headerLayout.setWidthFull();
		headerLayout.setSpacing(true);
		headerLayout.getStyle().set("margin-bottom", "8px");
		// Source entity display (read-only)
		if (getEntity().getSourceEntityType() != null && getEntity().getSourceEntityId() != null) {
			try {
				final Class<?> sourceClass = CEntityRegistry.getEntityClass(getEntity().getSourceEntityType());
				final String sourceDisplay =
						String.format("%s #%d", CEntityRegistry.getEntityTitleSingular(sourceClass), getEntity().getSourceEntityId());
				final Span sourceLabel = new Span("Source: " + sourceDisplay);
				sourceLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
				headerLayout.add(sourceLabel);
			} catch (final Exception e) {
				LOGGER.debug("Could not display source entity: {}", e.getMessage());
			}
		}
		// Target entity display (shows selected target, updated dynamically)
		targetInfoLabel = new Span("Target: (not selected)");
		targetInfoLabel.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-error-color)").set("font-style", "italic");
		headerLayout.add(targetInfoLabel);
		// Update target label if in edit mode with existing target
		if (!isNew && getEntity().getTargetEntityType() != null && getEntity().getTargetEntityId() != null) {
			updateTargetInfoLabel(getEntity().getTargetEntityType(), getEntity().getTargetEntityId());
		}
		formLayout.add(headerLayout);
		// Target entity selection component (special component, not in FormBuilder)
		targetSelection = createTargetSelectionComponent();
		formLayout.add(targetSelection);
		formLayout.setFlexGrow(1, targetSelection); // Allow targetSelection to expand and fill space
		// Use FormBuilder for standard fields (linkType, description)
		final List<String> fields = List.of("linkType", "description");
		final CVerticalLayoutTop formFields = formBuilder.build(CLink.class, binder, fields);
		formFields.setWidthFull(); // Ensure form fields take full width
		formLayout.add(formFields);
		getDialogLayout().add(formLayout);
		getDialogLayout().setFlexGrow(1, formLayout); // Allow formLayout to expand in dialog
	}

	private CComponentEntitySelection<CEntityDB<?>> createTargetSelectionComponent() throws Exception {
		targetEntityTypes = buildTargetEntityTypes();
		Check.notEmpty(targetEntityTypes, "No linkable entity types available for selection");
		final ItemsProvider<CEntityDB<?>> itemsProvider = this::loadEntitiesForConfig;
		final CComponentEntitySelection<CEntityDB<?>> selection =
				new CComponentEntitySelection<>(targetEntityTypes, itemsProvider, this::onTargetSelectionChanged, false);
		selection.setId(TARGET_SELECTION_ID);
		selection.setFixedGridHeight("350px"); // Fixed height prevents shrinking
		// selection.setWidthFull(); // Ensure full width
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
			final CProject<?> project = sessionService.getActiveProject()
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
		LOGGER.debug("[DialogLink] Target selection changed: {} items selected (isNew: {})", selectedItems != null ? selectedItems.size() : 0, isNew);
		if (selectedItems == null || selectedItems.isEmpty()) {
			// CRITICAL: Don't clear fields during form initialization in edit mode
			// When dialog opens in edit mode, createFormFields() triggers grid load which fires
			// this event with empty selection BEFORE we restore the actual selection
			if (!isNew) {
				LOGGER.debug("[DialogLink] Ignoring selection clear in edit mode during initialization");
				return;
			}
			// Preserve existing selection if a temporary filter clears the grid
			if (getEntity().getTargetEntityType() != null && getEntity().getTargetEntityId() != null) {
				LOGGER.debug("[DialogLink] Ignoring selection clear; target already set");
				return;
			}
			getEntity().setTargetEntityId(null);
			getEntity().setTargetEntityType(null);
			updateTargetInfoLabel(null, null);
			LOGGER.debug("[DialogLink] Target entity cleared");
			return;
		}
		final CEntityDB<?> selected = selectedItems.iterator().next();
		getEntity().setTargetEntityId(selected.getId());
		getEntity().setTargetEntityType(selected.getClass().getSimpleName());
		updateTargetInfoLabel(selected.getClass().getSimpleName(), selected.getId());
		LOGGER.debug("[DialogLink] Target entity set: {} #{}", selected.getClass().getSimpleName(), selected.getId());
	}

	@Override
	protected void populateForm() {
		try {
			createFormFields();
			// Set default link type for new links
			if (isNew && (getEntity().getLinkType() == null || getEntity().getLinkType().isEmpty())) {
				getEntity().setLinkType("Related");
			}
			// CRITICAL FIX: Save target entity info BEFORE readBean
			// readBean() clears fields that are not bound to form components
			final String savedTargetType = getEntity().getTargetEntityType();
			final Long savedTargetId = getEntity().getTargetEntityId();
			LOGGER.debug("[DialogLink] BEFORE readBean - targetType: {}, targetId: {}", savedTargetType, savedTargetId);
			// Read entity data into form fields (only linkType and description are bound)
			binder.readBean(getEntity());
			// CRITICAL FIX: Restore target entity info AFTER readBean
			getEntity().setTargetEntityType(savedTargetType);
			getEntity().setTargetEntityId(savedTargetId);
			LOGGER.debug("[DialogLink] AFTER restore - targetType: {}, targetId: {}", getEntity().getTargetEntityType(),
					getEntity().getTargetEntityId());
			// Restore target selection in edit mode
			restoreTargetSelection();
			// Auto-select first available target in new mode if none selected yet
			if (isNew && getEntity().getTargetEntityType() == null && targetSelection != null) {
				final boolean selected = targetSelection.selectFirstAvailable();
				LOGGER.debug("[DialogLink] Auto-select first target in new mode: {}", selected);
			}
			LOGGER.debug("Form populated for link: {}", getEntity().getId() != null ? getEntity().getId() : "new");
		} catch (final Exception e) {
			LOGGER.error("Error populating form", e);
			CNotificationService.showException("Error loading link data", e);
		}
	}

	private void restoreTargetSelection() {
		if (targetSelection == null) {
			LOGGER.debug("[DialogLink] Target selection component is null");
			return;
		}
		final String targetType = getEntity().getTargetEntityType();
		final Long targetId = getEntity().getTargetEntityId();
		if (targetType == null || targetId == null) {
			LOGGER.debug("[DialogLink] Target type or ID is null in edit mode - skipping restore");
			return;
		}
		try {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(targetType);
			if (entityClass == null) {
				LOGGER.warn("[DialogLink] Could not find entity class for type: {}", targetType);
				return;
			}
			final EntityTypeConfig<?> config = findEntityTypeConfig(entityClass);
			if (config == null) {
				LOGGER.warn("[DialogLink] Could not find entity type config for class: {}", entityClass.getSimpleName());
				return;
			}
			LOGGER.debug("[DialogLink] Restoring target selection: {} #{}", targetType, targetId);
			// Set entity type first (this triggers grid load)
			targetSelection.setEntityType(config);
			// Load target entity
			final Optional<CEntityDB<?>> targetEntityOpt = findTargetEntity(config, targetId);
			if (targetEntityOpt.isEmpty()) {
				LOGGER.warn("[DialogLink] Target entity not found: {} #{}", targetType, targetId);
				return;
			}
			final CEntityDB<?> targetEntity = targetEntityOpt.get();
			// CRITICAL: Delay setValue to allow grid to finish loading after setEntityType
			// Without this, setValue tries to select an item that's not yet in the grid
			com.vaadin.flow.component.UI.getCurrent().access(() -> {
				try {
					targetSelection.setValue(Set.of(targetEntity));
					LOGGER.debug("[DialogLink] Successfully restored target selection: {} #{}", targetType, targetId);
				} catch (final Exception e) {
					LOGGER.error("[DialogLink] Error setting restored value", e);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("[DialogLink] Error restoring target selection in edit mode: {}", e.getMessage(), e);
			CNotificationService.showWarning("Could not load target entity for editing");
		}
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
		// Width handled by CDialog base class (responsive pattern)
		setResizable(true);
	}

	@Override
	protected void setupDialog() throws Exception {
		super.setupDialog();
		// Fixed dialog width to prevent resizing and horizontal scroll
		setWidth("700px");
		setHeight("650px");
	}

	/** Update the target info label in the header.
	 * @param targetType the target entity type (simple class name)
	 * @param targetId   the target entity ID */
	private void updateTargetInfoLabel(final String targetType, final Long targetId) {
		if (targetInfoLabel == null) {
			return;
		}
		if (targetType == null || targetId == null) {
			targetInfoLabel.setText("Target: (not selected)");
			targetInfoLabel.getStyle().set("color", "var(--lumo-error-color)");
			return;
		}
		try {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(targetType);
			final String displayName = CEntityRegistry.getEntityTitleSingular(entityClass);
			targetInfoLabel.setText(String.format("Target: %s #%d", displayName, targetId));
			targetInfoLabel.getStyle().set("color", "var(--lumo-success-color)");
		} catch (final Exception e) {
			LOGGER.debug("Could not format target label: {}", e.getMessage());
			targetInfoLabel.setText(String.format("Target: %s #%d", targetType, targetId));
			targetInfoLabel.getStyle().set("color", "var(--lumo-secondary-text-color)");
		}
	}

	@Override
	protected void validateForm() {
		LOGGER.debug("[DialogLink] Validating form...");
		// CRITICAL: Write form data back to entity using binder
		if (!binder.writeBeanIfValid(getEntity())) {
			LOGGER.warn("[DialogLink] Binder validation failed");
			throw new IllegalStateException("Please correct validation errors");
		}
		// Ensure target selection is synced before validation
		if (targetSelection != null) {
			final Set<CEntityDB<?>> currentSelection = targetSelection.getValue();
			if (currentSelection != null && !currentSelection.isEmpty()) {
				final CEntityDB<?> selected = currentSelection.iterator().next();
				getEntity().setTargetEntityId(selected.getId());
				getEntity().setTargetEntityType(selected.getClass().getSimpleName());
				updateTargetInfoLabel(selected.getClass().getSimpleName(), selected.getId());
			} else {
				targetSelection.selectFirstAvailable();
				final Set<CEntityDB<?>> fallbackSelection = targetSelection.getValue();
				if (fallbackSelection != null && !fallbackSelection.isEmpty()) {
					final CEntityDB<?> selected = fallbackSelection.iterator().next();
					getEntity().setTargetEntityId(selected.getId());
					getEntity().setTargetEntityType(selected.getClass().getSimpleName());
					updateTargetInfoLabel(selected.getClass().getSimpleName(), selected.getId());
				}
			}
		}
		// Validate source entity fields are set
		if (getEntity().getSourceEntityType() == null || getEntity().getSourceEntityId() == null) {
			LOGGER.error("[DialogLink] Source entity not set");
			throw new IllegalStateException("Source entity information is required");
		}
		LOGGER.debug("[DialogLink] Source entity: {} #{}", getEntity().getSourceEntityType(), getEntity().getSourceEntityId());
		// Validate target entity type and ID are set
		if (getEntity().getTargetEntityType() == null) {
			LOGGER.warn("[DialogLink] Target entity type not selected");
			throw new IllegalStateException("Please select a target entity type");
		}
		if (getEntity().getTargetEntityId() == null) {
			LOGGER.warn("[DialogLink] Target entity not selected");
			throw new IllegalStateException("Please select a target entity");
		}
		LOGGER.debug("[DialogLink] Target entity: {} #{}", getEntity().getTargetEntityType(), getEntity().getTargetEntityId());
		// Validate target entity type is valid and implements IHasLinks
		final String targetType = getEntity().getTargetEntityType();
		try {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(targetType);
			if (entityClass == null) {
				LOGGER.error("[DialogLink] Invalid target entity type: {}", targetType);
				throw new IllegalStateException("Invalid target entity type: " + targetType);
			}
			if (!IHasLinks.class.isAssignableFrom(entityClass)) {
				LOGGER.error("[DialogLink] Target entity type does not support links: {}", targetType);
				throw new IllegalStateException("Target entity type does not support links: " + targetType);
			}
		} catch (final Exception e) {
			LOGGER.error("[DialogLink] Error validating target entity type: {}", targetType, e);
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
