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

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.MirroredTypeException;
import net.sf.jelly.apt.Context;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import org.codehaus.enunciate.contract.jaxb.adapters.Adaptable;
import static org.codehaus.enunciate.contract.jaxb.util.JAXBUtil.unwrapComponentType;
import org.codehaus.enunciate.contract.validation.ValidationException;

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
  public static XmlType findSpecifiedType(Adaptable adaptable) throws XmlTypeException {
    XmlType xmlType = null;
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
                throw new ValidationException(pckg.getPosition(), "A type must be specified in " + XmlSchemaType.class.getName() + " at the package-level.");
              }
              typeFqn = specifiedClass.getName();
            }
            catch (MirroredTypeException e) {
              TypeMirror explicitTypeMirror = e.getTypeMirror();
              if (!(explicitTypeMirror instanceof DeclaredType)) {
                throw new ValidationException(pckg.getPosition(), "Only a declared type can be adapted.  Offending type: " + explicitTypeMirror);
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
   * @throws XmlTypeException If the type is invalid or unknown as an xml type.
   */
  public static XmlType getXmlType(TypeMirror typeMirror) throws XmlTypeException {
    XmlTypeVisitor visitor = new XmlTypeVisitor();
    visitor.isInArray = (typeMirror instanceof ArrayType);
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
