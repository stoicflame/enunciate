/*
 * © 2023 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.resources.*;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.AnnotationMirror;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SwaggerMethod implements Method {
  
  private final String httpMethod;
  private final List<Method> delegates = new ArrayList<>();

  public SwaggerMethod(Method delegate) {
    this.delegates.add(delegate);
    this.httpMethod = delegate.getHttpMethod();
  }
  
  public void merge(Method delegate) {
    if (!StringUtils.equals(delegate.getHttpMethod(), httpMethod)) {
      throw new IllegalArgumentException();
    }
    this.delegates.add(delegate);
  }

  private <R> R doDelegation(Function<Method, R> accessor) {
    return this.delegates.stream().map(accessor)
       .filter(Objects::nonNull)
       .findFirst().orElse(null);
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return doDelegation(delegate -> delegate.getAnnotation(annotationType));
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    LinkedHashMap<String, AnnotationMirror> annotations = new LinkedHashMap<>();
    this.delegates.forEach(delegate -> annotations.putAll(delegate.getAnnotations()));
    return annotations;
  }

  @Override
  public Set<String> getStyles() {
    TreeSet<String> styles = new TreeSet<>();
    this.delegates.forEach(delegate -> styles.addAll(delegate.getStyles()));
    return styles;
  }

  @Override
  public Resource getResource() {
    return doDelegation(Method::getResource);
  }

  @Override
  public String getLabel() {
    return doDelegation(Method::getLabel);
  }

  @Override
  public String getDeveloperLabel() {
    return doDelegation(Method::getDeveloperLabel);
  }

  @Override
  public String getHttpMethod() {
    return this.httpMethod;
  }

  @Override
  public String getSlug() {
    return doDelegation(Method::getSlug);
  }

  @Override
  public String getSummary() {
    return doDelegation(Method::getSummary);
  }

  @Override
  public String getDescription() {
    return doDelegation(Method::getDescription);
  }

  @Override
  public String getDeprecated() {
    return doDelegation(Method::getDeprecated);
  }

  @Override
  public String getSince() {
    return doDelegation(Method::getSince);
  }

  @Override
  public List<String> getSeeAlso() {
    ArrayList<String> seeAlsos = new ArrayList<>();
    this.delegates.forEach(delegate -> seeAlsos.addAll(delegate.getSeeAlso()));
    return seeAlsos;
  }

  @Override
  public String getVersion() {
    return doDelegation(Method::getVersion);
  }

  @Override
  public boolean isIncludeDefaultParameterValues() {
    return this.delegates.stream().anyMatch(Method::isIncludeDefaultParameterValues);
  }

  @Override
  public List<? extends Parameter> getParameters() {
    Set<Parameter> alreadyDeclaredParams = new TreeSet<>(Comparator.comparing(Parameter::getName));
    List<Parameter> parameters = new ArrayList<>();
    this.delegates.stream().flatMap(delegate -> delegate.getParameters().stream()).filter(alreadyDeclaredParams::add).forEach(parameters::add);
    return parameters;
  }

  @Override
  public boolean isHasParameterConstraints() {
    return this.delegates.stream().anyMatch(Method::isHasParameterConstraints);
  }

  @Override
  public boolean isHasParameterMultiplicity() {
    return this.delegates.stream().anyMatch(Method::isHasParameterMultiplicity);
  }

  @Override
  public Entity getRequestEntity() {
    List<Entity> requestEntities = this.delegates.stream().map(Method::getRequestEntity).filter(Objects::nonNull).collect(Collectors.toList());
    return requestEntities.isEmpty() ? null : new SwaggerEntity(requestEntities);
  }

  @Override
  public List<? extends StatusCode> getResponseCodes() {
    ArrayList<StatusCode> statusCodes = new ArrayList<>();
    this.delegates.forEach(delegate -> statusCodes.addAll(delegate.getResponseCodes()));
    return statusCodes;
  }

  @Override
  public Entity getResponseEntity() {
    List<Entity> responseEntities = this.delegates.stream().map(Method::getResponseEntity).filter(Objects::nonNull).collect(Collectors.toList());
    return responseEntities.isEmpty() ? null : new SwaggerEntity(responseEntities);
  }

  @Override
  public List<? extends StatusCode> getWarnings() {
    ArrayList<StatusCode> statusCodes = new ArrayList<>();
    this.delegates.forEach(delegate -> statusCodes.addAll(delegate.getWarnings()));
    return statusCodes;
  }

  @Override
  public List<? extends Parameter> getResponseHeaders() {
    Set<Parameter> alreadyDeclared = new TreeSet<>(Comparator.comparing(Parameter::getName));
    List<Parameter> parameters = new ArrayList<>();
    this.delegates.stream().flatMap(delegate -> delegate.getResponseHeaders().stream()).filter(alreadyDeclared::add).forEach(parameters::add);
    return parameters;
  }

  @Override
  public Set<String> getSecurityRoles() {
    TreeSet<String> securityRoles = new TreeSet<>();
    this.delegates.forEach(delegate -> securityRoles.addAll(delegate.getSecurityRoles()));
    return securityRoles;
  }

  @Override
  public JavaDoc getJavaDoc() {
    return doDelegation(Method::getJavaDoc);
  }

  @Override
  public Example getExample() {
    return doDelegation(Method::getExample);
  }

  @Override
  public Set<Facet> getFacets() {
    TreeSet<Facet> facets = new TreeSet<>();
    this.delegates.forEach(delegate -> facets.addAll(delegate.getFacets()));
    return facets;
  }
}
