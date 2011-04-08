package org.codehaus.enunciate;

import org.codehaus.enunciate.qname.XmlQNameEnum;
import org.codehaus.enunciate.qname.XmlQNameEnumValue;
import org.codehaus.enunciate.qname.XmlUnknownQNameEnumValue;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;

/**
 * Utilities for converting a QName to/from an QNameEnum. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
 * 
 * @author Ryan Heaton
 */
public class XmlQNameEnumUtil {

  private XmlQNameEnumUtil() {}

  /**
   * Convert a QName to a QName enum. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
   *
   * @param qname The qname to convert.
   * @param clazz The enum clazz.
   * @return The matching enum, or the {@link XmlUnknownQNameEnumValue unknown enum} if unable to find an enum for the specified QName, or <code>null</code>
   * if unable to find an enum for the specified QName and there is no unknown enum specified.
   * @throws IllegalArgumentException If <code>clazz</code> isn't a QName enum.
   */
  public static <Q extends Enum<Q>> Q fromQName(final QName qname, Class<Q> clazz) {
    if (qname == null) {
      return null;
    }

    XmlQNameEnum enumInfo = clazz.getAnnotation(XmlQNameEnum.class);
    if (enumInfo == null) {
      throw new IllegalArgumentException(String.format("Class %s isn't a QName enum.", clazz.getName()));
    }

    String namespace = enumInfo.namespace();
    if ("##default".equals(namespace)) {
      Package pkg = clazz.getPackage();
      if (pkg != null) {
        XmlSchema schemaInfo = pkg.getAnnotation(XmlSchema.class);
        namespace = schemaInfo.namespace();
      }
    }

    Field unknown = null;
    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (field.isEnumConstant()) {
        if (field.getAnnotation(XmlUnknownQNameEnumValue.class) != null) {
          unknown = field;
          continue;
        }

        XmlQNameEnumValue enumValueInfo = field.getAnnotation(XmlQNameEnumValue.class);
        String ns = namespace;
        String localPart = field.getName();
        if (enumValueInfo != null) {
          if (enumValueInfo.exclude()) {
            continue;
          }
          if (!"##default".equals(enumValueInfo.namespace())) {
            ns = enumValueInfo.namespace();
          }
          if (!"##default".equals(enumValueInfo.localPart())) {
            localPart = enumValueInfo.localPart();
          }
        }

        if (new QName(ns, localPart).equals(qname)) {
          return Enum.valueOf(clazz, field.getName());
        }
      }
    }

    if (unknown != null) {
      return Enum.valueOf(clazz, unknown.getName());
    }
    else {
      return null;
    }
  }

  /**
   * Convert an enum to a QName. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
   *
   * @param e The enum.
   * @return The QName.
   * @throws IllegalArgumentException If <code>e</code> isn't of a valid QName enum type,
   * or if <code>e</code> is the {@link XmlUnknownQNameEnumValue unknown enum},
   * or if {@link org.codehaus.enunciate.qname.XmlQNameEnumValue#exclude() the enum is excluded as an enum value}.
   */
  public static QName toQName(Enum e) {
    if (e == null) {
      return null;
    }

    Class<?> clazz = e.getDeclaringClass();
    XmlQNameEnum enumInfo = clazz.getAnnotation(XmlQNameEnum.class);
    if (enumInfo == null) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " isn't a QName enum.");
    }

    String namespace = enumInfo.namespace();
    if ("##default".equals(namespace)) {
      Package pkg = clazz.getPackage();
      if (pkg != null) {
        XmlSchema schemaInfo = pkg.getAnnotation(XmlSchema.class);
        namespace = schemaInfo.namespace();
      }
    }

    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (field.isEnumConstant() && field.getName().equals(e.name())) {
        if (field.getAnnotation(XmlUnknownQNameEnumValue.class) != null) {
          throw new IllegalArgumentException(e.getDeclaringClass().getName() + "." + e + " is not a QName enum value.");
        }

        XmlQNameEnumValue enumValueInfo = field.getAnnotation(XmlQNameEnumValue.class);
        String ns = namespace;
        String localPart = field.getName();
        if (enumValueInfo != null && !enumValueInfo.exclude()) {
          if (enumValueInfo.exclude()) {
            throw new IllegalArgumentException(e.getDeclaringClass().getName() + "." + e + " is excluded a QName enum value.");
          }
          if (!"##default".equals(enumValueInfo.namespace())) {
            ns = enumValueInfo.namespace();
          }
          if (!"##default".equals(enumValueInfo.localPart())) {
            localPart = enumValueInfo.localPart();
          }
        }

        return new QName(ns, localPart);
      }
    }

    throw new IllegalStateException("Unable to find " + e.getDeclaringClass().getName() + "." + e + " as a QName enum value.");
  }
}
