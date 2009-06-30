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

package org.codehaus.enunciate.contract.jaxb.adapters;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.type.TypeMirror;
import net.sf.jelly.apt.Context;
import org.codehaus.enunciate.contract.jaxb.Accessor;
import static org.codehaus.enunciate.contract.jaxb.util.JAXBUtil.unwrapComponentType;
import org.codehaus.enunciate.contract.jaxws.WebParam;
import org.codehaus.enunciate.contract.validation.ValidationException;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Logic for the XML adapter stuff according to the JAXB specification. 
 *
 * @author Ryan Heaton
 */
public class AdapterUtil {

  private static final HashMap<String, HashMap<String, XmlJavaTypeAdapter>> ADAPTERS_BY_PACKAGE = new HashMap<String, HashMap<String, XmlJavaTypeAdapter>>();

  /**
   * Finds the adapter type for the specified declaration, if any.
   *
   * @param declaration The declaration for which to find that adapter type.
   * @return The adapter type, or null if none was specified.
   */
  public static AdapterType findAdapterType(Declaration declaration) {
    if (declaration instanceof Accessor) {
      //jaxb accessor can be adapted.
      Accessor accessor = ((Accessor) declaration);
      return findAdapterType(accessor.getAccessorType(), accessor, accessor.getDeclaringType().getPackage());
    }
    else if (declaration instanceof MethodDeclaration) {
      //assume the return type of the method is adaptable (e.g. web results, fault bean getters).
      MethodDeclaration method = ((MethodDeclaration) declaration);
      return findAdapterType(method.getReturnType(), method, method.getDeclaringType().getPackage());
    }
    else if (declaration instanceof WebParam) {
      WebParam parameter = ((WebParam) declaration);
      return findAdapterType(parameter.getType(), parameter, parameter.getWebMethod().getDeclaringEndpointInterface().getPackage());
    }
    else if (declaration instanceof TypeDeclaration) {
      TypeDeclaration typeDeclaration = (TypeDeclaration) declaration;
      AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
      typeDeclaration = ape.getTypeDeclaration(typeDeclaration.getQualifiedName()); //unwrap the decorated stuff as necessary...
      DeclaredType declaredType = ape.getTypeUtils().getDeclaredType(typeDeclaration);
      return findAdapterType(declaredType, null, null);
    }
    else {
      throw new IllegalArgumentException("A " + declaration.getClass().getSimpleName() + " is not an adaptable declaration according to the JAXB spec.");
    }
  }

  /**
   * Finds the adapter type of the specified type, given the referer and the package of the referer.
   *
   * @param adaptedType The type for which to find the adapter.
   * @param referer The referer.
   * @param pckg The package of the referer.
   * @return The adapter type, or null if none was found.
   */
  private static AdapterType findAdapterType(TypeMirror adaptedType, Declaration referer, PackageDeclaration pckg) {
    adaptedType = unwrapComponentType(adaptedType);
    XmlJavaTypeAdapter typeAdapterInfo = referer != null ? referer.getAnnotation(XmlJavaTypeAdapter.class) : null;
    if (adaptedType instanceof DeclaredType) {
      if (typeAdapterInfo == null) {
        TypeDeclaration typeDeclaration = ((DeclaredType) adaptedType).getDeclaration();
        if (typeDeclaration == null) {
          throw new IllegalStateException("Class not found: " + adaptedType);
        }
        typeAdapterInfo = typeDeclaration.getAnnotation(XmlJavaTypeAdapter.class);
      }

      if ((typeAdapterInfo == null) && (pckg != null)) {
        TypeDeclaration typeDeclaration = ((DeclaredType) adaptedType).getDeclaration();
        if (typeDeclaration == null) {
          throw new IllegalStateException("Class not found: " + adaptedType);
        }
        typeAdapterInfo = getAdaptersOfPackage(pckg).get(typeDeclaration.getQualifiedName());
      }

      if (typeAdapterInfo != null) {
        ClassType adapterTypeMirror;

        try {
          Class adaptedClass = typeAdapterInfo.value();
          AnnotationProcessorEnvironment ape = Context.getCurrentEnvironment();
          adapterTypeMirror = (ClassType) ape.getTypeUtils().getDeclaredType(ape.getTypeDeclaration(adaptedClass.getName()));
        }
        catch (MirroredTypeException e) {
          adapterTypeMirror = (ClassType) e.getTypeMirror();
        }

        AdapterType adapterType = new AdapterType(adapterTypeMirror);
        if (!adapterType.canAdapt((DeclaredType) adaptedType)) {
          throw new ValidationException(referer.getPosition(), referer.getSimpleName() + ": adapter " + adapterTypeMirror.getDeclaration().getQualifiedName() + " does not adapt " + adaptedType);
        }
        return adapterType;
      }
    }
    else if (typeAdapterInfo != null) {
      throw new ValidationException(referer.getPosition(), referer.getSimpleName() + ": an XML adapter can only adapt a declared type (" + adaptedType + " cannot be adapted).");
    }

    return null;

  }

  /**
   * Gets the adapters of the specified package.
   *
   * @param pckg the package for which to get the adapters.
   * @return The adapters for the package.
   */
  private static Map<String, XmlJavaTypeAdapter> getAdaptersOfPackage(PackageDeclaration pckg) {
    HashMap<String, XmlJavaTypeAdapter> adaptersOfPackage = ADAPTERS_BY_PACKAGE.get(pckg.getQualifiedName());

    if (adaptersOfPackage == null) {
      adaptersOfPackage = new HashMap<String, XmlJavaTypeAdapter>();
      ADAPTERS_BY_PACKAGE.put(pckg.getQualifiedName(), adaptersOfPackage);

      XmlJavaTypeAdapter javaType = pckg.getAnnotation(XmlJavaTypeAdapter.class);
      XmlJavaTypeAdapters javaTypes = pckg.getAnnotation(XmlJavaTypeAdapters.class);

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

          try {
            Class adaptedClass = adaptedTypeInfo.type();
            if (adaptedClass == XmlJavaTypeAdapter.DEFAULT.class) {
              throw new ValidationException(pckg.getPosition(), "Package " + pckg.getSimpleName() + ": a type must be specified in " + XmlJavaTypeAdapter.class.getName() + " at the package-level.");
            }
            typeFqn = adaptedClass.getName();

          }
          catch (MirroredTypeException e) {
            TypeMirror adaptedType = e.getTypeMirror();
            if (!(adaptedType instanceof DeclaredType)) {
              throw new ValidationException(pckg.getPosition(), "Package " + pckg.getSimpleName() + ": unadaptable type: " + adaptedType);
            }
            TypeDeclaration typeDeclaration = ((DeclaredType) adaptedType).getDeclaration();
            if (typeDeclaration == null) {
              throw new IllegalStateException("Class not found: " + adaptedType);
            }
            typeFqn = typeDeclaration.getQualifiedName();
          }

          adaptersOfPackage.put(typeFqn, adaptedTypeInfo);
        }
      }
    }

    return adaptersOfPackage;
  }

}
