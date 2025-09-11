package tech.derbent.screens.view;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.hilla.ApplicationContextProvider;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.domain.CGridEntity.FieldConfig;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

public class CComponentGridEntity extends CDiv {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentGridEntity.class);
	@SuppressWarnings ("rawtypes")
	private CGrid grid;
	private CGridEntity gridEntity;

	public CComponentGridEntity(CGridEntity gridEntity) {
		super();
		this.gridEntity = gridEntity;
		createContent();
	}

	private void createContent() {
		try {
			if (gridEntity == null) {
				add(new Div("No grid configuration provided"));
				return;
			}
			String serviceBeanName = gridEntity.getDataServiceBeanName();
			if (serviceBeanName == null || serviceBeanName.trim().isEmpty()) {
				add(new Div("No data service bean specified"));
				return;
			}
			// Get the entity class from the service bean
			Class<?> entityClass = getEntityClassFromService(serviceBeanName);
			if (entityClass == null) {
				add(new Div("Could not determine entity class for service: " + serviceBeanName));
				return;
			}
			// Create the grid
			grid = new CGrid(entityClass);
			// Parse selected fields and create columns
			String selectedFields = gridEntity.getSelectedFields();
			List<FieldConfig> fieldConfigs = parseSelectedFields(selectedFields, entityClass);
			// Create columns based on field configurations
			for (FieldConfig fieldConfig : fieldConfigs) {
				createColumnForField(fieldConfig);
			}
			// Load data from the service
			loadDataFromService(serviceBeanName);
			this.add(grid);
		} catch (Exception e) {
			LOGGER.error("Error creating grid content: {}", e.getMessage(), e);
			add(new Div("Error creating grid: " + e.getMessage()));
		}
	}

	private Class<?> getEntityClassFromService(String serviceBeanName) {
		try {
			if (ApplicationContextProvider.getApplicationContext() == null) {
				LOGGER.warn("ApplicationContext is not available");
				return null;
			}
			Object serviceBean = ApplicationContextProvider.getApplicationContext().getBean(serviceBeanName);
			if (!(serviceBean instanceof CAbstractService)) {
				LOGGER.warn("Service bean {} does not extend CAbstractService", serviceBeanName);
				return null;
			}
			CAbstractService<?> abstractService = (CAbstractService<?>) serviceBean;
			return getEntityClassFromService(abstractService);
		} catch (Exception e) {
			LOGGER.error("Error getting entity class from service {}: {}", serviceBeanName, e.getMessage());
			return null;
		}
	}

	private Class<?> getEntityClassFromService(CAbstractService<?> service) {
		try {
			Class<?> serviceClass = service.getClass();
			if (serviceClass.getName().contains("$$SpringCGLIB$$")) {
				serviceClass = serviceClass.getSuperclass();
			}
			java.lang.reflect.Method getEntityClassMethod = serviceClass.getDeclaredMethod("getEntityClass");
			getEntityClassMethod.setAccessible(true);
			return (Class<?>) getEntityClassMethod.invoke(service);
		} catch (Exception e) {
			LOGGER.debug("Could not get entity class from service: {}", e.getMessage());
			return null;
		}
	}

	private List<FieldConfig> parseSelectedFields(String selectedFields, Class<?> entityClass) {
		List<FieldConfig> fieldConfigs = new ArrayList<>();
		if (selectedFields == null || selectedFields.trim().isEmpty()) {
			return fieldConfigs;
		}
		String[] fieldPairs = selectedFields.split(",");
		for (String fieldPair : fieldPairs) {
			String[] parts = fieldPair.trim().split(":");
			if (parts.length == 2) {
				String fieldName = parts[0].trim();
				try {
					int order = Integer.parseInt(parts[1].trim());
					// Get field information using reflection
					Field field = findField(entityClass, fieldName);
					if (field != null) {
						EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(field);
						fieldConfigs.add(new FieldConfig(fieldInfo, order, field));
					}
				} catch (NumberFormatException e) {
					LOGGER.warn("Invalid order number for field {}: {}", fieldName, parts[1]);
				}
			}
		}
		// Sort by order
		fieldConfigs.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
		return fieldConfigs;
	}

	private Field findField(Class<?> entityClass, String fieldName) {
		Class<?> currentClass = entityClass;
		while (currentClass != null) {
			try {
				return currentClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				currentClass = currentClass.getSuperclass();
			}
		}
		return null;
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void createColumnForField(FieldConfig fieldConfig) {
		Field field = fieldConfig.getField();
		EntityFieldInfo fieldInfo = fieldConfig.getFieldInfo();
		String fieldName = field.getName();
		String displayName = fieldInfo.getDisplayName();
		Class<?> fieldType = field.getType();
		try {
			// Handle different field types using appropriate CGrid methods
			if (CEntityDB.class.isAssignableFrom(fieldType)) {
				// Entity reference - use addColumnEntityNamed for better formatting with colors/styling
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (CEntityDB<?>) field.get(entity);
					} catch (Exception e) {
						LOGGER.warn("Error accessing entity field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addColumnEntityNamed(valueProvider, displayName);
			} else if (Collection.class.isAssignableFrom(fieldType)) {
				// Collection field - use addColumnEntityCollection if it contains entities
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						Object value = field.get(entity);
						return value instanceof Collection ? (Collection) value : Collections.emptyList();
					} catch (Exception e) {
						LOGGER.warn("Error accessing collection field {}: {}", fieldName, e.getMessage());
						return Collections.emptyList();
					}
				};
				grid.addColumnEntityCollection(valueProvider, displayName);
			} else if (fieldName.toLowerCase().contains("id")
					&& (fieldType == Long.class || fieldType == long.class || fieldType == Integer.class || fieldType == int.class)) {
				// ID fields - use addIdColumn for consistent ID formatting
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return field.get(entity);
					} catch (Exception e) {
						LOGGER.warn("Error accessing ID field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addIdColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == Integer.class || fieldType == int.class) {
				// Integer fields - use addIntegerColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (Integer) field.get(entity);
					} catch (Exception e) {
						LOGGER.warn("Error accessing integer field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addIntegerColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == BigDecimal.class) {
				// BigDecimal fields - use addDecimalColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (BigDecimal) field.get(entity);
					} catch (Exception e) {
						LOGGER.warn("Error accessing decimal field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addDecimalColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == LocalDate.class) {
				// LocalDate fields - use addDateColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (LocalDate) field.get(entity);
					} catch (Exception e) {
						LOGGER.warn("Error accessing date field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addDateColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == LocalDateTime.class) {
				// LocalDateTime fields - use addDateTimeColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (LocalDateTime) field.get(entity);
					} catch (Exception e) {
						LOGGER.warn("Error accessing datetime field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addDateTimeColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == Boolean.class || fieldType == boolean.class) {
				// Boolean fields - use addBooleanColumn with appropriate true/false text
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (Boolean) field.get(entity);
					} catch (Exception e) {
						LOGGER.warn("Error accessing boolean field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addBooleanColumn(valueProvider, displayName, "Yes", "No");
			} else if (fieldName.toLowerCase().contains("description") || fieldName.toLowerCase().contains("comment")
					|| (fieldInfo.getMaxLength() > 100)) {
				// Long text fields - use addLongTextColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						Object value = field.get(entity);
						return value != null ? value.toString() : "";
					} catch (Exception e) {
						LOGGER.warn("Error accessing long text field {}: {}", fieldName, e.getMessage());
						return "";
					}
				};
				grid.addLongTextColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == String.class) {
				// Short text fields - use addShortTextColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						Object value = field.get(entity);
						return value != null ? value.toString() : "";
					} catch (Exception e) {
						LOGGER.warn("Error accessing text field {}: {}", fieldName, e.getMessage());
						return "";
					}
				};
				grid.addShortTextColumn(valueProvider, displayName, fieldName);
			} else {
				// For any other type, try to use addEntityColumn which provides metadata-based styling
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return field.get(entity);
					} catch (Exception e) {
						LOGGER.warn("Error accessing field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addEntityColumn(valueProvider, displayName, fieldName);
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to create column for field {}, falling back to addColumnByProperty: {}", fieldName, e.getMessage());
			// Fallback to addColumnByProperty for simple property-based columns
			grid.addColumnByProperty(fieldName, displayName);
		}
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void loadDataFromService(String serviceBeanName) {
		try {
			if (ApplicationContextProvider.getApplicationContext() == null) {
				LOGGER.warn("ApplicationContext is not available for loading data");
				return;
			}
			Object serviceBean = ApplicationContextProvider.getApplicationContext().getBean(serviceBeanName);
			if (!(serviceBean instanceof CAbstractService)) {
				LOGGER.warn("Service bean {} does not extend CAbstractService", serviceBeanName);
				return;
			}
			CAbstractService service = (CAbstractService) serviceBean;
			// Use pageable to get data - limit to first 1000 records for performance
			PageRequest pageRequest = PageRequest.of(0, 1000);
			List data = service.list(pageRequest).getContent();
			grid.setItems(data != null ? data : Collections.emptyList());
		} catch (Exception e) {
			LOGGER.error("Error loading data from service {}: {}", serviceBeanName, e.getMessage());
			grid.setItems(Collections.emptyList());
		}
	}
}
