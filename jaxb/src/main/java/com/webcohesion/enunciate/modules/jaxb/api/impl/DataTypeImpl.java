package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.Label;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    Label label = this.typeDefinition.getAnnotation(Label.class);
    if (label != null) {
      return label.value();
    }

    JavaDoc.JavaDocTagList tags = this.typeDefinition.getJavaDoc().get("label");
    if (tags != null && tags.size() > 0) {
      String tag = tags.get(0).trim();
      if (!tag.isEmpty()) {
        return tag;
      }
    }

    return this.typeDefinition.isAnonymous() ? this.typeDefinition.getSimpleName() + " (Anonymous)" : this.typeDefinition.getName();
  }

  @Override
  public String getSlug() {
    String ns = this.typeDefinition.getContext().getNamespacePrefixes().get(this.typeDefinition.getNamespace());
    return "xml_" + ns + "_" + (this.typeDefinition.isAnonymous() ? "anonymous_" + this.typeDefinition.getSimpleName() : this.typeDefinition.getName());
  }

  @Override
  public String getDescription() {
    return this.typeDefinition.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.typeDefinition);
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

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.typeDefinition.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.typeDefinition.getJavaDoc();
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.typeDefinition, this.typeDefinition.getContext().getContext().getConfiguration().getAnnotationStyles());
  }
}
