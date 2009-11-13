package org.codehaus.enunciate.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Explicitly defines a mapping of the specified type to a target JSON type.
 * </p>
 * @author Steven Cummings
 */
@Target ( ElementType.PACKAGE )
@Retention ( RetentionPolicy.RUNTIME )
public @interface JsonTypeMapping {

  /**
   * Fully qualified name of the java source type.
   */
  String javaType();

  /**
   * Name of the target JSON type. This is expected to be one of the following:
   * <ul>
   * <li>{@code string} (case-insensitive)</li>
   * <li>{@code number} (case-insensitive)</li>
   * <li>{@code boolean} (case-insensitive)</li>
   * <li>Fully qualified name of another type, whose JSON type should be used for the source type of this mapping.</li>
   * </ul>
   */
  String jsonType();
}
