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
package com.webcohesion.enunciate.modules.java_json_client;

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
import com.webcohesion.enunciate.modules.jackson.model.util.MapType;
import com.webcohesion.enunciate.util.HasClientConvertibleType;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * @author Ryan Heaton
 */
public class JsonContext {

  private final EnunciateJacksonContext jacksonContext;

  public JsonContext(EnunciateJacksonContext jacksonContext) {
    this.jacksonContext = jacksonContext;
  }

  public EnunciateContext getContext() {
    return jacksonContext.getContext();
  }

  public String getLabel() {
    return SyntaxImpl.SYNTAX_LABEL;
  }

  public DecoratedTypeElement findType(DataTypeReference dataType) {
    if (dataType instanceof DataTypeReferenceImpl) {
      JsonType jsonType = ((DataTypeReferenceImpl) dataType).getJsonType();
      if (jsonType instanceof JsonClassType) {
        return ((JsonClassType) jsonType).getTypeDefinition();
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

    return null;
  }

  public TypeMirror findAdaptingType(HasClientConvertibleType element) {
    if (element instanceof Adaptable && ((Adaptable)element).isAdapted()) {
      return ((Adaptable) element).getAdapterType().getAdaptingType();
    }

    return null;
  }

  public DeclaredType findMapType(TypeMirror candidate) {
    if (this.jacksonContext != null) {
      DeclaredType mapType = MapType.findMapTypeDeclaration(candidate, this.jacksonContext);
      if (mapType != null) {
        return mapType;
      }
    }

    return null;
  }
}
