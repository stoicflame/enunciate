package org.codehaus.enunciate.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Denotes that a field or method exposes a JSON object property.
 * </p>
 *
 * @author Steven Cummings
 */
@Target ( {ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER} )
@Retention ( RetentionPolicy.RUNTIME )
public @interface JsonProperty {
  /**
   * Optional. Defines the logical name for the property. If not given, the logical name for the property will be derived as the bean property name based on the field or method name.
   */
  String name() default "";
}
