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
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.PropertyDeclaration;
import net.sf.jelly.apt.decorations.type.DecoratedClassType;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import org.codehaus.enunciate.contract.jaxb.Schema;
import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.XmlSchemaType;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * A decorator that decorates the relevant type mirrors as xml type mirrors.
 *
 * @author Ryan Heaton
 */
public class XmlTypeFactory {

  /**
   * Finds the specified type of the given parameter declaration, if it exists.
   *
   * @param param The parameter declaration for which to find the specified type.
   * @return The specified XML type, or null if it doesn't exist.
   */
  public static XmlType findSpecifiedType(ParameterDeclaration param) {
    return findSpecifiedType(param.getType(), param);
  }

  /**
   * Finds the xml type of the specified accessor.
   *
   * @param accessor The accessor.
   * @return The specified XML type, or null if it doesn't exist.
   */
  public static XmlType findSpecifiedType(Accessor accessor) {
    TypeMirror accessorType = accessor.getAccessorType();
    XmlType xmlType = findSpecifiedType(accessorType, accessor);

    if ((xmlType == null) && (accessorType instanceof DeclaredType)) {
      xmlType = findSpecifiedType(((DeclaredType) accessorType).getDeclaration(), accessor.getDeclaringType().getPackage());
    }

    return xmlType;
  }

  /**
   * Finds the xml type of the specified property.
   *
   * @param property The property.
   * @return The specified XML type, or null if it doesn't exist.
   */
  public static XmlType findSpecifiedType(PropertyDeclaration property) {
    TypeMirror propertyType = property.getPropertyType();
    XmlType xmlType = findSpecifiedType(propertyType, property);

    if ((xmlType == null) && (propertyType instanceof DeclaredType)) {
      xmlType = findSpecifiedType(((DeclaredType) propertyType).getDeclaration(), property.getDeclaringType().getPackage());
    }

    return xmlType;
  }

  /**
   * Finds the xml type of the specified field.
   *
   * @param field The field.
   * @return The specified XML type, or null if it doesn't exist.
   */
  public static XmlType findSpecifiedType(FieldDeclaration field) {
    TypeMirror fieldType = field.getType();
    XmlType xmlType = findSpecifiedType(fieldType, field);

    if ((xmlType == null) && (fieldType instanceof DeclaredType)) {
      xmlType = findSpecifiedType(((DeclaredType) fieldType).getDeclaration(), field.getDeclaringType().getPackage());
    }

    return xmlType;
  }

  /**
   * Finds the specified type for the given type and given referring declaration.
   *
   * @param typeMirror The type.
   * @param referer    The referring declaration.
   * @return The XML type, or null if none was specified.
   */
  public static XmlType findSpecifiedType(TypeMirror typeMirror, Declaration referer) {
    DecoratedTypeMirror decoratedType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
    
    if (decoratedType.isArray()) {
      typeMirror = ((ArrayType) typeMirror).getComponentType();
    }
    else if (decoratedType.isCollection()) {
      //if it's a collection type, the xml type is its component type.
      Iterator<TypeMirror> actualTypeArguments = ((DeclaredType) typeMirror).getActualTypeArguments().iterator();
      if (!actualTypeArguments.hasNext()) {
        //no type arguments, java.lang.Object type.
        return KnownXmlType.ANY_TYPE;
      }
      else {
        typeMirror = actualTypeArguments.next();
      }
    }

    XmlType xmlType = null;
    XmlJavaTypeAdapter typeAdapter = referer.getAnnotation(XmlJavaTypeAdapter.class);
    XmlSchemaType schemaType = referer.getAnnotation(XmlSchemaType.class);

    if (typeAdapter != null) {
      if (!(typeMirror instanceof ReferenceType)) {
        throw new ValidationException(referer.getPosition(), "XmlJavaTypeAdapter can only be applied to a reference type. "
          + typeMirror + " isn't a reference type.");
      }

      try {
        xmlType = getAdaptedType((ReferenceType) typeMirror, typeAdapter);
      }
      catch (XmlTypeException e) {
        throw new ValidationException(referer.getPosition(), e.getMessage());
      }
    }
    else if (schemaType != null) {
      xmlType = new SpecifiedXmlType(schemaType);
    }
    else if (typeMirror instanceof DeclaredType) {
      xmlType = findAdaptedTypeOfDeclaration((DeclaredType) typeMirror);
    }

    return xmlType;
  }

