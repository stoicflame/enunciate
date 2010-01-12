package org.codehaus.enunciate.contract.jaxrs;

import org.codehaus.enunciate.contract.common.rest.RESTResourceParameter;
import org.codehaus.enunciate.contract.common.rest.RESTResourceParameterType;

/**
 * A resource parameter with explicit values.
 * 
 * @author Ryan Heaton
 */
public class ExplicitResourceParameter implements RESTResourceParameter {

  private final String docValue;
  private final String resourceParameterName;
  private final RESTResourceParameterType resourceParameterType;

  public ExplicitResourceParameter(String docValue, String resourceParameterName, RESTResourceParameterType resourceParameterType) {
    this.docValue = docValue;
    this.resourceParameterName = resourceParameterName;
    this.resourceParameterType = resourceParameterType;
  }

  public String getDocValue() {
    return docValue;
  }

  public String getResourceParameterName() {
    return resourceParameterName;
  }

  public RESTResourceParameterType getResourceParameterType() {
    return resourceParameterType;
  }
}
