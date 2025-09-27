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

	boolean autoSelectFirst() default false;

	boolean clearOnEmptyData() default false;

	boolean colorField() default false;

	boolean comboboxReadOnly() default false;

	String dataProviderBean() default "";

	Class<?> dataProviderClass() default Object.class;

	String dataProviderMethod() default "list";

	String dataProviderParamMethod() default "";

	/** Indicates that the data provider should call a method on the content owner (page) instead of a service bean. Use "content" to call methods on
	 * the current page/context. */
	String dataProviderOwner() default "";

	String dataUpdateMethod() default "";

	String defaultValue() default "";

	String description() default "";

	String displayName() default "Field";

	String filterMethod() default "";

	String createComponentMethod() default "";

	boolean hidden() default false;

	boolean imageData() default false;

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
