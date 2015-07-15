package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.services.Parameter;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxws.model.WebParam;

/**
 * @author Ryan Heaton
 */
public class ParameterImpl implements Parameter {

  private final WebParam param;

  public ParameterImpl(WebParam param) {
    this.param = param;
  }

  @Override
  public String getName() {
    String name = this.param.getSimpleName().toString();
    if (!name.equals(param.getBaseParamName())) {
      name += " (" + param.getBaseParamName() + ")";
    }
    return name;
  }

  @Override
  public String getDescription() {
    return this.param.getDocValue();
  }

  @Override
  public DataTypeReference getDataType() {
    return new DataTypeReferenceImpl(this.param.getXmlType(), false);
  }

}
