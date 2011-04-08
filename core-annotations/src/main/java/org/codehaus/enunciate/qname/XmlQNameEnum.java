package org.codehaus.enunciate.qname;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Special Enunciate support for qname enumerations. See http://docs.codehaus.org/display/ENUNCIATE/QName+Enums.
 *
 * @author Ryan Heaton
 */
@Retention (RUNTIME) @Target ({TYPE})
public @interface XmlQNameEnum {

  /**
   * The namespace for this qname enumeration. If left unspecified (i.e. the value is "##default"), then
   * standard JAXB namespacing applies, which looks like this:
   *
   * <ol>
   *  <li>If the enclosing package has {@link javax.xml.bind.annotation.XmlSchema} annotation,
   *  and its {@link javax.xml.bind.annotation.XmlSchema#elementFormDefault() elementFormDefault}
   *  is {@link javax.xml.bind.annotation.XmlNsForm#QUALIFIED QUALIFIED}, then the namespace of
   *  the enclosing class.</li>
   *  <li>Otherwise "" (which produces unqualified element in the default namespace).</li>
   * </ol>
   *
   * @return The namespace for this qname enumeration.
   */
  String namespace() default "##default";

}
