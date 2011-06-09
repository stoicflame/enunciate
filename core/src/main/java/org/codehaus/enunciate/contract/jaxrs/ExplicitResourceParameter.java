package org.codehaus.enunciate.contract.jaxrs;


/**
 * A resource parameter with explicit values.
 * 
 * @author Ryan Heaton
 */
public class ExplicitResourceParameter extends ResourceParameter {

  private final String docValue;
  private final String paramName;
  private final ResourceParameterType type;

  public ExplicitResourceParameter(ResourceMethod method, String docValue, String paramName, ResourceParameterType type) {
    super(method);
    this.docValue = docValue;
    this.paramName = paramName;
    this.type = type;
  }

  public String getDocValue() {
    return docValue;
  }

  @Override
  public String getParameterName() {
    return paramName;
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public boolean isMatrixParam() {
    return this.type == ResourceParameterType.MATRIX;
  }

  @Override
  public boolean isQueryParam() {
    return this.type == ResourceParameterType.QUERY;
  }

  @Override
  public boolean isPathParam() {
    return this.type == ResourceParameterType.PATH;
  }

  @Override
  public boolean isCookieParam() {
    return this.type == ResourceParameterType.COOKIE;
  }

  @Override
  public boolean isHeaderParam() {
    return this.type == ResourceParameterType.HEADER;
  }

  @Override
  public boolean isFormParam() {
    return this.type == ResourceParameterType.FORM;
  }

  @Override
  public ResourceParameterType getResourceParameterType() {
    return this.type;
  }
}
