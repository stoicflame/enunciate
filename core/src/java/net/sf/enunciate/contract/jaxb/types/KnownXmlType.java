package net.sf.enunciate.contract.jaxb.types;

import javax.xml.namespace.QName;

/**
 * Set of known xml types.
 *
 * @author Ryan Heaton
 */
public enum KnownXmlType implements XmlTypeMirror {

  //known xml-schema types.
  STRING("string", "http://www.w3.org/2001/XMLSchema"),
  NORMALIZED_STRING("normalizedString", "http://www.w3.org/2001/XMLSchema"),
  TOKEN("token", "http://www.w3.org/2001/XMLSchema"),
  BASE64_BINARY("base64Binary", "http://www.w3.org/2001/XMLSchema"),
  HEX_BINARY("hexBinary", "http://www.w3.org/2001/XMLSchema"),
  INTEGER("integer", "http://www.w3.org/2001/XMLSchema"),
  POSITIVE_INTEGER("positiveInteger", "http://www.w3.org/2001/XMLSchema"),
  NEGATIVE_INTEGER("negativeInteger", "http://www.w3.org/2001/XMLSchema"),
  NONPOSITIVE_INTEGER("nonPositiveInteger", "http://www.w3.org/2001/XMLSchema"),
  NONNEGATIVE_INTEGER("nonNegativeInteger", "http://www.w3.org/2001/XMLSchema"),
  LONG("long", "http://www.w3.org/2001/XMLSchema"),
  UNSIGNED_LONG("unsignedLong", "http://www.w3.org/2001/XMLSchema"),
  INT("int", "http://www.w3.org/2001/XMLSchema"),
  UNSIGNED_INT("unsignedInt", "http://www.w3.org/2001/XMLSchema"),
  SHORT("short", "http://www.w3.org/2001/XMLSchema"),
  UNSIGNED_SHORT("unsignedShort", "http://www.w3.org/2001/XMLSchema"),
  BYTE("byte", "http://www.w3.org/2001/XMLSchema"),
  UNSIGNED_BYTE("unsignedByte", "http://www.w3.org/2001/XMLSchema"),
  DECIMAL("decimal", "http://www.w3.org/2001/XMLSchema"),
  FLOAT("float", "http://www.w3.org/2001/XMLSchema"),
  DOUBLE("double", "http://www.w3.org/2001/XMLSchema"),
  BOOLEAN("boolean", "http://www.w3.org/2001/XMLSchema"),
  DURATION("duration", "http://www.w3.org/2001/XMLSchema"),
  DATE_TIME("dateTime", "http://www.w3.org/2001/XMLSchema"),
  DATE("date", "http://www.w3.org/2001/XMLSchema"),
  TIME("time", "http://www.w3.org/2001/XMLSchema"),
  GYEAR("gYear", "http://www.w3.org/2001/XMLSchema"),
  GYEAR_MONTH("gYearMonth", "http://www.w3.org/2001/XMLSchema"),
  GMONTH("gMonth", "http://www.w3.org/2001/XMLSchema"),
  GMONTH_DAY("gMonthDay", "http://www.w3.org/2001/XMLSchema"),
  GDAY("gDay", "http://www.w3.org/2001/XMLSchema"),
  NAME("Name", "http://www.w3.org/2001/XMLSchema"),
  QNAME("QName", "http://www.w3.org/2001/XMLSchema"),
  NCNAME("NCName", "http://www.w3.org/2001/XMLSchema"),
  ANY_URI("anyURI", "http://www.w3.org/2001/XMLSchema"),
  ANY_SIMPLE_TYPE("anySimpleType", "http://www.w3.org/2001/XMLSchema"),
  ANY_TYPE("anyType", "http://www.w3.org/2001/XMLSchema"),
  LANGUAGE("language", "http://www.w3.org/2001/XMLSchema"),
  ID("ID", "http://www.w3.org/2001/XMLSchema"),
  IDREF("IDREF", "http://www.w3.org/2001/XMLSchema"),
  IDREFS("IDREFS", "http://www.w3.org/2001/XMLSchema"),
  ENTITY("ENTITY", "http://www.w3.org/2001/XMLSchema"),
  ENTITIES("ENTITIES", "http://www.w3.org/2001/XMLSchema"),
  NOTATION("NOTATION", "http://www.w3.org/2001/XMLSchema"),
  NMTOKEN("NMTOKEN", "http://www.w3.org/2001/XMLSchema"),
  NMTOKENS("NMTOKENS", "http://www.w3.org/2001/XMLSchema"),

  //others...
  SWAREF("swaRef", "http://ws-i.org/profiles/basic/1.1/xsd");

  private final String name;
  private final String namespace;

  KnownXmlType(String name, String namespace) {
    this.name = name;
    this.namespace = namespace;
  }

  /**
   * The name of the known type.
   *
   * @return The name of the known type.
   */
  public String getName() {
    return name;
  }

  /**
   * The name of the known type.
   *
   * @return The name of the known type.
   */
  public String getNamespace() {
    return namespace;
  }

  /**
   * The qname of the known type.
   *
   * @return The qname of the known type.
   */
  public QName getQname() {
    return new QName(getNamespace(), getName());
  }

  /**
   * The definition of this type is never anonymous.
   *
   * @return The definition of this type is never anonymous.
   */
  public boolean isAnonymous() {
    return false;
  }

  /**
   * Any known type is assumed to be simple.
   *
   * @return true
   */
  public boolean isSimple() {
    return true;
  }

}
