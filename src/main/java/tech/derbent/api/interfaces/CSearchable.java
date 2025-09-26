package tech.derbent.api.interfaces;
/** CSearchable - Interface for entities that support text-based searching. Layer: Domain (MVC) Entities implementing this interface can define how
 * they match against search text, allowing for flexible and customizable search behavior across different entity types. The search should be
 * case-insensitive and match against relevant string fields.
 * @author Derbent Framework
 * @since 1.0 */
public interface CSearchable {

	/** Determines if this entity matches the given search text. The implementation should check relevant string fields in a case-insensitive manner.
	 * @param searchText the text to search for (null or empty means match all)
	 * @return true if the entity matches the search text, false otherwise */
	boolean matches(String searchText);
}
