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

import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.Adaptable;

import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSchemaTypes;
import java.util.HashMap;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * A decorator that decorates the relevant type mirrors as xml type mirrors.
 *
 * @author Ryan Heaton
 */
public class XmlTypeFactory {

  private static final HashMap<String, HashMap<String, XmlSchemaType>> EXPLICIT_ELEMENTS_BY_PACKAGE = new HashMap<String, HashMap<String, XmlSchemaType>>();

  /**
   * Find the specified type of the given adaptable element, if it exists.
   *
   * @param adaptable The adaptable element for which to find the specified type.
   * @return The specified XML type, or null if it doesn't exist.
   */
  public static XmlType findSpecifiedType(Adaptable adaptable) {
    XmlType xmlType = null;

    if (adaptable instanceof Accessor) {
      XmlSchemaType specified = ((Accessor) adaptable).getAnnotation(XmlSchemaType.class);
      if (specified != null) {
        return new SpecifiedXmlType(specified);
      }
    }

    if (adaptable.isAdapted()) {
      xmlType = getXmlType(adaptable.getAdapterType().getAdaptingType());
    }
    else if (adaptable instanceof Accessor) {
      //The XML type of accessors can be explicitly defined...
      xmlType = findExplicitSchemaType((Accessor) adaptable);
    }

    return xmlType;
  }

  /**
   * Finds the explicit schema type for the given accessor.
   *
   * @param accessor    The accessor.
   * @return The XML type, or null if none was specified.
   */
  public static XmlType findExplicitSchemaType(Accessor accessor) {
    TypeMirror typeMirror = unwrapComponentType(accessor.getAccessorType());

    XmlType xmlType = null;
    XmlSchemaType schemaType = accessor.getAnnotation(XmlSchemaType.class);

    if ((schemaType == null) && (typeMirror instanceof DeclaredType)) {
      PackageDeclaration pckg = accessor.getDeclaringType().getPackage();
      String packageName = pckg.getQualifiedName();
      HashMap<String, XmlSchemaType> explicitTypes = EXPLICIT_ELEMENTS_BY_PACKAGE.get(packageName);
      if (explicitTypes == null) {
        explicitTypes = new HashMap<String, XmlSchemaType>();
        EXPLICIT_ELEMENTS_BY_PACKAGE.put(packageName, explicitTypes);

        XmlSchemaType schemaTypeInfo = pckg.getAnnotation(XmlSchemaType.class);
        XmlSchemaTypes schemaTypes = pckg.getAnnotation(XmlSchemaTypes.class);

        if ((schemaTypeInfo != null) || (schemaTypes != null)) {
          ArrayList<XmlSchemaType> allSpecifiedTypes = new ArrayList<XmlSchemaType>();
          if (schemaTypeInfo != null) {
            allSpecifiedTypes.add(schemaTypeInfo);
          }

          if (schemaTypes != null) {
            allSpecifiedTypes.addAll(Arrays.asList(schemaTypes.value()));
          }

          for (XmlSchemaType specifiedType : allSpecifiedTypes) {
            String typeFqn;
            try {
              Class specifiedClass = specifiedType.type();
              if (specifiedClass == XmlSchemaType.DEFAULT.class) {
                throw new ValidationException(pckg.getPosition(), pckg.getQualifiedName() + ": a type must be specified in " + XmlSchemaType.class.getName() + " at the package-level.");
              }
              typeFqn = specifiedClass.getName();
            }
            catch (MirroredTypeException e) {
              TypeMirror explicitTypeMirror = e.getTypeMirror();
              if (!(explicitTypeMirror instanceof DeclaredType)) {
                throw new ValidationException(pckg.getPosition(), pckg.getQualifiedName() + ": only a declared type can be adapted.  Offending type: " + explicitTypeMirror);
              }
              typeFqn = ((DeclaredType) explicitTypeMirror).getDeclaration().getQualifiedName();
            }

            explicitTypes.put(typeFqn, specifiedType);
          }
        }

      }

      schemaType = explicitTypes.get(((DeclaredType) typeMirror).getDeclaration().getQualifiedName());
    }

    if (schemaType != null) {
      xmlType = new SpecifiedXmlType(schemaType);
    }

    return xmlType;
  }

  /**
   * Get the XML type for the specified type mirror.
   *
   * @param typeMirror The type mirror.
   * @return The xml type for the specified type mirror.
   */
  public static XmlType getXmlType(TypeMirror typeMirror) {
    XmlTypeVisitor visitor = new XmlTypeVisitor();
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
    visitor.isInCollection = decorated.isCollection();
    visitor.isInArray = decorated.isArray();
    unwrapComponentType(typeMirror).accept(visitor);

    if (visitor.getErrorMessage() != null) {
      throw new XmlTypeException(visitor.getErrorMessage());
    }

    return visitor.getXmlType();
  }

  /**
   * Get the XML type for the specified type.
   *
   * @param type The type mirror.
   * @return The xml type for the specified type mirror.
   * @throws XmlTypeException If the type is invalid or unknown as an xml type.
   */
  public static XmlType getXmlType(Class type) throws XmlTypeException {
    AnnotationProcessorEnvironment env = Context.getCurrentEnvironment();
    TypeDeclaration declaration = env.getTypeDeclaration(type.getName());
    return getXmlType(env.getTypeUtils().getDeclaredType(declaration));
  }

}
