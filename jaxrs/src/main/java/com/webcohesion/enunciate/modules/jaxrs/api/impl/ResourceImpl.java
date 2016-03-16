package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;

import javax.lang.model.element.AnnotationMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ResourceImpl implements Resource {

  private final ResourceMethod resourceMethod;
  private final ResourceGroup group;

  public ResourceImpl(ResourceMethod resourceMethod, ResourceGroup group) {
    this.resourceMethod = resourceMethod;
    this.group = group;
  }

  @Override
  public String getPath() {
    return resourceMethod.getFullpath();
  }

  @Override
  public String getRelativePath() {
    String relativePath = getPath();
    while (relativePath.startsWith("/")) {
      relativePath = relativePath.substring(1);
    }
    return relativePath;
  }

  @Override
  public String getSlug() {
    return group.getSlug() + "_" + this.resourceMethod.getSimpleName();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.resourceMethod);
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.resourceMethod.getJavaDoc().get("since");
    return tags == null ? null : tags.toString();
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.resourceMethod.getJavaDoc().get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public List<? extends Method> getMethods() {
    Set<String> httpMethods = this.resourceMethod.getHttpMethods();
    List<Method> methodList = new ArrayList<Method>(httpMethods.size());
    for (String httpMethod : httpMethods) {
      methodList.add(new MethodImpl(httpMethod, this.resourceMethod, this.group));
    }
    return methodList;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.resourceMethod.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.resourceMethod.getJavaDoc();
  }
}
