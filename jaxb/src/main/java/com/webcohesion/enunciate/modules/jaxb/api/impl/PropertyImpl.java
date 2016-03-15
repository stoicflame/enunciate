package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.PropertyMetadata;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.Attribute;
import com.webcohesion.enunciate.modules.jaxb.model.Element;
import com.webcohesion.enunciate.modules.jaxb.model.Value;

import javax.lang.model.element.AnnotationMirror;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class PropertyImpl implements Property {

  private final Accessor accessor;

  public PropertyImpl(Accessor accessor) {
    this.accessor = accessor;
  }

  @Override
  public String getName() {
    if (this.accessor.isValue()) {
      return "(value)";
    }

    return this.accessor.getName();
  }

  public String getType() {
    return this.accessor.isAttribute() ? "attribute" : this.accessor.isValue() ? "(value)" : "element";
  }

  public PropertyMetadata getNamespaceInfo() {
    return new PropertyMetadata(getNamespacePrefix(), getNamespace(), null);
  }

  public String getNamespacePrefix() {
    String namespace = getNamespace();
    String prefix = this.accessor.getContext().getNamespacePrefixes().get(namespace);
    if (namespace == null || "".equals(namespace)) {
      prefix = "";
    }
    return prefix;
  }

  public String getNamespace() {
    return this.accessor.getNamespace();
  }

  @Override
  public boolean isRequired() {
    if (this.accessor instanceof Attribute) {
      return ((Attribute) this.accessor).isRequired();
    }
    else if (this.accessor instanceof Value) {
      return true;
    }
    else if (this.accessor instanceof Element) {
      return ((Element) this.accessor).isRequired();
    }

    return false;
  }

  public String getMinMaxOccurs() {
    String minMaxOccurs = null;

    if (this.accessor instanceof Attribute) {
      minMaxOccurs = String.format("%s/1", ((Attribute) this.accessor).isRequired() ? "1" : "0");
    }
    else if (this.accessor instanceof Value) {
      minMaxOccurs = "0/1";
    }
    else if (this.accessor instanceof Element) {
      minMaxOccurs = String.format("%s/%s", ((Element)this.accessor).getMinOccurs(), ((Element)this.accessor).getMaxOccurs());
    }

    return minMaxOccurs;
  }

  public String getDefaultValue() {
    String defaultValue = null;
    if (this.accessor instanceof Element) {
      defaultValue = ((Element) this.accessor).getDefaultValue();
    }
    return defaultValue;
  }

  @Override
  public String getDescription() {
    JavaDoc doc = this.accessor.getJavaDoc();

    String description = doc.toString();

    if (description.trim().isEmpty()) {
      description = null;
    }

    if (description == null) {
      JavaDoc.JavaDocTagList tags = doc.get("return");
      if (tags != null && !tags.isEmpty()) {
        description = tags.toString();
      }
    }

    return description;
  }

  public boolean isAttribute() {
    return this.accessor.isAttribute();
  }

  @Override
  public DataTypeReference getDataType() {
    return new DataTypeReferenceImpl(accessor.getXmlType(), accessor.isXmlList());
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.accessor);
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.accessor.getAnnotations();
  }
}
