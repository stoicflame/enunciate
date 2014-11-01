package com.webcohesion.enunciate.models.xml;

import com.webcohesion.enunciate.models.binding.CodeBindingMetadata;
import com.webcohesion.enunciate.models.binding.HasCodeBindingMetadata;
import com.webcohesion.enunciate.models.facets.Facet;
import com.webcohesion.enunciate.models.facets.HasFacets;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class XmlSchema implements HasCodeBindingMetadata, HasFacets {

  private String namespace;
  private Namespaces namespaces;
  private NamespaceForm elementFormDefault;
  private NamespaceForm attributeFormDefault;
  private Set<Facet> facets;
  private CodeBindingMetadata bindingMetadata;

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public Namespaces getNamespaces() {
    return namespaces;
  }

  public void setNamespaces(Namespaces namespaces) {
    this.namespaces = namespaces;
  }

  public NamespaceForm getElementFormDefault() {
    return elementFormDefault;
  }

  public void setElementFormDefault(NamespaceForm elementFormDefault) {
    this.elementFormDefault = elementFormDefault;
  }

  public NamespaceForm getAttributeFormDefault() {
    return attributeFormDefault;
  }

  public void setAttributeFormDefault(NamespaceForm attributeFormDefault) {
    this.attributeFormDefault = attributeFormDefault;
  }

  public Set<Facet> getFacets() {
    return facets;
  }

  public void setFacets(Set<Facet> facets) {
    this.facets = facets;
  }

  public CodeBindingMetadata getBindingMetadata() {
    return bindingMetadata;
  }

  public void setBindingMetadata(CodeBindingMetadata bindingMetadata) {
    this.bindingMetadata = bindingMetadata;
  }
}
