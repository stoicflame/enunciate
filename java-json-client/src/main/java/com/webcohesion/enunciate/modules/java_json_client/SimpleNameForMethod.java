package com.webcohesion.enunciate.modules.java_json_client;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod;
import com.webcohesion.enunciate.util.freemarker.SimpleNameWithParamsMethod;
import freemarker.template.TemplateModelException;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class SimpleNameForMethod extends SimpleNameWithParamsMethod {

  private final MergedJsonContext jsonContext;

  public SimpleNameForMethod(ClientClassnameForMethod typeConversion, MergedJsonContext jsonContext) {
    super(typeConversion);
    this.jsonContext = jsonContext;
  }

  @Override
  public String simpleNameFor(Object unwrapped, boolean noParams) throws TemplateModelException {
    if (unwrapped instanceof Entity) {
      List<? extends MediaTypeDescriptor> mediaTypes = ((Entity) unwrapped).getMediaTypes();
      for (MediaTypeDescriptor mediaType : mediaTypes) {
        if (mediaType.getSyntax().equals(this.jsonContext.getLabel())) {
          DataTypeReference dataType = mediaType.getDataType();
          unwrapped = this.jsonContext.findType(dataType);
        }
      }
    }

    if (unwrapped instanceof Entity) {
      return "Object";
    }

    return super.simpleNameFor(unwrapped, noParams);
  }
}
