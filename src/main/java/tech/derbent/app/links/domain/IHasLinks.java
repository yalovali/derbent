package tech.derbent.app.links.domain;

import java.util.Set;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.entity.domain.CEntityDB;

/**
 * IHasLinks - Interface for entities that can have links to other entities.
 * 
 * Entities implementing this interface can have bidirectional links managed via link components.
 * Links are bidirectional: creating a link from A to B automatically creates a reverse link from B to A.
 * 
 * Pattern: Unidirectional @OneToMany from parent entity to CLink.
 * CLink has NO back-reference to parent (clean unidirectional relationship).
 * 
 * Usage in entity:
 * <pre>
 * public class CActivity extends CProjectItem<CActivity> implements IHasLinks {
 *
 *     {@literal @}OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
 *     {@literal @}JoinColumn(name = "source_entity_activity_id")
 *     {@literal @}AMetaData(
 *         displayName = "Links",
 *         dataProviderBean = "CLinkService",
 *         createComponentMethod = "createComponent"
 *     )
 *     private Set<CLink> links = new HashSet<>();
 *
 *     {@literal @}Override
 *     public Set<CLink> getLinks() {
 *         if (links == null) {
 *             links = new HashSet<>();
 *         }
 *         return links;
 *     }
 *
 *     {@literal @}Override
 *     public void setLinks(Set<CLink> links) {
 *         this.links = links;
 *     }
 * }
 * </pre>
 *
 * Layer: Domain (MVC)
 */
public interface IHasLinks {

    /**
     * Get the set of links for this entity.
     * Implementation should never return null - return empty set if no links.
     * Initialize the set if null before returning.
     * 
     * @return set of links, never null
     */
    Set<CLink> getLinks();

    /**
     * Set the set of links for this entity.
     * 
     * @param links the links set, can be null (will be initialized on next get)
     */
    void setLinks(Set<CLink> links);

    /**
     * Copy links from source to target if both implement IHasLinks and options allow.
     * This default method reduces code duplication by providing a standard implementation of link copying.
     * 
     * Note: Links are NOT copied by default due to their bidirectional nature and potential complexity.
     * If links should be copied, the options must explicitly request it.
     * 
     * @param source the source entity
     * @param target the target entity
     * @param options copy options controlling whether links are included
     * @return true if links were copied, false if skipped
     */
    static boolean copyLinksTo(final CEntityDB<?> source, final CEntityDB<?> target, final CCloneOptions options) {
        // Links are not included in standard copying due to their bidirectional nature
        // and potential for creating complex relationship webs
        if (!options.includesRelations()) {
            return false;
        }
        
        // Check if both source and target implement IHasLinks
        if (!(source instanceof IHasLinks) || !(target instanceof IHasLinks)) {
            return false; // Skip silently if target doesn't support links
        }
        
        try {
            final IHasLinks sourceWithLinks = (IHasLinks) source;
            final IHasLinks targetWithLinks = (IHasLinks) target;
            
            // Copy link collection using source's copyCollection method
            source.copyCollection(sourceWithLinks::getLinks, 
                    (col) -> targetWithLinks.setLinks((java.util.Set<CLink>) col), 
                    true);  // createNew = true to clone links
            return true;
        } catch (final Exception e) {
            // Log and skip on error - don't fail entire copy operation
            return false;
        }
    }
}
