package com.webcohesion.enunciate.modules.swagger;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Parameter;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class SwaggerResponse {

  private final int code;
  private final DataTypeReference dataType;
  private final List<? extends Parameter> headers;
  private final String description;

  public SwaggerResponse(int code, DataTypeReference dataType, List<? extends Parameter> headers, String description) {
    this.code = code;
    this.dataType = dataType;
    this.headers = headers;
    this.description = description;
  }

  public int getCode() {
    return code;
  }

  public DataTypeReference getDataType() {
    return dataType;
  }

  public List<? extends Parameter> getHeaders() {
    return headers;
  }

  public String getDescription() {
    return description;
  }
}
