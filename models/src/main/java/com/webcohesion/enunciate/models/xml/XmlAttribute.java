package com.webcohesion.enunciate.models.xml;

import com.webcohesion.enunciate.models.binding.CodeBindingMetadata;
import com.webcohesion.enunciate.models.binding.HasCodeBindingMetadata;
import com.webcohesion.enunciate.models.facets.Facet;
import com.webcohesion.enunciate.models.facets.HasFacets;

import javax.xml.namespace.QName;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class XmlAttribute implements HasFacets, HasCodeBindingMetadata {

  private String name;
  private String namespace;
  private NamespaceForm form;
  private XmlTypeDefinition baseType;
  private QName ref;
  private boolean required;
  private Set<Facet> facets;
  private CodeBindingMetadata bindingMetadata;

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

  public NamespaceForm getForm() {
    return form;
  }

  public void setForm(NamespaceForm form) {
    this.form = form;
  }

  public XmlTypeDefinition getBaseType() {
    return baseType;
  }

  public void setBaseType(XmlTypeDefinition baseType) {
    this.baseType = baseType;
  }

  public QName getRef() {
    return ref;
  }

  public void setRef(QName ref) {
    this.ref = ref;
  }

  public Set<Facet> getFacets() {
    return facets;
  }

  public void setFacets(Set<Facet> facets) {
    this.facets = facets;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public boolean isAttribute() {
    return true;
  }

  public boolean isElement() {
    return false;
  }

  public boolean isValue() {
    return false;
  }

  public CodeBindingMetadata getBindingMetadata() {
    return bindingMetadata;
  }

  public void setBindingMetadata(CodeBindingMetadata bindingMetadata) {
    this.bindingMetadata = bindingMetadata;
  }
}
