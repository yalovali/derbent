package tech.derbent.plm.links.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.links.service.CLinkService;

/** CComponentListLinks - Component for managing links on entities.
 * <p>
 * Displays a list of links with link type, target entity (with color badge), target name, status, and responsible. Supports CRUD operations (Create,
 * Read, Update, Delete) with expandable details. Links can be clicked to expand and show full description.
 * <p>
 * This component uses the IHasLinks interface for clean, type-safe integration with any entity that can have links.
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentListLinks component = new CComponentListLinks(service, sessionService);
 * component.setMasterEntity(activity); // activity implements IHasLinks
 * </pre>
 */
public class CComponentLink extends CVerticalLayout
		implements IContentOwner, IGridComponent<CLink>, IGridRefreshListener<CLink>, IPageServiceAutoRegistrable {

	public static final String ID_GRID = "custom-links-grid";
	public static final String ID_HEADER = "custom-links-header";
	public static final String ID_ROOT = "custom-links-component";
	public static final String ID_TOOLBAR = "custom-links-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentLink.class);
	private static final long serialVersionUID = 1L;

	/** Compare two nullable strings.
	 * @param s1 first string
	 * @param s2 second string
	 * @return comparison result */
	private static int compareNullable(final String s1, final String s2) {
		if (s1 == null && s2 == null) {
			return 0;
		}
		if (s1 == null) {
			return 1;
		}
		if (s2 == null) {
			return -1;
		}
		return s1.compareTo(s2);
	}

	/** Get target entity from link.
	 * @param link the link
	 * @return target entity or null */
	private static void saveMasterEntity(final CEntityDB<?> entity) {
		Check.notNull(entity, "Entity cannot be null");
		try {
			saveMasterEntityTyped(entity);
		} catch (final Exception e) {
			LOGGER.error("Failed to save master entity after link update", e);
			CNotificationService.showException("Failed to save link to parent entity", e);
		}
	}

	@SuppressWarnings ("unchecked")
	private static <T extends CEntityDB<T>> void saveMasterEntityTyped(final CEntityDB<?> entity) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entity.getClass());
		final CAbstractService<T> service = (CAbstractService<T>) CSpringContext.getBean(serviceClass);
		service.save((T) entity);
	}

	private CButton buttonAdd;
	private CButton buttonDelete;
	private CButton buttonEdit;
	private CGrid<CLink> grid;
	private CHorizontalLayout layoutToolbar;
	private final CLinkService linkService;
	private IHasLinks masterEntity;
	private final List<Consumer<CLink>> refreshListeners = new ArrayList<>();
	private final ISessionService sessionService;

	/** Constructor for link list component.
	 * @param linkService    the link service
	 * @param sessionService the session service */
	public CComponentLink(final CLinkService linkService, final ISessionService sessionService) {
		Check.notNull(linkService, "LinkService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.linkService = linkService;
		this.sessionService = sessionService;
		initializeComponent();
	}

	@Override
	public void addRefreshListener(final Consumer<CLink> listener) {
		Check.notNull(listener, "Refresh listener cannot be null");
		refreshListeners.add(listener);
	}

	@Override
	public void clearGrid() {
		Check.notNull(grid, "Grid cannot be null when clearing links");
		grid.setItems(List.of());
		grid.asSingleSelect().clear();
		buttonEdit.setEnabled(false);
		buttonDelete.setEnabled(false);
		updateCompactMode(true);
	}

	/** Configure grid columns with expandable details. */
	@Override
	public void configureGrid(final CGrid<CLink> grid1) {
		try {
			Check.notNull(grid1, "Grid cannot be null");
			// Target entity ID column (not link ID)
			grid1.addCustomColumn(link -> {
				final Long targetId = link.getTargetEntityId();
				return targetId != null ? targetId.toString() : "";
			}, "Target ID", "80px", "targetId", 0);
			// Link type column
			grid1.addCustomColumn(CLink::getLinkType, "Link Type", "120px", "linkType", 1);
			// Target entity type column with color badge - use addCustomColumn for consistent header styling
			grid1.addCustomColumn(link -> {
				try {
					final CEntityDB<?> targetEntity = CLinkService.getTargetEntity(link);
					if (targetEntity != null) {
						return targetEntity.getClass().getSimpleName();
					}
					// Fallback to type name if entity not loaded
					final String entityType = link.getTargetEntityType();
					final Class<?> entityClass = CEntityRegistry.getEntityClass(entityType);
					return CEntityRegistry.getEntityTitleSingular(entityClass);
				} catch (final Exception e) {
					LOGGER.debug("Could not render target entity: {}", e.getMessage());
					return link.getTargetEntityType();
				}
			}, "Target Entity", "180px", "targetEntity", 2);
			// Target entity name column
			grid1.addCustomColumn(link -> {
				try {
					final CEntityDB<?> targetEntity = CLinkService.getTargetEntity(link);
					if (targetEntity instanceof CEntityNamed) {
						final String name = ((CEntityNamed<?>) targetEntity).getName();
						if (name != null && !name.isEmpty()) {
							return name;
						}
						// Fall back to ID if name is null/empty
						return targetEntity.getId() != null ? "#" + targetEntity.getId() : "";
					}
					return link.getTargetEntityName();
				} catch (final Exception e) {
					LOGGER.debug("Could not get target name: {}", e.getMessage());
					return link.getTargetEntityName();
				}
			}, "Name", "180px", "name", 3);
			// Link description column (from CLink.description field, editable in dialog)
			grid1.addCustomColumn(link -> {
				final String description = link.getDescription();
				return description != null ? description : "";
			}, "Description", "200px", "description", 4);
			// Status column (from target entity if ISprintableItem) - use addCustomColumn for consistent header styling
			grid1.addCustomColumn(link -> {
				try {
					final CEntityDB<?> targetEntity = CLinkService.getTargetEntity(link);
					if (targetEntity == null || !(targetEntity instanceof ISprintableItem)) {
						return "";
					}
					final CProjectItemStatus status = ((ISprintableItem) targetEntity).getStatus();
					return status != null ? status.getName() : "";
				} catch (final Exception e) {
					LOGGER.error("[LinkGrid] Error getting status for link: {}", e.getMessage(), e);
					return "";
				}
			}, "Status", "150px", "status", 5);
			// Responsible column (from target entity if ISprintableItem) - use addCustomColumn for consistent header styling
			grid1.addCustomColumn(link -> {
				try {
					final CEntityDB<?> targetEntity = CLinkService.getTargetEntity(link);
					if (targetEntity == null || !(targetEntity instanceof ISprintableItem)) {
						return "";
					}
					final CUser responsible = ((ISprintableItem) targetEntity).getAssignedTo();
					return responsible != null ? responsible.getName() : "";
				} catch (final Exception e) {
					LOGGER.error("[LinkGrid] Error getting responsible for link: {}", e.getMessage(), e);
					return "";
				}
			}, "Responsible", "180px", "responsible", 6);
			// Add expandable details renderer for full link description
			grid1.setItemDetailsRenderer(new ComponentRenderer<>(link -> {
				final CVerticalLayout detailsLayout = new CVerticalLayout();
				detailsLayout.setPadding(true);
				detailsLayout.setSpacing(true);
				detailsLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-left",
						"3px solid var(--lumo-primary-color)");
				// Link description
				if (link.getDescription() != null && !link.getDescription().isEmpty()) {
					final CSpan descriptionText = new CSpan(link.getDescription());
					descriptionText.getStyle().set("white-space", "pre-wrap").set("word-wrap", "break-word");
					detailsLayout.add(descriptionText);
				} else {
					final CSpan noDescription = new CSpan("No description");
					noDescription.getStyle().set("font-style", "italic").set("color", "var(--lumo-secondary-text-color)");
					detailsLayout.add(noDescription);
				}
				// Metadata footer
				final CSpan metadata =
						new CSpan(String.format("%s → %s (%s)", link.getSourceEntityName(), link.getTargetEntityName(), link.getLinkType()));
				metadata.getStyle().set("font-size", "0.875rem").set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
				detailsLayout.add(metadata);
				return detailsLayout;
			}));
			// Enable single-click to expand/collapse
			grid1.addItemClickListener(event -> {
				final CLink link = event.getItem();
				if (grid1.isDetailsVisible(link)) {
					grid1.setDetailsVisible(link, false);
				} else {
					grid1.setDetailsVisible(link, true);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Error configuring links grid", e);
			CNotificationService.showException("Error configuring links grid", e);
		}
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		throw new UnsupportedOperationException("Links are managed via link dialog.");
	}

	/** Create toolbar buttons. */
	private void createToolbarButtons() {
		// Add button
		buttonAdd = new CButton(VaadinIcon.PLUS.create());
		buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonAdd.setTooltipText("Add link");
		buttonAdd.addClickListener(event -> on_buttonAdd_clicked());
		layoutToolbar.add(buttonAdd);
		// Edit button
		buttonEdit = new CButton(VaadinIcon.EDIT.create());
		buttonEdit.setTooltipText("Edit link");
		buttonEdit.addClickListener(event -> on_buttonEdit_clicked());
		buttonEdit.setEnabled(false);
		layoutToolbar.add(buttonEdit);
		// Delete button
		buttonDelete = new CButton(VaadinIcon.TRASH.create());
		buttonDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		buttonDelete.setTooltipText("Delete link");
		buttonDelete.addClickListener(event -> on_buttonDelete_clicked());
		buttonDelete.setEnabled(false);
		layoutToolbar.add(buttonDelete);
	}

	@Override
	public String getComponentName() { return "links"; }

	@Override
	public String getCurrentEntityIdString() {
		if (masterEntity instanceof CEntityDB<?>) {
			final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
			return entity.getId() != null ? entity.getId().toString() : null;
		}
		return null;
	}

	@Override
	public CAbstractService<?> getEntityService() { return linkService; }

	@Override
	public CGrid<CLink> getGrid() { return grid; }

	@Override
	public CEntityDB<?> getValue() {
		if (masterEntity instanceof CEntityDB<?>) {
			return (CEntityDB<?>) masterEntity;
		}
		return null;
	}

	/** Initialize the component layout and grid. */
	private void initializeComponent() {
		setId(ID_ROOT);
		setPadding(false);
		setSpacing(true);
		// Header
		final CH3 header = new CH3("Links");
		header.setId(ID_HEADER);
		add(header);
		// Toolbar
		layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setId(ID_TOOLBAR);
		layoutToolbar.setSpacing(true);
		createToolbarButtons();
		add(layoutToolbar);
		// Grid
		grid = new CGrid<>(CLink.class);
		grid.setId(ID_GRID);
		CGrid.setupGrid(grid);
		grid.setRefreshConsumer(event -> refreshGrid());
		configureGrid(grid);
		grid.setHeight("300px"); // Default height
		// Enhanced selection styling for better visibility
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		grid.getStyle().set("--lumo-primary-color-50pct", "rgba(33, 150, 243, 0.15)");
		grid.asSingleSelect().addValueChangeListener(e -> on_grid_selectionChanged(e.getValue()));
		// Add double-click to edit
		grid.addItemDoubleClickListener(e -> on_grid_doubleClicked(e.getItem()));
		add(grid);
		// Set initial compact mode (will adjust when data loaded)
		updateCompactMode(true);
	}

	private void linkLinkToMaster(final CLink link) {
		Check.notNull(link, "Link cannot be null");
		Check.notNull(masterEntity, "Master entity cannot be null");
		Set<CLink> items = masterEntity.getLinks();
		if (items == null) {
			items = new HashSet<>();
			masterEntity.setLinks(items);
		}
		// Check if link already exists (by ID for persisted, by reference for new)
		final Long linkId = link.getId();
		final boolean exists = items.stream().anyMatch(existing -> {
			if (linkId != null && existing != null && existing.getId() != null) {
				return linkId.equals(existing.getId());
			}
			// For new links (null ID), check by reference to avoid duplicates
			return existing == link;
		});
		if (!exists) {
			items.add(link);
		}
		if (masterEntity instanceof CEntityDB<?>) {
			final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
			if (entity.getId() != null) {
				saveMasterEntity(entity);
			} else {
				LOGGER.warn("Master entity has no ID; link will persist when the parent entity is saved");
			}
		}
	}

	@Override
	public void notifyRefreshListeners(final CLink changedItem) {
		if (!refreshListeners.isEmpty()) {
			for (final Consumer<CLink> listener : refreshListeners) {
				try {
					listener.accept(changedItem);
				} catch (final Exception e) {
					LOGGER.error("Error notifying refresh listener", e);
				}
			}
		}
	}

	/** Handle add button click. */
	protected void on_buttonAdd_clicked() {
		try {
			if (masterEntity == null) {
				CNotificationService.showWarning("Please select an entity first");
				return;
			}
			final Object parentEntity = masterEntity;
			if (!(parentEntity instanceof CEntityDB)) {
				CNotificationService.showError("Entity does not support links");
				LOGGER.error("Master entity does not extend CEntityDB: {}", parentEntity.getClass().getSimpleName());
				return;
			}
			// Create new link
			final CLink newLink = new CLink();
			final CDialogLink dialog = new CDialogLink(linkService, sessionService, newLink, link -> {
				try {
					linkLinkToMaster(link);
					refreshGrid();
					notifyRefreshListeners(link);
				} catch (final Exception e) {
					LOGGER.error("Error refreshing grid after adding link", e);
				}
			}, true);
			// Set source entity from master entity
			if (parentEntity instanceof CEntityDB<?>) {
				dialog.setSourceEntity((CEntityDB<?>) parentEntity);
			}
			dialog.open();
		} catch (final Exception e) {
			CNotificationService.showException("Error opening link dialog", e);
		}
	}

	/** Handle delete button click. */
	protected void on_buttonDelete_clicked() {
		try {
			final CLink selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No link selected");
			CNotificationService.showConfirmationDialog("Delete this link?", () -> {
				try {
					unlinkLinkFromMaster(selected);
					linkService.delete(selected);
					refreshGrid();
					notifyRefreshListeners(selected);
					CNotificationService.showDeleteSuccess();
				} catch (final Exception e) {
					CNotificationService.showException("Error deleting link", e);
				}
			});
		} catch (final Exception e) {
			CNotificationService.showException("Failed to delete link", e);
		}
	}

	/** Handle edit button click. */
	protected void on_buttonEdit_clicked() {
		try {
			final CLink selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No link selected");
			
			LOGGER.debug("[ComponentLink] Selected link from grid - ID: {}, targetType: {}, targetId: {}", 
				selected.getId(), selected.getTargetEntityType(), selected.getTargetEntityId());
			
			// Ensure the link entity is fully loaded with all fields
			final CLink refreshedLink =
					linkService.getById(selected.getId()).orElseThrow(() -> new IllegalStateException("Link not found: " + selected.getId()));
			
			LOGGER.debug("[ComponentLink] Refreshed link from DB - ID: {}, targetType: {}, targetId: {}", 
				refreshedLink.getId(), refreshedLink.getTargetEntityType(), refreshedLink.getTargetEntityId());
			
			final CDialogLink dialog = new CDialogLink(linkService, sessionService, refreshedLink, link -> {
				try {
					linkService.save(link);
					
					// CRITICAL: Reload master entity from database to get updated link collection
					reloadMasterEntity();
					
					refreshGrid();
					notifyRefreshListeners(link);
					CNotificationService.showSuccess("Link updated successfully");
				} catch (final Exception e) {
					LOGGER.error("Error saving link", e);
					CNotificationService.showException("Error saving link", e);
				}
			}, false);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error opening edit dialog", e);
			CNotificationService.showException("Error opening edit dialog", e);
		}
	}

	/** Handle grid double-click to edit. */
	protected void on_grid_doubleClicked(final CLink link) {
		if (link != null) {
			on_buttonEdit_clicked();
		}
	}

	/** Handle grid selection changes. */
	private void on_grid_selectionChanged(final CLink selected) {
		buttonEdit.setEnabled(selected != null);
		buttonDelete.setEnabled(selected != null);
	}

	@Override
	public void populateForm() {
		refreshGrid();
	}

	@Override
	public void refreshGrid() {
		Check.notNull(grid, "Grid cannot be null when refreshing links");
		if (masterEntity == null) {
			LOGGER.debug("Master entity is null, clearing grid");
			clearGrid();
			return;
		}
		// Load links from parent entity's collection
		final List<CLink> items = new ArrayList<>(masterEntity.getLinks());
		// Sort by link type, then target entity type
		items.sort((l1, l2) -> {
			final int typeCompare = compareNullable(l1.getLinkType(), l2.getLinkType());
			if (typeCompare != 0) {
				return typeCompare;
			}
			return compareNullable(l1.getTargetEntityType(), l2.getTargetEntityType());
		});
		grid.setItems(items);
		grid.asSingleSelect().clear();
		updateCompactMode(items.isEmpty());
		LOGGER.debug("Loaded {} links for entity", items.size());
	}

	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		pageService.registerComponent(getComponentName(), this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}'", getClass().getSimpleName(), getComponentName());
	}

	@Override
	public void removeRefreshListener(final Consumer<CLink> listener) {
		if (listener != null) {
			refreshListeners.remove(listener);
		}
	}

	public void setEntity(final Object entity) {
		if (entity == null) {
			setValue(null);
			return;
		}
		if (entity instanceof CEntityDB<?>) {
			setValue((CEntityDB<?>) entity);
			return;
		}
		if (entity instanceof IHasLinks) {
			masterEntity = (IHasLinks) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("Entity does not implement IHasLinks: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	/** Set the master entity for this component.
	 * @param masterEntity the master entity that owns the links */
	public void setMasterEntity(final IHasLinks masterEntity) {
		this.masterEntity = masterEntity;
		refreshGrid();
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		if (entity == null) {
			masterEntity = null;
			clearGrid();
			return;
		}
		if (entity instanceof IHasLinks) {
			masterEntity = (IHasLinks) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("setValue called with unexpected entity type: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	private void unlinkLinkFromMaster(final CLink link) {
		Check.notNull(link, "Link cannot be null");
		Check.notNull(masterEntity, "Master entity cannot be null");
		Check.instanceOf(masterEntity, CEntityDB.class, "Master entity must support database persistence");
		final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
		Check.notNull(entity.getId(), "Master entity must be saved before deleting links");
		final Set<CLink> items = masterEntity.getLinks();
		Check.notNull(items, "Links list cannot be null");
		final Long linkId = link.getId();
		final boolean removed = items.removeIf(existing -> {
			if (linkId != null && existing != null) {
				return linkId.equals(existing.getId());
			}
			return existing == link;
		});
		Check.isTrue(removed, "Link not found in master entity");
		saveMasterEntity(entity);
	}

	/** Reload master entity from database to refresh link collection after save/delete.
	 * This ensures the grid shows the latest link data including edited fields. */
	private void reloadMasterEntity() {
		if (masterEntity == null) {
			LOGGER.debug("Master entity is null, nothing to reload");
			return;
		}
		
		Check.instanceOf(masterEntity, CEntityDB.class, "Master entity must be CEntityDB to reload");
		final CEntityDB<?> entity = (CEntityDB<?>) masterEntity;
		final Long entityId = entity.getId();
		
		if (entityId == null) {
			LOGGER.warn("Cannot reload master entity - no ID (not persisted yet)");
			return;
		}
		
		try {
			// Get service for the master entity
			final Class<?> entityClass = entity.getClass();
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
			final CAbstractService<?> service = (CAbstractService<?>) CSpringContext.getBean(serviceClass);
			
			// Reload entity from database
			final java.util.Optional<?> reloaded = service.getById(entityId);
			if (reloaded.isPresent() && reloaded.get() instanceof IHasLinks) {
				masterEntity = (IHasLinks) reloaded.get();
				LOGGER.debug("Master entity reloaded from database: {} #{}", entityClass.getSimpleName(), entityId);
			} else {
				LOGGER.warn("Could not reload master entity: {} #{}", entityClass.getSimpleName(), entityId);
			}
		} catch (final Exception e) {
			LOGGER.error("Error reloading master entity", e);
		}
	}

	/** Update component height based on content.
	 * @param isEmpty true if no links exist */
	private void updateCompactMode(final boolean isEmpty) {
		if (isEmpty) {
			// Compact mode: narrow height when empty
			grid.setHeight("150px");
			setHeight("200px"); // Component total height
			LOGGER.debug("Compact mode: No links");
		} else {
			// Normal mode: full height when has content
			grid.setHeight("300px");
			setHeight("auto"); // Component auto-adjusts
			LOGGER.debug("Normal mode: Has links");
		}
	}
}
