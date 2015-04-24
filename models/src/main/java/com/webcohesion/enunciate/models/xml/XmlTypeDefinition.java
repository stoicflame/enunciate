package com.webcohesion.enunciate.models.xml;

import com.webcohesion.enunciate.models.binding.CodeBindingMetadata;
import com.webcohesion.enunciate.models.binding.HasCodeBindingMetadata;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;

import java.util.Set;
import java.util.SortedSet;

/**
 * @author Ryan Heaton
 */
public abstract class XmlTypeDefinition implements HasFacets, HasCodeBindingMetadata {

  private XmlSchema schema;
  private String name;
  private String namespace;
  private XmlTypeDefinition baseType;
  private SortedSet<XmlAttribute> attributes;
  private SortedSet<XmlElement> elements;
  private XmlValue value;
  private XmlAnyAttribute anyAttribute;
  private XmlAnyElement anyElement;
  private CodeBindingMetadata bindingMetadata;
  private Set<Facet> facets;

  public XmlSchema getSchema() {
    return schema;
  }

  public void setSchema(XmlSchema schema) {
    this.schema = schema;
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

  public XmlTypeDefinition getBaseType() {
    return baseType;
  }

  public void setBaseType(XmlTypeDefinition baseType) {
    this.baseType = baseType;
  }

  public boolean isSimple() {
    return false;
  }

  public boolean isComplex() {
    return false;
  }

  public boolean isEnum() {
    return false;
  }

  public SortedSet<XmlAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(SortedSet<XmlAttribute> attributes) {
    this.attributes = attributes;
  }

  public SortedSet<XmlElement> getElements() {
    return elements;
  }

  public void setElements(SortedSet<XmlElement> elements) {
    this.elements = elements;
  }

  public XmlValue getValue() {
    return value;
  }

  public void setValue(XmlValue value) {
    this.value = value;
  }

  public XmlAnyAttribute getAnyAttribute() {
    return anyAttribute;
  }

  public void setAnyAttribute(XmlAnyAttribute anyAttribute) {
    this.anyAttribute = anyAttribute;
  }

  public XmlAnyElement getAnyElement() {
    return anyElement;
  }

  public void setAnyElement(XmlAnyElement anyElement) {
    this.anyElement = anyElement;
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
