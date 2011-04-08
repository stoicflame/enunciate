package org.codehaus.enunciate.qname;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Customize how an enum constant is mapped to a QName. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
 *
 * @author Ryan Heaton
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface XmlQNameEnumValue {

  /**
   * The namespace for this QName enum value. If left unspecified (i.e. the value is "##default"), then
   * standard JAXB namespacing applies, which looks like this:
   *
   * <ol>
   *  <li>If the enclosing class has {@link XmlQNameEnum} annotation and its namespace value is not "##default",
   *  then the namespace of the enclosing class.</li>
   *  <li>If the enclosing package has {@link javax.xml.bind.annotation.XmlSchema} annotation,
   *  and its {@link javax.xml.bind.annotation.XmlSchema#elementFormDefault() elementFormDefault}
   *  is {@link javax.xml.bind.annotation.XmlNsForm#QUALIFIED QUALIFIED}, then the namespace of
   *  the enclosing package.</li>
   *  <li>Otherwise "" (which produces unqualified element in the default namespace).</li>
   * </ol>
   *
   * @return The namespace for this QName enumeration.
   */
  String namespace() default "##default";

  /**
   * The QName local part. If the value is "##default" the local part will be the name of the enum constant.
   *
   * @return The QName local part.
   */
  String localPart() default "##default";

  /**
   * Exclude this enum constant from the known QName enums.
   *
   * @return Whether this enum constant should be excluded from the list of known QName constants.
   */
  boolean exclude() default false;
}
