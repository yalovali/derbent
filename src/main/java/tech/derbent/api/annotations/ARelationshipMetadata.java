package tech.derbent.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to define metadata for entity relationships.
 * This annotation can be used to configure generic relationship components
 * and automatically generate relationship management features.
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ARelationshipMetadata {
    
    /** The parent entity class in the relationship */
    Class<?> parentEntityClass() default Object.class;
    
    /** The child entity class in the relationship */
    Class<?> childEntityClass() default Object.class;
    
    /** The relationship entity class that manages the connection */
    Class<?> relationshipEntityClass() default Object.class;
    
    /** Display name for the relationship */
    String displayName() default "";
    
    /** Description of the relationship */
    String description() default "";
    
    /** Whether this is a one-to-many relationship */
    boolean oneToMany() default true;
    
    /** Whether this is a many-to-many relationship */
    boolean manyToMany() default false;
    
    /** Whether ownership/privileges are supported in this relationship */
    boolean supportsOwnership() default false;
    
    /** Default ownership level for new relationships */
    String defaultOwnership() default "MEMBER";
    
    /** Available ownership levels */
    String[] ownershipLevels() default {"OWNER", "ADMIN", "MEMBER", "VIEWER"};
    
    /** Whether this relationship should be managed automatically */
    boolean autoManaged() default true;
    
    /** Field name in the parent entity that holds the relationship collection */
    String parentCollectionField() default "";
    
    /** Field name in the child entity that holds the relationship collection */
    String childCollectionField() default "";
    
    /** Service class that manages this relationship */
    Class<?> serviceClass() default Object.class;
}