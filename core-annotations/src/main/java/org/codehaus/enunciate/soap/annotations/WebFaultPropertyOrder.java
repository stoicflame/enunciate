package org.codehaus.enunciate.soap.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Allows users to specify the property order of a web fault bean.
 *
 * @author Ryan Heaton
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ( ElementType.TYPE )
public @interface WebFaultPropertyOrder {

  /**
   * The property order.
   *
   * @return The property order.
   */
  String[] value();
}
