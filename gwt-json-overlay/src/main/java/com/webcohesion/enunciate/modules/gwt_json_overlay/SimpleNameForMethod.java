package com.webcohesion.enunciate.modules.gwt_json_overlay;

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
        if (this.jsonContext.getLabel().equals(mediaType.getSyntax())) {
          DataTypeReference dataType = mediaType.getDataType();
          unwrapped = this.jsonContext.findType(dataType);
          if (unwrapped == null) {
            return "JavaScriptObject";
          }
        }
      }
    }

    if (unwrapped instanceof Entity) {
      return "JavaScriptObject";
    }

    return super.simpleNameFor(unwrapped, noParams);
  }
}
