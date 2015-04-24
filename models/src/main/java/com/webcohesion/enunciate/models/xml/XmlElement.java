package com.webcohesion.enunciate.models.xml;

import com.webcohesion.enunciate.models.binding.CodeBindingMetadata;
import com.webcohesion.enunciate.facets.Facet;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class XmlElement {

  private String name;
  private String namespace;
  private QName ref;
  private NamespaceForm form;
  private XmlTypeDefinition baseType;
  private boolean nillable;
  private boolean required;
  private int minOccurs;
  private int maxOccurs;
  private String defaultValue;
  private List<? extends XmlElement> choices;
  private boolean wrapped;
  private String wrapperName;
  private String wrapperNamespace;
  private boolean wrapperNillable;
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

  public QName getRef() {
    return ref;
  }

  public void setRef(QName ref) {
    this.ref = ref;
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

  public boolean isNillable() {
    return nillable;
  }

  public void setNillable(boolean nillable) {
    this.nillable = nillable;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public int getMinOccurs() {
    return minOccurs;
  }

  public void setMinOccurs(int minOccurs) {
    this.minOccurs = minOccurs;
  }

  public int getMaxOccurs() {
    return maxOccurs;
  }

  public void setMaxOccurs(int maxOccurs) {
    this.maxOccurs = maxOccurs;
  }

  public String getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  public List<? extends XmlElement> getChoices() {
    return choices;
  }

  public void setChoices(List<? extends XmlElement> choices) {
    this.choices = choices;
  }

  public boolean isWrapped() {
    return wrapped;
  }

  public void setWrapped(boolean wrapped) {
    this.wrapped = wrapped;
  }

  public String getWrapperName() {
    return wrapperName;
  }

  public void setWrapperName(String wrapperName) {
    this.wrapperName = wrapperName;
  }

  public String getWrapperNamespace() {
    return wrapperNamespace;
  }

  public void setWrapperNamespace(String wrapperNamespace) {
    this.wrapperNamespace = wrapperNamespace;
  }

  public boolean isWrapperNillable() {
    return wrapperNillable;
  }

  public void setWrapperNillable(boolean wrapperNillable) {
    this.wrapperNillable = wrapperNillable;
  }

  public boolean isAttribute() {
    return false;
  }

  public boolean isElement() {
    return true;
  }

  public boolean isValue() {
    return false;
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
