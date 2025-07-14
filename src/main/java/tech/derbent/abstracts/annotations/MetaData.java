package tech.derbent.abstracts.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetaData {

	String defaultValue() default "Varsayılan Değer";

	String description() default "Nesne açıklaması";

	String displayName() default "Ad";

	boolean hidden() default false;

	boolean readOnly() default false;

	boolean required() default false;
}