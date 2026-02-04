package tech.derbent.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
public @interface AMetaData {

	boolean allowCustomValue() default false;

	// list possible profile field to ALLOW this field, or empty for no restriction
	String allowedProfiles() default "";

	/** When true, the field value is automatically calculated and populated after entity is loaded from database using JPA @PostLoad lifecycle
	 * callback. The calculation uses the dataProviderBean and dataProviderMethod to invoke the service method. This is useful for transient
	 * calculated fields that should be populated immediately after entity load without requiring form builder or UI interaction. */
	boolean autoCalculate() default false;

	boolean autoSelectFirst() default false;

	boolean captionVisible() default true;

	boolean clearOnEmptyData() default false;

	boolean colorField() default false;

	boolean comboboxReadOnly() default false;

	String createComponentMethod() default "";

	String dataProviderBean() default "";

	String dataProviderMethod() default "findAll";

	String dataProviderParamBean() default "";

	String dataProviderParamMethod() default "";

	String dataUpdateMethod() default "";

	String defaultValue() default "";

	String description() default "";

	String displayName() default "Field";

	String filterMethod() default "";

	boolean hidden() default false;

	boolean imageData() default false;

	double max() default Double.MAX_VALUE;

	int maxLength() default -1;

	double min() default Double.MIN_VALUE;

	boolean passwordField() default false;

	boolean passwordRevealButton() default false;

	String placeholder() default "";

	boolean readOnly() default false;

	boolean required() default false;

	boolean setBackgroundFromColor() default false;

	/** When true, uses a dual list selector component (available/selected lists with add/remove/ordering buttons) instead of MultiSelectComboBox for
	 * Set fields. This provides a better UX for selecting and ordering multiple items. */
	boolean useDualListSelector() default false;

	/** When true, uses a grid-based list selector component (single grid with checkmarks for selected items) instead of MultiSelectComboBox for
	 * List/Set fields. This provides a simpler selection UX without ordering controls. If both useDualListSelector and useGridSelection are true,
	 * useGridSelection takes precedence. */
	boolean useGridSelection() default false;

	boolean useIcon() default false;

	boolean useRadioButtons() default false;

	String width() default "";
}