  /**
   * Returns the specified type of the given declaration as refered to in the given package.
   *
   * @param declaration The declaration for which to find the specified type.
   * @param pckg        The referring package.
   * @return The XML type, or null if the XML type isn't specified.
   */
  public static XmlType findSpecifiedType(TypeDeclaration declaration, PackageDeclaration pckg) {
    XmlType xmlType = null;
    Schema schema = new Schema(pckg);
    Map<String, XmlType> packageSpecifiedTypes = schema.getSpecifiedTypes();
    if (packageSpecifiedTypes.containsKey(declaration.getQualifiedName())) {
      xmlType = packageSpecifiedTypes.get(declaration.getQualifiedName());
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
    DecoratedTypeMirror decoratedType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror);
    if (decoratedType.isArray()) {
      typeMirror = ((ArrayType) typeMirror).getComponentType();
    }
    else if (decoratedType.isCollection()) {
      //if it's a collection type, the xml type is its component type.
      Iterator<TypeMirror> actualTypeArguments = ((DeclaredType) typeMirror).getActualTypeArguments().iterator();
      if (!actualTypeArguments.hasNext()) {
        //no type arguments, java.lang.Object type.
        return KnownXmlType.ANY_TYPE;
      }
      else {
        typeMirror = actualTypeArguments.next();
      }
    }

    XmlTypeVisitor visitor = new XmlTypeVisitor();
    visitor.isInArray = decoratedType.isArray();
    typeMirror.accept(visitor);

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

  /**
   * Gets the xml type for the specified adapted type and the specified adapted type metadata.
   *
   * @param adaptedType     The adapted type.
   * @param adaptedTypeInfo the adapted type metadata.
   * @return The xml type.
   * @throws XmlTypeException If there was a problem getting the adapted type for the specified clas type and annotation.
   */
  public static XmlType getAdaptedType(ReferenceType adaptedType, XmlJavaTypeAdapter adaptedTypeInfo) throws XmlTypeException {
    ClassType adaptorType;
    XmlType adaptingType;
    try {
      Class<? extends XmlAdapter> adaptorClass = adaptedTypeInfo.value();

      AnnotationProcessorEnvironment aptenv = Context.getCurrentEnvironment();
      TypeDeclaration adaptorDeclaration = aptenv.getTypeDeclaration(adaptorClass.getName());
      if (adaptorDeclaration == null) {
        throw new XmlTypeException("The type " + adaptorClass.getName() +
          " specified by the XmlJavaTypeAdapter is not a valid class type (it can't be anonymous or local.");
      }
      DeclaredType declaredType = aptenv.getTypeUtils().getDeclaredType(adaptorDeclaration);
      if (!(declaredType instanceof ClassType)) {
        throw new XmlTypeException("The type " + adaptorClass.getName() + " needs to be a class type (not an interface type).");
      }
      adaptorType = (ClassType) declaredType;

    }
    catch (MirroredTypeException e) {
      TypeMirror typeMirror = e.getTypeMirror();
      if (!(typeMirror instanceof ClassType)) {
        throw new XmlTypeException("The type " + typeMirror + " needs to be a class type (not an interface type).");
      }
      adaptorType = ((ClassType) typeMirror);
    }

    ClassDeclaration adaptorDeclaration = adaptorType.getDeclaration();

    ClassType adaptorInterfaceType = findXmlAdapterType(adaptorDeclaration);
    if (adaptorInterfaceType == null) {
      throw new IllegalStateException("Unable to find the type of XmlAdapter for " + adaptorDeclaration.getQualifiedName());
    }

    Collection<TypeMirror> adaptorTypeArgs = adaptorInterfaceType.getActualTypeArguments();
    if ((adaptorTypeArgs == null) || (adaptorTypeArgs.size() != 2)) {
      throw new XmlTypeException("The type " + adaptorDeclaration.getQualifiedName() +
        " needs to be a class that extends an XmlAdapter type that has two type arguments specifying the value type and the bound type.");
    }

    Iterator<TypeMirror> formalTypeIt = adaptorTypeArgs.iterator();
    TypeMirror valueTypeMirror = formalTypeIt.next();
    TypeMirror boundTypeMirror = formalTypeIt.next();
    if (!adaptedType.equals(boundTypeMirror)) {
      throw new XmlTypeException(adaptorType + " is not adapting type " + adaptedType);
    }

    return new AdaptedXmlType(adaptedType, adaptorType, valueTypeMirror);
  }

  /**
   * Finds the interface type that declares that the specified declaration implements XmlAdapter.
   *
   * @param declaration The declaration.
   * @return The interface type, or null if none found.
   */
  protected static ClassType findXmlAdapterType(ClassDeclaration declaration) {
    if (Object.class.getName().equals(declaration.getQualifiedName())) {
      return null;
    }

    ClassType superClass = declaration.getSuperclass();
    if (XmlAdapter.class.getName().equals(superClass.getDeclaration().getQualifiedName())) {
      return superClass;
    }

    return findXmlAdapterType(superClass.getDeclaration());
  }

  /**
   * Finds the adapted type of the specified declared type, if any.
   *
   * @param declaredType The declared type.
   * @return The adapted type, or null.
   */
  protected static XmlType findAdaptedTypeOfDeclaration(DeclaredType declaredType) {
    XmlType xmlType = null;
    TypeDeclaration declaration = declaredType.getDeclaration();
    if (declaration != null) {
      XmlJavaTypeAdapter typeAdapter = declaration.getAnnotation(XmlJavaTypeAdapter.class);

      if (typeAdapter != null) {
        try {
          xmlType = getAdaptedType(declaredType, typeAdapter);
        }
        catch (XmlTypeException e) {
          throw new ValidationException(declaration.getPosition(), e.getMessage());
        }
      }
    }
    return xmlType;
  }

}
