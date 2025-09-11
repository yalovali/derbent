package tech.derbent.screens.view;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.hilla.ApplicationContextProvider;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.views.components.CDiv;
import tech.derbent.screens.domain.CGridEntity;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

public class CComponentGridEntity extends CDiv {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentGridEntity.class);
	
	private Grid<Object> grid;
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
			grid = new Grid<>();
			
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

	private void createColumnForField(FieldConfig fieldConfig) {
		Field field = fieldConfig.getField();
		EntityFieldInfo fieldInfo = fieldConfig.getFieldInfo();
		String fieldName = field.getName();
		String displayName = fieldInfo.getDisplayName();
		
		// Create a value provider using reflection
		ValueProvider<Object, String> valueProvider = entity -> {
			try {
				field.setAccessible(true);
				Object value = field.get(entity);
				return value != null ? value.toString() : "";
			} catch (Exception e) {
				LOGGER.warn("Error accessing field {}: {}", fieldName, e.getMessage());
				return "";
			}
		};

		// Create column and set appropriate formatting
		Grid.Column<Object> column = grid.addColumn(valueProvider)
			.setHeader(displayName)
			.setKey(fieldName)
			.setSortable(true);
			
		// Set width based on field type
		Class<?> fieldType = field.getType();
		
		if (fieldType == Long.class || fieldType == long.class || fieldType == Integer.class || fieldType == int.class) {
			if (fieldName.toLowerCase().contains("id")) {
				column.setWidth("80px").setFlexGrow(0);
			} else {
				column.setWidth("100px").setFlexGrow(0);
			}
		} else if (fieldType == BigDecimal.class || fieldType == Double.class || fieldType == double.class || 
				   fieldType == Float.class || fieldType == float.class) {
			column.setWidth("120px").setFlexGrow(0);
		} else if (fieldType == LocalDate.class || fieldType == LocalDateTime.class || fieldType == java.util.Date.class) {
			column.setWidth("150px").setFlexGrow(0);
		} else if (fieldType == Boolean.class || fieldType == boolean.class) {
			column.setWidth("100px").setFlexGrow(0);
		} else if (CEntityDB.class.isAssignableFrom(fieldType)) {
			// Related entity - format as string
			column.setWidth("200px").setFlexGrow(0);
		} else if (fieldName.toLowerCase().contains("description") || fieldName.toLowerCase().contains("comment") ||
				   (fieldInfo.getMaxLength() > 100)) {
			// Long text fields
			column.setWidth("300px").setFlexGrow(0);
		} else {
			// Default to short text
			column.setWidth("200px").setFlexGrow(0);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
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

	// Helper class to hold field configuration
	private static class FieldConfig {
		private final EntityFieldInfo fieldInfo;
		private final int order;
		private final Field field;

		public FieldConfig(EntityFieldInfo fieldInfo, int order, Field field) {
			this.fieldInfo = fieldInfo;
			this.order = order;
			this.field = field;
		}

		public EntityFieldInfo getFieldInfo() { return fieldInfo; }
		public int getOrder() { return order; }
		public Field getField() { return field; }
	}
}
