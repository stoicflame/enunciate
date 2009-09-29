package org.codehaus.enunciate.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Overrides the JSON name naturally derived for a type or property.
 * </p>
 * @author Steven Cummings
 */
@Target ( {ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE} )
@Retention ( RetentionPolicy.RUNTIME )
public @interface JsonName {
  /**
   * Value of the JSON name to expose the type or property as.
   */
  String value();
}
