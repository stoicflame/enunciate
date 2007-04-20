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

package org.codehaus.enunciate.contract.jaxb;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.decorations.declaration.DecoratedPackageDeclaration;
import org.codehaus.enunciate.contract.jaxb.types.SpecifiedXmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlType;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeException;
import org.codehaus.enunciate.contract.jaxb.types.XmlTypeFactory;
import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.util.*;

/**
 * A package declaration decorated so as to be able to describe itself an XML-Schema root element.
 *
 * @author Ryan Heaton
 * @see "The JAXB 2.0 Specification"
 * @see <a href="http://www.w3.org/TR/2004/REC-xmlschema-1-20041028/structures.html">XML Schema Part 1: Structures Second Edition</a>
 */
public class Schema extends DecoratedPackageDeclaration implements Comparable<Schema> {

  private static final Map<String, Schema> SCHEMA_CACHE = Collections.synchronizedMap(new HashMap<String, Schema>());

  private final XmlSchema xmlSchema;
  private final XmlAccessorType xmlAccessorType;
  private final XmlAccessorOrder xmlAccessorOrder;
  private final Map<String, XmlType> adaptedJavaTypes;
  private final Map<String, XmlType> explicitSchemaTypes;

  public Schema(PackageDeclaration delegate) {
    super(delegate);

    xmlSchema = getAnnotation(XmlSchema.class);
    xmlAccessorType = getAnnotation(XmlAccessorType.class);
    xmlAccessorOrder = getAnnotation(XmlAccessorOrder.class);
    adaptedJavaTypes = loadAdaptedJavaTypes();
    explicitSchemaTypes = loadExplicitSchemaTypes();
    SCHEMA_CACHE.put(getQualifiedName(), this);
  }

  /**
   * The namespace of this package, or null if none.
   *
   * @return The namespace of this package.
   */
  public String getNamespace() {
    String namespace = null;

    if (xmlSchema != null) {
      namespace = xmlSchema.namespace();
    }

    return namespace;
  }

  /**
   * The element form default of this namespace.
   *
   * @return The element form default of this namespace.
   */
  public XmlNsForm getElementFormDefault() {
    XmlNsForm form = null;

    if ((xmlSchema != null) && (xmlSchema.elementFormDefault() != XmlNsForm.UNSET)) {
      form = xmlSchema.elementFormDefault();
    }

    return form;
  }

  /**
   * The attribute form default of this namespace.
   *
   * @return The attribute form default of this namespace.
   */
  public XmlNsForm getAttributeFormDefault() {
    XmlNsForm form = null;

    if ((xmlSchema != null) && (xmlSchema.attributeFormDefault() != XmlNsForm.UNSET)) {
      form = xmlSchema.attributeFormDefault();
    }

    return form;
  }

  /**
   * The default access type for the beans in this package.
   *
   * @return The default access type for the beans in this package.
   */
  public XmlAccessType getAccessType() {
    XmlAccessType accessType = XmlAccessType.PUBLIC_MEMBER;

    if (xmlAccessorType != null) {
      accessType = xmlAccessorType.value();
    }

    return accessType;
  }

  /**
   * The default accessor order of the beans in this package.
   *
   * @return The default accessor order of the beans in this package.
   */
  public XmlAccessOrder getAccessorOrder() {
    XmlAccessOrder order = XmlAccessOrder.UNDEFINED;

    if (xmlAccessorOrder != null) {
      order = xmlAccessorOrder.value();
    }

    return order;
  }

  /**
   * The map of classes to their xml schema types.
   *
   * @return The map of classes to their xml schema types.
   */
  public Map<String, XmlType> getSpecifiedTypes() {
    Map<String, XmlType> types = new HashMap<String, XmlType>();
    types.putAll(getExplicitSchemaTypes());
    types.putAll(getAdaptedJavaTypes());
    return types;
  }

  /**
   * Get the adapted Java types for this package.
   *
   * @return The adapted Java types for this package.
   */
  public Map<String, XmlType> getAdaptedJavaTypes() {
    return this.adaptedJavaTypes;
  }

