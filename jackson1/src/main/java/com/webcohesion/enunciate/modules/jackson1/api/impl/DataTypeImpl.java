package com.webcohesion.enunciate.modules.jackson1.api.impl;

import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.Label;
import com.webcohesion.enunciate.modules.jackson1.model.Member;
import com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition;
import org.codehaus.jackson.map.annotate.JsonRootName;

import javax.lang.model.element.AnnotationMirror;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    String label = this.typeDefinition.getSimpleName().toString();

    JsonRootName rootName = this.typeDefinition.getAnnotation(JsonRootName.class);
    label = rootName == null ? label : rootName.value();

    JavaDoc.JavaDocTagList tags = this.typeDefinition.getJavaDoc().get("label");
    if (tags != null && tags.size() > 0) {
      String tag = tags.get(0).trim();
      label = tag.isEmpty() ? label : tag;
    }

    Label labelInfo = this.typeDefinition.getAnnotation(Label.class);
    label = labelInfo == null ? label : labelInfo.value();

    return label;
  }

  @Override
  public String getSlug() {
    return "json_" + this.typeDefinition.getContext().getSlug(this.typeDefinition);
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
    return this.typeDefinition.getContext().getNamespace();
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
  public Map<String, String> getPropertyMetadata() {
    Map<String, String> propertyMetadata = new LinkedHashMap<String, String>();
    boolean showRequired = false;
    boolean showDefaultValue = false;
    for (Member member : this.typeDefinition.getMembers()) {
      if (member.isRequired()) {
        showRequired = true;
      }

      if (member.getDefaultValue() != null) {
        showDefaultValue = true;
      }
    }

    if (showRequired) {
      propertyMetadata.put("constraints", "constraints");
    }

    if (showDefaultValue) {
      propertyMetadata.put("defaultValue", "default");
    }

    return propertyMetadata;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.typeDefinition.getAnnotations();
  }

}
