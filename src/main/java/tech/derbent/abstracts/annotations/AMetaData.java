package tech.derbent.abstracts.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** MetaData annotation for field metadata and form generation. Used by CEntityFormBuilder to automatically generate UI components.
 * <p>
 * This annotation provides comprehensive metadata for entity fields to control how they are displayed and validated in automatically generated forms.
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 *
 * @MetaData (displayName = "User Name", required = true, description = "The full name of the user", order = 1, maxLength = 255)
 * private String name;
 * }</pre>
 * <p>
 * <strong>Parameter Validation:</strong>
 * </p>
 * <ul>
 * <li>All String parameters are null-safe (empty string used as default)</li>
 * <li>Numeric parameters have sensible defaults and ranges</li>
 * <li>Boolean parameters default to false for safety</li>
 * </ul>
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.CEntityFormBuilder */
@Documented
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.FIELD)
public @interface AMetaData {

	boolean allowCustomValue() default false;

	boolean autoSelectFirst() default false;

	boolean clearOnEmptyData() default false;

	boolean comboboxReadOnly() default false;

	String dataProviderBean() default "";

	Class<?> dataProviderClass() default Object.class;

	String dataProviderMethod() default "list";

	String dataProviderParamMethod() default "";

	String dataUpdateMethod() default "";

	String defaultValue() default "";

	String description() default "";

	String displayName() default "Field";

	String filterMethod() default "";

	boolean hidden() default false;

	double max() default Double.MAX_VALUE;

	int maxLength() default -1;

	double min() default Double.MIN_VALUE;

	int order() default 100;

	boolean passwordField() default false;

	boolean passwordRevealButton() default false;

	String placeholder() default "";

	boolean readOnly() default false;

	boolean required() default false;

	boolean setBackgroundFromColor() default false;

	boolean useIcon() default false;

	boolean useRadioButtons() default false;

	String width() default "";
}
