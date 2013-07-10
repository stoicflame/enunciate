package org.codehaus.enunciate;

import org.codehaus.enunciate.qname.XmlQNameEnum;
import org.codehaus.enunciate.qname.XmlQNameEnumValue;
import org.codehaus.enunciate.qname.XmlUnknownQNameEnumValue;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilities for converting a QName to/from an QNameEnum. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
 * 
 * @author Ryan Heaton
 */
public class XmlQNameEnumUtil {

  private XmlQNameEnumUtil() {}

  private static final AtomicReference<String> DEFAULT_BASE_URI = new AtomicReference<String>();
  private static final AtomicBoolean WRITE_RELATIVE_URIS = new AtomicBoolean(false);

  /**
   * Set the default base uri for resolving qname URIs.
   *
   * @param uri The default base URI.
   */
  public static void setDefaultBaseUri(String uri) {
    DEFAULT_BASE_URI.set(uri);
  }

  /**
   * Get the default base uri for resolving qname URIs.
   *
   * @return The default base URI.
   */
  public static String getDefaultBaseUri() {
    return DEFAULT_BASE_URI.get();
  }

  /**
   * Whether to write URI enums using relative URIs.
   *
   * @return Whether to write URI enums using relative URIs.
   */
  public static boolean isWriteRelativeUris() {
    return WRITE_RELATIVE_URIS.get();
  }

  /**
   * Whether to write URI enums using relative URIs.
   *
   * @param writeRelativeUris Whether to write URI enums using relative URIs.
   */
  public static void setWriteRelativeUris(boolean writeRelativeUris) {
    WRITE_RELATIVE_URIS.set(writeRelativeUris);
  }

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
    else if (enumInfo.base() != XmlQNameEnum.BaseType.QNAME) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " is supposed to be converted from a URI (not QName).");
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
    else if (enumInfo.base() != XmlQNameEnum.BaseType.QNAME) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " is supposed to be converted to a URI (not QName).");
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

  /**
   * Convert a URI to a QName enum. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
   *
   * @param uri The uri to convert.
   * @param clazz The enum clazz.
   * @return The matching enum, or the {@link XmlUnknownQNameEnumValue unknown enum} if unable to find an enum for the specified URI, or <code>null</code>
   * if unable to find an enum for the specified URI and there is no unknown enum specified.
   * @throws IllegalArgumentException If <code>clazz</code> isn't a QName enum.
   */
  public static <Q extends Enum<Q>> Q fromURI(final URI uri, Class<Q> clazz) {
    if (uri == null) {
      return null;
    }

    return fromURIValue(uri.toString(), clazz);
  }

  /**
   * Convert a URI to a QName enum. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
   *
   * @param uriValue The value of the uri to convert.
   * @param clazz The enum clazz.
   * @return The matching enum, or the {@link XmlUnknownQNameEnumValue unknown enum} if unable to find an enum for the specified URI, or <code>null</code>
   * if unable to find an enum for the specified URI and there is no unknown enum specified.
   * @throws IllegalArgumentException If <code>clazz</code> isn't a QName enum.
   */
  public static <Q extends Enum<Q>> Q fromURIValue(String uriValue, Class<Q> clazz) {
    return fromURIValue(uriValue, clazz, getDefaultBaseUri());
  }

  /**
   * Convert a URI to a QName enum. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
   *
   * @param uriValue The value of the uri to convert.
   * @param clazz The enum clazz.
   * @param defaultBaseUri The default base uri, used to resolve relative URI references (null is allowed).
   * @return The matching enum, or the {@link XmlUnknownQNameEnumValue unknown enum} if unable to find an enum for the specified URI, or <code>null</code>
   * if unable to find an enum for the specified URI and there is no unknown enum specified.
   * @throws IllegalArgumentException If <code>clazz</code> isn't a QName enum.
   */
  public static <Q extends Enum<Q>> Q fromURIValue(String uriValue, Class<Q> clazz, String defaultBaseUri) {
    if (uriValue == null) {
      return null;
    }

    if (defaultBaseUri != null) {
      uriValue = URI.create(defaultBaseUri).resolve(uriValue).toString();
    }

    XmlQNameEnum enumInfo = clazz.getAnnotation(XmlQNameEnum.class);
    if (enumInfo == null) {
      throw new IllegalArgumentException(String.format("Class %s isn't a QName enum.", clazz.getName()));
    }
    else if (enumInfo.base() != XmlQNameEnum.BaseType.URI) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " is supposed to be converted to a QName (not URI).");
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

        if ((ns + localPart).equals(uriValue)) {
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
   * Convert an enum to a URI. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
   *
   * @param e The enum.
   * @return The URI.
   * @throws IllegalArgumentException If <code>e</code> isn't of a valid QName enum type,
   * or if <code>e</code> is the {@link XmlUnknownQNameEnumValue unknown enum},
   * or if {@link org.codehaus.enunciate.qname.XmlQNameEnumValue#exclude() the enum is excluded as an enum value}.
   */
  public static URI toURI(Enum e) {
    if (e == null) {
      return null;
    }

    return URI.create(toURIValue(e));
  }

  /**
   * Convert an enum to a URI. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
   *
   * @param e The enum.
   * @return The URI.
   * @throws IllegalArgumentException If <code>e</code> isn't of a valid QName enum type,
   * or if <code>e</code> is the {@link XmlUnknownQNameEnumValue unknown enum},
   * or if {@link org.codehaus.enunciate.qname.XmlQNameEnumValue#exclude() the enum is excluded as an enum value}.
   */
  public static String toURIValue(Enum e) {
    return toURIValue(e, getDefaultBaseUri());
  }

  /**
   * Convert an enum to a URI. See <a href="http://docs.codehaus.org/display/ENUNCIATE/QName+Enums">QName Enums</a>.
   *
   * @param e The enum.
   * @param defaultBaseUri The default base uri, used to resolve relative URI references (null is allowed).
   * @return The URI.
   * @throws IllegalArgumentException If <code>e</code> isn't of a valid QName enum type,
   * or if <code>e</code> is the {@link XmlUnknownQNameEnumValue unknown enum},
   * or if {@link org.codehaus.enunciate.qname.XmlQNameEnumValue#exclude() the enum is excluded as an enum value}.
   */
  public static String toURIValue(Enum e, String defaultBaseUri) {
    if (e == null) {
      return null;
    }

    String uriValue = null;

    Class<?> clazz = e.getDeclaringClass();
    XmlQNameEnum enumInfo = clazz.getAnnotation(XmlQNameEnum.class);
    if (enumInfo == null) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " isn't a QName enum.");
    }
    else if (enumInfo.base() != XmlQNameEnum.BaseType.URI) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " is supposed to be converted from a QName (not URI).");
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

        if (ns.equals(defaultBaseUri) && isWriteRelativeUris()) {
          ns = "";
        }

        uriValue = ns + localPart;
        break;
      }
    }

    if (uriValue == null) {
      throw new IllegalStateException("Unable to find " + e.getDeclaringClass().getName() + "." + e + " as a QName enum value.");
    }

    return uriValue;
  }
}
