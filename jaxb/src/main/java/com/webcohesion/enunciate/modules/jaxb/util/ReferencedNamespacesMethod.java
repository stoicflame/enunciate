package com.webcohesion.enunciate.modules.jaxb.util;

import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.*;
import com.webcohesion.enunciate.modules.jaxb.model.types.MapXmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.TypeElement;
import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gets all the referenced namespaces for a specific root element.
 *
 * @author Ryan Heaton
 */
public class ReferencedNamespacesMethod implements TemplateMethodModelEx {

  private final EnunciateJaxbContext context;

  public ReferencedNamespacesMethod(EnunciateJaxbContext context) {
    this.context = context;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The referencedNamespaces method must have an element as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = BeansWrapper.getDefaultInstance().unwrap(from);
    if (!(unwrapped instanceof ElementDeclaration)) {
      throw new TemplateModelException("The referencedNamespaces method must have an element as a parameter.");
    }

    ElementDeclaration elementDeclaration = (ElementDeclaration) unwrapped;
    Set<String> referencedNamespaces = new HashSet<String>();
    referencedNamespaces.add(elementDeclaration.getNamespace());
    if (elementDeclaration instanceof RootElementDeclaration) {
      TypeDefinition typeDef = ((RootElementDeclaration) elementDeclaration).getTypeDefinition();
      addReferencedNamespaces(typeDef, referencedNamespaces);
    }
    else if (elementDeclaration instanceof LocalElementDeclaration) {
      TypeElement typeElement = ((LocalElementDeclaration) elementDeclaration).getElementType();
      TypeDefinition typeDefinition = this.context.findTypeDefinition(typeElement);
      if (typeDefinition != null) {
        addReferencedNamespaces(typeDefinition, referencedNamespaces);
      }
    }

    referencedNamespaces.remove(null);
    referencedNamespaces.remove("");
    referencedNamespaces.remove("http://www.w3.org/2001/XMLSchema");
    return referencedNamespaces;
  }

  /**
   * Adds the referenced namespaces of the given type definition to the given set.
   *
   * @param typeDefinition The type definition.
   * @param referencedNamespaces The set of referenced namespaces.
   */
  private void addReferencedNamespaces(TypeDefinition typeDefinition, Set<String> referencedNamespaces) {
    for (Attribute attribute : typeDefinition.getAttributes()) {
      QName ref = attribute.getRef();
      if (ref != null) {
        referencedNamespaces.add(ref.getNamespaceURI());
      }
      else {
        addReferencedNamespaces(attribute.getBaseType(), referencedNamespaces);
      }
    }

    for (Element element : typeDefinition.getElements()) {
      for (Element choice : element.getChoices()) {
        QName ref = choice.getRef();
        if (ref != null) {
          referencedNamespaces.add(ref.getNamespaceURI());
        }
        else {
          addReferencedNamespaces(choice.getBaseType(), referencedNamespaces);
        }
      }
    }

    Value value = typeDefinition.getValue();
    if (value != null) {
      addReferencedNamespaces(value.getBaseType(), referencedNamespaces);
    }

    if (typeDefinition instanceof QNameEnumTypeDefinition) {
      for (Object qnameValue : ((QNameEnumTypeDefinition) typeDefinition).getEnumValues().values()) {
        QName qname = (QName) qnameValue;
        if (qname != null) {
          referencedNamespaces.add(qname.getNamespaceURI());
        }
      }
    }

    addReferencedNamespaces(typeDefinition.getBaseType(), referencedNamespaces);
  }

  /**
   * Adds the referenced namespaces of the given xml type to the given set.
   *
   * @param xmlType The xml type.
   * @param referencedNamespaces The set of referenced namespaces.
   */
  private void addReferencedNamespaces(XmlType xmlType, Set<String> referencedNamespaces) {
    if (!xmlType.isAnonymous()) {
      referencedNamespaces.add(xmlType.getNamespace());
    }
    else if (xmlType instanceof MapXmlType) {
      referencedNamespaces.add(((MapXmlType) xmlType).getKeyType().getNamespace());
      referencedNamespaces.add(((MapXmlType) xmlType).getValueType().getNamespace());
    }
    else if (xmlType instanceof XmlClassType) {
      addReferencedNamespaces(((XmlClassType) xmlType).getTypeDefinition(), referencedNamespaces);
    }
  }

}