package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.javac.decorations.DecoratedElements;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public abstract class DataTypeImpl implements DataType {

  private final TypeDefinition typeDefinition;

  protected DataTypeImpl(TypeDefinition typeDefinition) {
    this.typeDefinition = typeDefinition;
  }

  @Override
  public String getLabel() {
    return this.typeDefinition.getName();
  }

  @Override
  public String getSlug() {
    String ns = this.typeDefinition.getContext().getNamespacePrefixes().get(this.typeDefinition.getNamespace());
    return "xml_" + ns + "_" + this.typeDefinition.getName();
  }

  @Override
  public String getDescription() {
    return this.typeDefinition.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return DecoratedElements.findDeprecationMessage(this.typeDefinition);
  }

  @Override
  public Namespace getNamespace() {
    return new NamespaceImpl(this.typeDefinition.getContext().getSchemas().get(this.typeDefinition.getNamespace()));
  }

  @Override
  public Syntax getSyntax() {
    return this.typeDefinition.getContext();
  }

  @Override
  public List<DataTypeReference> getSupertypes() {
    return null;
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.typeDefinition.getJavaDoc().get("since");
    return tags == null ? null : tags.toString();
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.typeDefinition.getJavaDoc().get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public Example getExample() {
    return null;
  }

}
