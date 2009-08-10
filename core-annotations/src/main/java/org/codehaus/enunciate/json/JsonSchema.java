package org.codehaus.enunciate.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Denotes that a package represents a JSON schema. In this sense, "schema" is used informally to represent a collection of types identified by a "schema id", and not any specification like <a
 * href="http://groups.google.com/group/json-schema">json-schema</a>. That could change when that specification is finalized.
 * </p>
 *
 * @author Steven Cummings
 */
@Target ( ElementType.PACKAGE )
@Retention ( RetentionPolicy.RUNTIME )
public @interface JsonSchema {
  /**
   * Specifies the ID of the schema represented by the annotated package.
   */
  String schemaId();
}
