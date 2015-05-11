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

package com.webcohesion.enunciate.modules.jackson1.model.types;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jackson1.model.Accessor;
import com.webcohesion.enunciate.modules.jackson1.model.adapters.Adaptable;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSchemaTypes;
import java.util.*;

import static com.webcohesion.enunciate.modules.jackson1.model.util.JAXBUtil.getComponentType;

/**
 * A decorator that decorates the relevant type mirrors as xml type mirrors.
 *
 * @author Ryan Heaton
 */
public class XmlTypeFactory {

  /**
   * Find the specified type of the given adaptable element, if it exists.
   *
   * @param adaptable The adaptable element for which to find the specified type.
   * @param context The context
   * @return The specified XML type, or null if it doesn't exist.
   */
  public static XmlType findSpecifiedType(Adaptable adaptable, EnunciateJaxbContext context) {
    XmlType xmlType = null;

    if (adaptable instanceof Accessor) {
      XmlSchemaType specified = ((Accessor) adaptable).getAnnotation(XmlSchemaType.class);
      if (specified != null) {
        return new SpecifiedXmlType(specified);
      }
    }

    if (adaptable.isAdapted()) {
      xmlType = getXmlType(adaptable.getAdapterType().getAdaptingType(), context);
    }
    else if (adaptable instanceof Accessor) {
      //The XML type of accessors can be explicitly defined...
      xmlType = findExplicitSchemaType((Accessor) adaptable, context);
    }

    return xmlType;
  }

  /**
   * Finds the explicit schema type for the given accessor.
   *
   * @param accessor    The accessor.
   * @param context The JAXB context.
   * @return The XML type, or null if none was specified.
   */
  public static XmlType findExplicitSchemaType(Accessor accessor, EnunciateJaxbContext context) {
    TypeMirror typeMirror = accessor.getCollectionItemType();

    XmlType xmlType = null;
    XmlSchemaType schemaType = accessor.getAnnotation(XmlSchemaType.class);
    if ((schemaType == null) && (typeMirror instanceof DeclaredType)) {
      PackageElement pckg = context.getContext().getProcessingEnvironment().getElementUtils().getPackageOf(accessor.getEnclosingElement());
      String packageName = pckg.getQualifiedName().toString();
      Map<String, XmlSchemaType> explicitTypes = context.getPackageSpecifiedTypes(packageName);
      if (explicitTypes == null) {
        explicitTypes = loadPackageExplicitTypes(pckg);
        context.setPackageSpecifiedTypes(packageName, explicitTypes);
      }

      schemaType = explicitTypes.get(((TypeElement) ((DeclaredType) typeMirror).asElement()).getQualifiedName().toString());
    }

    if (schemaType != null) {
      xmlType = new SpecifiedXmlType(schemaType);
    }

    return xmlType;
  }

  /**
   * Load any explicit schema types specified by the package.
   *
   * @param pckg The package.
   * @return Any explicit schema types specified by the package.
   */
  protected static Map<String, XmlSchemaType> loadPackageExplicitTypes(PackageElement pckg) {
    Map<String, XmlSchemaType> explicitTypes = new HashMap<String, XmlSchemaType>();

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
            throw new EnunciateException(pckg.getQualifiedName() + ": a type must be specified in " + XmlSchemaType.class.getName() + " at the package-level.");
          }
          typeFqn = specifiedClass.getName();
        }
        catch (MirroredTypeException e) {
          TypeMirror explicitTypeMirror = e.getTypeMirror();
          if (!(explicitTypeMirror instanceof DeclaredType)) {
            throw new EnunciateException(pckg.getQualifiedName() + ": only a declared type can be adapted.  Offending type: " + explicitTypeMirror);
          }
          typeFqn = ((TypeElement) ((DeclaredType) explicitTypeMirror).asElement()).getQualifiedName().toString();
        }

        explicitTypes.put(typeFqn, specifiedType);
      }
    }

    return Collections.unmodifiableMap(explicitTypes);
  }

  /**
   * Get the XML type for the specified type mirror.
   *
   * @param typeMirror The type mirror.
   * @param context The context.
   * @return The xml type for the specified type mirror.
   */
  public static XmlType getXmlType(TypeMirror typeMirror, EnunciateJaxbContext context) {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror, context.getContext().getProcessingEnvironment());
    XmlTypeVisitor visitor = new XmlTypeVisitor();
    TypeMirror componentType = getComponentType(decorated, context.getContext().getProcessingEnvironment());
    componentType = componentType == null ? decorated : componentType;
    return componentType.accept(visitor, new XmlTypeVisitor.Context(context, decorated.isArray(), decorated.isCollection()));
  }

  /**
   * Get the XML type for the specified type.
   *
   * @param type The type mirror.
   * @param context The context.
   * @return The xml type for the specified type mirror.
   */
  public static XmlType getXmlType(Class type, EnunciateJaxbContext context) {
    return getXmlType(TypeMirrorUtils.mirrorOf(type, context.getContext().getProcessingEnvironment()), context);
  }

}
