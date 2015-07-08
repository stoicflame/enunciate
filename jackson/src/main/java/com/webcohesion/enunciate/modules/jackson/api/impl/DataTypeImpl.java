package com.webcohesion.enunciate.modules.jackson.api.impl;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.javac.decorations.DecoratedElements;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jackson.model.TypeDefinition;

import java.util.Collections;
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
    return this.typeDefinition.getSimpleName().toString();
  }

  @Override
  public String getSlug() {
    return "json_" + getLabel().toLowerCase();
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
    return this.typeDefinition.getContext().getNamespace();
  }

  @Override
  public Syntax getSyntax() {
    return this.typeDefinition.getContext();
  }

  @Override
  public DataType getSupertype() {
    //since json itself has no notion of "types" or "super types", we're not going to try to invent one.
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
    //todo: example json
    return null; //new ExampleImpl(this.typeDefinition);
  }

  @Override
  public List<String> getPropertyMetadata() {
    return Collections.emptyList();
  }
}
