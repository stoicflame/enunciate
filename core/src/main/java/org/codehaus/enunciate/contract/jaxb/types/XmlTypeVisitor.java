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

package org.codehaus.enunciate.contract.jaxb.types;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterType;
import org.codehaus.enunciate.contract.jaxb.adapters.AdapterUtil;
import org.codehaus.enunciate.util.MapType;
import org.codehaus.enunciate.util.MapTypeUtil;

import java.util.Iterator;

/**
 * Utility visitor for discovering the xml types of type mirrors.
 *
 * @author Ryan Heaton
 */
class XmlTypeVisitor implements TypeVisitor {

  /**
   * State-keeping variable used to determine whether we've already been visited by an array type.
   */
  boolean isInArray;
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
    if (isInArray && (primitiveType.getKind() == PrimitiveType.Kind.BYTE)) {
      //special case for byte[]
      this.xmlType = KnownXmlType.BASE64_BINARY;
    }
    else {
      this.xmlType = new XmlPrimitiveType(primitiveType);
    }
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
    MapType mapType = MapTypeUtil.findMapType(declaredType);
    if (mapType != null) {
      setMapXmlType(mapType);
    }
    else {
      this.xmlType = null;
      this.errorMessage = "Unknown xml type: " + declaredType;
    }
  }

  public void visitClassType(ClassType classType) {
    MapType mapType = MapTypeUtil.findMapType(classType);
    if (mapType != null) {
      setMapXmlType(mapType);
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
        this.errorMessage = "Unknown xml type for class: " + classType +
          ".  If this is a class that is already compiled, you either need to specify an 'api-import' " +
          "element in the configuration file, or your class needs to be explicitly exported. See the FAQ " +
          "( http://tinyurl.com/cte3oq ) for details.";
      }
    }
  }

  public void visitEnumType(EnumType enumType) {
    visitClassType(enumType);
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
      XmlType keyType = XmlTypeFactory.getXmlType(mapType.getKeyType());
      XmlType valueType = XmlTypeFactory.getXmlType(mapType.getValueType());
      this.xmlType = new MapXmlType(keyType, valueType);
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


}