  /**
   * Loads the adapted java types for this package.
   *
   * @return The adapted java types for this package.
   */
  private Map<String, XmlType> loadAdaptedJavaTypes() {
    if (SCHEMA_CACHE.containsKey(getQualifiedName())) {
      //cache this result because its expensive.
      return SCHEMA_CACHE.get(getQualifiedName()).adaptedJavaTypes;
    }
    
    HashMap<String, XmlType> types = new HashMap<String, XmlType>();

    XmlJavaTypeAdapter javaType = getAnnotation(XmlJavaTypeAdapter.class);
    XmlJavaTypeAdapters javaTypes = getAnnotation(XmlJavaTypeAdapters.class);

    if ((javaType != null) || (javaTypes != null)) {
      ArrayList<XmlJavaTypeAdapter> allAdaptedTypes = new ArrayList<XmlJavaTypeAdapter>();
      if (javaType != null) {
        allAdaptedTypes.add(javaType);
      }

      if (javaTypes != null) {
        allAdaptedTypes.addAll(Arrays.asList(javaTypes.value()));
      }

      for (XmlJavaTypeAdapter adaptedTypeInfo : allAdaptedTypes) {
        String typeFqn;
        TypeMirror adaptedType;

        try {
          Class adaptedClass = adaptedTypeInfo.type();
          if (adaptedClass == XmlJavaTypeAdapter.DEFAULT.class) {
            throw new ValidationException(getPosition(), "A type must be specified in " + XmlJavaTypeAdapter.class.getName() + " at the package-level.");
          }
          typeFqn = adaptedClass.getName();

          AnnotationProcessorEnvironment aptenv = Context.getCurrentEnvironment();
          TypeDeclaration typeDeclaration = aptenv.getTypeDeclaration(typeFqn);
          if (typeDeclaration == null) {
            throw new ValidationException(getPosition(), "The type " + typeFqn +
              " specified by the XmlJavaTypeAdapter is not a valid class type (it can't be an array, anonymous, local, or primitive.");
          }
          adaptedType = aptenv.getTypeUtils().getDeclaredType(typeDeclaration);
        }
        catch (MirroredTypeException e) {
          adaptedType = e.getTypeMirror();
          if (!(adaptedType instanceof DeclaredType)) {
            throw new ValidationException(getPosition(), "Unadaptable type: " + adaptedType);
          }
          typeFqn = ((DeclaredType) adaptedType).getDeclaration().getQualifiedName();
        }

        XmlType xmlType;
        try {
          xmlType = XmlTypeFactory.getAdaptedType((DeclaredType) adaptedType, adaptedTypeInfo);
        }
        catch (XmlTypeException e) {
          throw new ValidationException(getPosition(), e.getMessage());
        }
        types.put(typeFqn, xmlType);
      }
    }
    return types;
  }

  /**
   * Get the types in this package that have explicitly-defined schema types.
   *
   * @return The types in this package that have explicitly-defined schema types.
   */
  public Map<String, XmlType> getExplicitSchemaTypes() {
    return this.explicitSchemaTypes;
  }

  /**
   * Loads the explicit schema types for this package.
   *
   * @return The explicit schema types for this package.
   */
  private Map<String, XmlType> loadExplicitSchemaTypes() {
    if (SCHEMA_CACHE.containsKey(getQualifiedName())) {
      //cache this result because its expensive.
      return SCHEMA_CACHE.get(getQualifiedName()).explicitSchemaTypes;
    }

    HashMap<String, XmlType> types = new HashMap<String, XmlType>();

    XmlSchemaType schemaType = getAnnotation(XmlSchemaType.class);
    XmlSchemaTypes schemaTypes = getAnnotation(XmlSchemaTypes.class);

    if ((schemaType != null) || (schemaTypes != null)) {
      ArrayList<XmlSchemaType> allSpecifiedTypes = new ArrayList<XmlSchemaType>();
      if (schemaType != null) {
        allSpecifiedTypes.add(schemaType);
      }

      if (schemaTypes != null) {
        allSpecifiedTypes.addAll(Arrays.asList(schemaTypes.value()));
      }

      for (XmlSchemaType specifiedType : allSpecifiedTypes) {
        String typeFqn;
        try {
          Class specifiedClass = specifiedType.type();
          if (specifiedClass == XmlSchemaType.DEFAULT.class) {
            throw new ValidationException(getPosition(), "A type must be specified in " + XmlSchemaType.class.getName() + " at the package-level.");
          }
          typeFqn = specifiedClass.getName();
        }
        catch (MirroredTypeException e) {
          TypeMirror typeMirror = e.getTypeMirror();
          if (!(typeMirror instanceof DeclaredType)) {
            throw new ValidationException(getPosition(), "Unrecognized type: " + typeMirror);
          }
          typeFqn = ((DeclaredType) typeMirror).getDeclaration().getQualifiedName();
        }

        types.put(typeFqn, new SpecifiedXmlType(specifiedType));
      }
    }
    return types;
  }

  /**
   * Gets the specified namespace prefixes for this package.
   *
   * @return The specified namespace prefixes for this package.
   */
  public Map<String, String> getSpecifiedNamespacePrefixes() {
    HashMap<String, String> namespacePrefixes = new HashMap<String, String>();
    if (xmlSchema != null) {
      XmlNs[] xmlns = xmlSchema.xmlns();
      if (xmlns != null) {
        for (XmlNs ns : xmlns) {
          namespacePrefixes.put(ns.namespaceURI(), ns.prefix());
        }
      }
    }

    return namespacePrefixes;
  }

  /**
   * Two "schemas" are equal if they decorate the same package.
   *
   * @param schema The schema to which to compare this schema.
   * @return The comparison.
   */
  public int compareTo(Schema schema) {
    return getQualifiedName().compareTo(schema.getQualifiedName());
  }
  
}
