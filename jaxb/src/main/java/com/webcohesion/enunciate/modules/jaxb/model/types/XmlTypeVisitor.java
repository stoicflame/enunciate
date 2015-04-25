/*
 * Copyright 2006-2008 Web Cohesion
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webcohesion.enunciate.modules.jaxb.model.types;

import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterUtil;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;

import javax.lang.model.element.Element;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor6;

/**
 * Utility visitor for discovering the xml types of type mirrors.
 *
 * @author Ryan Heaton
 */
public class XmlTypeVisitor extends SimpleTypeVisitor6<XmlType, XmlTypeVisitor.Context> {

  @Override
  protected XmlType defaultAction(TypeMirror typeMirror, Context context) {
    throw new IllegalStateException(typeMirror + " is not recognized as an XML type.");
  }

  @Override
  public XmlType visitPrimitive(PrimitiveType primitiveType, Context context) {
    if (context.isInArray() && (primitiveType.getKind() == TypeKind.BYTE)) {
      //special case for byte[]
      return KnownXmlType.BASE64_BINARY;
    }
    else {
      return new XmlPrimitiveType(primitiveType);
    }
  }

  @Override
  public XmlType visitDeclared(DeclaredType declaredType, Context context) {
    Element declaredElement = declaredType.asElement();
    AdapterType adapterType = AdapterUtil.findAdapterType(declaredElement);
    if (adapterType != null) {
      adapterType.getAdaptingType().accept(this, context);
    }
    else {
      MapType mapType = MapType.findMapType(declaredType, context.getContext().getContext());
      if (mapType != null) {
        XmlType keyType = XmlTypeFactory.getXmlType(mapType.getKeyType(), context.getContext());
        XmlType valueType = XmlTypeFactory.getXmlType(mapType.getValueType(), context.getContext());
        return new MapXmlType(keyType, valueType);
      }
      else {
        switch (declaredElement.getKind()) {
          case ENUM:
          case CLASS:
            XmlType knownType = context.getContext().getKnownType(declaredElement);
            if (knownType != null) {
              return knownType;
            }
            else {
              //type not known, not specified.  Last chance: look for the type definition.
              TypeDefinition typeDefinition = context.getContext().findTypeDefinition(declaredElement);
              if (typeDefinition != null) {
                return new XmlClassType(typeDefinition);
              }
            }
            break;
          case INTERFACE:
            if (context.isInCollection()) {
              return KnownXmlType.ANY_TYPE;
            }
            break;
        }
      }
    }

    return super.visitDeclared(declaredType, context);
  }

  @Override
  public XmlType visitArray(ArrayType arrayType, Context context) {
    if (context.isInArray()) {
      throw new UnsupportedOperationException("Enunciate doesn't yet support multi-dimensional arrays.");
    }

    return arrayType.getComponentType().accept(this, context);
  }

  @Override
  public XmlType visitTypeVariable(TypeVariable typeVariable, Context context) {
    TypeMirror bound = typeVariable.getUpperBound();
    if (bound == null) {
      return KnownXmlType.ANY_TYPE;
    }
    else {
      return bound.accept(this, context);
    }
  }

  @Override
  public XmlType visitWildcard(WildcardType wildcardType, Context context) {
    TypeMirror bound = wildcardType.getExtendsBound();
    if (bound == null) {
      return KnownXmlType.ANY_TYPE;
    }
    else {
      return bound.accept(this, context);
    }
  }

  public static class Context {

    private final EnunciateJaxbContext context;
    private final boolean inArray;
    private final boolean inCollection;

    public Context(EnunciateJaxbContext context, boolean inArray, boolean inCollection) {
      this.context = context;
      this.inArray = inArray;
      this.inCollection = inCollection;
    }

    public EnunciateJaxbContext getContext() {
      return context;
    }

    public boolean isInArray() {
      return inArray;
    }

    public boolean isInCollection() {
      return inCollection;
    }
  }
}
