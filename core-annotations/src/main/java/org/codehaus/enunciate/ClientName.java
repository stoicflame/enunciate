package org.codehaus.enunciate;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to name a declaration something different on the client-side. E.g. rename an AMF class or C# property or something like that.
 *
 * @author Ryan Heaton
 */
@Target (
  { ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD ,ElementType.TYPE, ElementType.PACKAGE }
)
@Retention (
  RetentionPolicy.RUNTIME
)
public @interface ClientName {

  /**
   * The value of the client name.
   *
   * @return The value of the client name.
   */
  String value();
}
