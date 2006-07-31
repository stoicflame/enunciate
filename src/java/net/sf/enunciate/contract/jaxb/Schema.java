package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.PackageDeclaration;
import net.sf.enunciate.contract.jaxb.types.SpecifiedXmlType;
import net.sf.enunciate.contract.jaxb.types.XmlTypeMirror;
import net.sf.enunciate.contract.validation.ValidationException;
import net.sf.jelly.apt.decorations.declaration.DecoratedPackageDeclaration;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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

    if (xmlSchema != null) {
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
  public XmlAccessType getAccessType() {
    XmlAccessType accessType = XmlAccessType.PUBLIC_MEMBER;

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
  public XmlAccessOrder getAccessorOrder() {
    XmlAccessOrder order = XmlAccessOrder.UNDEFINED;

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
  public Map<String, XmlTypeMirror> getSpecifiedTypes() {
    HashMap<String, XmlTypeMirror> types = new HashMap<String, XmlTypeMirror>();

    XmlSchemaType schemaType = getAnnotation(XmlSchemaType.class);
    XmlSchemaTypes schemaTypes = getAnnotation(XmlSchemaTypes.class);

    if ((schemaType != null) || (schemaTypes != null)) {
      ArrayList<XmlSchemaType> allSpecifiedTypes = new ArrayList<XmlSchemaType>();
      if (schemaType != null) {
        allSpecifiedTypes.add(schemaType);
      }

      if (schemaTypes != null) {
        allSpecifiedTypes.addAll(Arrays.asList(schemaTypes.value()));
      }

      for (XmlSchemaType specifiedType : allSpecifiedTypes) {
        if (specifiedType.type() == XmlSchemaType.DEFAULT.class) {
          throw new ValidationException(getPosition(), "A type must be specified in " + XmlSchemaType.class.getName() + " at the package-level.");
        }

        types.put(specifiedType.type().getName(), new SpecifiedXmlType(specifiedType));
      }
    }

    return types;
  }

  public Map<String, String> getSpecifiedNamespacePrefixes() {
    HashMap<String, String> namespacePrefixes = new HashMap<String, String>();
    if (xmlSchema != null) {
      XmlNs[] xmlns = xmlSchema.xmlns();
      if (xmlns != null) {
        for (XmlNs ns : xmlns) {
          namespacePrefixes.put(ns.namespaceURI(), ns.prefix());
        }
      }
    }

    return namespacePrefixes;
  }
}
