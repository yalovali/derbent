package tech.derbent.api.pagequery.domain;

/** CPageViewFilterSpecialValue - Special (non-entity) filter values for {@link CPageViewQuery}.
 * <p>
 * We cannot use {@code null} to represent "no value" for a relationship field because {@code null}
 * already means "no filtering" (Select All). This enum provides explicit "no value" semantics.
 * </p>
 */
public enum CPageViewFilterSpecialValue {
	/** Filters for entities where the filtered reference is not set (e.g., no sprint, no epic). */
	NO_VALUE;
}
