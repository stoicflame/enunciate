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

package com.webcohesion.enunciate.modules.php_xml_client;

import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBUtil;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import freemarker.template.TemplateModelException;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

import javax.activation.DataHandler;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

import static com.webcohesion.enunciate.javac.decorations.element.ElementUtils.isCollection;
import static com.webcohesion.enunciate.javac.decorations.element.ElementUtils.isMap;

/**
 * Conversion from java types to PHP types.
 *
 * @author Ryan Heaton
 */
public class TypeNameForMethod extends com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod {

  private final Map<String, String> classConversions = new HashMap<String, String>();
  private final EnunciateJaxbContext jaxbContext;

  public TypeNameForMethod(Map<String, String> conversions, EnunciateJaxbContext jaxbContext) {
    super(conversions, jaxbContext.getContext());
    this.jaxbContext = jaxbContext;

    classConversions.put(Boolean.class.getName(), "boolean");
    classConversions.put(String.class.getName(), "string");
    classConversions.put(Integer.class.getName(), "integer");
    classConversions.put(Short.class.getName(), "integer");
    classConversions.put(Byte.class.getName(), "integer");
    classConversions.put(Double.class.getName(), "double");
    classConversions.put(Long.class.getName(), "integer");
    classConversions.put(java.math.BigInteger.class.getName(), "integer");
    classConversions.put(java.math.BigDecimal.class.getName(), "integer");
    classConversions.put(Float.class.getName(), "double");
    classConversions.put(Character.class.getName(), "string");
    classConversions.put(Date.class.getName(), "integer");
    classConversions.put(Timestamp.class.getName(), "integer");
    classConversions.put(DataHandler.class.getName(), "byte[]");
    classConversions.put(java.awt.Image.class.getName(), "byte[]");
    classConversions.put(javax.xml.transform.Source.class.getName(), "string");
    classConversions.put(QName.class.getName(), "string");
    classConversions.put(URI.class.getName(), "string");
    classConversions.put(UUID.class.getName(), "string");
    classConversions.put(XMLGregorianCalendar.class.getName(), "integer");
    classConversions.put(GregorianCalendar.class.getName(), "integer");
    classConversions.put(Calendar.class.getName(), "integer");
    classConversions.put(javax.xml.datatype.Duration.class.getName(), "string");
    classConversions.put(javax.xml.bind.JAXBElement.class.getName(), "mixed");
    classConversions.put(Object.class.getName(), "mixed");
  }

  @Override
  public String convert(TypeElement declaration) throws TemplateModelException {
    String fqn = declaration.getQualifiedName().toString();
    if (classConversions.containsKey(fqn)) {
      return classConversions.get(fqn);
    }
    else if (declaration.getKind() == ElementKind.ENUM) {
      return "string";
    }
    else if (isCollection(declaration) || isMap(declaration)) {
      return "array";
    }

    AdapterType adapterType = JAXBUtil.findAdapterType(declaration, this.jaxbContext);
    if (adapterType != null) {
      return convert(adapterType.getAdaptingType());
    }

    String convertedPackage = convertPackage(this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration));
    ClientName specifiedName = declaration.getAnnotation(ClientName.class);
    String simpleName = specifiedName == null ? declaration.getSimpleName().toString() : specifiedName.value();
    return "\\" + convertedPackage + getPackageSeparator() + simpleName;
  }

  @Override
  public String convert(HasClientConvertibleType element) throws TemplateModelException {
    if (element instanceof Adaptable && ((Adaptable) element).isAdapted()) {
      return convert(((Adaptable) element).getAdapterType().getAdaptingType((DecoratedTypeMirror) element.getClientConvertibleType(), this.context));
    }

    if (element instanceof Accessor && ((Accessor) element).isXmlIDREF()) {
      return "string";
    }

    return super.convert(element);
  }

  @Override
  public String convert(TypeMirror typeMirror) throws TemplateModelException {
    DecoratedTypeMirror decorated = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(typeMirror, this.context.getProcessingEnvironment());
    if (decorated.isPrimitive()) {
      TypeKind kind = decorated.getKind();
      switch (kind) {
        case BOOLEAN:
          return "boolean";
        case BYTE:
        case INT:
        case SHORT:
        case LONG:
          return "integer";
        case FLOAT:
        case DOUBLE:
          return "double";
        default:
          return "string";
      }
    }
    else if (decorated.isEnum()) {
      return "string";
    }
    else if (decorated.isCollection()) {
      List<? extends TypeMirror> typeArgs = ((DeclaredType) decorated).getTypeArguments();
      if (typeArgs.size() == 1) {
        String conversion = convert(typeArgs.iterator().next());
        return "mixed".equals(conversion) ? conversion : conversion + "[]";
      }
      else {
        return "array";
      }
    }
    else if (decorated.isArray()) {
      String conversion = convert(((ArrayType) decorated).getComponentType());
      return "mixed".equals(conversion) ? conversion : conversion + "[]";
    }

    return super.convert(typeMirror);
  }

  @Override
  public String convertDeclaredTypeArguments(List<? extends TypeMirror> actualTypeArguments) throws TemplateModelException {
    return ""; //we'll handle generics ourselves.
  }

  @Override
  public String convert(TypeVariable typeVariable) throws TemplateModelException {
    String conversion = "mixed";

    if (typeVariable.getUpperBound() != null) {
      conversion = convert(typeVariable.getUpperBound());
    }

    return conversion;
  }

  @Override
  protected String getPackageSeparator() {
    return "\\";
  }

}