package org.codehaus.enunciate.qname;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks an enumeration constant as the enum to use when converting from an unknown QName to an enum. See
 * <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
 *
 * @author Ryan Heaton
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface XmlUnknownQNameEnumValue {
}
