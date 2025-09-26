package tech.derbent.api.components;

import java.lang.reflect.Method;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.derbent.api.annotations.ARelationshipMetadata;
import tech.derbent.api.domains.CAbstractEntityRelationship;
import tech.derbent.api.utils.Check;

/** Generic component for managing entity relationships using reflection and metadata annotations. This component provides a reusable framework for
 * handling bidirectional relationships between any two entity types with automatic collection management. */
@Component
public class CGenericRelationshipManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGenericRelationshipManager.class);

	/** Automatically establish a bidirectional relationship between two entities. Uses reflection and metadata annotations to determine how to manage
	 * the relationship.
	 * @param parentEntity       The parent entity in the relationship
	 * @param childEntity        The child entity in the relationship
	 * @param relationshipEntity The relationship entity that connects them
	 * @param <P>                Parent entity type
	 * @param <C>                Child entity type
	 * @param <R>                Relationship entity type */
	public <P, C, R extends CAbstractEntityRelationship<R>> void establishRelationship(P parentEntity, C childEntity, R relationshipEntity) {
		Check.notNull(parentEntity, "Parent entity cannot be null");
		Check.notNull(childEntity, "Child entity cannot be null");
		Check.notNull(relationshipEntity, "Relationship entity cannot be null");
		try {
			// Get metadata from the relationship entity
			ARelationshipMetadata metadata = relationshipEntity.getClass().getAnnotation(ARelationshipMetadata.class);
			if (metadata == null) {
				LOGGER.warn("No relationship metadata found for {}", relationshipEntity.getClass().getSimpleName());
				return;
			}
			// Set the entity references in the relationship object
			setEntityReference(relationshipEntity, "setUser", childEntity);
			setEntityReference(relationshipEntity, "setCompany", parentEntity);
			// Add to parent entity collection
			if (!metadata.parentCollectionField().isEmpty()) {
				addToCollection(parentEntity, metadata.parentCollectionField(), childEntity);
			}
			// Add to child entity collection
			if (!metadata.childCollectionField().isEmpty()) {
				addToCollection(childEntity, metadata.childCollectionField(), relationshipEntity);
			}
			LOGGER.debug("Successfully established relationship between {} and {}", parentEntity.getClass().getSimpleName(),
					childEntity.getClass().getSimpleName());
		} catch (Exception e) {
			LOGGER.error("Error establishing relationship: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to establish relationship", e);
		}
	}

	/** Remove a bidirectional relationship between two entities.
	 * @param parentEntity      The parent entity in the relationship
	 * @param childEntity       The child entity in the relationship
	 * @param relationshipClass The relationship entity class
	 * @param <P>               Parent entity type
	 * @param <C>               Child entity type
	 * @param <R>               Relationship entity type */
	public <P, C, R extends CAbstractEntityRelationship<R>> void removeRelationship(P parentEntity, C childEntity, Class<R> relationshipClass) {
		Check.notNull(parentEntity, "Parent entity cannot be null");
		Check.notNull(childEntity, "Child entity cannot be null");
		Check.notNull(relationshipClass, "Relationship class cannot be null");
		try {
			// Get metadata from the relationship class
			ARelationshipMetadata metadata = relationshipClass.getAnnotation(ARelationshipMetadata.class);
			if (metadata == null) {
				LOGGER.warn("No relationship metadata found for {}", relationshipClass.getSimpleName());
				return;
			}
			// Remove from parent entity collection
			if (!metadata.parentCollectionField().isEmpty()) {
				removeFromCollection(parentEntity, metadata.parentCollectionField(), childEntity);
			}
			// Remove from child entity collection (find by parent entity)
			if (!metadata.childCollectionField().isEmpty()) {
				removeFromCollectionByParent(childEntity, metadata.childCollectionField(), parentEntity);
			}
			LOGGER.debug("Successfully removed relationship between {} and {}", parentEntity.getClass().getSimpleName(),
					childEntity.getClass().getSimpleName());
		} catch (Exception e) {
			LOGGER.error("Error removing relationship: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to remove relationship", e);
		}
	}

	/** Check if a relationship exists between two entities.
	 * @param parentEntity      The parent entity
	 * @param childEntity       The child entity
	 * @param relationshipClass The relationship entity class
	 * @param <P>               Parent entity type
	 * @param <C>               Child entity type
	 * @param <R>               Relationship entity type
	 * @return true if relationship exists */
	public <P, C, R extends CAbstractEntityRelationship<R>> boolean relationshipExists(P parentEntity, C childEntity, Class<R> relationshipClass) {
		Check.notNull(parentEntity, "Parent entity cannot be null");
		Check.notNull(childEntity, "Child entity cannot be null");
		Check.notNull(relationshipClass, "Relationship class cannot be null");
		try {
			ARelationshipMetadata metadata = relationshipClass.getAnnotation(ARelationshipMetadata.class);
			if (metadata == null) {
				return false;
			}
			if (!metadata.childCollectionField().isEmpty()) {
				List<?> collection = getCollection(childEntity, metadata.childCollectionField());
				if (collection != null) {
					return collection.stream().anyMatch(item -> {
						try {
							Method getParentMethod = findGetterMethod(item, metadata.parentEntityClass());
							if (getParentMethod != null) {
								Object parent = getParentMethod.invoke(item);
								return parent != null && parent.equals(parentEntity);
							}
						} catch (Exception e) {
							LOGGER.debug("Error checking relationship existence: {}", e.getMessage());
						}
						return false;
					});
				}
			}
			return false;
		} catch (Exception e) {
			LOGGER.error("Error checking relationship existence: {}", e.getMessage(), e);
			return false;
		}
	}

	/** Get all related entities of a specific type from an entity.
	 * @param entity              The entity to get relationships from
	 * @param collectionFieldName The name of the collection field
	 * @param relationshipClass   The relationship entity class
	 * @param targetEntityClass   The target entity class to extract
	 * @param <E>                 Entity type
	 * @param <R>                 Relationship entity type
	 * @param <T>                 Target entity type
	 * @return list of related entities */
	@SuppressWarnings ("unchecked")
	public <E, R extends CAbstractEntityRelationship<R>, T> List<T> getRelatedEntities(E entity, String collectionFieldName,
			Class<R> relationshipClass, Class<T> targetEntityClass) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notBlank(collectionFieldName, "Collection field name cannot be blank");
		Check.notNull(relationshipClass, "Relationship class cannot be null");
		Check.notNull(targetEntityClass, "Target entity class cannot be null");
		try {
			List<R> relationships = (List<R>) getCollection(entity, collectionFieldName);
			if (relationships == null) {
				return List.of();
			}
			Method getTargetMethod = findGetterMethod(relationshipClass.getDeclaredConstructor().newInstance(), targetEntityClass);
			if (getTargetMethod == null) {
				return List.of();
			}
			return relationships.stream().map(relationship -> {
				try {
					return (T) getTargetMethod.invoke(relationship);
				} catch (Exception e) {
					LOGGER.debug("Error extracting target entity: {}", e.getMessage());
					return null;
				}
			}).filter(java.util.Objects::nonNull).toList();
		} catch (Exception e) {
			LOGGER.error("Error getting related entities: {}", e.getMessage(), e);
			return List.of();
		}
	}

	// Helper methods
	private void setEntityReference(Object relationshipEntity, String setterName, Object entityToSet) throws Exception {
		Method setter = findSetterMethod(relationshipEntity, setterName);
		if (setter != null) {
			setter.invoke(relationshipEntity, entityToSet);
		} else {
			LOGGER.debug("Setter method {} not found in {}", setterName, relationshipEntity.getClass().getSimpleName());
		}
	}

	@SuppressWarnings ("unchecked")
	private void addToCollection(Object entity, String collectionFieldName, Object itemToAdd) throws Exception {
		List<Object> collection = (List<Object>) getCollection(entity, collectionFieldName);
		if (collection != null && !collection.contains(itemToAdd)) {
			collection.add(itemToAdd);
		}
	}

	@SuppressWarnings ("unchecked")
	private void removeFromCollection(Object entity, String collectionFieldName, Object itemToRemove) throws Exception {
		List<Object> collection = (List<Object>) getCollection(entity, collectionFieldName);
		if (collection != null) {
			collection.remove(itemToRemove);
		}
	}

	@SuppressWarnings ("unchecked")
	private void removeFromCollectionByParent(Object entity, String collectionFieldName, Object parentEntity) throws Exception {
		List<Object> collection = (List<Object>) getCollection(entity, collectionFieldName);
		if (collection != null) {
			collection.removeIf(item -> {
				try {
					Method getParentMethod = findGetterMethod(item, parentEntity.getClass());
					if (getParentMethod != null) {
						Object parent = getParentMethod.invoke(item);
						return parent != null && parent.equals(parentEntity);
					}
				} catch (Exception e) {
					LOGGER.debug("Error checking parent during removal: {}", e.getMessage());
				}
				return false;
			});
		}
	}

	private List<?> getCollection(Object entity, String fieldName) throws Exception {
		Method getter = findGetterMethod(entity, fieldName);
		if (getter != null) {
			return (List<?>) getter.invoke(entity);
		}
		return null;
	}

	private Method findGetterMethod(Object entity, String fieldName) {
		Class<?> clazz = entity.getClass();
		String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		try {
			return clazz.getMethod(getterName);
		} catch (NoSuchMethodException e) {
			LOGGER.debug("Getter method {} not found in {}", getterName, clazz.getSimpleName());
			return null;
		}
	}

	private Method findGetterMethod(Object entity, Class<?> returnType) {
		Class<?> clazz = entity.getClass();
		for (Method method : clazz.getMethods()) {
			if (method.getName().startsWith("get") && method.getParameterCount() == 0 && returnType.isAssignableFrom(method.getReturnType())) {
				return method;
			}
		}
		return null;
	}

	private Method findSetterMethod(Object entity, String setterName) {
		Class<?> clazz = entity.getClass();
		for (Method method : clazz.getMethods()) {
			if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
				return method;
			}
		}
		return null;
	}
}
