package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.modules.jaxb.model.ElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.LocalElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.RootElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.types.KnownXmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;

import javax.activation.DataHandler;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EnunciateJaxbContext {

  private final EnunciateContext context;
  private final Map<String, XmlType> knownTypes;
  private final Map<String, TypeDefinition> typeDefinitions;
  private final Map<String, ElementDeclaration> elementDeclarations;
  private final Map<String, Map<String, XmlSchemaType>> packageSpecifiedTypes;

  public EnunciateJaxbContext(EnunciateContext context) {
    this.context = context;
    this.knownTypes = loadKnownTypes();
    this.typeDefinitions = new HashMap<String, TypeDefinition>();
    this.elementDeclarations = new HashMap<String, ElementDeclaration>();
    this.packageSpecifiedTypes = new HashMap<String, Map<String, XmlSchemaType>>();
  }

  public EnunciateContext getContext() {
    return context;
  }

  public XmlType getKnownType(Element declaration) {
    if (declaration instanceof TypeElement) {
      return this.knownTypes.get(((TypeElement) declaration).getQualifiedName().toString());
    }
    return null;
  }

  public TypeDefinition findTypeDefinition(Element declaration) {
    if (declaration instanceof TypeElement) {
      return this.typeDefinitions.get(((TypeElement) declaration).getQualifiedName().toString());
    }
    return null;
  }

  public void addTypeDefinition(TypeDefinition typeDef) {
    this.typeDefinitions.put(typeDef.getQualifiedName().toString(), typeDef);
  }

  public ElementDeclaration findElementDeclaration(Element declaredElement) {
    if (declaredElement instanceof TypeElement) {
      return this.elementDeclarations.get(((TypeElement) declaredElement).getQualifiedName().toString());
    }
    else if (declaredElement instanceof ExecutableElement) {
      return this.elementDeclarations.get(declaredElement.toString());
    }
    return null;
  }

  public void addElementDeclaration(RootElementDeclaration element) {
    this.elementDeclarations.put(element.getQualifiedName().toString(), element);
  }

  public void addElementDeclaration(LocalElementDeclaration element) {
    this.elementDeclarations.put(element.toString(), element);
  }

  public Map<String, XmlSchemaType> getPackageSpecifiedTypes(String packageName) {
    return this.packageSpecifiedTypes.get(packageName);
  }

  public void setPackageSpecifiedTypes(String packageName, Map<String, XmlSchemaType> explicitTypes) {
    this.packageSpecifiedTypes.put(packageName, explicitTypes);
  }

  protected Map<String, XmlType> loadKnownTypes() {
    HashMap<String, XmlType> knownTypes = new HashMap<String, XmlType>();

    knownTypes.put(Boolean.class.getName(), KnownXmlType.BOOLEAN);
    knownTypes.put(Byte.class.getName(), KnownXmlType.BYTE);
    knownTypes.put(Character.class.getName(), KnownXmlType.UNSIGNED_SHORT);
    knownTypes.put(Double.class.getName(), KnownXmlType.DOUBLE);
    knownTypes.put(Float.class.getName(), KnownXmlType.FLOAT);
    knownTypes.put(Integer.class.getName(), KnownXmlType.INT);
    knownTypes.put(Long.class.getName(), KnownXmlType.LONG);
    knownTypes.put(Short.class.getName(), KnownXmlType.SHORT);
    knownTypes.put(Boolean.TYPE.getName(), KnownXmlType.BOOLEAN);
    knownTypes.put(Byte.TYPE.getName(), KnownXmlType.BYTE);
    knownTypes.put(Double.TYPE.getName(), KnownXmlType.DOUBLE);
    knownTypes.put(Float.TYPE.getName(), KnownXmlType.FLOAT);
    knownTypes.put(Integer.TYPE.getName(), KnownXmlType.INT);
    knownTypes.put(Long.TYPE.getName(), KnownXmlType.LONG);
    knownTypes.put(Short.TYPE.getName(), KnownXmlType.SHORT);
    knownTypes.put(Character.TYPE.getName(), KnownXmlType.UNSIGNED_SHORT);
    knownTypes.put(String.class.getName(), KnownXmlType.STRING);
    knownTypes.put(java.math.BigInteger.class.getName(), KnownXmlType.INTEGER);
    knownTypes.put(java.math.BigDecimal.class.getName(), KnownXmlType.DECIMAL);
    knownTypes.put(java.util.Calendar.class.getName(), KnownXmlType.DATE_TIME);
    knownTypes.put(java.util.Date.class.getName(), KnownXmlType.DATE_TIME);
    knownTypes.put(Timestamp.class.getName(), KnownXmlType.DATE_TIME);
    knownTypes.put(javax.xml.namespace.QName.class.getName(), KnownXmlType.QNAME);
    knownTypes.put(java.net.URI.class.getName(), KnownXmlType.STRING);
    knownTypes.put(javax.xml.datatype.Duration.class.getName(), KnownXmlType.DURATION);
    knownTypes.put(java.lang.Object.class.getName(), KnownXmlType.ANY_TYPE);
    knownTypes.put(byte[].class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put(java.awt.Image.class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put(DataHandler.class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put(javax.xml.transform.Source.class.getName(), KnownXmlType.BASE64_BINARY);
    knownTypes.put(java.util.UUID.class.getName(), KnownXmlType.STRING);
    knownTypes.put(XMLGregorianCalendar.class.getName(), KnownXmlType.DATE_TIME); //JAXB spec says it maps to anySimpleType, but we can just assume dateTime...
    knownTypes.put(GregorianCalendar.class.getName(), KnownXmlType.DATE_TIME);

    return knownTypes;
  }
}
