package org.codehaus.enunciate.qname;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Special Enunciate support for QName enumerations. See http://docs.codehaus.org/display/ENUNCIATE/QName+Enums.
 *
 * @author Ryan Heaton
 */
@Retention (RUNTIME) @Target ({FIELD, ElementType.METHOD})
public @interface XmlQNameEnumRef {

  /**
   * The reference to the {@link XmlQNameEnum class} that defines the "known" QNames for this accessor.
   *
   * @return The QName enum class.
   */
  Class<? extends Enum> value();

}
