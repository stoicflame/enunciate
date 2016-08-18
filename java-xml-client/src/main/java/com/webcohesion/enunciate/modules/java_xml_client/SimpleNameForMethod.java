/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.java_xml_client;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod;
import com.webcohesion.enunciate.util.freemarker.SimpleNameWithParamsMethod;
import freemarker.template.TemplateModelException;

import java.util.List;

/**
 * @author Ryan Heaton
 */
public class SimpleNameForMethod extends SimpleNameWithParamsMethod {

  public SimpleNameForMethod(ClientClassnameForMethod typeConversion) {
    super(typeConversion);
  }

  @Override
  public String simpleNameFor(Object unwrapped, boolean noParams) throws TemplateModelException {
    if (unwrapped instanceof Entity) {
      List<? extends MediaTypeDescriptor> mediaTypes = ((Entity) unwrapped).getMediaTypes();
      for (MediaTypeDescriptor mediaType : mediaTypes) {
        if (EnunciateJaxbContext.SYNTAX_LABEL.equals(mediaType.getSyntax())) {
          DataTypeReference dataType = mediaType.getDataType();
          if (dataType instanceof DataTypeReferenceImpl) {
            XmlType xmlType = ((DataTypeReferenceImpl) dataType).getXmlType();
            if (xmlType instanceof XmlClassType) {
              unwrapped = ((XmlClassType) xmlType).getTypeDefinition();
            }
          }
        }
      }
    }

    if (unwrapped instanceof Entity) {
      return "Object";
    }

    return super.simpleNameFor(unwrapped, noParams);
  }
}
