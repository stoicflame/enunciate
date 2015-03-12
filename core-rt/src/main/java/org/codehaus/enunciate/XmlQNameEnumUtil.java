package org.codehaus.enunciate;

import org.codehaus.enunciate.qname.XmlQNameEnum;
import org.codehaus.enunciate.qname.XmlQNameEnumValue;
import org.codehaus.enunciate.qname.XmlUnknownQNameEnumValue;

import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
  private static final QName UNKNOWN_QNAME_ENUM = new QName("enunciate:qname-enum", "UNKNOWN");
  private static final QName EXCLUDED_QNAME_ENUM = new QName("enunciate:qname-enum", "EXCLUDED");
  private static final Map<Class<? extends Enum>, Map<? extends Enum, QName>> QNAME_CACHE = new ConcurrentHashMap<Class<? extends Enum>, Map<? extends Enum, QName>>();

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

    if (!clazz.isEnum()) {
      throw new IllegalArgumentException(String.format("Class %s isn't a QName enum.", clazz.getName()));
    }

    Map<? extends Enum, QName> qNameMap = QNAME_CACHE.get(clazz);
    if (qNameMap == null) {
      qNameMap = createQNameMap(clazz);
      QNAME_CACHE.put(clazz, qNameMap);
    }

    XmlQNameEnum enumInfo = clazz.getAnnotation(XmlQNameEnum.class);
    if (enumInfo.base() != XmlQNameEnum.BaseType.QNAME) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " is supposed to be converted from a URI (not QName).");
    }

    Q defaultValue = null;
    for (Map.Entry<? extends Enum, QName> qNameEntry : qNameMap.entrySet()) {
      if (qNameEntry.getValue().equals(qname)) {
        return (Q) qNameEntry.getKey();
      }
      else if (defaultValue == null && UNKNOWN_QNAME_ENUM.equals(qNameEntry.getValue())) {
        defaultValue = (Q) qNameEntry.getKey();
      }
    }

    return defaultValue;
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

    if (!e.getDeclaringClass().isEnum()) {
      throw new IllegalArgumentException(String.format("Class %s isn't a QName enum.", e.getDeclaringClass().getName()));
    }
    Class<Enum> clazz = e.getDeclaringClass();

    Map<? extends Enum, QName> qNameMap = QNAME_CACHE.get(clazz);
    if (qNameMap == null) {
      qNameMap = createQNameMap(clazz);
      QNAME_CACHE.put(clazz, qNameMap);
    }

    XmlQNameEnum enumInfo = clazz.getAnnotation(XmlQNameEnum.class);
    if (enumInfo.base() != XmlQNameEnum.BaseType.QNAME) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " is supposed to be converted from a URI (not QName).");
    }

    QName result = qNameMap.get(e);
    if (result == null) {
      throw new IllegalStateException("Unable to find " + e.getDeclaringClass().getName() + "." + e + " as a QName enum value.");
    }
    else if (UNKNOWN_QNAME_ENUM.equals(result)) {
      throw new IllegalArgumentException(e.getDeclaringClass().getName() + "." + e + " is not a QName enum value.");
    }
    else if (EXCLUDED_QNAME_ENUM.equals(result)) {
      throw new IllegalArgumentException(e.getDeclaringClass().getName() + "." + e + " is excluded a QName enum value.");
    }
    else {
      return result;
    }
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

    if (!clazz.isEnum()) {
      throw new IllegalArgumentException(String.format("Class %s isn't a QName enum.", clazz.getName()));
    }

    Map<? extends Enum, QName> qNameMap = QNAME_CACHE.get(clazz);
    if (qNameMap == null) {
      qNameMap = createQNameMap(clazz);
      QNAME_CACHE.put(clazz, qNameMap);
    }

    XmlQNameEnum enumInfo = clazz.getAnnotation(XmlQNameEnum.class);
    if (enumInfo.base() != XmlQNameEnum.BaseType.URI) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " is supposed to be converted to a QName (not URI).");
    }

    Q defaultValue = null;
    for (Map.Entry<? extends Enum, QName> qNameEntry : qNameMap.entrySet()) {
      String uri = qNameEntry.getValue().getNamespaceURI() + qNameEntry.getValue().getLocalPart();
      if (uri.equals(uriValue)) {
        return (Q) qNameEntry.getKey();
      }
      else if (defaultValue == null && UNKNOWN_QNAME_ENUM.equals(qNameEntry.getValue())) {
        defaultValue = (Q) qNameEntry.getKey();
      }
    }

    return defaultValue;
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

    if (!e.getDeclaringClass().isEnum()) {
      throw new IllegalArgumentException(String.format("Class %s isn't a QName enum.", e.getDeclaringClass().getName()));
    }
    Class<Enum> clazz = e.getDeclaringClass();

    XmlQNameEnum enumInfo = clazz.getAnnotation(XmlQNameEnum.class);
    if (enumInfo.base() != XmlQNameEnum.BaseType.URI) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " is supposed to be converted to a QName (not URI).");
    }

    Map<? extends Enum, QName> qNameMap = QNAME_CACHE.get(clazz);
    if (qNameMap == null) {
      qNameMap = createQNameMap(clazz);
      QNAME_CACHE.put(clazz, qNameMap);
    }

    QName result = qNameMap.get(e);
    if (result == null) {
      throw new IllegalStateException("Unable to find " + e.getDeclaringClass().getName() + "." + e + " as a QName enum value.");
    }
    else if (UNKNOWN_QNAME_ENUM.equals(result)) {
      throw new IllegalArgumentException(e.getDeclaringClass().getName() + "." + e + " is not a QName enum value.");
    }
    else if (EXCLUDED_QNAME_ENUM.equals(result)) {
      throw new IllegalArgumentException(e.getDeclaringClass().getName() + "." + e + " is excluded a QName enum value.");
    }
    else if (result.getNamespaceURI().equals(defaultBaseUri) && isWriteRelativeUris()) {
      return result.getLocalPart();
    }
    else {
      return result.getNamespaceURI() + result.getLocalPart();
    }
  }

  private static <Q extends Enum<Q>> Map<? extends Enum, QName> createQNameMap(Class<Q> clazz) {
    EnumMap<Q, QName> enumQNameEnumMap = new EnumMap<Q, QName>(clazz);

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
    for (Q e : clazz.getEnumConstants()) {
      for (Field field : fields) {
        if (field.isEnumConstant() && field.getName().equals(e.name())) {
          if (field.getAnnotation(XmlUnknownQNameEnumValue.class) != null) {
            enumQNameEnumMap.put(e, UNKNOWN_QNAME_ENUM);
            break;
          }

          XmlQNameEnumValue enumValueInfo = field.getAnnotation(XmlQNameEnumValue.class);
          String ns = namespace;
          String localPart = field.getName();
          if (enumValueInfo != null) {
            if (enumValueInfo.exclude()) {
              enumQNameEnumMap.put(e, EXCLUDED_QNAME_ENUM);
              break;
            }
            else {
              if (!"##default".equals(enumValueInfo.namespace())) {
                ns = enumValueInfo.namespace();
              }
              if (!"##default".equals(enumValueInfo.localPart())) {
                localPart = enumValueInfo.localPart();
              }
            }
          }

          enumQNameEnumMap.put(e, new QName(ns, localPart));
        }
      }
    }
    return enumQNameEnumMap;
  }

}
