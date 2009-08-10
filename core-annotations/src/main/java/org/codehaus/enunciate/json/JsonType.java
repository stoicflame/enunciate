package org.codehaus.enunciate.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Denotes that a type represents a JSON type.
 * </p>
 *
 * @author Steven Cummings
 */
@Target ( ElementType.TYPE )
@Retention ( RetentionPolicy.RUNTIME )
public @interface JsonType {
  /**
   * Optional. Defines the logical name for the type. If not given, the fully-qualified name of the java type will be used.
   */
  String name() default "";
}
