package net.sf.enunciate.decorations.jaxb;

import net.sf.jelly.apt.decorations.declaration.DecoratedPackageDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;

import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;

/**
 * A package declaration decorated so as to be able to describe itself an XML-Schema root element.
 *
 * @author Ryan Heaton
 * @see "The JAXB 2.0 Specification"
 * @see <a href="http://www.w3.org/TR/2004/REC-xmlschema-1-20041028/structures.html">XML Schema Part 1: Structures Second Edition</a>
 */
public class Schema extends DecoratedPackageDeclaration {

  private final XmlSchema xmlSchema;
  private final XmlAccessorType xmlAccessorType;
  private final XmlAccessorOrder xmlAccessorOrder;

  public Schema(PackageDeclaration delegate) {
    super(delegate);

    xmlSchema = getAnnotation(XmlSchema.class);
    xmlAccessorType = getAnnotation(XmlAccessorType.class);
    xmlAccessorOrder = getAnnotation(XmlAccessorOrder.class);
  }

  /**
   * The namespace of this package, or null if none.
   *
   * @return The namespace of this package.
   */
  public String getNamespace() {
    String namespace = null;

    if ((xmlSchema != null) && (!"".equals(xmlSchema.namespace()))) {
      namespace = xmlSchema.namespace();
    }

    return namespace;
  }

  /**
   * The element form default of this namespace.
   *
   * @return The element form default of this namespace.
   */
  public XmlNsForm getElementFormDefault() {
    XmlNsForm form = null;

    if ((xmlSchema != null) && (xmlSchema.elementFormDefault() != XmlNsForm.UNSET)) {
      form = xmlSchema.elementFormDefault();
    }

    return form;
  }

  /**
   * The attribute form default of this namespace.
   *
   * @return The attribute form default of this namespace.
   */
  public XmlNsForm getAttributeFormDefault() {
    XmlNsForm form = null;

    if ((xmlSchema != null) && (xmlSchema.attributeFormDefault() != XmlNsForm.UNSET)) {
      form = xmlSchema.attributeFormDefault();
    }

    return form;
  }

  /**
   * The default access type for the beans in this package.
   *
   * @return The default access type for the beans in this package.
   */
  public AccessType getAccessType() {
    AccessType accessType = AccessType.PUBLIC_MEMBER;

    if (xmlAccessorType != null) {
      accessType = xmlAccessorType.value();
    }

    return accessType;
  }

  /**
   * The default accessor order of the beans in this package.
   *
   * @return The default accessor order of the beans in this package.
   */
  public AccessorOrder getAccessorOrder() {
    AccessorOrder order = AccessorOrder.UNDEFINED;

    if (xmlAccessorOrder != null) {
      order = xmlAccessorOrder.value();
    }

    return order;
  }

  /**
   * The map of classes to their xml schema types.
   *
   * @return The map of classes to their xml schema types.
   */
  public Map<Class, QName> getXmlSchemaTypes() {
    HashMap<Class, QName> types = new HashMap<Class, QName>();

    XmlSchemaType schemaType = getAnnotation(XmlSchemaType.class);
    XmlSchemaTypes schemaTypes = getAnnotation(XmlSchemaTypes.class);
    if ((schemaType != null) && (schemaTypes != null)) {
      throw new IllegalArgumentException(getPosition() + ": " + XmlSchemaType.class.getName() + " cannot be used with " +
        XmlSchemaTypes.class.getName() + ", according to the spec.");
    }

    if (schemaType != null) {
      if (schemaType.type() == XmlSchemaType.DEFAULT.class) {
        throw new IllegalArgumentException(getPosition() + ": A type class must be specified in " + XmlSchemaType.class.getName() + " at the package-level.");
      }

      String name = schemaType.name();
      //todo: validate that 'name' is a valid xml schema type as detailed in 6.2.2 in the spec.

      String namespace = schemaType.namespace();
      //todo: validate that 'namespace' is a valid xml schema namespace?

      types.put(schemaType.type(), new QName(namespace, name));
    }
    else if (schemaTypes != null) {
      for (XmlSchemaType type : schemaTypes.value()) {
        if (type.type() == XmlSchemaType.DEFAULT.class) {
          throw new IllegalArgumentException(getPosition() + ": A type class must be specified in " + XmlSchemaType.class.getName() + " when being listed with "
            + XmlSchemaTypes.class.getName() + ".");
        }

        String name = type.name();
        //todo: validate that 'name' is a valid xml schema type as detailed in 6.2.2 in the spec.

        String namespace = type.namespace();
        //todo: validate that 'namespace' is a valid xml schema namespace?

        types.put(type.type(), new QName(namespace, name));
      }
    }

    return types;
  }
}
