package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.javac.decorations.DecoratedElements;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;

import java.util.ArrayList;
import java.util.List;
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
  public String getSlug() {
    return group.getSlug() + "_" + this.resourceMethod.getSimpleName();
  }

  @Override
  public String getDeprecated() {
    return DecoratedElements.findDeprecationMessage(this.resourceMethod);
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
}
