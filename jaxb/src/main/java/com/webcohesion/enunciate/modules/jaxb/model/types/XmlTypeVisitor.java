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

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterUtil;
import com.webcohesion.enunciate.modules.jaxb.model.util.MapType;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
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
    if (context.inArray && (primitiveType.getKind() == TypeKind.BYTE)) {
      //special case for byte[]
      return KnownXmlType.BASE64_BINARY;
    }
    else {
      return new XmlPrimitiveType(primitiveType);
    }
  }

  @Override
  public XmlType visitDeclared(DeclaredType declaredType, Context context) {
    Element declaration = declaredType.asElement();
    AdapterType adapterType = AdapterUtil.findAdapterType(declaration);
    if (adapterType != null) {
      adapterType.getAdaptingType().accept(this, context);
    }
    else {
      MapType mapType = MapType.findMapType(declaredType, context.enunciate);
      if (mapType != null) {
        XmlType keyType = XmlTypeFactory.getXmlType(mapType.getKeyType());
        XmlType valueType = XmlTypeFactory.getXmlType(mapType.getValueType());
        return new MapXmlType(keyType, valueType);
      }
      else {
        switch (declaration.getKind()) {
          case ENUM:
          case CLASS:
            XmlType knownType = model.getKnownType(declaration);
            if (knownType != null) {
              xmlType = knownType;
            }
            else {
              //type not known, not specified.  Last chance: look for the type definition.
              TypeDefinition typeDefinition = model.findTypeDefinition(declaration);
              if (typeDefinition != null) {
                xmlType = new XmlClassType(typeDefinition);
              }
            }
            break;
          case INTERFACE:
            if (context.inCollection) {
              return KnownXmlType.ANY_TYPE;
            }
            break;
        }
      }
    }

    return super.visitDeclared(declaredType, context);
  }

  public void visitInterfaceType(InterfaceType interfaceType) {
    AdapterType adapterType = AdapterUtil.findAdapterType(interfaceType.getDeclaration());
    if (adapterType != null) {
      adapterType.getAdaptingType().accept(this);
    }
    else {
      MapType mapType = MapTypeUtil.findMapType(interfaceType);
      if (mapType != null) {
        setMapXmlType(mapType);
      }
      else if (isInCollection) {
        this.xmlType = KnownXmlType.ANY_TYPE;
      }
      else {
        this.xmlType = null;
        this.errorMessage = "An interface type cannot be an xml type.";
      }
    }
  }

  /**
   * Sets the map xml type.
   *
   * @param mapType The map type to use.
   */
  private void setMapXmlType(MapType mapType) {
    try {
    }
    catch (XmlTypeException e) {
      this.errorMessage = "Error with map type: " + e.getMessage();
    }
  }

  public void visitAnnotationType(AnnotationType annotationType) {
    this.xmlType = null;
    this.errorMessage = "An annotation type cannot be an xml type.";
  }

  public void visitArrayType(ArrayType arrayType) {
    if (isInArray) {
      this.xmlType = null;
      this.errorMessage = "No support yet for multidimensional arrays.";
      return;
    }

    arrayType.getComponentType().accept(this);

    if (this.errorMessage != null) {
      this.errorMessage = "Problem with the array component type: " + this.errorMessage;
    }
  }

  public void visitTypeVariable(TypeVariable typeVariable) {
    Iterator<ReferenceType> bounds = typeVariable.getDeclaration().getBounds().iterator();
    if (!bounds.hasNext()) {
      this.xmlType = KnownXmlType.ANY_TYPE;
    }
    else {
      bounds.next().accept(this);
      if (this.errorMessage != null) {
        this.errorMessage = "Problem with the type variable bounds: " + this.errorMessage;
      }
    }
  }

  public void visitWildcardType(WildcardType wildcardType) {
    Iterator<ReferenceType> upperBounds = wildcardType.getUpperBounds().iterator();
    if (!upperBounds.hasNext()) {
      this.xmlType = KnownXmlType.ANY_TYPE;
    }
    else {
      upperBounds.next().accept(this);

      if (this.errorMessage != null) {
        this.errorMessage = "Problem with wildcard bounds: " + this.errorMessage;
      }
    }
  }

  public static class Context {

    private final EnunciateContext enunciate;
    private boolean inArray;
    private boolean inCollection;

    public Context(EnunciateContext enunciate) {
      this.enunciate = enunciate;
    }
  }
}
