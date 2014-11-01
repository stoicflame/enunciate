package com.webcohesion.enunciate.models.xml;

import com.webcohesion.enunciate.models.binding.CodeBindingMetadata;
import com.webcohesion.enunciate.models.binding.HasCodeBindingMetadata;
import com.webcohesion.enunciate.models.facets.Facet;
import com.webcohesion.enunciate.models.facets.HasFacets;

import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class XmlGlobalElement implements HasCodeBindingMetadata, HasFacets {

  private XmlSchema schema;
  private XmlTypeDefinition typeDefinition;
  private String name;
  private String namespace;
  private CodeBindingMetadata bindingMetadata;
  private Set<Facet> facets;

  public XmlSchema getSchema() {
    return schema;
  }

  public void setSchema(XmlSchema schema) {
    this.schema = schema;
  }

  public XmlTypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  public void setTypeDefinition(XmlTypeDefinition typeDefinition) {
    this.typeDefinition = typeDefinition;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public CodeBindingMetadata getBindingMetadata() {
    return bindingMetadata;
  }

  public void setBindingMetadata(CodeBindingMetadata bindingMetadata) {
    this.bindingMetadata = bindingMetadata;
  }

  public Set<Facet> getFacets() {
    return facets;
  }

  public void setFacets(Set<Facet> facets) {
    this.facets = facets;
  }
}
