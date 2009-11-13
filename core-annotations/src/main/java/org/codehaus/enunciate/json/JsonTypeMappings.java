package org.codehaus.enunciate.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Explicitly defines mappings of the specified types to target JSON types.
 * </p>
 * @author Steven Cummings
 */
@Target ( ElementType.PACKAGE )
@Retention ( RetentionPolicy.RUNTIME )
public @interface JsonTypeMappings {
  JsonTypeMapping[] value();
}
