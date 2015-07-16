package com.webcohesion.enunciate.modules.jaxrs.api.impl;

import com.webcohesion.enunciate.api.resources.*;
import com.webcohesion.enunciate.javac.decorations.DecoratedElements;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxrs.model.*;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class MethodImpl implements Method {

  private final String httpMethod;
  private final ResourceMethod resourceMethod;
  private final ResourceGroup group;

  public MethodImpl(String httpMethod, ResourceMethod resourceMethod, ResourceGroup group) {
    this.httpMethod = httpMethod;
    this.resourceMethod = resourceMethod;
    this.group = group;
  }

  @Override
  public String getLabel() {
    return this.httpMethod;
  }

  @Override
  public String getSlug() {
    return this.group.getSlug() + "_" + resourceMethod.getSimpleName() + "_" + this.httpMethod;
  }

  @Override
  public String getDescription() {
    return this.resourceMethod.getJavaDoc().toString();
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
  public List<? extends Parameter> getParameters() {
    List<ResourceParameter> resourceParams = this.resourceMethod.getResourceParameters();
    ArrayList<Parameter> parameters = new ArrayList<Parameter>(resourceParams.size());
    for (ResourceParameter param : resourceParams) {
      parameters.add(new ParameterImpl(param));
    }

    Collections.sort(parameters, new Comparator<Parameter>() {
      @Override
      public int compare(Parameter o1, Parameter o2) {
        return (o1.getTypeLabel() + o1.getName()).compareTo(o2.getTypeLabel() + o2.getName());
      }
    });
    return parameters;
  }

  @Override
  public Entity getRequestEntity() {
    ResourceEntityParameter entityParameter = this.resourceMethod.getEntityParameter();
    return entityParameter == null ? null : new RequestEntityImpl(this.resourceMethod, entityParameter);
  }

  @Override
  public List<? extends StatusCode> getResponseCodes() {
    return this.resourceMethod.getStatusCodes();
  }

  @Override
  public Entity getResponseEntity() {
    ResourceRepresentationMetadata responseMetadata = this.resourceMethod.getRepresentationMetadata();
    return responseMetadata == null ? null : new ResponseEntityImpl(this.resourceMethod, responseMetadata);
  }

  @Override
  public List<? extends StatusCode> getWarnings() {
    return this.resourceMethod.getWarnings();
  }

  @Override
  public List<? extends Parameter> getResponseHeaders() {
    Map<String, String> responseHeaders = this.resourceMethod.getResponseHeaders();
    ArrayList<Parameter> headerValues = new ArrayList<Parameter>();
    for (Map.Entry<String, String> responseHeader : responseHeaders.entrySet()) {
      headerValues.add(new ResponseHeaderParameterImpl(responseHeader.getKey(), responseHeader.getValue()));
    }
    return headerValues;
  }
}
