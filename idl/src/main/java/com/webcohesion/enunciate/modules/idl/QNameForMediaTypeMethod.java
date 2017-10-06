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
package com.webcohesion.enunciate.modules.idl;

import java.util.List;

import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Template method used to determine qname for a given type mirror.
 *
 * @author Ryan Heaton
 */
public class QNameForMediaTypeMethod implements TemplateMethodModelEx {

  private final EnunciateJaxbContext context;
  private final boolean associateJsonWithXml;

  public QNameForMediaTypeMethod(EnunciateJaxbContext context, boolean associateJsonWithXml) {
    this.context = context;
    this.associateJsonWithXml = associateJsonWithXml;
  }

  public Object exec(List list) throws TemplateModelException {
    if (list.size() < 1) {
      throw new TemplateModelException("The QNameForType method must have a type mirror as a parameter.");
    }

    TemplateModel from = (TemplateModel) list.get(0);
    Object unwrapped = new BeansWrapperBuilder(Configuration.getVersion()).build().unwrap(from);
    if (unwrapped instanceof MediaTypeDescriptor) {
      MediaTypeDescriptor mt = (MediaTypeDescriptor) unwrapped;
      DataTypeReference typeReference = mt.getDataType();
      if (typeReference != null) {
        if (typeReference instanceof DataTypeReferenceImpl) {
          return ((DataTypeReferenceImpl) typeReference).getElementQName();
        } else if (associateJsonWithXml && mt.getMediaType().endsWith("json")) {
          DataType dataType = typeReference.getValue();
          if (dataType != null) {
            XmlType knownType = this.context.getKnownType(dataType.getJavaElement());
            if (knownType != null) {
              return knownType.getQname();
            }

            TypeDefinition typeDefinition = this.context.findTypeDefinition(dataType.getJavaElement());
            if (typeDefinition != null) {
              return typeDefinition.getQname();
            }
          }
        }
      }
    }

    return null;
  }
}
