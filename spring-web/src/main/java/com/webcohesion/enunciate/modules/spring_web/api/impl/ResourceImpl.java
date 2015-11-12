package com.webcohesion.enunciate.modules.spring_web.api.impl;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.spring_web.model.RequestMapping;

import javax.lang.model.element.AnnotationMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ResourceImpl implements Resource {

  private final RequestMapping requestMapping;
  private final ResourceGroup group;

  public ResourceImpl(RequestMapping requestMapping, ResourceGroup group) {
    this.requestMapping = requestMapping;
    this.group = group;
  }

  @Override
  public String getPath() {
    return requestMapping.getFullpath();
  }

  @Override
  public String getSlug() {
    return group.getSlug() + "_" + this.requestMapping.getSimpleName();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.requestMapping);
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.requestMapping.getJavaDoc().get("since");
    return tags == null ? null : tags.toString();
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.requestMapping.getJavaDoc().get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public List<? extends Method> getMethods() {
    Set<String> httpMethods = this.requestMapping.getHttpMethods();
    List<Method> methodList = new ArrayList<Method>(httpMethods.size());
    for (String httpMethod : httpMethods) {
      methodList.add(new MethodImpl(httpMethod, this.requestMapping, this.group));
    }
    return methodList;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.requestMapping.getAnnotations();
  }
}
