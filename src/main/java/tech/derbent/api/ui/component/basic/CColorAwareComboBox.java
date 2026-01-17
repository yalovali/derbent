package tech.derbent.api.ui.component.basic;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.annotations.CDataProviderResolver;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

public class CColorAwareComboBox<T extends CEntityDB<T>> extends ComboBox<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CColorAwareComboBox.class);
	private static final long serialVersionUID = 1L;
	private Boolean autoContrast = Boolean.TRUE;
	private final Class<T> entityType;
	private String minWidth = "100%";
	private String padding = "4px 8px";
	private Function<String, T> persistenceConverter;
	private boolean persistenceEnabled = false;
	private String persistenceKey;
	// Styling configuration
	private Boolean roundedCorners = Boolean.TRUE;
	private ISessionService sessionService;

	/** Constructor for CColorAwareComboBox with entity type.
	 * @param entityType the entity class for the ComboBox */
	public CColorAwareComboBox(final Class<T> entityType) {
		super();
		this.entityType = entityType;
		initializeComboBox();
		CAuxillaries.setId(this);
	}

	/** Constructor for CColorAwareComboBox that loads items from a service with optional context.
	 * @param entityType    the entity class for the ComboBox
	 * @param serviceClass  the service class used to load items
	 * @param currentEntity the current entity context for project-scoped services
	 * @throws Exception if item loading fails */
	public CColorAwareComboBox(final Class<T> entityType, final Class<? extends CAbstractService<T>> serviceClass, final CEntityDB<?> currentEntity)
			throws Exception {
		this(entityType, serviceClass, currentEntity, List.of());
	}

	/** Constructor for CColorAwareComboBox that loads items from a service with optional context and additional items.
	 * @param entityType      the entity class for the ComboBox
	 * @param serviceClass    the service class used to load items
	 * @param currentEntity   the current entity context for project-scoped services
	 * @param additionalItems optional items to prepend in the ComboBox list
	 * @throws Exception if item loading fails */
	public CColorAwareComboBox(final Class<T> entityType, final Class<? extends CAbstractService<T>> serviceClass, final CEntityDB<?> currentEntity,
			final List<T> additionalItems) throws Exception {
		super();
		this.entityType = entityType;
		initializeComboBox();
		CAuxillaries.setId(this);
		populateItemsFromService(serviceClass, currentEntity, additionalItems);
	}

	/** Constructor for CColorAwareComboBox with entity type and label.
	 * @param entityType the entity class for the ComboBox
	 * @param label      the label for the ComboBox */
	public CColorAwareComboBox(final Class<T> entityType, final String label) {
		super(label);
		this.entityType = entityType;
		initializeComboBox();
	}

	/** Constructor for CColorAwareComboBox with entity type, label, and items.
	 * @param entityType the entity class for the ComboBox
	 * @param label      the label for the ComboBox
	 * @param items      the items to populate the ComboBox */
	public CColorAwareComboBox(final Class<T> entityType, final String label, final List<T> items) {
		super(label);
		this.entityType = entityType;
		initializeComboBox();
		if (items != null) {
			setItems(items);
		}
	}

	@SuppressWarnings ("unchecked")
	public CColorAwareComboBox(final EntityFieldInfo fieldInfo) {
		super();
		entityType = (Class<T>) fieldInfo.getFieldTypeClass();
		initializeComboBox();
		CAuxillaries.setId(this);
		updateFromInfo(fieldInfo);
	}

	@SuppressWarnings ("unchecked")
	public CColorAwareComboBox(IContentOwner contentOwner, final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder,
			CDataProviderResolver dataProviderResolver) throws Exception {
		try {
			entityType = (Class<T>) fieldInfo.getFieldTypeClass();
			Check.notNull(fieldInfo, "FieldInfo for ComboBox creation");
			// LOGGER.debug("Creating CColorAwareComboBox for field: {}", fieldInfo.getFieldName());
			// Initialize this instance properly instead of creating a new one
			initializeComboBox();
			CAuxillaries.setId(this);
			updateFromInfo(fieldInfo);
			List<T> items = null;
			Check.notNull(dataProviderResolver, "DataProviderResolver for field " + fieldInfo.getFieldName());
			items = dataProviderResolver.resolveDataList(contentOwner, fieldInfo);
			Check.notNull(items, "Items for field " + fieldInfo.getFieldName() + " of type " + fieldInfo.getJavaType());
			if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
				setValue(null);
			}
			setItems(items);
			if (!items.isEmpty()) {
				if (fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty()) {
					// For entity types, try to find by name or toString match
					final T defaultItem = items.stream().filter(item -> {
						final String itemDisplay = CColorUtils.getDisplayTextFromEntity(item);
						return fieldInfo.getDefaultValue().equals(itemDisplay);
					}).findFirst().orElse(null);
					if (defaultItem != null) {
						setValue(defaultItem);
					}
				} else if (fieldInfo.isAutoSelectFirst()) {
					setValue(items.get(0));
				}
			}
			if (binder != null) {
				// this is valid
				binder.bind(this, fieldInfo.getFieldName());
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to create or bind ComboBox for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	/** Configures the enhanced renderer for entities with colors and/or icons. Now uses the new CEntityLabel base class for consistent rendering. */
	private void configureColorRenderer() {
		setRenderer(new ComponentRenderer<>(item -> {
			try {
				if (item == null) {
					return new Span("N/A");
				}
				if (item instanceof CEntityNamed == false) {
					return new Span("Invalid Entity");
				}
				// Use the new CEntityLabel for consistent entity display
				return new CEntityLabel((CEntityNamed<?>) item, padding, autoContrast, roundedCorners);
			} catch (final Exception e) {
				e.printStackTrace();
				return null;
			}
		}));
	}

	/** Disables automatic persistence for this ComboBox.
	 * <p>
	 * After calling this method, the ComboBox will no longer automatically save or restore its value.
	 * </p>
	 * @see #enablePersistence(String, Function) */
	public void disablePersistence() {
		persistenceEnabled = false;
		LOGGER.info("[CColorAwareComboBox] Persistence disabled for key: {}", persistenceKey);
	}

	/** Enables automatic persistence for this ComboBox.
	 * <p>
	 * Once enabled, the ComboBox will automatically:
	 * <ul>
	 * <li>Save its value to session storage whenever the user (or code) changes it</li>
	 * <li>Restore its value from session storage when the component attaches to the UI</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <b>Persistence Mechanism:</b>
	 * <ul>
	 * <li><b>Save:</b> Uses entity ID for storage</li>
	 * <li><b>Restore:</b> Uses provided converter function to find entity by ID</li>
	 * </ul>
	 * </p>
	 * @param storageKey The unique key to use for storing the value in session storage
	 * @param converter  Function to convert stored ID back to entity (return null if not found)
	 * @throws IllegalArgumentException if storageKey is null/blank or converter is null
	 * @see #disablePersistence() */
	public void enablePersistence(final String storageKey, final Function<String, T> converter) {
		if (storageKey == null || storageKey.isBlank()) {
			throw new IllegalArgumentException("Storage key cannot be null or blank");
		}
		if (converter == null) {
			throw new IllegalArgumentException("Converter function cannot be null");
		}
		persistenceKey = storageKey;
		persistenceConverter = converter;
		persistenceEnabled = true;
		// Get session service
		if (sessionService == null) {
			sessionService = CSpringContext.getBean(ISessionService.class);
		}
		LOGGER.info("[CColorAwareComboBox] Persistence enabled for key: {}", storageKey);
		// Add value change listener to save on every change
		addValueChangeListener(event -> {
			if (!event.isFromClient()) {
				LOGGER.debug("[CColorAwareComboBox] Value change not from client, skipping save for key: {}", persistenceKey);
				return;
			}
			if (persistenceEnabled) {
				saveValue();
			}
		});
		// Add attach listener to restore when component is added to UI
		addAttachListener(event -> {
			if (persistenceEnabled) {
				restoreValue();
			}
		});
		// If already attached, restore immediately
		if (isAttached()) {
			restoreValue();
		}
	}

	/** Gets the entity type for this ComboBox.
	 * @return the entity type class */
	public Class<T> getEntityType() { return entityType; }

	@Override
	public String getMinWidth() { return minWidth; }

	public String getPadding() { return padding; }

	/** Initializes the ComboBox with enhanced rendering for all entities. All entities now use the enhanced rendering with icons and colors. */
	private void initializeComboBox() {
		setAllowCustomValue(false);
		setItemLabelGenerator(item -> CColorUtils.getDisplayTextFromEntity(item));
		configureColorRenderer();
		setupSelectedValueDisplay();
	}

	public boolean isAutoContrast() { return autoContrast; }

	/** Checks if persistence is enabled for this ComboBox.
	 * @return true if persistence is enabled, false otherwise */
	public boolean isPersistenceEnabled() { return persistenceEnabled; }

	public boolean isRoundedCorners() { return roundedCorners; }

	private List<T> mergeItems(final List<T> additionalItems, final List<T> items) {
		final LinkedHashSet<T> uniqueItems = new LinkedHashSet<>();
		if (additionalItems != null) {
			uniqueItems.addAll(additionalItems);
		}
		if (items != null) {
			uniqueItems.addAll(items);
		}
		return new ArrayList<>(uniqueItems);
	}

	@SuppressWarnings ({
			"unchecked"
	})
	private void populateItemsFromService(final Class<? extends CAbstractService<T>> serviceClass, final CEntityDB<?> currentEntity,
			final List<T> additionalItems) throws Exception {
		Check.notNull(serviceClass, "Service class cannot be null");
		final CAbstractService<T> service = CSpringContext.getBean(serviceClass);
		Check.notNull(service, "Service instance cannot be null for " + serviceClass.getSimpleName());
		try {
			List<T> items = null;
			if (currentEntity != null && service instanceof CEntityOfCompanyService) {
				final CEntityOfCompanyService<?> companyService = (CEntityOfCompanyService<?>) service;
				if (currentEntity instanceof CEntityOfCompany) {
					final CCompany company = ((CEntityOfCompany<?>) currentEntity).getCompany();
					if (company != null) {
						items = (List<T>) companyService.listByCompany(company);
					}
				} else if (currentEntity instanceof CEntityOfProject) {
					final CProject project = ((CEntityOfProject<?>) currentEntity).getProject();
					final CCompany company = project != null ? project.getCompany() : null;
					if (company != null) {
						items = (List<T>) companyService.listByCompany(company);
					}
				} else if (currentEntity instanceof CProject) {
					final CCompany company = ((CProject) currentEntity).getCompany();
					if (company != null) {
						items = (List<T>) companyService.listByCompany(company);
					}
				}
			} else if (currentEntity != null && service instanceof CEntityOfProjectService) {
				final CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) service;
				if (currentEntity instanceof CEntityOfProject) {
					final CProject project = ((CEntityOfProject<?>) currentEntity).getProject();
					if (project != null) {
						items = (List<T>) projectService.listByProject(project);
					}
				} else if (currentEntity instanceof CProject) {
					items = (List<T>) projectService.listByProject((CProject) currentEntity);
				}
			}
			if (items == null) {
				items = service.findAll();
			}
			setItems(mergeItems(additionalItems, items));
		} catch (final Exception e) {
			LOGGER.error("Failed to load items for {} combo box from service {}", entityType != null ? entityType.getSimpleName() : "<null>",
					serviceClass.getSimpleName(), e);
			throw e;
		}
	}

	/** Restores the value from session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the component attaches. It can also be called manually if needed.
	 * </p>
	 */
	private void restoreValue() {
		if (!persistenceEnabled || sessionService == null || persistenceConverter == null) {
			return;
		}
		try {
			final Optional<String> storedValue = sessionService.getSessionValue(persistenceKey);
			if (storedValue.isPresent()) {
				final String serialized = storedValue.get();
				LOGGER.debug("[CColorAwareComboBox] Restoring value '{}' for key: {}", serialized, persistenceKey);
				final T converted = persistenceConverter.apply(serialized);
				if (converted != null) {
					setValue(converted);
					LOGGER.info("[CColorAwareComboBox] Restored value for key: {}", persistenceKey);
				} else {
					LOGGER.warn("[CColorAwareComboBox] Stored value '{}' is no longer valid for key: {}", serialized, persistenceKey);
				}
			}
		} catch (final Exception e) {
			LOGGER.error("[CColorAwareComboBox] Error restoring value for key: {}", persistenceKey, e);
		}
	}

	/** Saves the current value to session storage.
	 * <p>
	 * This method is called automatically when persistence is enabled and the value changes. It can also be called manually if needed.
	 * </p>
	 */
	private void saveValue() {
		LOGGER.debug("[CColorAwareComboBox] Saving value for key: {}", persistenceKey);
		if (!persistenceEnabled || sessionService == null) {
			return;
		}
		try {
			final T value = getValue();
			if (value != null && value.getId() != null) {
				final String serialized = value.getId().toString();
				sessionService.setSessionValue(persistenceKey, serialized);
				LOGGER.debug("[CColorAwareComboBox] Saved value '{}' for key: {}", serialized, persistenceKey);
			} else {
				sessionService.removeSessionValue(persistenceKey);
				LOGGER.debug("[CColorAwareComboBox] Cleared value for key: {}", persistenceKey);
			}
		} catch (final Exception e) {
			LOGGER.error("[CColorAwareComboBox] Error saving value for key: {}", persistenceKey, e);
		}
	}

	public void setAutoContrast(final boolean autoContrast) {
		this.autoContrast = autoContrast;
		configureColorRenderer();
	}

	@Override
	public void setMinWidth(final String minWidth) {
		this.minWidth = minWidth;
		configureColorRenderer();
	}

	public void setPadding(final String padding) {
		this.padding = padding;
		configureColorRenderer();
	}

	public void setRoundedCorners(final boolean roundedCorners) {
		this.roundedCorners = roundedCorners;
		configureColorRenderer();
	}

	/** Sets up a value change listener to update the prefix component with the selected item's icon and applies styling to show the color. This
	 * ensures the selected value also displays with color and icon, not just the dropdown items. **IMPORTANT**: Text color is applied only to the
	 * input field, not the dropdown overlay, to prevent white-on-white text visibility issues when dropdown items have their own background
	 * colors. */
	@SuppressWarnings ("unused")
	private void setupSelectedValueDisplay() {
		addValueChangeListener(event -> {
			try {
				final T selectedItem = event.getValue();
				if (selectedItem == null) {
					setPrefixComponent(null);
					getElement().getStyle().remove("--vaadin-input-field-background");
					getElement().executeJs("this.inputElement.style.color = ''");
					return;
				}
				String backgroundColor = null;
				try {
					backgroundColor = CColorUtils.getColorFromEntity(selectedItem);
				} catch (final Exception e) {
					backgroundColor = "#1F3FcF";
				}
				final String textColor = CColorUtils.getContrastTextColor(backgroundColor);
				getElement().executeJs("this.inputElement.style.color = $0", textColor);
				getElement().executeJs("this.inputElement.style.background = $0", backgroundColor);
				if (selectedItem instanceof IHasIcon) {
					final Icon icon = CColorUtils.getIconForEntity(selectedItem);
					if (icon != null) {
						// CColorUtils.styleIcon(icon);
						setPrefixComponent(icon);
						// Apply text color to icon as well if we have a background color
						icon.getElement().getStyle().set("color", textColor);
						icon.getElement().getStyle().set("background", backgroundColor);
						icon.getElement().getStyle().set("height", "100%");
						icon.getElement().getStyle().remove("margin-right");
						icon.getElement().getStyle().remove("width");
						// must have to have alignment with input field color with real border radius
						icon.getElement().getStyle().set("border-top-left-radius", "4px");
						icon.getElement().getStyle().set("border-bottom-left-radius", "4px");
					} else {
						setPrefixComponent(null);
					}
				} else {
					getElement().executeJs("this.inputElement.style['border-top-left-radius'] = $0", "4px");
					getElement().executeJs("this.inputElement.style['border-bottom-left-radius'] = $0", "4px");
				}
			} catch (final Exception e) {
				LOGGER.error("Error updating selected value display in CColorAwareComboBox: {}", e.getMessage());
			}
		});
	}
	// Getter and setter methods for styling configuration

	private void updateFromInfo(final EntityFieldInfo fieldInfo) {
		Check.notNull(fieldInfo, "Field info cannot be null");
		setAllowCustomValue(fieldInfo.isAllowCustomValue());
		// Set placeholder text if specified
		if (!fieldInfo.getPlaceholder().trim().isEmpty()) {
			setPlaceholder(fieldInfo.getPlaceholder());
		}
		// Set read-only state for combobox if specified
		if (fieldInfo.isComboboxReadOnly() || fieldInfo.isReadOnly()) {
			setReadOnly(true);
		}
		// Set width if specified
		if (!fieldInfo.getWidth().trim().isEmpty()) {
			setWidth(fieldInfo.getWidth());
		}
	}
}
