package org.codehaus.enunciate.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation hint for declaring the grouping of certain documentation elements.
 *
 * @author Ryan Heaton
 */
@Target ( { ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD } )
@Retention ( RetentionPolicy.RUNTIME )
public @interface DocumentationGroup {

  /**
   * The groups for this documentation element.
   *
   * @return The groups for this documentation element.
   */
  String[] value();
}
