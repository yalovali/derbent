package tech.derbent.app.links.domain;

/**
 * ILinkable - Marker interface for entities that can be linked to other entities.
 * 
 * Any entity implementing this interface can participate in bidirectional links.
 * The link system stores entity type and ID, making it flexible to work with any entity.
 * 
 * Entities that implement this interface can be both source and target of links.
 * 
 * Layer: Domain (MVC)
 */
public interface ILinkable {
    // Marker interface - no methods required
    // Entities must have getId() and getName() from base classes
}
