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
package com.webcohesion.enunciate.modules.gwt_json_overlay;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import com.webcohesion.enunciate.modules.jackson.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jackson.api.impl.SyntaxImpl;
import com.webcohesion.enunciate.modules.jackson.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jackson.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonClassType;
import com.webcohesion.enunciate.modules.jackson.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson.model.util.JacksonUtil;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import com.webcohesion.enunciate.util.HasClientConvertibleType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author Ryan Heaton
 */
public class MergedJsonContext {

  private final EnunciateJacksonContext jacksonContext;
  private final EnunciateJackson1Context jackson1Context;

  public MergedJsonContext(EnunciateJacksonContext jacksonContext, EnunciateJackson1Context jackson1Context) {
    this.jacksonContext = jacksonContext;
    this.jackson1Context = jackson1Context;
  }

  public EnunciateContext getContext() {
    return jacksonContext == null ? jackson1Context.getContext() : jacksonContext.getContext();
  }

  public String getLabel() {
    return jacksonContext == null ? com.webcohesion.enunciate.modules.jackson1.api.impl.SyntaxImpl.SYNTAX_LABEL : SyntaxImpl.SYNTAX_LABEL;
  }

  public DecoratedTypeElement findType(DataTypeReference dataType) {
    if (dataType instanceof DataTypeReferenceImpl) {
      JsonType jsonType = ((DataTypeReferenceImpl) dataType).getJsonType();
      if (jsonType instanceof JsonClassType) {
        return ((JsonClassType) jsonType).getTypeDefinition();
      }
    }

    if (dataType instanceof com.webcohesion.enunciate.modules.jackson1.api.impl.DataTypeReferenceImpl) {
      com.webcohesion.enunciate.modules.jackson1.model.types.JsonType jsonType = ((com.webcohesion.enunciate.modules.jackson1.api.impl.DataTypeReferenceImpl) dataType).getJsonType();
      if (jsonType instanceof com.webcohesion.enunciate.modules.jackson1.model.types.JsonClassType) {
        return ((com.webcohesion.enunciate.modules.jackson1.model.types.JsonClassType) jsonType).getTypeDefinition();
      }
    }

    return null;
  }

  public TypeMirror findAdaptingType(TypeElement declaration) {
    if (this.jacksonContext != null) {
      AdapterType adapterType = JacksonUtil.findAdapterType(declaration, this.jacksonContext);
      if (adapterType != null) {
        return adapterType.getAdaptingType();
      }
    }

    if (this.jackson1Context != null) {
      com.webcohesion.enunciate.modules.jackson1.model.adapters.AdapterType otherAdapterType = com.webcohesion.enunciate.modules.jackson1.model.util.JacksonUtil.findAdapterType(declaration, this.jackson1Context);
      if (otherAdapterType != null) {
        return otherAdapterType.getAdaptingType();
      }
    }

    return null;
  }

  public TypeMirror findAdaptingType(HasClientConvertibleType element) {
    if (element instanceof Adaptable && ((Adaptable)element).isAdapted()) {
      return ((Adaptable) element).getAdapterType().getAdaptingType();
    }

    if (element instanceof com.webcohesion.enunciate.modules.jackson1.model.adapters.Adaptable && ((com.webcohesion.enunciate.modules.jackson1.model.adapters.Adaptable)element).isAdapted()) {
      return ((com.webcohesion.enunciate.modules.jackson1.model.adapters.Adaptable) element).getAdapterType().getAdaptingType();
    }

    return null;
  }
}
