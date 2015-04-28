package com.webcohesion.enunciate.modules.jaxb;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.modules.jaxb.model.ElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;

import javax.lang.model.element.Element;
import javax.xml.bind.annotation.XmlSchemaType;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class EnunciateJaxbContext {

  private final EnunciateContext context;

  public EnunciateJaxbContext(EnunciateContext context) {
    this.context = context;
  }

  public EnunciateContext getContext() {
    return context;
  }

  public XmlType getKnownType(Element declaration) {
    //todo:
  }

  public TypeDefinition findTypeDefinition(Element declaredElement) {
    //todo:
  }

  public ElementDeclaration findElementDeclaration(Element declaredElement) {
    //todo:
  }

  public Map<String, XmlSchemaType> getPackageSpecifiedTypes(String packageName) {
    //todo:
  }

  public void setPackageSpecifiedTypes(String packageName, Map<String, XmlSchemaType> explicitTypes) {
    //todo:
  }
}
