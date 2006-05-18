package net.sf.enunciate.contract.jaxb;

import com.sun.mirror.declaration.ClassDeclaration;

import java.util.Map;

/**
 * A class declaration decorated so as to be able to describe itself as an XML-Schema element declaration with global scope.
 *
 * @author Ryan Heaton
 */
public class GlobalElementDeclaration extends TypeDefinition {

  public GlobalElementDeclaration(ClassDeclaration delegate, Map<String, String> ns2prefix) {
    super(delegate, ns2prefix);

    javax.xml.bind.annotation.XmlRootElement rootElement = getAnnotation(javax.xml.bind.annotation.XmlRootElement.class);
    if (rootElement == null) {
      throw new IllegalArgumentException(delegate.getQualifiedName() + " is not a root element.");
    }
  }

  /**
   * The namespace of the element.
   *
   * @return The namespace of the element.
   */
  public String getElementNamespace() {
    //todo: implement
    return null;
  }

  @Override
  public String getTypeNamespace() {
    String typeNamespace = getElementNamespace();

    //see spec, table 8-4, {target namespace}
    if ((xmlType != null) && ("".equals(xmlType.name())) && ("##default".equals(xmlType.namespace()))) {
      typeNamespace = xmlType.namespace();
    }

    return typeNamespace;
  }

}
