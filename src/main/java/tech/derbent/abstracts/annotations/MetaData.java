package tech.derbent.abstracts.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * MetaData annotation for field metadata and form generation.
 * Used by CEntityFormBuilder to automatically generate UI components.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetaData {

	String defaultValue() default "";

	String description() default "";

	String displayName() default "Field";

	boolean hidden() default false;

	boolean readOnly() default false;

	boolean required() default false;

	/**
	 * Order of the field in forms. Lower values appear first.
	 */
	int order() default 100;

	/**
	 * Width hint for the component (e.g. "200px", "50%").
	 */
	String width() default "";

	/**
	 * For numeric fields, specify minimum value.
	 */
	double min() default Double.MIN_VALUE;

	/**
	 * For numeric fields, specify maximum value.
	 */
	double max() default Double.MAX_VALUE;

	/**
	 * For text fields, specify maximum length.
	 */
	int maxLength() default -1;

	/**
	 * For enum fields, specify if should use radio buttons instead of combo box.
	 */
	boolean useRadioButtons() default false;
}