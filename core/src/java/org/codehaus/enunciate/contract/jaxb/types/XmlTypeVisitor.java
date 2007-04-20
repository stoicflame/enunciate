/*
 * Copyright 2006 Web Cohesion
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

package org.codehaus.enunciate.contract.jaxb.types;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedClassType;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;

import java.util.Iterator;

/**
 * Utility visitor for discovering the xml types of type mirrors.
 *
 * @author Ryan Heaton
 */
class XmlTypeVisitor implements TypeVisitor {

  private XmlType xmlType;
  private String errorMessage = null;

  XmlTypeVisitor() {
  }

  /**
   * Get the xml type.
   *
   * @return The xml type of the visitor.
   */
  public XmlType getXmlType() {
    return xmlType;
  }

  /**
   * The error message.  Its existence implies an error when visiting a type.
   *
   * @return The error message.
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  public void visitTypeMirror(TypeMirror typeMirror) {
    this.xmlType = null;
    this.errorMessage = "Unknown xml type: " + typeMirror;
  }

  public void visitPrimitiveType(PrimitiveType primitiveType) {
    this.xmlType = new XmlPrimitiveType(primitiveType);
  }

  public void visitVoidType(VoidType voidType) {
    this.xmlType = null;
    this.errorMessage = "Void is not a valid xml type.";
  }

  public void visitReferenceType(ReferenceType referenceType) {
    this.xmlType = null;
    this.errorMessage = "Unknown xml type: " + referenceType;
  }

  public void visitDeclaredType(DeclaredType declaredType) {
    this.xmlType = null;
    this.errorMessage = "Unknown xml type: " + declaredType;
  }

  public void visitClassType(ClassType classType) {
    DecoratedClassType type = (DecoratedClassType) TypeMirrorDecorator.decorate(classType);
    XmlType adaptedType = XmlTypeFactory.findAdaptedTypeOfDeclaration(classType);
    if (adaptedType != null) {
      this.xmlType = adaptedType;
    }
    else if (type.isCollection()) {
      visitCollectionType(type);
    }
    else {
      XmlType xmlType = null;
      EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();
      ClassDeclaration declaration = classType.getDeclaration();
      if (declaration != null) {
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
      }

      this.xmlType = xmlType;
      if (xmlType == null) {
        this.errorMessage = "Unknown xml type for class: " + classType;
      }
    }
  }

  protected void visitCollectionType(DeclaredType classType) {
    //if it's a colleciton type, the xml type is its component type.
    Iterator<TypeMirror> actualTypeArguments = classType.getActualTypeArguments().iterator();
    if (!actualTypeArguments.hasNext()) {
      //no type arguments, java.lang.Object type.
      this.xmlType = KnownXmlType.ANY_TYPE;
    }
    else {
      TypeMirror componentType = actualTypeArguments.next();
      componentType.accept(this);
    }
  }

  public void visitEnumType(EnumType enumType) {
    visitClassType(enumType);
  }

  public void visitInterfaceType(InterfaceType interfaceType) {
    DecoratedTypeMirror type = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(interfaceType);
    XmlType adaptedType = XmlTypeFactory.findAdaptedTypeOfDeclaration(interfaceType);
    if (adaptedType != null) {
      this.xmlType = adaptedType;
    }
    else if (type.isCollection()) {
      visitCollectionType(interfaceType);
    }
    else {
      this.xmlType = null;
      this.errorMessage = "An interface type cannot be an xml type.";
    }
  }

  public void visitAnnotationType(AnnotationType annotationType) {
    this.xmlType = null;
    this.errorMessage = "An annotation type cannot be an xml type.";
  }

  public void visitArrayType(ArrayType arrayType) {
    //special case for byte[]...
    TypeMirror componentType = arrayType.getComponentType();
    if ((componentType instanceof PrimitiveType) && (((PrimitiveType) componentType).getKind() == PrimitiveType.Kind.BYTE)) {
      this.xmlType = KnownXmlType.BASE64_BINARY;
    }
    else {
      componentType.accept(this);

      if (this.errorMessage != null) {
        this.errorMessage = "Problem with the array component type: " + this.errorMessage;
      }
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


}
