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
package com.webcohesion.enunciate.modules.jaxb.model.types;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBUtil;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor6;
import java.util.LinkedList;

/**
 * Utility visitor for discovering the xml types of type mirrors.
 *
 * @author Ryan Heaton
 */
public class XmlTypeVisitor extends SimpleTypeVisitor6<XmlType, XmlTypeVisitor.Context> {

  @Override
  protected XmlType defaultAction(TypeMirror typeMirror, Context context) {
    throw new EnunciateException(typeMirror + " is not recognized as an XML type.");
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
    String fqn = declaredElement instanceof TypeElement ? ((TypeElement) declaredElement).getQualifiedName().toString() : declaredType.toString();
    if (context.getStack().contains(fqn)) {
      return KnownXmlType.ANY_TYPE; //break the recursion.
    }

    context.getStack().push(fqn);
    try {
      AdapterType adapterType = JAXBUtil.findAdapterType(declaredElement, context.getContext());
      if (adapterType != null) {
        adapterType.getAdaptingType().accept(this, context);
      }
      else {
        MapType mapType = MapType.findMapType(declaredType, context.getContext());
        if (mapType != null) {
          XmlType keyType = mapType.getKeyType().accept(this, new Context(context.getContext(), false, false, context.stack));
          XmlType valueType = mapType.getValueType().accept(this, new Context(context.getContext(), false, false, context.stack));
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
          }
        }
      }

      return KnownXmlType.ANY_TYPE;
    }
    finally {
      context.getStack().pop();
    }
  }

  @Override
  public XmlType visitArray(ArrayType arrayType, Context context) {
    if (context.isInArray()) {
      throw new UnsupportedOperationException("Enunciate JAXB support doesn't handle multi-dimensional arrays.");
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
    private final LinkedList<String> stack;

    public Context(EnunciateJaxbContext context, boolean inArray, boolean inCollection, LinkedList<String> stack) {
      this.context = context;
      this.inArray = inArray;
      this.inCollection = inCollection;
      this.stack = stack;
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

    public LinkedList<String> getStack() {
      return stack;
    }
  }
}
