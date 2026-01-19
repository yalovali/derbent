package tech.derbent.plm.links.domain;

/**
 * ILinkable - DEPRECATED: Merged into IHasLinks.
 * 
 * This interface has been deprecated and merged into IHasLinks.
 * Since links are bidirectional, entities only need to implement IHasLinks
 * to both HAVE links and BE linkable as targets.
 * 
 * @deprecated Use {@link IHasLinks} instead. This interface will be removed in a future release.
 * 
 * Migration: Replace "implements ILinkable" with "implements IHasLinks" and add the links
 * collection as documented in IHasLinks.
 * 
 * Layer: Domain (MVC)
 */
@Deprecated(since = "2026-01-18", forRemoval = true)
public interface ILinkable {
    // Marker interface - no methods required
    // Entities must have getId() and getName() from base classes
}
