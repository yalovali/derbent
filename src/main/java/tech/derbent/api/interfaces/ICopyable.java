package tech.derbent.api.interfaces;

/**
 * Marker interface for entities that support copying to same or different types.
 * Entities implementing this interface can use the copyTo pattern for flexible
 * entity duplication with optional cross-type copying.
 * 
 * The copyTo() method is implemented in CEntityDB which provides
 * the actual copy implementation using getter/setter based field mapping.
 * 
 * @param <T> the entity type
 */
public interface ICopyable<T> {
    
    /**
     * Checks if this entity can be copied to the specified target class.
     * Default implementation allows copying to same type only.
     * 
     * @param targetClass the class to check compatibility with
     * @return true if copying to target class is supported
     */
    default boolean canCopyTo(final Class<?> targetClass) {
        return targetClass.equals(getClass());
    }
}
