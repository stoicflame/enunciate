/*
 * © 2020 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.modules.spring_web.model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public class RequestMappingAnnotationAdapter implements RequestMapping {

  private final RequestMethod[] method;
  private final String name;
  private final String[] value;
  private final String[] path;
  private final String[] params;
  private final String[] headers;
  private final String[] consumes;
  private final String[] produces;

  public RequestMappingAnnotationAdapter(AnnotationMirror annotationElement, RequestMethod[] method) {
    this.method = method;

    String name = "";
    String[] value = {};
    String[] path = {};
    String[] params = {};
    String[] headers = {};
    String[] consumes = {};
    String[] produces = {};

    Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotationElement.getElementValues();
    for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
      if (entry.getKey().getSimpleName().contentEquals("name")) {
        Object elementValue = entry.getValue().getValue();
        if (elementValue instanceof String) {
          name = (String) elementValue;
        }
      }
      if (entry.getKey().getSimpleName().contentEquals("value")) {
        Object elementValue = entry.getValue().getValue();
        if (elementValue instanceof List) {
          value = new String[((List) elementValue).size()];
          for (int i = 0; i < ((List) elementValue).size(); i++) {
            AnnotationValue valueItem = (AnnotationValue) ((List) elementValue).get(i);
            value[i] = String.valueOf(valueItem.getValue());
          }
        }
      }
      if (entry.getKey().getSimpleName().contentEquals("path")) {
        Object elementValue = entry.getValue().getValue();
        if (elementValue instanceof List) {
          path = new String[((List) elementValue).size()];
          for (int i = 0; i < ((List) elementValue).size(); i++) {
            AnnotationValue valueItem = (AnnotationValue) ((List) elementValue).get(i);
            path[i] = String.valueOf(valueItem.getValue());
          }
        }
      }
      if (entry.getKey().getSimpleName().contentEquals("params")) {
        Object elementValue = entry.getValue().getValue();
        if (elementValue instanceof List) {
          params = new String[((List) elementValue).size()];
          for (int i = 0; i < ((List) elementValue).size(); i++) {
            AnnotationValue valueItem = (AnnotationValue) ((List) elementValue).get(i);
            params[i] = String.valueOf(valueItem.getValue());
          }
        }
      }
      if (entry.getKey().getSimpleName().contentEquals("headers")) {
        Object elementValue = entry.getValue().getValue();
        if (elementValue instanceof List) {
          headers = new String[((List) elementValue).size()];
          for (int i = 0; i < ((List) elementValue).size(); i++) {
            AnnotationValue valueItem = (AnnotationValue) ((List) elementValue).get(i);
            headers[i] = String.valueOf(valueItem.getValue());
          }
        }
      }
      if (entry.getKey().getSimpleName().contentEquals("consumes")) {
        Object elementValue = entry.getValue().getValue();
        if (elementValue instanceof List) {
          consumes = new String[((List) elementValue).size()];
          for (int i = 0; i < ((List) elementValue).size(); i++) {
            AnnotationValue valueItem = (AnnotationValue) ((List) elementValue).get(i);
            consumes[i] = String.valueOf(valueItem.getValue());
          }
        }
      }
      if (entry.getKey().getSimpleName().contentEquals("produces")) {
        Object elementValue = entry.getValue().getValue();
        if (elementValue instanceof List) {
          produces = new String[((List) elementValue).size()];
          for (int i = 0; i < ((List) elementValue).size(); i++) {
            AnnotationValue valueItem = (AnnotationValue) ((List) elementValue).get(i);
            produces[i] = String.valueOf(valueItem.getValue());
          }
        }
      }
    }

    this.name = name;
    this.value = value;
    this.path = path;
    this.params = params;
    this.headers = headers;
    this.consumes = consumes;
    this.produces = produces;
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public String[] value() {
    return this.value;
  }

  @Override
  public String[] path() {
    return this.path;
  }

  @Override
  public RequestMethod[] method() {
    return this.method;
  }

  @Override
  public String[] params() {
    return this.params;
  }

  @Override
  public String[] headers() {
    return this.headers;
  }

  @Override
  public String[] consumes() {
    return this.consumes;
  }

  @Override
  public String[] produces() {
    return this.produces;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return RequestMapping.class;
  }
}
