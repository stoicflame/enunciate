package com.webcohesion.enunciate.modules.csharp_client;

import com.webcohesion.enunciate.util.freemarker.*;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.TypeElement;

/**
 * @author Ryan Heaton
 */
public class SimpleNameFor extends SimpleNameWithParamsMethod {

  public SimpleNameFor(com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod typeConversion) {
    super(typeConversion);
  }

  @Override
  protected String convertTypeParams(TypeElement declaration) throws TemplateModelException {
    return ""; //we'll handle type params ourselves.
  }
}
